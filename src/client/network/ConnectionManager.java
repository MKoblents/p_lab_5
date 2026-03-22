package client.network;

import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.utils.SerializationUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ConnectionManager {
    private SocketChannel socketChannel;
    private boolean isConnected = false;
    private static final int BUFFER_SIZE = 16384; // 16KB
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int RESPONSE_TIMEOUT_MS = 5000;

    /**
     * Connects to server with timeout.
     * @param host server hostname
     * @param port server port
     * @return true if connected successfully
     */
    public boolean connect(String host, int port) {
        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            InetSocketAddress address = new InetSocketAddress(host, port);
            long start = System.currentTimeMillis();
            while (!socketChannel.finishConnect()) {
                if (System.currentTimeMillis() - start > CONNECT_TIMEOUT_MS) {
                    System.err.println("Connection timeout to " + host + ":" + port);
                    return false;
                }
                Thread.sleep(100);
            }
            isConnected = true;
            System.out.println("Connected to server at " + host + ":" + port);
            return true;
        } catch (IOException e) {
            System.err.println("Connection failed: " + e.getMessage());
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Sends CommandRequest to server with retry logic.
     * @param request the request to send
     * @throws IOException if send fails after retries
     */
    public void sendRequest(CommandRequest request) throws IOException {
        if (!isConnected || socketChannel == null || !socketChannel.isConnected()) {
            throw new IOException("Not connected to server. Try reconnecting.");
        }
        byte[] data = SerializationUtil.serialize(request);
        ByteBuffer buffer = ByteBuffer.allocate(4 + data.length);
        buffer.putInt(data.length);
        buffer.put(data);
        buffer.flip();
        int retries = 3;
        while (retries > 0) {
            try {
                while (buffer.hasRemaining()) {
                    socketChannel.write(buffer);
                }
                return;
            } catch (IOException e) {
                retries--;
                if (retries == 0) {
                    isConnected = false;
                    throw new IOException("Failed to send request after 3 attempts: " + e.getMessage());
                }
                System.err.println("Send failed, retrying... (" + retries + " attempts left)");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Waits for CommandResponse from server with timeout.
     * @param timeoutMs timeout in milliseconds
     * @return CommandResponse or null if timeout/error
     */
    public CommandResponse waitForResponse(long timeoutMs) {
        if (!isConnected || socketChannel == null) {
            return null;
        }
        try {
            long start = System.currentTimeMillis();
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            while (System.currentTimeMillis() - start < timeoutMs) {
                int bytesRead = socketChannel.read(buffer);
                if (bytesRead == -1) {
                    System.err.println("Server disconnected");
                    isConnected = false;
                    return null;
                }
                if (bytesRead > 0) {
                    buffer.flip();
                    if (buffer.remaining() < 4) {
                        buffer.compact();
                        Thread.sleep(100);
                        continue;
                    }
                    int length = buffer.getInt();
                    if (buffer.remaining() < length) {
                        buffer.compact();
                        Thread.sleep(100);
                        continue;
                    }
                    byte[] data = new byte[length];
                    buffer.get(data);
                    return (CommandResponse) SerializationUtil.deserialize(data);
                }
                Thread.sleep(100);
            }
            System.err.println("Response timeout (" + timeoutMs + "ms)");
            return null;
        } catch (IOException e) {
            System.err.println("Error receiving response: " + e.getMessage());
            isConnected = false;
            return null;
        } catch (ClassNotFoundException e) {
            System.err.println("Error deserializing response: " + e.getMessage());
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    /**
     * Disconnects from server and closes channel.
     */
    public void disconnect() {
        try {
            if (socketChannel != null && socketChannel.isOpen()) {
                socketChannel.close();
                System.out.println("Connection closed.");
            }
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        } finally {
            socketChannel = null;
            isConnected = false;
        }
    }

    /**
     * Checks if currently connected to server.
     */
    public boolean isConnected() {
        return isConnected && socketChannel != null && socketChannel.isConnected();
    }

    /**
     * Attempts to reconnect to server.
     */
    public boolean reconnect(String host, int port) {
        disconnect();
        return connect(host, port);
    }
}