package server.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.client.ConnectedClient;
import server.commands.*;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;

import java.util.HashMap;
import java.util.Map;
/**
 * Command pattern invoker: stores and executes registered commands.
 * Maps command names to Command implementations.
 */
public class Invoker {
    private static final Logger logger = LoggerFactory.getLogger(Invoker.class);
    /** Registry of available commands by name. */
    private Map<String, Command> commandMap = new HashMap<>();
    private ClientRegistry clientRegistry;
    /**
     * Registers a command under the specified name.
     * @param name the command name/key
     * @param command the Command implementation to execute
     */
    public void registerCommand(String name, Command command){
        commandMap.put(name, command);
    }
    /**
     * Executes the command registered under the given key.
     */
    public CommandResponse runCommand(CommandRequest request) {
        String commandType = request.commandType();
        logger.debug("Executing command: {}", commandType);
        Command command = commandMap.get(commandType);
        if (command == null) {
            logger.warn("Unknown command requested: {}", commandType);
            return new CommandResponse(false, null, "Unknown command: " + commandType, request.requestId(), request.clientId());
        }
        try {
            CommandResponse response = command.execute(request);
            // After command.execute(request) succeeds:
            clientRegistry.getClient(request.clientId()).ifPresent(ConnectedClient::markOnline);
            logger.debug("Command {} completed: success={}", commandType, response.success());
            return response;
        } catch (Exception e) {
            logger.error("Error executing command {}: {}", commandType, e.getMessage(), e);
            return new CommandResponse(false, null, "Internal error", request.requestId(), request.clientId());
        }
    }
    public Invoker(CollectionManager collectionManager, ClientRegistry clientRegistry){
        this.clientRegistry = clientRegistry;
        registerCommand("add", new AddCommand(collectionManager));
        registerCommand("clear", new ClearCommand(collectionManager));
        registerCommand("filter_less_than_melee_weapon", new FilterLessThanMeleeWeaponCommand(collectionManager));
        registerCommand("info", new InfoCommand(collectionManager));
        registerCommand("insert_at", new InsertAtCommand(collectionManager));
        registerCommand("min_by_melee_weapon", new MinByMeleeWeaponCommand(collectionManager));
        registerCommand("remove_by_id", new RemoveByIdCommand(collectionManager));
        registerCommand("remove_greater", new RemoveGreaterCommand(collectionManager));
        registerCommand("show", new ShowCommand(collectionManager));
        registerCommand("shuffle", new ShuffleCommand(collectionManager));
        registerCommand("sum_of_health", new SumOfHealthCommand(collectionManager));
        registerCommand("update", new UpdateCommand(collectionManager));
        registerCommand("help", new HelpCommand(this));
        registerCommand("spawn_client", new SpawnClientCommand());
        registerCommand("forward_command", new ForwardCommand(clientRegistry));
        registerCommand(CommandRequest.CMD_HEARTBEAT, new HeartbeatCommand(clientRegistry));
        registerCommand("could_be_updated", new CouldBeUpdatedCommand(collectionManager));
        logger.debug("Registering commands with Invoker");
    }
    /**
     * Returns the command registry.
     * @return mutable map of registered commands (use with caution)
     */
    public Map<String, Command> getCommandMap() {
        return commandMap;
    }
}
