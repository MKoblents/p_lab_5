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
    private boolean connected = false;

    public boolean connect(String host, int port) {
        try {
            socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(host, port));
            socketChannel.configureBlocking(true);
            connected = true;
            System.out.println("Connected to " + host + ":" + port);
            return true;
        } catch (IOException e) {
            System.err.println("Connection failed: " + e.getMessage());
            connected = false;
            return false;
        }
    }
    public void sendRequest(CommandRequest request) throws IOException {
        if (!connected || socketChannel == null) {
            throw new IOException("Not connected to server");
        }
        byte[] data = SerializationUtil.serialize(request);
        ByteBuffer buffer = ByteBuffer.allocate(4 + data.length);
        buffer.putInt(data.length);
        buffer.put(data);
        buffer.flip();
        while (buffer.hasRemaining()) {
            socketChannel.write(buffer);
        }
    }
    public CommandResponse readResponse() {
        if (!connected || socketChannel == null) {
            return null;
        }
        try {
            ByteBuffer buffer = ByteBuffer.allocate(4);
            while (buffer.hasRemaining()) {
                socketChannel.read(buffer);
            }
            buffer.flip();
            int length = buffer.getInt();
            ByteBuffer dataBuffer = ByteBuffer.allocate(length);
            while (dataBuffer.hasRemaining()) {
                socketChannel.read(dataBuffer);
            }
            dataBuffer.flip();
            byte[] data = new byte[length];
            dataBuffer.get(data);
            return (CommandResponse) SerializationUtil.deserialize(data);
        } catch (Exception e) {
            System.err.println("Error reading response: " + e.getMessage());
            connected = false;
            return null;
        }
    }
    public void disconnect() {
        try {
            if (socketChannel != null && socketChannel.isOpen()) {
                socketChannel.close();
            }
        } catch (IOException e) {
        }
        connected = false;
    }

//    public boolean isConnected() {
//        return connected;
//    }
}