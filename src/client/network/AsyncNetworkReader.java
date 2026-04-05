package client.network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shared.dto.CommandResponse;
import shared.dto.ForwardCommandObject;
import shared.utils.SerializationUtil;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AsyncNetworkReader implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(AsyncNetworkReader.class);
    private final SocketChannel channel;
    private final ConcurrentLinkedQueue<CommandResponse> responseQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<ForwardCommandObject> forwardQueue = new ConcurrentLinkedQueue<>();
    private volatile boolean running = true;

    private final ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
    private int expectedLength = -1;
    private ByteBuffer dataBuffer;

    public AsyncNetworkReader(SocketChannel channel) {
        this.channel = channel;
    }

    @Override
    public void run() {
        try {
            while (running && channel.isOpen()) {
                if (expectedLength == -1) {
                    while (lengthBuffer.hasRemaining()) {
                        if (channel.read(lengthBuffer) == -1) { close(); return; }
                    }
                    lengthBuffer.flip();
                    expectedLength = lengthBuffer.getInt();
                    dataBuffer = ByteBuffer.allocate(expectedLength);
                    lengthBuffer.clear();
                }
                while (dataBuffer.hasRemaining()) {
                    if (channel.read(dataBuffer) == -1) { close(); return; }
                }
                dataBuffer.flip();
                byte[] payload = new byte[expectedLength];
                dataBuffer.get(payload);
                dataBuffer.clear();
                expectedLength = -1;

                Object obj = SerializationUtil.deserialize(payload);
                if (obj instanceof CommandResponse resp) {
                    responseQueue.offer(resp);
                } else if (obj instanceof ForwardCommandObject fwd) {
                    forwardQueue.offer(fwd);
                } else {
                    logger.warn("Unknown object type received: {}", obj.getClass().getSimpleName());
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Reader thread terminated: {}", e.getMessage());
            close();
        }
    }

    public ConcurrentLinkedQueue<CommandResponse> getResponseQueue() { return responseQueue; }
    public ConcurrentLinkedQueue<ForwardCommandObject> getForwardQueue() { return forwardQueue; }
    public void stop() { running = false; }
    private void close() { running = false; try { channel.close(); } catch (IOException ignored) {} }
}