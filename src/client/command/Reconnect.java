package client.command;

import client.context.ClientContext;
import client.context.ClientSession;
import client.network.ConnectionManager;
import client.utils.SideFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.dto.HandshakeRequest;

import java.io.IOException;

public class Reconnect implements ClientCommand {
    private static final Logger logger = LoggerFactory.getLogger(Reconnect.class);
    private final ConnectionManager connectionManager;
    private final ClientContext context;
    private ClientSession clientSession;
    public Reconnect(ConnectionManager connectionManager, ClientContext context, ClientSession clientSession) {
        this.connectionManager = connectionManager;
        this.context = context;
        this.clientSession = clientSession;
    }

    @Override
    public CommandRequest execute(SideFlag flag) {
        if (connectionManager.isConnected()) {
            System.out.println("Already connected to server.");
            logger.debug("Reconnect command ignored: client is already connected.");
            return null;
        }
        logger.info("Initiating reconnection to {}:{}", connectionManager.getHost(), connectionManager.getPort());
        connectionManager.disconnect();
        String host = connectionManager.getHost();
        int port = connectionManager.getPort();
        if (connectionManager.connect(host, port)) {
            try {
                HandshakeRequest handshake = new HandshakeRequest(
                        context.getClientId(),
                        context.getParentClientId()
                );
                connectionManager.sendHandshake(handshake);
                logger.debug("Handshake request sent to server.");
                CommandResponse response = connectionManager.readResponse();
                if (response != null && response.success()) {
                    System.out.println("Reconnected to server and handshake confirmed.");
                    logger.info("Reconnection successful: handshake accepted for client {}", context.getClientId());
                    clientSession.restartNetworkReader();
                } else {
                    System.err.println("Error: Handshake rejected by server. Please verify your client credentials.");
                    logger.warn("Handshake rejected by server for client {}: response={}", context.getClientId(), response);
                    connectionManager.disconnect();
                }
            } catch (IOException e) {
                logger.error("IO error during reconnection handshake: {}", e.getMessage());
                System.err.println("Error: Reconnection failed due to a communication error. Please check your network connection.");
                connectionManager.disconnect();
            }
        } else {
            logger.warn("Connection attempt failed: unable to reach server at {}:{}", host, port);
            System.err.println("Error: Could not connect to server. Please ensure the server is running and the address is correct.");
        }
        return null;
    }

    public void setClientSession(ClientSession clientSession) {
        this.clientSession = clientSession;
    }
}