package server.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.Server;
import server.manager.ClientRegistry;
import server.manager.Invoker;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.dto.ForwardCommandObject;
import shared.dto.HandshakeRequest;
import shared.utils.SerializationUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class ClientHandler {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    public static void handleClient(Socket clientSocket, Invoker invoker, ClientRegistry clientRegistry) {
        try (
                InputStream in = clientSocket.getInputStream();
                OutputStream out = clientSocket.getOutputStream()
        ) {
            HandshakeRequest handshake = processHandshake(in, out, clientRegistry);

            if (handshake == null) {
                logger.warn("Handshake failed, closing connection");
                return;
            }
            clientRegistry.registerStream(handshake.clientId(), out);
            logger.debug("Streams opened for client: {}", clientSocket.getRemoteSocketAddress());
            while (!clientSocket.isClosed()) {
                try {
                    byte[] lengthBytes = new byte[4];
                    int bytesRead = 0;
                    while (bytesRead < 4) {
                        int read = in.read(lengthBytes, bytesRead, 4 - bytesRead);
                        if (read == -1) {
                            return;
                        }
                        bytesRead += read;
                    }
                    int length = java.nio.ByteBuffer.wrap(lengthBytes).getInt();
                    logger.trace("Request length: {} bytes", length);
                    byte[] data = new byte[length];
                    int dataRead = 0;
                    while (dataRead < length) {
                        int read = in.read(data, dataRead, length - dataRead);
                        if (read == -1) {
                            logger.warn("Unexpected disconnect while reading request");
                            return;
                        }
                        dataRead += read;
                    }
                    CommandRequest request = (CommandRequest) SerializationUtil.deserialize(data);
                    logger.debug("Received request: command={}, requestId={}",
                            request.commandType(), request.requestId());
                    CommandResponse response = invoker.runCommand(request);
                    logger.debug("Command executed: success={}", response.success());
                    byte[] responseData = SerializationUtil.serialize(response);
                    ByteBuffer responseBuffer = ByteBuffer.allocate(4 + responseData.length);
                    responseBuffer.putInt(responseData.length);
                    responseBuffer.put(responseData);
                    out.write(responseBuffer.array());
                    out.flush();
                    logger.info("Response sent for requestId: {}", response.requestId());
//                    CommandRequest pendingRequest = clientRegistry.getPendingCommandQueue().poll(request.clientId());
                    // Проверка: если текущий запрос был forward_command, нужно отправить ожидающую команду ребёнку
                    if ("forward_command".equals(request.commandType()) && request.args() instanceof ForwardCommandObject fco) {
                        String targetChildId = fco.childId();

                        // Опрос очереди по ID ребёнка (не родителя!)
                        CommandRequest pendingRequest = clientRegistry.getPendingCommandQueue().poll(targetChildId);

                        if (pendingRequest != null) {
                            logger.info("Found pending command for child {}: {}", targetChildId, pendingRequest.commandType());

                            // Получаем сокет ребёнка
                            OutputStream childOut = clientRegistry.getStream(targetChildId);
                            if (childOut != null) {
                                try {
                                    // Отправляем готовый CommandRequest ребёнку
                                    byte[] pendingData = SerializationUtil.serialize(pendingRequest);
                                    ByteBuffer pendingBuffer = ByteBuffer.allocate(4 + pendingData.length);
                                    pendingBuffer.putInt(pendingData.length);
                                    pendingBuffer.put(pendingData);
                                    childOut.write(pendingBuffer.array());
                                    childOut.flush();
                                    logger.info("Forwarded command {} to client {}",
                                            pendingRequest.commandType(), pendingRequest.clientId());
                                } catch (IOException e) {
                                    logger.error("Failed to forward command to child {}: {}",
                                            targetChildId, e.getMessage(), e);
                                }
                            } else {
                                logger.warn("OutputStream not registered for child client {}", targetChildId);
                            }
                        }
                    }
                } catch (java.io.EOFException e) {
                    logger.info("Client disconnected normally: {}", clientSocket.getRemoteSocketAddress());
                    break;
                } catch (IOException e) {
                    logger.error("IO error reading from client: {}", e.getMessage());
                    break;
                } catch (ClassNotFoundException e) {
                    logger.error("Deserialization error: {}", e.getMessage(), e);
                } catch (Exception e) {
                    logger.error("Unexpected error processing request: {}", e.getMessage(), e);
                }
            }
        } catch (IOException e) {
            logger.error("IO error with client {}: {}",
                    clientSocket.getRemoteSocketAddress(), e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
//            TODO
        } finally {
            try {
                logger.debug("Closing connection to client: {}", clientSocket.getRemoteSocketAddress());
                clientSocket.close();
                logger.info("Client disconnected: {}", clientSocket.getRemoteSocketAddress());
            } catch (IOException e) {
                logger.error("Error closing client socket: {}", e.getMessage());
            }
        }
    }
    private static HandshakeRequest processHandshake(InputStream in, OutputStream out, ClientRegistry registry)
            throws IOException, ClassNotFoundException {
        byte[] lenBytes = new byte[4];
        int read = in.read(lenBytes,0, 4);
        if (read < 4) return null;

        int length = ByteBuffer.wrap(lenBytes).getInt();
        byte[] data = new byte[length];
        int totalRead = 0;
        while (totalRead < length) {
            int r = in.read(data, totalRead, length - totalRead);
            if (r == -1) return null;
            totalRead += r;
        }
        HandshakeRequest handshake = (HandshakeRequest) SerializationUtil.deserialize(data);
        if (handshake.parentClientId() != null && !registry.exists(handshake.parentClientId())) {
            CommandResponse error = new CommandResponse(false, null, "Parent not found", "0", handshake.clientId());
            out.write(SerializationUtil.serialize(error));
            out.flush();
            return null;
        }
        registry.register(handshake.clientId(), handshake.parentClientId());
        logger.info("Client {} registered (parent: {})", handshake.clientId(),
                handshake.parentClientId() != null ? handshake.parentClientId() : "ROOT");
        CommandResponse ack = new CommandResponse(true, null, "Handshake OK", "0", handshake.clientId());
        byte[] responseData = SerializationUtil.serialize(ack);
        ByteBuffer responseBuffer = ByteBuffer.allocate(4 + responseData.length);
        responseBuffer.putInt(responseData.length);
        responseBuffer.put(responseData);
        out.write(responseBuffer.array());
        out.flush();

        return handshake;
    }
}
