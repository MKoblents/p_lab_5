import commands.*;
import inputWorkers.*;
import io.ConsoleScanner;
import manager.*;
import outputWorkers.CollectionSaver;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        CollectionManager collectionManager = new CollectionManager();
        ProgramManager programManager = new ProgramManager();
        String filePath = System.getenv("PLAB5");
        ConsoleScanner consoleScanner = new ConsoleScanner();
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
        invoker.registerCommand("sumofhealth", new SumOfHealthCommand(collectionManager));
        invoker.registerCommand("minbymeleeweaponvalue", new MinByMeleeWeaponCommand(collectionManager));
        invoker.registerCommand("removebyid", new removeByIdCommand(collectionManager,inputManager));
        invoker.registerCommand("add", new AddCommand(collectionManager,inputManager));
        invoker.registerCommand("insertat", new InsertAtCommand(collectionManager, inputManager));
        invoker.registerCommand("filterlessthanmeleeweapon", new FilterLessThanMeleeWeaponCommand(collectionManager, inputManager));
        invoker.registerCommand("update", new UpdateCommand(collectionManager, inputManager));
        invoker.registerCommand("save", new SaveCommand(collectionManager, collectionSaver));
        while (true){
        try {

            String commandLine = inputManager.parseCommand();
            System.out.println(commandLine);
            System.out.println(inputManager.getLastLong());
            invoker.runCommand(commandLine);
//            invoker.runCommand("clear");
//            invoker.runCommand("help");
//            invoker.runCommand("info");
//            invoker.runCommand("show");
//            System.out.println("-------------------------------------------------------------------------------------");
//            invoker.runCommand("shuffle");
//            invoker.runCommand("show");
//            invoker.runCommand("sum of health");
//            invoker.runCommand("min by meleeWeapon value");
//
//            invoker.runCommand("exit");


        } catch (Exception e) {
            System.err.println("Error during parsing:");
            e.printStackTrace();
        }
    }
}}


// main
// class Invoker (call commands)
//  Command (Add, Hel[, ...) interface or abstract class
// ?? remember something
// FileProducer, FileValidator (parser) (we can use libs to read from file but not for valid data), FileUploader
// class Validation
// Models (Coordinates, StudiGroup, .. )
// CollectionManager

/// /////////// HELP
// 1 - output menu of commands
// 2 -  input
// 3 - data validation
// -- create object of command (?)
// -- add to the list of commands (for history), maybe to file
// 4 - command is correct --> do the command


// переполнение тоже ошибка

// ///////////// ADD
// 1 - output menu of commands
// 2 -  input
// 3 - data validation
// -- create object of command (?)
// -- add to the list of commands (for history)
// 4 - command is correct -->
//          -- input name
//                           --> correct
//           -- do the command
//                           --> incorrect
//           -- output exception (safe exception in file)

// инвокер -- регистр команд, мы туда просто наши условные 10 команд кидаем и вызываем их по ключам. То есть запускаем команды через инвокер.
// еще у нас есть инпут менеджер для закидывания в инвокер команд, у которых есть инпут
// в инпут манагере мы можем прописать парсер, в нем мы задаем общий порядок считывания данных