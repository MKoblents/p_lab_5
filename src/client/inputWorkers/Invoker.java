package client.inputWorkers;

import client.command.*;
import shared.dto.CommandRequest;

import java.util.HashMap;
import java.util.Map;

public class Invoker {
//TODO
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
        registerCommand("clear", new Clear());
        registerCommand("filter_less_than_melee_weapon", new FilterLessThanMeleeWeapon(inputManager));
        registerCommand("help", new Help());
        registerCommand("info", new Info());
        registerCommand("insert_at", new InsertAt(inputManager));
        registerCommand("min_by_melee_weapon", new MinByMeleeWeapon());
        registerCommand("exit", new Exit());
        registerCommand("remove_by_id", new RemoveById(inputManager));
        registerCommand("remove_greater", new RemoveGreater(inputManager));
        registerCommand("shuffle", new Shuffle());
        registerCommand("sum_of_health", new SumOfHealth());
        registerCommand("show", new Show());
        registerCommand("update", new Update(inputManager));
    }
}


