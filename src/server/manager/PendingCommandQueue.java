package server.manager;

import shared.dto.CommandRequest;
import shared.dto.ForwardCommandObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PendingCommandQueue {
    private Map<String, ConcurrentLinkedQueue<CommandRequest>> pendingMap = new ConcurrentHashMap<>();

    public boolean addPendingCommand(String clientId, CommandRequest commandRequest){
        return pendingMap.computeIfAbsent(clientId, k -> new ConcurrentLinkedQueue<CommandRequest>()).add(commandRequest);
    }
    public CommandRequest poll(String clientId){
        if (pendingMap.get(clientId) == null){
            return null;
        }
        return pendingMap.get(clientId).poll();
    }

}
