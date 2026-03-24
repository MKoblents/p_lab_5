package client.network;

import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.utils.SerializationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ConnectionManager {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);
    private SocketChannel socketChannel;
    private boolean connected = false;
    public boolean connect(String host, int port) {
        logger.debug("Connecting to {}:{}", host, port);
        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            InetSocketAddress address = new InetSocketAddress(host, port);
            boolean connected = socketChannel.connect(address);
            if (!connected) {
                int timeout = 5000;
                long start = System.currentTimeMillis();
                while (!socketChannel.finishConnect()) {
                    if (System.currentTimeMillis() - start > timeout) {
                        throw new IOException("Connection timeout");
                    }
                    Thread.sleep(100);
                }
            }
            this.connected = true;
            logger.info("Connected to {}:{}", host, port);
            System.out.println("Connected to " + host + ":" + port);
            return true;
        } catch (IOException | InterruptedException e) {
            logger.error("Connection failed: {}", e.getMessage());
            this.connected = false;
            System.err.println("Connection failed: " + e.getMessage());
            return false;
        }
    }
    public void sendRequest(CommandRequest request) throws IOException {
        if (!connected || socketChannel == null) {
            throw new IOException("Not connected to server");
        }
        logger.debug("Sending command: {}", request.commandType());
        byte[] data = SerializationUtil.serialize(request);
        ByteBuffer buffer = ByteBuffer.wrap(data);
        while (buffer.hasRemaining()) {
            socketChannel.write(buffer);
        }
        logger.debug("Request sent");
    }
    public CommandResponse readResponse() {
        if (!connected || socketChannel == null) {
            return null;
        }

        logger.debug("Waiting for response...");

        try {
            ByteBuffer buffer = ByteBuffer.allocate(8192);
            int totalRead = 0;

            while (true) {
                int read = socketChannel.read(buffer);
                logger.debug("socketChannel.read() returned: {}", read);  // ✅ ДОБАВЬ ЭТО

                if (read == -1) {
                    logger.error("Server disconnected");
                    connected = false;
                    return null;
                }

                if (read == 0) {
                    logger.trace("No data available, sleeping...");  // ✅ И ЭТО
                    if (totalRead > 0) {
                        // Пробуем десериализовать то, что есть
                        buffer.flip();
                        byte[] data = new byte[buffer.remaining()];
                        buffer.get(data);
                        try {
                            CommandResponse response = (CommandResponse) SerializationUtil.deserialize(data);
                            logger.debug("Response received from partial data");
                            return response;
                        } catch (IOException | ClassNotFoundException e) {
                            buffer.clear();
                        }
                    }
                    Thread.sleep(50);
                    continue;
                }

                totalRead += read;
                logger.info("Read {} bytes (total: {})", read, totalRead);  // ✅ И ЭТО

                buffer.flip();
                byte[] data = new byte[buffer.remaining()];
                buffer.get(data);

                try {
                    CommandResponse response = (CommandResponse) SerializationUtil.deserialize(data);
                    logger.debug("Response received");
                    return response;
                } catch (IOException | ClassNotFoundException e) {
                    logger.warn("Cannot deserialize yet ({} bytes), waiting for more...", totalRead);
                    buffer.clear();
                }
            }

        } catch (Exception e) {
            logger.error("Error reading response: {}", e.getMessage(), e);
            return null;
        }
    }
    public void disconnect() {
        logger.info("Disconnecting");
        try {
            if (socketChannel != null && socketChannel.isOpen()) {
                socketChannel.close();
            }
        } catch (IOException e) {
            logger.error("Error closing connection: {}", e.getMessage());
        } finally {
            connected = false;
            socketChannel = null;
        }
    }

    public boolean isConnected() {
        return connected;
    }
}