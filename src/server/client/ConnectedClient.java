package server.client;

import server.network.ClientConnection;
import shared.dto.ClientStatus;
import shared.enums.ClientState;

import java.time.Instant;
import java.util.Optional;

public class ConnectedClient {
    private final String clientId;
    private ClientState clientState;
    private Instant lastHeartbeat;
    private int commandsExecuted;
    private ClientConnection clientConnection;
    public ConnectedClient(String clientId, ClientState clientState, ClientConnection clientConnection){
        this.clientId = clientId;
        this.clientState = clientState;
        this.lastHeartbeat = Instant.now();
        this.clientConnection = clientConnection;
    }
    public void upgradeHeartbeat(){
        this.lastHeartbeat = Instant.now();
    }
    public ClientStatus getClientStatus(){
        return new ClientStatus(clientId,clientState,lastHeartbeat,commandsExecuted, Optional.empty());
    }
    public void markOffline(){
        this.clientState = ClientState.OFFLINE;
    }
    public void markOnline(){
        this.clientState = ClientState.ONLINE;
    }

    public ClientConnection getClientConnection() {
        return clientConnection;
    }
}
