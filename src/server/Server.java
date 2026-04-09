package server;

import server.client.ConnectionState;
import server.config.ServerConfig;
import server.console.ConsoleHandler;
import server.io.NonBlockingConsoleReader;
import server.manager.ClientRegistry;
import server.manager.CollectionManager;
import server.manager.Invoker;
import server.outputWorkers.CollectionSaver;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.dto.ForwardCommandObject;
import shared.dto.HandshakeRequest;
import shared.utils.LoggingConfigurator;
import shared.utils.SerializationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final AtomicBoolean running = new AtomicBoolean(true);
    private static final long HEARTBEAT_TIMEOUT_MS = 30_000L;
    private static final long SELECT_TIMEOUT_MS = 1_000L;

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
            ServerConfig config = ServerConfig.parse(args);
            LoggingConfigurator.configure(config.getLogLevel());
            logger.info("=== Single-Threaded NIO Server Starting ===");

            CollectionManager collectionManager = new CollectionManager();
            CollectionSaver collectionSaver = new CollectionSaver();
            clientRegistry = new ClientRegistry();
            invoker = new Invoker(collectionManager, clientRegistry);
            consoleHandler = new ConsoleHandler(collectionManager, collectionSaver, config.getFile(), running, clientRegistry);

            try { collectionManager.loadFromFile(config.getFile()); }
            catch (Exception e) { logger.error("Failed to load collection, starting empty: {}", e.getMessage()); }
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
                if (Duration.between(conn.lastHeartbeat, now).toMillis() > HEARTBEAT_TIMEOUT_MS) {
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
            } catch (Exception e) {
                String cid = key.attachment() instanceof ClientConnection c ? c.clientId : "UNKNOWN";
                logger.error("Key processing error for {}: {}", cid, e.getMessage(), e);
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
        SocketChannel sc = conn.channel;

        if (conn.state == ConnectionState.READ_LENGTH) {
            if (!readUntilFull(sc, conn.lengthBuffer)) return;
            conn.lengthBuffer.flip();
            conn.expectedLength = conn.lengthBuffer.getInt();
            conn.payloadBuffer = ByteBuffer.allocate(conn.expectedLength);
            conn.lengthBuffer.clear();
            conn.state = ConnectionState.READ_PAYLOAD;
        }

        if (conn.state == ConnectionState.READ_PAYLOAD) {
            if (!readUntilFull(sc, conn.payloadBuffer)) return;
            conn.payloadBuffer.flip();
            byte[] data = new byte[conn.expectedLength];
            conn.payloadBuffer.get(data);
            conn.payloadBuffer.clear();
            conn.expectedLength = -1;
            conn.state = ConnectionState.READ_LENGTH;
            processMessage(conn, data, key);
        }
    }

    private static boolean readUntilFull(SocketChannel sc, ByteBuffer buf) throws IOException {
        while (buf.hasRemaining()) {
            int read = sc.read(buf);
            if (read == -1) throw new ClosedChannelException();
            if (read == 0) return false;
        }
        return true;
    }

    private static void processMessage(ClientConnection conn, byte[] data, SelectionKey key) {
        try {
            Object obj = SerializationUtil.deserialize(data);
            conn.lastHeartbeat = Instant.now();

            if (obj instanceof HandshakeRequest hs) {
                conn.clientId = hs.clientId();
                conn.handshakeComplete = true;
                clientRegistry.register(hs.clientId(), hs.parentClientId());
                logger.info("Handshake OK: {} (parent: {})", hs.clientId(), hs.parentClientId());
                writeResponse(conn, new CommandResponse(true, null, "Handshake OK", "0", hs.clientId()));
                return;
            }

            if (!conn.handshakeComplete) { logger.warn("Data before handshake, ignoring."); return; }

            if (obj instanceof CommandRequest req) {
                CommandResponse resp = invoker.runCommand(req);
                writeResponse(conn, resp);

                if ("forward_command".equals(req.commandType()) && req.args() instanceof ForwardCommandObject fco) {
                    schedulePendingWrite(fco.childId());
                }

                if ("kill_client".equals(req.commandType()) && resp.success() && req.args() instanceof String targetId) {
                    cascadeDisconnect(targetId, "KILLED_BY_PARENT");
                }
            }
        } catch (Exception e) {
            logger.error("Message processing failed for {}: {}", conn.clientId, e.getMessage(), e);
        }
    }

    private static void writeResponse(ClientConnection conn, CommandResponse resp) throws IOException {
        byte[] data = SerializationUtil.serialize(resp);
        ByteBuffer buf = ByteBuffer.allocate(4 + data.length);
        buf.putInt(data.length);
        buf.put(data);
        buf.flip();
        conn.writeBuffer = buf;
        conn.state = ConnectionState.WRITE;
        conn.channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, conn);
    }

    private static void handleWrite(SelectionKey key) throws IOException {
        ClientConnection conn = (ClientConnection) key.attachment();
        SocketChannel sc = conn.channel;

        while (conn.writeBuffer != null && conn.writeBuffer.hasRemaining()) {
            int written = sc.write(conn.writeBuffer);
            if (written == 0) return;
            if (written == -1) throw new ClosedChannelException();
        }

        conn.writeBuffer = null;
        conn.state = ConnectionState.READ_LENGTH;
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
            Set<String> children = clientRegistry.getChildren(targetId);
            if (children != null) {
                for (String childId : new ArrayList<>(children)) {
                    cascadeDisconnect(childId, "PARENT_TERMINATED");
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
                        targetConn.channel.write(buf);
                    }
                }
            } catch (IOException e) {
                logger.debug("Failed to send termination to {}: {}", targetId, e.getMessage());
            }
            if (targetKey != null && targetKey.isValid()) {
                try { if (targetConn != null && targetConn.channel.isOpen()) targetConn.channel.close(); }
                catch (IOException ignored) {}
                targetKey.cancel();
            }
            try { clientRegistry.unregister(targetId); }
            catch (Exception e) { logger.debug("Registry cleanup warning for {}: {}", targetId, e.getMessage()); }

            logger.info("Client {} disconnected. Reason: {}", targetId, reason);
        } catch (Exception e) {
            logger.error("Cascade disconnect failed for {}: {}", targetId, e.getMessage(), e);
        }
    }
}