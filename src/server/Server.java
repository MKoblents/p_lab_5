package server;

import server.client.ConnectionState;
import server.config.ServerConfig;
import server.console.ConsoleHandler;
import server.db.DbInitializer;
import server.db.config.DbConfig;
import server.db.dao.SpaceMarineMongoDAO;
import server.db.dao.UserDAO;
import server.io.NonBlockingConsoleReader;
import server.manager.ClientRegistry;
import server.manager.CollectionCache;
import server.manager.Invoker;
import server.outputWorkers.CollectionSaver;
import server.service.AuthService;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.dto.ForwardCommandObject;
import shared.dto.HandshakeRequest;
import shared.enums.DisconnectReason;
import shared.utils.LoggingConfigurator;
import shared.utils.SerializationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import server.db.provider.*;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final AtomicBoolean running = new AtomicBoolean(true);
    private static final long HEARTBEAT_TIMEOUT_MS = 30_000L;
    private static final long SELECT_TIMEOUT_MS = 1_000L;

    private static ExecutorService readParsePool =Executors.newVirtualThreadPerTaskExecutor();
    private static ExecutorService commandPool = Executors.newVirtualThreadPerTaskExecutor();
    private static ExecutorService writePool = Executors.newVirtualThreadPerTaskExecutor();

    private static Selector selector;
    private static ClientRegistry clientRegistry;
    private static Invoker invoker;
    private static ConsoleHandler consoleHandler;

    private static class ClientConnection {
        final SocketChannel channel;
        ConnectionState state = ConnectionState.READ_LENGTH;
        final ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
        ByteBuffer payloadBuffer;
        int expectedLength = -1;
        ByteBuffer writeBuffer;
        String clientId;
        Instant lastHeartbeat = Instant.now();
        boolean handshakeComplete = false;

        ClientConnection(SocketChannel channel) { this.channel = channel; }
    }

    public static void main(String[] args) {
        try {
            Path dbConfigPath = Paths.get("src/server/db.properties");
            if (!Files.exists(dbConfigPath)) {
                throw new FileNotFoundException("db.properties not found at: " + dbConfigPath);
            }
            DbConfig dbConfig = DbConfig.loadFromProperties(dbConfigPath);
            DbProvider dbProvider = new HikariDbProvider(dbConfig);
            DbInitializer dbInitializer = new DbInitializer(dbProvider);
            dbInitializer.start(Path.of("./sql/"));
            String mongoUri = System.getenv("MONGO_URI") != null ? System.getenv("MONGO_URI") : "mongodb://localhost:27017";
            String mongoDb = "studs_db";

            MongoProvider mongoProvider = new MongoDbProvider(mongoUri, mongoDb);
            mongoProvider.initialize();
            mongoProvider.start();
            SpaceMarineMongoDAO spaceMarineMongoDAO = new SpaceMarineMongoDAO(mongoProvider);

            ServerConfig config = ServerConfig.parse(args);
            LoggingConfigurator.configure(config.getLogLevel());
            logger.info("=== Single-Threaded NIO Server Starting ===");
            UserDAO userDAO = new UserDAO(dbProvider);
//            SpaceMarineDAO spaceMarineDAO = new SpaceMarineDAO(dbProvider);
            AuthService authService = new AuthService(userDAO);
            CollectionCache collectionCache = new CollectionCache(userDAO, spaceMarineMongoDAO);
            ScheduledExecutorService reloadScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "db-reloader");
                t.setDaemon(true);
                return t;
            });
            reloadScheduler.scheduleAtFixedRate(()->{
                try {
                    collectionCache.reload();
                } catch (SQLException e) {
                   logger.error("Failed to reload collection from BD: {}", e.getMessage());
                }
            }, 0, 5, TimeUnit.SECONDS);
            CollectionSaver collectionSaver = new CollectionSaver();
            clientRegistry = new ClientRegistry();
            invoker = new Invoker(collectionCache, clientRegistry, authService);
            consoleHandler = new ConsoleHandler(collectionCache, collectionSaver, config.getFile(), running, clientRegistry);

            NonBlockingConsoleReader consoleReader = new NonBlockingConsoleReader();
//            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//                try {
//                    logger.info("Shutdown: saving collection...");
//                    collectionSaver.save(collectionManager, config.getFile());
//                } catch (Exception e) { logger.error("Shutdown save failed: {}", e.getMessage(), e); }
//                running.set(false);
//            }));


