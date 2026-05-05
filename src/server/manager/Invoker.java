package server.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.client.ConnectedClient;
import server.commands.*;
import server.service.AuthService;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.dto.UserInfo;

import java.util.HashMap;
import java.util.Map;
/**
 * Command pattern invoker: stores and executes registered commands.
 * Maps command names to Command implementations.
 */
public class Invoker {
    private static final Logger logger = LoggerFactory.getLogger(Invoker.class);
    private final AuthService authService;
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
        if (commandType.equals("log_in")|| commandType.equals("sign_in")){
            return commandMap.get(commandType).execute(request);
        }
        UserInfo userInfo = request.userInfo();
        try {
            var validatedUser = authService.validate(userInfo);
            if (validatedUser.isEmpty()) {
                logger.warn("Auth failed for user: {}", userInfo.name());
                return new CommandResponse(false, null, "Authentication failed: invalid credentials",
                        request.requestId(), request.clientId());
            }
        } catch (Exception e) {
            logger.error("Auth error for user : {}", e.getMessage(), e);
            return new CommandResponse(false, null, "Authentication error",
                    request.requestId(), request.clientId());
        }

        logger.debug("Executing command: {}", commandType);
        Command command = commandMap.get(commandType);
        if (command == null) {
            logger.warn("Unknown command requested: {}", commandType);
            return new CommandResponse(false, null, "Unknown command: " + commandType, request.requestId(), request.clientId());
        }
        try {
            CommandResponse response = command.execute(request);
            clientRegistry.getClient(request.clientId()).ifPresent(ConnectedClient::markOnline);
            logger.debug("Command {} completed: success={}", commandType, response.success());
            return response;
        } catch (Exception e) {
            logger.error("Error executing command {}: {}", commandType, e.getMessage(), e);
            return new CommandResponse(false, null, "Internal error", request.requestId(), request.clientId());
        }
    }
    public Invoker(CollectionService collectionService, ClientRegistry clientRegistry, AuthService authService){
        this.authService = authService;
        this.clientRegistry = clientRegistry;
        registerCommand("add", new AddCommand(collectionService));
        registerCommand("clear", new ClearCommand(collectionService));
        registerCommand("filter_less_than_melee_weapon", new FilterLessThanMeleeWeaponCommand(collectionService));
        registerCommand("info", new InfoCommand(collectionService));
        registerCommand("insert_at", new InsertAtCommand(collectionService));
        registerCommand("min_by_melee_weapon", new MinByMeleeWeaponCommand(collectionService));
        registerCommand("remove_by_id", new RemoveByIdCommand(collectionService));
        registerCommand("remove_greater", new RemoveGreaterCommand(collectionService));
        registerCommand("show", new ShowCommand(collectionService));
        registerCommand("shuffle", new ShuffleCommand(collectionService));
        registerCommand("sum_of_health", new SumOfHealthCommand(collectionService));
        registerCommand("update", new UpdateCommand(collectionService));
        registerCommand("help", new HelpCommand(this));
        registerCommand("spawn_client", new SpawnClientCommand());
        registerCommand("forward_command", new ForwardCommand(clientRegistry));
        registerCommand(CommandRequest.CMD_HEARTBEAT, new HeartbeatCommand(clientRegistry));
        registerCommand("could_be_updated", new CouldBeUpdatedCommand(collectionService));
        registerCommand("kill_client", new KillClientCommand(clientRegistry));
        registerCommand("log_in", new LogInCommand(authService, clientRegistry));
        registerCommand("sign_in", new SignInCommand(authService,clientRegistry));
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
