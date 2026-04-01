package client.context;

import client.network.ConnectionManager;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientContext {
    private final String clientId;
    private final String parentClientId;
    private final ConnectionManager connection;
    private final Instant createdAt;
    private final boolean isRoot;
    private volatile boolean active;
    private final int peerPort;
    private final List<String> childClientIds = new CopyOnWriteArrayList<>();  // Список ID детей

    public ClientContext(String clientId,
                         String parentClientId,
                         ConnectionManager connection,
                         boolean isRoot,
                         int peerPort) {
        this.clientId = clientId;
        this.parentClientId = parentClientId;
        this.connection = connection;
        this.createdAt = Instant.now();
        this.isRoot = isRoot;
        this.active = true;
        this.peerPort = peerPort;
    }
    public String getClientId() {
        return clientId;
    }
    public String getParentClientId() {
        return parentClientId;
    }
    public ConnectionManager getConnection() {
        return connection;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public boolean isRoot() {
        return isRoot;
    }
    public boolean isActive() {
        return active;
    }
    public void deactivate() {
        this.active = false;
        connection.disconnect();
    }
    public boolean isParentOf(String otherClientId) {
//TODO
        return false;
    }
    @Override
    public String toString() {
        return String.format("ClientContext{id=%s, parent=%s, root=%s, active=%s}",
                clientId,
                parentClientId != null ? parentClientId : "null",
                isRoot,
                active);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientContext that = (ClientContext) o;
        return clientId.equals(that.clientId);
    }
    @Override
    public int hashCode() {
        return clientId.hashCode();
    }

    public int getPeerPort() {
        return peerPort;
    }
    public boolean addChild(String childClientId){
        return childClientIds.add(childClientId);
    }
    public List<String> getChildClientIds() {
        return List.copyOf(childClientIds);
    }
    public boolean removeChild(String childClientId) {
        return childClientIds.remove(childClientId);
    }
    public void clearChildren() {
        childClientIds.clear();
    }
}