//            Thread consoleThread = new Thread(() -> {
//                try {
//                    new ConsoleHandler(collectionManager, collectionSaver, config.getFile(), running, clientRegistry)
//                            .handleConsoleInput();
//                } catch (Exception e) { logger.error("Console crashed, server continues: {}", e.getMessage(), e); }
//            });
//            consoleThread.setDaemon(true);
//            consoleThread.start();

            selector = Selector.open();
            ServerSocketChannel ssc = ServerSocketChannel.open();
            ssc.configureBlocking(false);
            ssc.bind(new InetSocketAddress(config.getPort()));
            ssc.register(selector, SelectionKey.OP_ACCEPT);
            logger.info("Server listening on port {}", config.getPort());

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutting down thread pools...");
                readParsePool.shutdownNow();
                commandPool.shutdownNow();
                writePool.shutdownNow();

                try {
                    reloadScheduler.awaitTermination(5, TimeUnit.SECONDS);
                    boolean terminated1 = readParsePool.awaitTermination(5, TimeUnit.SECONDS);
                    boolean terminated2 = commandPool.awaitTermination(5, TimeUnit.SECONDS);
                    boolean terminated3 = writePool.awaitTermination(5, TimeUnit.SECONDS);

                    if (!terminated1 || !terminated2 || !terminated3) {
                        logger.warn("Some thread pools did not terminate gracefully");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("Interrupted while waiting for thread pool termination");
                }
                logger.info("All pools shutdown initiated.");
            }));
            Runtime.getRuntime().addShutdownHook(new Thread(mongoProvider::shutdown));

            while (running.get()) {
                try {
                    selector.select(SELECT_TIMEOUT_MS);
                    checkHeartbeats();
                    processSelectedKeys();
                    String consoleCommand = consoleReader.pollLine();
                    if (consoleCommand != null){
                        handleConsoleCommand(consoleCommand);
                    }
                } catch (Exception e) {
                    logger.error("Selector loop error, recovering: {}", e.getMessage(), e);
                    Thread.sleep(500);
                }
            }
            selector.close();
            ssc.close();
            logger.info("Server shut down cleanly.");

        } catch (Exception e) {
            logger.error("Fatal startup error: {}", e.getMessage(), e);
            System.err.println("Server failed to start: " + e.getMessage());
        }
    }

    private static void handleConsoleCommand(String consoleCommand) {
        consoleHandler.handleConsoleInput(consoleCommand);
    }

    private static void checkHeartbeats() {
        try {
            Instant now = Instant.now();
            for (SelectionKey key : selector.keys()) {
                if (!key.isValid() || !(key.attachment() instanceof ClientConnection conn)) continue;
                if (conn.clientId != null && Duration.between(conn.lastHeartbeat, now).toMillis() > HEARTBEAT_TIMEOUT_MS) {
                    logger.warn("Client {} timed out", conn.clientId != null ? conn.clientId : "UNKNOWN");
                    cascadeDisconnect(conn.clientId, "TIMEOUT");
                }
            }
        } catch (Exception e) { logger.error("Heartbeat check failed: {}", e.getMessage(), e); }
    }

    private static void processSelectedKeys() {
        Iterator<SelectionKey> it = selector.selectedKeys().iterator();
        while (it.hasNext()) {
            SelectionKey key = it.next();
            it.remove();
            if (!key.isValid()) continue;

            try {
                if (key.isAcceptable()) handleAccept(key);
                else if (key.isReadable()) handleRead(key);
                else if (key.isWritable()) handleWrite(key);
            }catch (java.io.EOFException e) {
                String cid = key.attachment() instanceof ClientConnection c ? c.clientId : "UNKNOWN";
                logger.info("Client {} disconnected gracefully (EOF/Ctrl+C)", cid);
                notifyParentOfChildDisconnect(cid);
                cascadeDisconnect(cid, DisconnectReason.USER_REQUEST.name());
            } catch (Exception e) {
                    String cid = key.attachment() instanceof ClientConnection c ? c.clientId : "UNKNOWN";
                    logger.error("Key processing error for {}: {}", cid, e.getMessage(), e);
                    notifyParentOfChildDisconnect(cid);
                    cascadeDisconnect(cid, "NETWORK_ERROR");
            }
        }
    }

    private static void handleAccept(SelectionKey acceptKey) throws IOException {
        ServerSocketChannel server = (ServerSocketChannel) acceptKey.channel();
        SocketChannel client = server.accept();
        logger.info("Accepted connection from remote: {}", client.getRemoteAddress());
        if (client != null) {
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ, new ClientConnection(client));
            logger.info("New connection accepted");
        }
    }

    private static void handleRead(SelectionKey key) throws IOException {
        ClientConnection conn = (ClientConnection) key.attachment();
        if (!key.isValid()) return;
        int ops = key.interestOps();
        key.interestOps(ops & ~SelectionKey.OP_READ);
        readParsePool.submit(()-> {
            try {
                SocketChannel sc = conn.channel;
                if (conn.state == ConnectionState.READ_LENGTH) {
                    if (!readUntilFull(sc, conn.lengthBuffer)) return;
                    conn.lengthBuffer.flip();
                    conn.expectedLength = conn.lengthBuffer.getInt();
                    conn.payloadBuffer = ByteBuffer.allocate(conn.expectedLength);
                    conn.lengthBuffer.clear();
                    synchronized (conn) {
                        conn.state = ConnectionState.READ_PAYLOAD;
                    }
                }

                if (conn.state == ConnectionState.READ_PAYLOAD) {
                    if (!readUntilFull(sc, conn.payloadBuffer)) return;
                    conn.payloadBuffer.flip();
                    byte[] data = new byte[conn.expectedLength];
                    conn.payloadBuffer.get(data);
                    conn.payloadBuffer.clear();
                    conn.expectedLength = -1;
                    synchronized (conn) {
                        conn.state = ConnectionState.READ_LENGTH;
                    }
                    processMessage(conn, data, key);
                }
            }catch (EOFException e) {
                String cid = conn.clientId != null ? conn.clientId : "UNKNOWN";
                logger.info("Client {} disconnected gracefully (EOF)", cid);
                cascadeDisconnect(cid, DisconnectReason.USER_REQUEST.name());
            } catch (Exception e) {
                logger.error("Read/Parse failed for {}: {}", conn.clientId, e.getMessage());
                cascadeDisconnect(conn.clientId, "NETWORK_ERROR");
            } finally {
                if (key.isValid() && conn.channel.isOpen()) {
                    key.interestOps(key.interestOps() | SelectionKey.OP_READ);
                    selector.wakeup();
                }
            }
        });
    }

    private static boolean readUntilFull(SocketChannel sc, ByteBuffer buf) throws IOException {
        while (buf.hasRemaining()) {
            int read = sc.read(buf);
            if (read == -1) throw new EOFException("Client closed connection");
            if (read == 0) try { Thread.sleep(1); } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return true;
    }

    private static void processMessage(ClientConnection conn, byte[] data, SelectionKey key) {
        commandPool.submit(()->{
        try {
            Object obj = SerializationUtil.deserialize(data);
            conn.lastHeartbeat = Instant.now();

            if (obj instanceof HandshakeRequest hs) {
                synchronized (conn){
                    conn.clientId = hs.clientId();
                    conn.handshakeComplete = true;
                }
                clientRegistry.register(hs.clientId(), hs.parentClientId());
                logger.info("Handshake OK: {} (parent: {})", hs.clientId(), hs.parentClientId());
                CommandResponse response =new CommandResponse(true, null, "Handshake OK", "0", hs.clientId());
                writePool.submit(() -> {
                    try {
                        writeResponse(conn, response);
                    } catch (IOException e) {
                        logger.error("Write failed for {}: {}", conn.clientId, e.getMessage());
                        cascadeDisconnect(conn.clientId, "WRITE_ERROR");
                    }
                });return;
            }

            if (!conn.handshakeComplete) { logger.warn("Data before handshake, ignoring."); return; }

            if (obj instanceof CommandRequest req) {
                System.out.println(req.commandType());
                CommandResponse resp = invoker.runCommand(req);
                writePool.submit(() -> {
                    try {
                        writeResponse(conn, resp);
                    } catch (IOException e) {
                        logger.error("Write failed for {}: {}", conn.clientId, e.getMessage());
                        cascadeDisconnect(conn.clientId, "WRITE_ERROR");
                    }
                });
                if ("forward_command".equals(req.commandType()) && req.args() instanceof ForwardCommandObject fco) {
                    schedulePendingWrite(fco.childId());
                }

                if ("kill_client".equals(req.commandType()) && resp.success() && req.args() instanceof String targetId) {
                    cascadeDisconnect(targetId, DisconnectReason.KILLED_BY_PARENT.name());
                }
            }
        } catch (Exception e) {
            logger.error("Message processing failed for {}: {}", conn.clientId, e.getMessage(), e);
        }
    });
    }

    private static void writeResponse(ClientConnection conn, CommandResponse resp) throws IOException {
        byte[] data = SerializationUtil.serialize(resp);
        ByteBuffer buf = ByteBuffer.allocate(4 + data.length);
        buf.putInt(data.length);
        buf.put(data);
        buf.flip();
        SocketChannel sc = conn.channel;
        if (!sc.isOpen()) return;
        try {
            while (buf.hasRemaining()){
                int written = sc.write(buf);
                if (written == 0) Thread.sleep(50);
            }
        } catch (InterruptedException |IOException e) {
            logger.warn("Failed to send response to {}: {}", conn.clientId, e.getMessage());
            cascadeDisconnect(conn.clientId, "WRITE_ERROR");
        }
    }

    private static void handleWrite(SelectionKey key) throws IOException {
        ClientConnection conn = (ClientConnection) key.attachment();
        SocketChannel sc = conn.channel;

        while (conn.writeBuffer != null && conn.writeBuffer.hasRemaining()) {
            int written = sc.write(conn.writeBuffer);
            if (written == 0) return;
            if (written == -1) throw new ClosedChannelException();
        }

        synchronized (conn) {
            conn.writeBuffer = null;
            conn.state = ConnectionState.READ_LENGTH;
        }
        checkAndSendPendingCommands(conn, key);
        conn.channel.register(selector, SelectionKey.OP_READ, conn);
    }

    private static void checkAndSendPendingCommands(ClientConnection conn, SelectionKey key) {
        try {
            if (conn.clientId == null) return;
            CommandRequest pending = clientRegistry.getPendingCommandQueue().poll(conn.clientId);
            if (pending != null) {
                byte[] data = SerializationUtil.serialize(pending);
                ByteBuffer buf = ByteBuffer.allocate(4 + data.length);
                buf.putInt(data.length);
                buf.put(data);
                buf.flip();
                conn.writeBuffer = buf;
                conn.state = ConnectionState.WRITE;
                conn.channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, conn);
                logger.info("Forwarded pending command to {}", conn.clientId);
            }
        } catch (Exception e) { logger.error("Pending command send failed for {}: {}", conn.clientId, e.getMessage(), e); }
    }

    private static void schedulePendingWrite(String targetClientId) {
        try {
            for (SelectionKey key : selector.keys()) {
                if (key.attachment() instanceof ClientConnection conn && targetClientId.equals(conn.clientId) && conn.writeBuffer == null) {
                    checkAndSendPendingCommands(conn, key);
                    break;
                }
            }
        } catch (Exception e) { logger.error("Schedule pending write failed for {}: {}", targetClientId, e.getMessage(), e); }
    }

    private static void cascadeDisconnect(String targetId, String reason) {
        try {
            notifyParentOfChildDisconnect(targetId);
            Set<String> children = clientRegistry.getChildren(targetId);
            if (children != null) {
                for (String childId : new ArrayList<>(children)) {
                    cascadeDisconnect(childId, DisconnectReason.PARENT_DOWN.name());
                }
            }

            SelectionKey targetKey = null;
            ClientConnection targetConn = null;
            for (SelectionKey key : selector.keys()) {
                if (key.attachment() instanceof ClientConnection c && targetId.equals(c.clientId)) {
                    targetKey = key;
                    targetConn = c;
                    break;
                }
            }

            try {
                if (targetConn != null && targetConn.channel.isOpen() && reason != null) {
                    CommandResponse termMsg = new CommandResponse(false, null, reason, "SYSTEM", targetId);
                    byte[] data = SerializationUtil.serialize(termMsg);
                    ByteBuffer buf = ByteBuffer.allocate(4 + data.length);
                    buf.putInt(data.length);
                    buf.put(data);
                    buf.flip();

                    while (buf.hasRemaining()) {
                        int written = targetConn.channel.write(buf);
                        if (written == 0) {
                            Thread.sleep(10);
                        }
                    }
                }
            } catch (IOException e) {
                logger.debug("Failed to send termination to {}: {}", targetId, e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            if (targetKey != null && targetKey.isValid()) {
                try { if (targetConn != null && targetConn.channel.isOpen()) targetConn.channel.close(); }
                catch (IOException ignored) {}
                targetKey.cancel();
            }
            try { clientRegistry.unregister(targetId); }
            catch (Exception e) { logger.debug("Registry cleanup warning for {}: {}", targetId, e.getMessage()); }
            notifyParentOfChildDisconnect(targetId);
            logger.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            logger.info("Client {} disconnected. Reason: {}", targetId, reason);
        } catch (Exception e) {
            logger.error("Cascade disconnect failed for {}: {}", targetId, e.getMessage(), e);
        }
    }
    private static void notifyParentOfChildDisconnect(String childId) {
        try {
            var parentOpt = clientRegistry.findParentByChild(childId);
            if (parentOpt.isEmpty()) {
                logger.debug("Client {} has no parent, skipping notification", childId);
                return;
            }

            String parentId = parentOpt.get();
            logger.info("Attempting to notify parent {} about child {} disconnect", parentId, childId);

            CommandRequest notification = new CommandRequest(
                    "child_disconnected",
                    childId,
                    UUID.randomUUID().toString(),
                    "SERVER",
                    null
            );

            clientRegistry.getPendingCommandQueue().addPendingCommand(parentId, notification);

            schedulePendingWrite(parentId);

            logger.info("Successfully queued notification for parent {} about child {}", parentId, childId);

        } catch (Exception e) {
            logger.error("Error in notifyParentOfChildDisconnect for child {}: {}", childId, e.getMessage(), e);
        }
    }
}