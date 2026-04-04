package client.network;

import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.dto.HandshakeRequest;
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
    private String host;
    private int port;
    public boolean connect(String host, int port) {
        this.host = host;
        this.port = port;
        logger.debug("Attempting to connect to {}:{}", host, port);
        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            InetSocketAddress address = new InetSocketAddress(host, port);
            logger.trace("Opening connection to {}", address);
            boolean connectionStarted = socketChannel.connect(address);
            if (!connectionStarted) {
                logger.trace("Connection not established immediately, waiting for finishConnect()");
                int timeout = 5000;
                long start = System.currentTimeMillis();
                while (!socketChannel.finishConnect()) {
                    if (System.currentTimeMillis() - start > timeout) {
                        logger.error("Connection timeout to {}:{}", host, port);
                        throw new IOException("Connection timeout");
                    }
                    Thread.sleep(100);
                }
                logger.trace("finishConnect() completed");
            }
            this.connected = true;
            logger.info("Successfully connected to {}:{}", host, port);
            System.out.println("Connected to " + host + ":" + port);
            return true;
        } catch (IOException e) {
            logger.error("IO error while connecting to {}:{}: {}", host, port, e.getMessage(), e);
            this.connected = false;
            System.err.println("Connection failed: " + e.getMessage());
            return false;
        } catch (InterruptedException e) {
            logger.error("Connection interrupted to {}:{}", host, port, e);
            Thread.currentThread().interrupt();
            this.connected = false;
            System.err.println("Connection failed: " + e.getMessage());
            return false;
        }
    }
    public void sendRequest(CommandRequest request) throws IOException {
        if (!connected || socketChannel == null) {
            logger.error("Attempted to send request but not connected");
            throw new IOException("Not connected to server");
        }
        String requestId = request.requestId();
        String commandType = request.commandType();
        logger.debug("Sending request: command={}, requestId={}", commandType, requestId);
        byte[] data = SerializationUtil.serialize(request);
        ByteBuffer buffer = ByteBuffer.allocate(4 + data.length);
        buffer.putInt(data.length);
        buffer.put(data);
        buffer.flip();
        logger.trace("Writing {} bytes to channel", buffer.remaining());
        while (buffer.hasRemaining()) {
            socketChannel.write(buffer);
        }
        logger.debug("Request sent successfully: requestId={}", requestId);
    }
    public CommandResponse readResponse() {
        if (!connected || socketChannel == null) {
            logger.warn("Attempted to read response but not connected");
            return null;
        }
        logger.debug("Waiting for response from server...");
        try {
            ByteBuffer buffer = ByteBuffer.allocate(4);
            while (buffer.hasRemaining()) {
                int read = socketChannel.read(buffer);
                if (read == -1) {
                    logger.error("Server disconnected while reading response length");
                    connected = false;
                    return null;
                }
            }
            buffer.flip();
            if (buffer.remaining() < 4) {
                logger.warn("Incomplete length header received");
                return null;
            }
            int length = buffer.getInt();
            logger.trace("Response length: {} bytes", length);
            ByteBuffer dataBuffer = ByteBuffer.allocate(length);
            while (dataBuffer.hasRemaining()) {
                int read = socketChannel.read(dataBuffer);
                if (read == -1) {
                    logger.error("Server disconnected while reading response data");
                    connected = false;
                    return null;
                }
            }
            dataBuffer.flip();
            byte[] data = new byte[length];
            dataBuffer.get(data);
            logger.trace("Deserializing response ({} bytes)", data.length);
            CommandResponse response = (CommandResponse) SerializationUtil.deserialize(data);
            if (response != null) {
                String requestId = response.requestId();
                logger.debug("Response received: success={}, requestId={}",
                        response.success(), requestId != null ? requestId : "null");
            } else {
                logger.warn("Deserialized response is null");
            }
            return response;
        } catch (IOException e) {
            logger.error("IO error while reading response: {}", e.getMessage(), e);
            connected = false;
            return null;
        } catch (ClassNotFoundException e) {
            logger.error("Deserialization error: class not found - {}", e.getMessage(), e);
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error while reading response: {}", e.getMessage(), e);
            return null;
        }
    }
    public void disconnect() {
        logger.info("Disconnecting from server");
        try {
            if (socketChannel != null && socketChannel.isOpen()) {
                logger.trace("Closing socket channel");
                socketChannel.close();
                logger.debug("Socket channel closed successfully");
            } else {
                logger.trace("Socket channel already closed or null");
            }
        } catch (IOException e) {
            logger.error("Error while closing connection: {}", e.getMessage(), e);
        } finally {
            connected = false;
            socketChannel = null;
            logger.info("Disconnected from server");
        }
    }
    public boolean isConnected() {
        return connected;
    }

    public String getHost() {
        return host;
    }
    public int getPort(){
        return port;
    }
    public boolean sendHandshake(HandshakeRequest handshake) throws IOException {
        if (!connected || socketChannel == null) {
            throw new IOException("Not connected to server");
        }
        logger.debug("Sending handshake for client: {}", handshake.clientId());
        byte[] data = SerializationUtil.serialize(handshake);
        ByteBuffer buffer = ByteBuffer.allocate(4 + data.length);
        buffer.putInt(data.length);
        buffer.put(data);
        buffer.flip();
        while (buffer.hasRemaining()) {
            socketChannel.write(buffer);
        }
        return true;
    }
}