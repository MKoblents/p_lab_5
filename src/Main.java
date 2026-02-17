import Commands.Command;
import Commands.HelpCommand;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    private Map hystory = new HashMap<>();
    public static void main(String[] args) {


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