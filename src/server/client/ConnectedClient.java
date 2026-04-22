package server.client;

import shared.dto.ClientStatus;
import shared.enums.ClientState;

import java.time.Instant;
import java.util.Optional;

public class ConnectedClient {
    private final String clientId;
    private ClientState clientState;
    private Instant lastHeartbeat;
    public ConnectedClient(String clientId, ClientState clientState){
        this.clientId = clientId;
        this.clientState = clientState;
        this.lastHeartbeat = Instant.now();
    }
    public void upgradeHeartbeat(){
        this.lastHeartbeat = Instant.now();
    }
    public ClientStatus getClientStatus(){
        return new ClientStatus(clientId,clientState,lastHeartbeat, Optional.empty());
    }
    public void markOffline(){
        this.clientState = ClientState.OFFLINE;
    }
    public void markOnline(){
        this.clientState = ClientState.ONLINE;
    }

}
