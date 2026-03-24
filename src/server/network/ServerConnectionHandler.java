package server.network;

import server.manager.Invoker;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.utils.SerializationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ServerConnectionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ServerConnectionHandler.class);
    private static final int BUFFER_SIZE = 8192;
    private final Selector selector;
    private final Invoker invoker;
    public ServerConnectionHandler(Selector selector, Invoker invoker) {
        this.selector = selector;
        this.invoker = invoker;
    }

    /**
     * Handles new client connections (OP_ACCEPT).
     */
    public void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        if (clientChannel != null) {
            clientChannel.configureBlocking(false);
            clientChannel.register(selector, SelectionKey.OP_READ);
            String clientAddress = ((InetSocketAddress) clientChannel.getRemoteAddress()).toString();
            logger.info("New connection accepted from: {}", clientAddress);
        }
    }
    /**
     * Handles incoming data from clients (OP_READ).
     */
    public void handleRead(SelectionKey key) {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        try {
            int bytesRead = clientChannel.read(buffer);
            if (bytesRead == -1) {
                logger.info("Client disconnected: {}", clientChannel.getRemoteAddress());
                key.cancel();
                clientChannel.close();
                return;
            }
            if (bytesRead > 0) {
                buffer.flip();
                byte[] data = new byte[buffer.remaining()];
                buffer.get(data);
                CommandRequest request = (CommandRequest) SerializationUtil.deserialize(data);
                logger.debug("Received command: {} from {}", request.getCommandKey(), clientChannel.getRemoteAddress());
                CommandResponse response = invoker.runCommand(request);
                logger.debug("Executed command: {}, success: {}", request.getCommandKey(), response.isSuccess());
                byte[] responseData = SerializationUtil.serialize(response);
                ByteBuffer responseBuffer = ByteBuffer.allocate(4 + responseData.length);
                responseBuffer.putInt(responseData.length);
                responseBuffer.put(responseData);
                responseBuffer.flip();
                while (responseBuffer.hasRemaining()) {
                    clientChannel.write(responseBuffer);
                }
                logger.trace("Response sent to {}", clientChannel.getRemoteAddress());
            }
        } catch (IOException e) {
            logger.error("IO Error with client {}: {}", clientChannel, e.getMessage());
            key.cancel();
            try { clientChannel.close(); } catch (IOException ex) { /* ignore */ }
        } catch (ClassNotFoundException e) {
            logger.error("Deserialization error: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error processing request: {}", e.getMessage(), e);
        }
    }
}