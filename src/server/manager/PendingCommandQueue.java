package server.manager;

import shared.dto.ForwardCommandObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PendingCommandQueue {
    private Map<String, ConcurrentLinkedQueue<ForwardCommandObject>> pendingMap = new ConcurrentHashMap<>();

    public boolean addPendingCommand(String clientId, ForwardCommandObject commandObject){
        return pendingMap.computeIfAbsent(clientId, k -> new ConcurrentLinkedQueue<ForwardCommandObject>()).add(commandObject);
    }
    public ForwardCommandObject poll(String clientId){
        if (pendingMap.get(clientId) == null){
            return null;
        }
        return pendingMap.get(clientId).poll();
    }

}
