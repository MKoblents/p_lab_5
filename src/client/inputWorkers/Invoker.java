package client.inputWorkers;

import client.command.Add;
import client.command.ClientCommand;
import shared.dto.CommandRequest;

import java.util.HashMap;
import java.util.Map;

public class Invoker {
    private Map<String, ClientCommand> commandMap = new HashMap<>();
    public void registerCommand(String name, ClientCommand command){
        commandMap.put(name, command);
    }
    public CommandRequest runCommand(String commandName) {
        ClientCommand command = commandMap.get(commandName);
        if (command == null) {
            return new CommandRequest(null, null, "Unknown command: " + commandName);
        }
        try {
            CommandRequest request = command.execute();
           return request;
        } catch (Exception e) {
            return new CommandRequest(null, null,null);
        }
        }
        public Invoker(InputManager inputManager){
        registerCommand("add", new Add(inputManager));
    }
}


