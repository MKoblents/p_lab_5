import commands.HelpCommand;
import commands.InfoCommand;
import inputWorkers.*;
import manager.*;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            CollectionManager collectionManager = new CollectionManager();
            ProgramManager programManager = new ProgramManager();
            String filePath = System.getenv("PLAB5");
            System.out.println(filePath);
            collectionManager.loadFromFile(filePath);
            Invoker invoker = new Invoker();
            invoker.registerCommand("help", new HelpCommand(programManager, invoker));
            invoker.registerCommand("info", new InfoCommand(programManager));
            invoker.runCommand("help");
//            String xmlFilePath = "/home/mkoblents/Yandex.Disk/maria/ITMO/progaaaaaaa/p_lab_5/src/sm.xml";
//
//            XMLParser parser = new XMLParser(xmlFilePath);
//
//            List<SpaceMarine> marines = parser.parseSpaceMarines(xmlFilePath);
//
//            System.out.println("=== Parsed Space Marines ===");
//            System.out.println("Total count: " + marines.size() + "\n");
            ArrayList<SpaceMarine> marines = collectionManager.getSpaceMarines();

            for (int i = 0; i < marines.size(); i++) {
                SpaceMarine marine = marines.get(i);
                System.out.println("Marine #" + (i + 1) + ":");
                System.out.println("  Name: " + marine.getName());
                System.out.println("  ID: " + marine.getId());
                System.out.println("  Health: " + marine.getHealth());
                System.out.println("  Category: " + marine.getCategory());
                System.out.println("  Weapon: " + marine.getWeaponType());
                System.out.println("  Melee Weapon: " + marine.getMeleeWeapon());

                if (marine.getCoordinates() != null) {
                    System.out.println("  Coordinates: (" +
                            marine.getCoordinates().getX() + ", " +
                            marine.getCoordinates().getY() + ")");
                }

                if (marine.getChapter() != null) {
                    System.out.println("  Chapter: " + marine.getChapter().getName());
                    System.out.println("  World: " + marine.getChapter().getWorld());
                }

                System.out.println();
            }

            System.out.println("=== Validation Tests ===");
            System.out.println("✓ Parsing completed successfully!");
            System.out.println("✓ Expected: 2 marines, Got: " + marines.size());

            if (marines.size() == 2) {
                System.out.println("✓ Test PASSED!");
            } else {
                System.out.println("✗ Test FAILED!");
            }

        } catch (Exception e) {
            System.err.println("Error during parsing:");
            e.printStackTrace();
        }
    }
}


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