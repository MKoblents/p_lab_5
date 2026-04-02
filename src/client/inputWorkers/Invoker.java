package client.inputWorkers;

import client.command.*;
import client.context.ClientContext;
import client.hierarchy.PeerConnection;
import client.network.ConnectionManager;
import client.process.ClientProcessManager;
import shared.dto.CommandRequest;

import java.util.HashMap;
import java.util.Map;

public class Invoker {
    private InputManager inputManager;
    private ClientContext context;
    private PeerConnection peerConnection;
    private ConnectionManager connection;
    private ClientProcessManager processManager;
//TODO
    private Map<String, ClientCommand> commandMap = new HashMap<>();
    public void registerCommand(String name, ClientCommand command){
        commandMap.put(name, command);
    }
    public CommandRequest runCommand(String commandName) {
        String targetClientId = inputManager.getTargetClientId();
        if (targetClientId == null && targetClientId.isEmpty() && !targetClientId.equals(context.getClientId())){
                return runServerCommand(commandName);
        }
        else{
            ForwardCommand fc = new ForwardCommand(
                    targetClientId,
                    commandName,
                    context,
                    peerConnection
            );
            fc.execute();
            return null;
        }
    }
    public Invoker(InputManager inputManager,
                   ClientContext context,
                   ConnectionManager connection,
                   ClientProcessManager processManager,
                   PeerConnection peerConnection){
        this.peerConnection = peerConnection;
        this.inputManager = inputManager;
        this.context = context;
        this.connection = connection;
        this.processManager = processManager;
        registerCommands();
    }
    private void registerCommands(){
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
        registerCommand("spawn_client", new SpawnClient(context,connection,processManager));
    }
    private CommandRequest runServerCommand(String commandName){
        ClientCommand command = commandMap.get(commandName);
        if (command == null) {
            return null;
        }
        try {
            CommandRequest request = command.execute();
            return request;
        } catch (Exception e) {
            return null;
        }
    }
}


