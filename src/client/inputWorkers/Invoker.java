package client.inputWorkers;

import client.command.*;
import client.context.ClientContext;
import client.network.ConnectionManager;
import client.process.ClientProcessManager;
import client.scripts.ScriptRunner;
import client.utils.SideFlag;
import shared.dto.CommandRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shared.dto.ForwardCommandObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Invoker {
    private static final Logger logger = LoggerFactory.getLogger(Invoker.class);

    private final InputManager inputManager;
    private final ClientContext context;
    private final ConnectionManager connection;
    private final ClientProcessManager processManager;
    private final ScriptRunner runner;

    private Map<String, ClientCommand> commandMap = new HashMap<>();

    public Invoker(InputManager inputManager,
                   ClientContext context,
                   ConnectionManager connection,
                   ClientProcessManager processManager,
                   ScriptRunner runner) {
        this.inputManager = inputManager;
        this.context = context;
        this.connection = connection;
        this.processManager = processManager;
        this.runner = runner;
        registerCommands();
    }

    public void registerCommand(String name, ClientCommand command) {
        commandMap.put(name, command);
    }

    private void registerCommands() {
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
        registerCommand("spawn_client", new SpawnClient(context, connection, processManager));
        registerCommand("execute_script", new ExecuteScript(inputManager, runner));
    }

    public CommandRequest runCommand(String commandName) {
        String targetClientId = inputManager.getTargetClientId();
        logger.debug("Executing command '{}' for client {}", commandName, context.getClientId());
        if (targetClientId != null && !targetClientId.isEmpty()) {
            if (targetClientId.equals(context.getClientId())) {
                System.out.println("Command targeted to self, executing locally");
                return runServerCommand(commandName, SideFlag.SELF);
            }
            System.out.println("FORWARDING command '" + commandName + "' to client: " + targetClientId);
            ForwardCommandObject fco = new ForwardCommandObject(context.getClientId(), targetClientId, commandName);
            CommandRequest cr = new CommandRequest("forward_command", fco, UUID.randomUUID().toString().substring(0, 8), context.getClientId());
            return cr;
        }
        System.out.println("No target, executing on server via current client");
        return runServerCommand(commandName, SideFlag.SELF);
    }

    public CommandRequest runServerCommand(String commandName, SideFlag flag) {
        ClientCommand command = commandMap.get(commandName);
        if (command == null) {
            System.err.println("Unknown command: " + commandName);
            return null;
        }
        try {
            CommandRequest request = command.execute(flag);
            if (request == null &&  !"execute_script".equals(commandName)) {
                System.err.println("Command returned null request: " + commandName);
            }
            return request;
        } catch (Exception e) {
            System.err.println("Error executing command: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}