package server.manager;

import shared.enums.DisconnectReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.client.ConnectedClient;
import shared.dto.CommandResponse;
import shared.enums.ClientState;
import shared.utils.SerializationUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class ClientRegistry {
    private static final Logger logger = LoggerFactory.getLogger(ClientRegistry.class);
    private final Map<String, ConnectedClient> clients = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> parentChildRelations = new ConcurrentHashMap<>();
    private PendingCommandQueue pendingCommandQueue = new PendingCommandQueue();
    private final Map<String, String> clientToUser = new ConcurrentHashMap<>();

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

    public Optional<String> findParentByChild(String childClientId) {
        for (Map.Entry<String, Set<String>> entry : parentChildRelations.entrySet()) {
            if (entry.getValue().contains(childClientId)) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }

    public synchronized void unregister(String clientId) {
        if (!clients.containsKey(clientId)) {
            return;
        }
        logger.info("Unregistering client: {}", clientId);
        Set<String> children = parentChildRelations.remove(clientId);
        if (children != null) {
            for (String childId : children) {
                logger.info("Forcing disconnect for child: {}", childId);
                unregister(childId);
            }
        }
        clients.remove(clientId);
        for (Set<String> childSet : parentChildRelations.values()) {
            childSet.remove(clientId);
        }
        logger.info("Client {} and subtree fully unregistered", clientId);
    }
    public void bindUserToClient(String clientId, String username) {
        clientToUser.put(clientId, username);
    }

    public String getUsernameByClientId(String clientId) {
        return clientToUser.get(clientId);
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
    public boolean isParentOf(String parentId, String childId){
        return existsInSubtree(parentId,childId::equals);
    }
    private boolean existsInSubtree(String parentId, Predicate<String> condition) {
        if (condition.test(parentId)) return true;
        for (String child : getChildren(parentId)) {
            if (existsInSubtree(child, condition)) return true;
        }
        return false;
    }

    public PendingCommandQueue getPendingCommandQueue() {
        return pendingCommandQueue;
    }

    public Collection<ConnectedClient> getAllClients() {
        return clients.values();
    }
    public void printStatusToConsole() {
        System.out.println("\n=== Connected Clients ===");
        System.out.printf("%-10s  %-10s %-10s %-35s %-10s%n", "ID","NAME", "STATE", "LAST_HEARTBEAT", "UPTIME");
        System.out.println("------------------------------------------------------------");
        for (ConnectedClient client : clients.values()) {
            var status = client.getClientStatus();
            String uptime = java.time.Duration.between(
                    status.lastHeartbeat(), java.time.Instant.now()
            ).toString().replaceAll("(\\d[HMS])(?!$)", "$1 ");
            System.out.printf("%-10s  %-10s %-10s %-35s %-10s%n",
                    status.clientId(),
                    getUsernameByClientId(status.clientId()),
                    status.clientState(),
                    status.lastHeartbeat(),
                    uptime
            );
        }
        System.out.println("============================================================\n");
    }
}