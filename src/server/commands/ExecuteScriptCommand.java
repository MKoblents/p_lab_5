//package server.commands;
//
//import client.inputWorkers.InputManager;
//import client.inputWorkers.XMLParser;
//import client.io.ConsoleBufferedScanner;
//import client.io.FileBufferedReader;
//import client.io.Reader;
//import server.manager.CollectionManager;
//import server.manager.FileManager;
//import server.manager.Invoker;
//import shared.dto.CommandRequest;
//import shared.dto.CommandResponse;
//
//import java.io.IOException;
//
//public class ExecuteScriptCommand implements Command{
//    private String helpInformation = "execute_script file_name : считать и исполнить скрипт из указанного файла. В скрипте содержатся команды в таком же виде, в котором их вводит пользователь в интерактивном режиме.";
//    private CollectionManager collectionManager;
//    private InputManager inputManager;
//    public FileManager fileManager;
//    public XMLParser xmlParser;
//    public Invoker invoker;
//    public ExecuteScriptCommand(CollectionManager collectionManager,
//                                InputManager inputManager,
//                                FileManager fileManager,
//                               Invoker invoker) {
//        this.collectionManager = collectionManager;
//        this.inputManager = inputManager;
//        this.fileManager = fileManager;
//        this.invoker = invoker;
//    }
//
//    @Override
//    public String getHelpInformation() {
//        return helpInformation;
//    }
//
//    @Override
//    public CommandResponse execute(CommandRequest commandRequest) {
//        String scriptPath = inputManager.getLastPath();
//        if (scriptPath == null || scriptPath.trim().isEmpty() || !fileManager.validate(scriptPath, FileManager.Operation.READ)) {
//            System.err.println("Error: Cannot read script file: " + scriptPath);
//            ConsoleBufferedScanner consoleBufferedScanner = new ConsoleBufferedScanner();
//            try {
//                scriptPath = consoleBufferedScanner.getInputString();
//                inputManager.setLastPath(scriptPath);
//                executeScript(scriptPath);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//            return  new CommandResponse(true, "Executed successfully", null);
//        }
//        executeScript(scriptPath);
//        return  new CommandResponse(true, "Executed successfully", null);
//
//    }
//    private void executeScript(String scriptPath){
//        try {
//            Reader reader = inputManager.getReader();
//            try {
//                FileBufferedReader scriptReader = new FileBufferedReader(scriptPath, new XMLParser(scriptPath, collectionManager));
//                inputManager.setReader(scriptReader);
//                while (scriptReader.hasNextLine()) {
//                    try {
//                        String commandName = inputManager.parseCommand();
//                        if (commandName == null || commandName.isEmpty()) continue;
//                        invoker.runCommand(commandName);
//                    } catch (Exception e) {
//                        System.err.println("  " + e.getMessage());
//                    }
//                }
//                scriptReader.close();
//            }
//            finally{
//                if (reader instanceof ConsoleBufferedScanner){
//                    reader.clearBuffer();
//                }
//                inputManager.setReader(reader);
//
//            }
//        }catch (IOException e) {
//            System.err.println("Error reading script: " + e.getMessage());
//        } catch (Exception e) {
//            System.err.println("Error executing script: " + e.getMessage());
//            e.printStackTrace();
//        }
//        }
//
//
//}
