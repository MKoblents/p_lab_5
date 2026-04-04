package server.network;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shared.dto.ServerMessage;
import shared.utils.SerializationUtil;

public class ClientConnection {
    private static final Logger logger = LoggerFactory.getLogger(ClientConnection.class);
    private final SocketChannel channel;
    private final ReentrantLock writeLock = new ReentrantLock();
    private volatile boolean active = true;

    public ClientConnection(SocketChannel channel) {
        this.channel = channel;
    }
    public boolean send(ServerMessage serverMessage) throws IOException {
        byte[] data = SerializationUtil.serialize(serverMessage);
        if (!active || !channel.isOpen()) {
            return false;
        }
        writeLock.lock();
        try {
            ByteBuffer buffer = ByteBuffer.allocate(4 + data.length);
            buffer.putInt(data.length);
            buffer.put(data);
            buffer.flip();
            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }
            return true;
        } catch (IOException e) {
            logger.error("Failed to send to client", e);
            active = false;
            return false;
        } finally {
            writeLock.unlock();
        }
    }


    public SocketChannel getChannel() {
        return channel;
    }

    public boolean isActive() {
        return active && channel.isOpen();
    }

    public void close() {
        active = false;
        try {
            if (channel.isOpen()) {
                channel.close();
            }
        } catch (IOException e) {
            logger.error("Error closing channel", e);
        }
    }
}