package server;

import client.inputWorkers.InputManager;
import server.commands.*;
import server.manager.CollectionManager;
import server.manager.FileManager;
import server.manager.Invoker;

import java.io.IOException;

public class ServerApp {

    private static final int PORT = 12345;

    public static void main(String[] args) {
        System.out.println("=== Server Starting ===");

        try {
            CollectionManager collectionManager = new CollectionManager();
            collectionManager.loadFromFile(System.getenv("PLAB5"));
            System.out.println("Collection loaded. Size: " + collectionManager.size());
            InputManager inputManager = new InputManager()
            Invoker invoker = new Invoker();
            invoker.registerCommand("help", new HelpCommand(invoker));
            invoker.registerCommand("info", new InfoCommand(collectionManager));
            invoker.registerCommand("show", new ShowCommand(collectionManager));
            invoker.registerCommand("clear", new ClearCommand(collectionManager));
            invoker.registerCommand("exit", new ExitCommand());
            invoker.registerCommand("shuffle", new ShuffleCommand(collectionManager));
            invoker.registerCommand("sum_of_health", new SumOfHealthCommand(collectionManager));
            invoker.registerCommand("min_by_melee_weapon", new MinByMeleeWeaponCommand(collectionManager));
            invoker.registerCommand("remove_by_id", new RemoveByIdCommand(collectionManager,inputManager));
            invoker.registerCommand("add", new AddCommand(collectionManager,inputManager));
            invoker.registerCommand("insert_at", new InsertAtCommand(collectionManager, inputManager));
            invoker.registerCommand("filter_less_than_melee_weapon", new FilterLessThanMeleeWeaponCommand(collectionManager, inputManager));
            invoker.registerCommand("update", new UpdateCommand(collectionManager, inputManager));
            invoker.registerCommand("save", new SaveCommand(collectionManager, collectionSaver));
            invoker.registerCommand("execute_script", new ExecuteScriptCommand(collectionManager, inputManager, new FileManager(),invoker));
            invoker.registerCommand("remove_greater", new RemoveGreaterCommand(collectionManager, inputManager));
            Server server = new Server(PORT, collectionManager, System.getenv("PLAB5"));
            System.out.println("Server listening on port " + PORT);

            server.start();

        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}