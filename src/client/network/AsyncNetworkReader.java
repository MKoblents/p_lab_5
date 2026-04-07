package client.network;
import client.utils.DisconnectReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.utils.SerializationUtil;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class AsyncNetworkReader implements Runnable {
    private final Consumer<DisconnectReason> onDisconnect;
    private static final Logger logger = LoggerFactory.getLogger(AsyncNetworkReader.class);
    private final SocketChannel channel;
    private final ConcurrentLinkedQueue<CommandResponse> responseQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<CommandRequest> forwardQueue = new ConcurrentLinkedQueue<>();
    private volatile boolean running = true;

    private final ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
    private int expectedLength = -1;
    private ByteBuffer dataBuffer;

    public AsyncNetworkReader(SocketChannel channel, Consumer<DisconnectReason> onDisconnect) {
        this.channel = channel;
        this.onDisconnect = onDisconnect;
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
                            return; }
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
                if (obj instanceof CommandResponse resp) {
                    responseQueue.offer(resp);
                } else if (obj instanceof CommandRequest fwd) {
                    forwardQueue.offer(fwd);
                } else {
                    logger.warn("Unknown object type received: {}", obj.getClass().getSimpleName());
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Reader thread terminated: {}", e.getMessage());
            triggerDisconnect(DisconnectReason.NETWORK_ERROR);
//TODO check
            close();
//            if (onDisconnect != null) onDisconnect.run();
        }
    }

    public ConcurrentLinkedQueue<CommandResponse> getResponseQueue() { return responseQueue; }
    public ConcurrentLinkedQueue<CommandRequest> getForwardQueue() { return forwardQueue; }
    public void stop() { running = false; }
    private void close() { running = false; try { channel.close(); } catch (IOException ignored) {} }
    private void triggerDisconnect(DisconnectReason reason) {
        if (onDisconnect != null) {
            onDisconnect.accept(reason);
        }
    }
}