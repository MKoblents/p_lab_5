import commands.*;
import inputWorkers.*;
import io.ConsoleBufferedScanner;
import manager.*;
import outputWorkers.CollectionSaver;

public class Main {
    public static void main(String[] args) throws Exception {
        CollectionManager collectionManager = new CollectionManager();
        String filePath = System.getenv("PLAB5");
        ConsoleBufferedScanner consoleScanner = new ConsoleBufferedScanner();
        Validator validator = new Validator(collectionManager);
        CommandParser commandParser = new CommandParser();
        InputManager inputManager= new InputManager(consoleScanner, validator, collectionManager, commandParser);
        System.out.println(filePath);
        collectionManager.loadFromFile(filePath);
        CollectionSaver collectionSaver = new CollectionSaver();
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
        invoker.registerCommand("filter_less_than_meleeweapon", new FilterLessThanMeleeWeaponCommand(collectionManager, inputManager));
        invoker.registerCommand("update", new UpdateCommand(collectionManager, inputManager));
        invoker.registerCommand("save", new SaveCommand(collectionManager, collectionSaver));
        invoker.registerCommand("execute_script", new ExecuteScriptCommand(collectionManager, inputManager, new FileManager(),invoker));
        invoker.registerCommand("remove_greater", new RemoveGreaterCommand(collectionManager, inputManager));
        while (true){
        try {
            String commandLine = inputManager.parseCommand();
            System.out.println(commandLine);
            invoker.runCommand(commandLine);


        } catch (Exception e) {
            System.err.println("Error during parsing:");
            e.printStackTrace();
        }
    }
}}
