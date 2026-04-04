package server.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.client.ConnectedClient;
import server.network.ClientConnection;
import shared.enums.ClientState;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClientRegistry {
    private static final Logger logger = LoggerFactory.getLogger(ClientRegistry.class);
    private final Map<String, ConnectedClient> clients = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> parentChildRelations = new ConcurrentHashMap<>();
    public void register(String clientId, String parentClientId) {
        ConnectedClient client = new ConnectedClient(clientId, ClientState.ONLINE);
        clients.put(clientId, client);
        if (parentClientId != null) {
            parentChildRelations
                    .computeIfAbsent(parentClientId, k -> ConcurrentHashMap.newKeySet())
                    .add(clientId);

            logger.info("Client {} registered as child of {}", clientId, parentClientId);
        } else {
            logger.info("Root client {} registered", clientId);
        }
    }
    public void unregister(String clientId) {
        logger.info("Unregistering client: {}", clientId);
        Set<String> children = parentChildRelations.remove(clientId);
        if (children != null) {
            for (String childId : children) {
                unregister(childId);
            }
        }
        ConnectedClient removed = clients.remove(clientId);
        if (removed != null) {
            removed.markOffline();
            logger.info("Client {} unregistered", clientId);
        }
        parentChildRelations.values().forEach(childrenSet ->
                childrenSet.remove(clientId)
        );
    }
    public boolean exists(String clientId) {
        return clients.containsKey(clientId);
    }
    public Optional<ConnectedClient> getClient(String clientId) {
        return Optional.ofNullable(clients.get(clientId));
    }
    public Set<String> getChildren(String parentId) {
        return parentChildRelations.getOrDefault(parentId, Collections.emptySet());
    }
    public int size() {
        return clients.size();
    }
    public void updateHeartbeat(String clientId) {
        ConnectedClient client = clients.get(clientId);
        if (client != null) {
            client.upgradeHeartbeat();
        }
    }
}