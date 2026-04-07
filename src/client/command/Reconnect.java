package client.command;

import client.context.ClientContext;
import client.context.ClientSession;
import client.network.ConnectionManager;
import client.utils.SideFlag;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.dto.HandshakeRequest;

import java.io.IOException;

public class Reconnect implements  ClientCommand{
    private ConnectionManager connectionManager;
    private ClientContext context;
    private ClientSession clientSession;
    public Reconnect(ConnectionManager connectionManager, ClientContext context, ClientSession clientSession){
        this.connectionManager = connectionManager;
        this.context = context;
        this.clientSession = clientSession;
    }

    @Override
    public CommandRequest execute(SideFlag flag) {
        if (connectionManager.isConnected()){
            System.out.println("Already connected to server.");
            return null;
        }
        String host = connectionManager.getHost();
        int port = connectionManager.getPort();
        if (connectionManager.connect(host, port)) {
            try {
                HandshakeRequest handshake = new HandshakeRequest(
                        context.getClientId(),
                        context.getParentClientId()
                );
                connectionManager.sendHandshake(handshake);
//                System.out.println("Reconnected to server");
                CommandResponse response = connectionManager.readResponse();
                if (response != null && response.success()) {
                    System.out.println(" Reconnected & handshake confirmed!");
                    clientSession.restartNetworkReader();
                } else {
                    System.err.println("⚠️ Handshake rejected by server.");
                    connectionManager.disconnect();
                }
            } catch (IOException e) {
                System.err.println(" Reconnection failed: " + e.getMessage());
                connectionManager.disconnect();
            }
        } else {
            System.err.println("❌ Подключение не удалось. Сервер запущен?");
        }
        return null;
    }

    public void setClientSession(ClientSession clientSession) {
        this.clientSession = clientSession;
    }
}
