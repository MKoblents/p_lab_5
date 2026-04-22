package server.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shared.dto.CommandRequest;
import shared.dto.ForwardCommandObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PendingCommandQueue {
    private static final Logger logger = LoggerFactory.getLogger(PendingCommandQueue.class);

    private Map<String, ConcurrentLinkedQueue<CommandRequest>> pendingMap = new ConcurrentHashMap<>();

    public boolean addPendingCommand(String clientId, CommandRequest commandRequest){
        logger.info(clientId + " added new comandRequest"+ commandRequest);
        return pendingMap.computeIfAbsent(clientId, k -> new ConcurrentLinkedQueue<CommandRequest>()).add(commandRequest);
    }
    public CommandRequest poll(String clientId){
        if (pendingMap.get(clientId) == null){
            return null;
        }
        return pendingMap.get(clientId).poll();
    }

}
