package client.context;

import client.network.ConnectionManager;
import com.sun.xml.bind.v2.TODO;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientContext {
    private final String clientId;
    private final String parentClientId;
    private final ConnectionManager connection;
    private final Instant createdAt;
    private final boolean isRoot;
    private volatile boolean active;
    private final List<String> childClientIds = new CopyOnWriteArrayList<>();
    public ClientContext(String clientId,
                         String parentClientId,
                         ConnectionManager connection,
                         boolean isRoot) {
        this.clientId = clientId;
        this.parentClientId = parentClientId;
        this.connection = connection;
        this.createdAt = Instant.now();
        this.isRoot = isRoot;
        this.active = true;
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
        return childClientIds.contains(otherClientId);
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