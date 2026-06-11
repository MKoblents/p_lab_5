package client.network;

import shared.enums.DisconnectReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.utils.SerializationUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class AsyncNetworkReader implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(AsyncNetworkReader.class);

    private final SocketChannel channel;
    private final Consumer<DisconnectReason> onDisconnect;

    private final ConcurrentHashMap<String, CompletableFuture<CommandResponse>> pendingRequests = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<CommandRequest> forwardQueue = new ConcurrentLinkedQueue<>();

    private volatile boolean running = true;
    private Selector selector;
    private enum ReadState { READING_LENGTH, READING_PAYLOAD }
    private ReadState readState = ReadState.READING_LENGTH;

    private final ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
    private ByteBuffer dataBuffer;
    private int expectedLength = -1;

    public AsyncNetworkReader(SocketChannel channel, Consumer<DisconnectReason> onDisconnect) {
        this.channel = channel;
        this.onDisconnect = onDisconnect;
    }

    public CompletableFuture<CommandResponse> registerRequest(String requestId, long timeoutMs) {
        CompletableFuture<CommandResponse> future = new CompletableFuture<>();
        pendingRequests.put(requestId, future);
        CompletableFuture.delayedExecutor(timeoutMs + 500, TimeUnit.MILLISECONDS)
                .execute(() -> {
                    if (!future.isDone()) {
                        future.completeExceptionally(new TimeoutException("Request " + requestId + " timed out"));
                        pendingRequests.remove(requestId);
                    }
                });
        return future;
    }

    @Override
    public void run() {
        try {
            channel.configureBlocking(false);
            selector = Selector.open();
            channel.register(selector, SelectionKey.OP_READ);
            logger.info("AsyncNetworkReader started in non-blocking mode with Selector");
            while (running && channel.isOpen() && selector.isOpen()) {
                int readyChannels = selector.select(1000);
                if (!running) break;
                if (readyChannels == 0) continue;

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if (!key.isValid()) continue;

                    if (key.isReadable()) {
                        readData(key);
                    }
                }
            }
        } catch (IOException e) {
            if (running) {
                logger.error("Reader thread terminated: {}", e.getMessage());
                triggerDisconnect(DisconnectReason.NETWORK_ERROR);
            }
        } finally {
            close();
        }
    }

    private void readData(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        while (true) {
            if (readState == ReadState.READING_LENGTH) {
                int bytesRead = channel.read(lengthBuffer);
                if (bytesRead == -1) {
                    handleDisconnect();
                    return;
                }
                if (bytesRead == 0 && lengthBuffer.hasRemaining()) {
                    break;
                }

                if (!lengthBuffer.hasRemaining()) {
                    lengthBuffer.flip();
                    expectedLength = lengthBuffer.getInt();
                    lengthBuffer.clear();

                    if (expectedLength <= 0 || expectedLength > 10_000_000) {
                        logger.error("Invalid message length: {}", expectedLength);
                        handleDisconnect();
                        return;
                    }

                    dataBuffer = ByteBuffer.allocate(expectedLength);
                    readState = ReadState.READING_PAYLOAD;
                }
            }
            if (readState == ReadState.READING_PAYLOAD) {
                int bytesRead = channel.read(dataBuffer);
                if (bytesRead == -1) {
                    handleDisconnect();
                    return;
                }

                if (bytesRead == 0 && dataBuffer.hasRemaining()) {
                    break;
                }

                if (!dataBuffer.hasRemaining()) {
                    dataBuffer.flip();
                    byte[] payload = new byte[expectedLength];
                    dataBuffer.get(payload);
                    dataBuffer.clear();
                    expectedLength = -1;
                    readState = ReadState.READING_LENGTH;
                    processMessage(payload);
                }
            }
        }
    }

    private void processMessage(byte[] payload) {
        try {
            Object obj = SerializationUtil.deserialize(payload);
            if (obj instanceof CommandResponse resp) {
                routeResponse(resp);
            } else if (obj instanceof CommandRequest fwd) {
                forwardQueue.offer(fwd);
            } else {
                logger.warn("Unknown object type received: {}", obj != null ? obj.getClass().getSimpleName() : "null");
            }
        } catch (Exception e) {
            logger.error("Deserialization error: {}", e.getMessage());
        }
    }

    private void handleDisconnect() {
        logger.warn("Server disconnected (EOF detected)");
        triggerDisconnect(DisconnectReason.SERVER_DOWN);
        close();
    }

    private void routeResponse(CommandResponse resp) {
        String reqId = resp.requestId();
        if (reqId != null) {
            CompletableFuture<CommandResponse> future = pendingRequests.remove(reqId);
            if (future != null) {
                future.complete(resp);
                logger.debug("Response {} routed to pending future", reqId);
                return;
            }
        }
        logger.debug("Response {} ignored (no pending requester)", reqId);
    }

    public ConcurrentLinkedQueue<CommandRequest> getForwardQueue() {
        return forwardQueue;
    }

    public void stop() {
        running = false;
        if (selector != null && selector.isOpen()) {
            selector.wakeup();
        }
        pendingRequests.forEach((id, future) -> future.completeExceptionally(new CancellationException("Reader stopped")));
        pendingRequests.clear();
    }

    private void close() {
        running = false;
        try {
            if (selector != null) selector.close();
            if (channel.isOpen()) channel.close();
        } catch (IOException ignored) {}
    }

    private void triggerDisconnect(DisconnectReason reason) {
        if (onDisconnect != null) {
            onDisconnect.accept(reason);
        }
    }
}