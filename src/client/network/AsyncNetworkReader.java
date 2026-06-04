package client.network;

import shared.enums.DisconnectReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.utils.SerializationUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class AsyncNetworkReader implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(AsyncNetworkReader.class);

    private final SocketChannel channel;
    private final Consumer<DisconnectReason> onDisconnect;

    // ✅ ГЛАВНОЕ ИЗМЕНЕНИЕ: Карта ожидающих запросов
    private final ConcurrentHashMap<String, CompletableFuture<CommandResponse>> pendingRequests = new ConcurrentHashMap<>();

    // Очередь только для форвард-команд (у них нет requestId, который ждет конкретный обработчик)
    private final ConcurrentLinkedQueue<CommandRequest> forwardQueue = new ConcurrentLinkedQueue<>();

    private volatile boolean running = true;
    private final ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
    private int expectedLength = -1;
    private ByteBuffer dataBuffer;

    public AsyncNetworkReader(SocketChannel channel, Consumer<DisconnectReason> onDisconnect) {
        this.channel = channel;
        this.onDisconnect = onDisconnect;
    }

    /**
     * Регистрирует ожидание ответа ПЕРЕД отправкой запроса.
     */
    public CompletableFuture<CommandResponse> registerRequest(String requestId, long timeoutMs) {
        CompletableFuture<CommandResponse> future = new CompletableFuture<>();
        pendingRequests.put(requestId, future);

        // Авто-очистка при таймауте, чтобы не засорять память
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
            while (running && channel.isOpen()) {
                if (expectedLength == -1) {
                    while (lengthBuffer.hasRemaining()) {
                        int read = channel.read(lengthBuffer);
                        if (read == -1) {
                            logger.warn("Server disconnected (EOF detected)");
                            triggerDisconnect(DisconnectReason.SERVER_DOWN);
                            close();
                            return;
                        }
                        if (read == 0) {
                            try { Thread.sleep(50); } catch (InterruptedException e) {}
                        }
                    }
                    lengthBuffer.flip();
                    expectedLength = lengthBuffer.getInt();
                    dataBuffer = ByteBuffer.allocate(expectedLength);
                    lengthBuffer.clear();
                }

                while (dataBuffer.hasRemaining()) {
                    int read = channel.read(dataBuffer);
                    if (read == -1) { close(); return; }
                    if (read == 0) {
                        try { Thread.sleep(50); } catch (InterruptedException e) {}
                    }
                }

                dataBuffer.flip();
                byte[] payload = new byte[expectedLength];
                dataBuffer.get(payload);
                dataBuffer.clear();
                expectedLength = -1;

                Object obj = SerializationUtil.deserialize(payload);
                System.out.println("re in reader: " + obj);

                if (obj instanceof CommandResponse resp) {
                    routeResponse(resp); // ✅ Маршрутизируем ответ нужному потоку
                } else if (obj instanceof CommandRequest fwd) {
                    forwardQueue.offer(fwd);
                } else {
                    logger.warn("Unknown object type received: {}", obj != null ? obj.getClass().getSimpleName() : "null");
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Reader thread terminated: {}", e.getMessage());
            triggerDisconnect(DisconnectReason.NETWORK_ERROR);
            close();
        }
    }

    /**
     * Распределяет ответ: если кто-то ждет этот requestId, отдаем ему. Иначе игнорируем.
     */
    private void routeResponse(CommandResponse resp) {
        String reqId = resp.requestId();
        if (reqId != null) {
            CompletableFuture<CommandResponse> future = pendingRequests.remove(reqId);
            if (future != null) {
                future.complete(resp); // ✅ Мгновенно доставляем ответ тому, кто его ждет
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
        // Отменяем все ожидающие запросы при остановке
        pendingRequests.forEach((id, future) -> future.completeExceptionally(new CancellationException("Reader stopped")));
        pendingRequests.clear();
    }

    private void close() {
        running = false;
        try { channel.close(); } catch (IOException ignored) {}
    }

    private void triggerDisconnect(DisconnectReason reason) {
        if (onDisconnect != null) {
            onDisconnect.accept(reason);
        }
    }
}