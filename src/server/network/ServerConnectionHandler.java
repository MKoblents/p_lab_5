//package server.network;
//
//import server.manager.ClientRegistry;
//import server.manager.Invoker;
//import shared.dto.CommandRequest;
//import shared.dto.CommandResponse;
//import shared.dto.HandshakeRequest;
//import shared.utils.SerializationUtil;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.slf4j.MDC;
//
//import java.io.IOException;
//import java.net.InetSocketAddress;
//import java.nio.ByteBuffer;
//import java.nio.channels.SelectionKey;
//import java.nio.channels.Selector;
//import java.nio.channels.ServerSocketChannel;
//import java.nio.channels.SocketChannel;
//
//public class ServerConnectionHandler {
//    private static final Logger logger = LoggerFactory.getLogger(ServerConnectionHandler.class);
//    private static final int BUFFER_SIZE = 16384;
//
//    private final Selector selector;
//    private final Invoker invoker;
//    private final ClientRegistry clientRegistry;
//
//    public ServerConnectionHandler(Selector selector, Invoker invoker, ClientRegistry clientRegistry) {
//        this.selector = selector;
//        this.invoker = invoker;
//        this.clientRegistry = clientRegistry;
//    }
//
//    public void handleAccept(SelectionKey key) throws IOException {
//        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
//        SocketChannel clientChannel = serverChannel.accept();
//
//        if (clientChannel != null) {
//            clientChannel.configureBlocking(false);
//            key.attach(ClientChannelState.HANDSHAKE_PENDING);
//            clientChannel.register(selector, SelectionKey.OP_READ);
//           String clientAddress = ((InetSocketAddress) clientChannel.getRemoteAddress()).toString();
//            logger.info("Client connected: {}", clientAddress);
//        }
//    }
//
//    public void handleRead(SelectionKey key) {
//        SocketChannel clientChannel = (SocketChannel) key.channel();
//        ClientChannelState state = (ClientChannelState) key.attachment();
//        try {
//            ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
//            int bytesRead = clientChannel.read(lengthBuffer);
//            if (bytesRead == -1) {
//                logger.info("Client disconnected: {}", clientChannel.getRemoteAddress());
//                key.cancel();
//                clientChannel.close();
//                return;
//            }
//            if (bytesRead < 4) {
//                return;
//            }
//            lengthBuffer.flip();
//            int messageLength = lengthBuffer.getInt();
//            ByteBuffer dataBuffer = ByteBuffer.allocate(messageLength);
//            while (dataBuffer.hasRemaining()) {
//                int read = clientChannel.read(dataBuffer);
//                if (read == -1) {
//                    logger.warn("Unexpected disconnect while reading message");
//                    key.cancel();
//                    clientChannel.close();
//                    return;
//                }
//            }
//
//            dataBuffer.flip();
//            byte[] data = new byte[messageLength];
//            dataBuffer.get(data);
//            if (state == ClientChannelState.HANDSHAKE_PENDING) {
//                processHandshake(clientChannel, key, data);
//            } else {
//                processCommand(clientChannel, data);
//            }
//        } catch (IOException e){
//            logger.error("IO error during read", e);
//            closeChannel(clientChannel, key);
//        } catch (Exception e) {
//            logger.error("Unexpected error", e);
//        }
//    }
//    private void processHandshake(SocketChannel channel, SelectionKey key, byte[] data)
//            throws IOException, ClassNotFoundException {
//        HandshakeRequest handshake = (HandshakeRequest) SerializationUtil.deserialize(data);
//        String clientId = handshake.clientId();
//        String parentClientId = handshake.parentClientId();
//        if (parentClientId != null && !clientRegistry.exists(parentClientId)) {
//            logger.warn("Handshake rejected: parent {} does not exist", parentClientId);
//            sendErrorResponse(channel, "Parent client not found");
//            closeChannel(channel, key);
//            return;
//        }
////        ClientConnection connection = new ClientConnection(channel);
//        clientRegistry.register(clientId, parentClientId);
//        key.attach(ClientChannelState.READY_FOR_COMMANDS);
//        logger.info("Client {} registered successfully. Parent: {}", clientId,
//                parentClientId != null ? parentClientId : "ROOT");
//    }
//
//    private void processCommand(SocketChannel channel, byte[] data) throws IOException, ClassNotFoundException {
//        CommandRequest request = (CommandRequest) SerializationUtil.deserialize(data);
//        String requestId = request.requestId();
//        if (requestId != null && !requestId.isEmpty()) {
//            MDC.put("requestId", requestId);
//        }
//        logger.debug("Received command: {}", request.commandType());
//        CommandResponse response = invoker.runCommand(request);
//        byte[] responseData = SerializationUtil.serialize(response);
//
//        ByteBuffer responseBuffer = ByteBuffer.allocate(4 + responseData.length);
//        responseBuffer.putInt(responseData.length);
//        responseBuffer.put(responseData);
//        responseBuffer.flip();
//        while (responseBuffer.hasRemaining()) {
//            channel.write(responseBuffer);
//        }
//        logger.trace("Response sent for requestId {} - connection KEPT OPEN", requestId);
//    }
//    private void sendErrorResponse(SocketChannel channel, String message) throws IOException {
//        byte[] msgBytes = message.getBytes(java.nio.charset.StandardCharsets.UTF_8);
//        ByteBuffer buf = ByteBuffer.allocate(4 + msgBytes.length);
//        buf.putInt(msgBytes.length);
//        buf.put(msgBytes);
//        buf.flip();
//        channel.write(buf);
//    }
//
//    private void closeChannel(SocketChannel channel, SelectionKey key) {
//        try {
//            if (channel.isOpen()) channel.close();
//        } catch (IOException ignored) {}
//        if (key != null) key.cancel();
//    }
//}