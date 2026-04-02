package client.inputWorkers;

import client.command.*;
import client.context.ClientContext;
import client.hierarchy.PeerConnection;
import client.network.ConnectionManager;
import client.process.ClientProcessManager;
import shared.dto.CommandRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Invoker {
    private static final Logger logger = LoggerFactory.getLogger(Invoker.class);

    private final InputManager inputManager;
    private final ClientContext context;
    private final PeerConnection peerConnection;
    private final ConnectionManager connection;
    private final ClientProcessManager processManager;

    private Map<String, ClientCommand> commandMap = new HashMap<>();

    // Команды, которые всегда выполняются локально
    private static final Set<String> LOCAL_COMMANDS = Set.of(
            "spawn_client", "exit", "help", "whoami"
    );

    public Invoker(InputManager inputManager,
                   ClientContext context,
                   ConnectionManager connection,
                   ClientProcessManager processManager,
                   PeerConnection peerConnection) {
        this.peerConnection = peerConnection;
        this.inputManager = inputManager;
        this.context = context;
        this.connection = connection;
        this.processManager = processManager;
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
//        registerCommand("whoami", new WhoAmI(context));
    }

    public CommandRequest runCommand(String commandName) {
        String targetClientId = inputManager.getTargetClientId();

        // ЛОГИРУЕМ ВСЕ ПАРАМЕТРЫ
        System.out.println("========================================");
        System.out.println("🔍 runCommand called:");
        System.out.println("   commandName: '" + commandName + "'");
        System.out.println("   targetClientId: '" + targetClientId + "'");
        System.out.println("   myClientId: '" + context.getClientId() + "'");
        System.out.println("   isLocalCommand: " + LOCAL_COMMANDS.contains(commandName));
        System.out.println("========================================");

        // Локальные команды всегда выполняем сами
        if (LOCAL_COMMANDS.contains(commandName)) {
            System.out.println("📌 Local command, executing directly");
            return runServerCommand(commandName);
        }

        // Если есть targetClientId
        if (targetClientId != null && !targetClientId.isEmpty()) {
            // Если команда адресована нам самим
            if (targetClientId.equals(context.getClientId())) {
                System.out.println("📌 Command targeted to self, executing locally");
                return runServerCommand(commandName);
            }

            // Иначе - отправляем другому клиенту
            System.out.println("📤 FORWARDING command '" + commandName + "' to client: " + targetClientId);
            ForwardCommand fc = new ForwardCommand(targetClientId, commandName, context, peerConnection);
            fc.execute();
            return null;
        }

        // Нет targetClientId - выполняем на сервере через текущий клиент
        System.out.println("📌 No target, executing on server via current client");
        return runServerCommand(commandName);
    }

    public CommandRequest runServerCommand(String commandName) {
        ClientCommand command = commandMap.get(commandName);
        if (command == null) {
            System.err.println("Unknown command: " + commandName);
            return null;
        }
        try {
            CommandRequest request = command.execute();
            if (request == null) {
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