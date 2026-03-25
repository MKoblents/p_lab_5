package server.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.Server;
import server.manager.Invoker;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.utils.SerializationUtil;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

public class ClientHandler {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    public static void handleClient(Socket clientSocket, Invoker invoker) {
        try (
                java.io.InputStream in = clientSocket.getInputStream();
                java.io.OutputStream out = clientSocket.getOutputStream()
        ) {
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

}
