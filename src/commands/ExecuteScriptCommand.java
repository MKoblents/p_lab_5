package commands;

import inputWorkers.InputManager;
import inputWorkers.XMLParser;
import io.FileBufferedReader;
import io.Reader;
import manager.CollectionManager;
import manager.FileManager;
import manager.Invoker;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

public class ExecuteScriptCommand implements Command{
    private String helpInformation = "execute_script file_name : считать и исполнить скрипт из указанного файла. В скрипте содержатся команды в таком же виде, в котором их вводит пользователь в интерактивном режиме.";
    private CollectionManager collectionManager;
    private InputManager inputManager;
    public FileManager fileManager;
    public XMLParser xmlParser;
    public Invoker invoker;
    public ExecuteScriptCommand(CollectionManager collectionManager,
                                InputManager inputManager,
                                FileManager fileManager,
                               Invoker invoker) {
        this.collectionManager = collectionManager;
        this.inputManager = inputManager;
        this.fileManager = fileManager;
        this.invoker = invoker;
    }

    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    @Override
    public void execute() {
        String scriptPath = inputManager.getLastPath();
        if (scriptPath == null || scriptPath.trim().isEmpty()) {
            System.err.println("ERROR: Script path is null or empty");
            return;
        }
        if (!fileManager.validate(scriptPath, FileManager.Operation.READ)) {
            System.err.println("Error: Cannot read script file: " + scriptPath);
            return;
        }
        try {
            executeScript(scriptPath);
            System.out.println("Script executed successfully: " + scriptPath);
        } catch (IOException e) {
            System.err.println("Error reading script: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error executing script: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void executeScript(String scriptPath) throws IOException, XMLStreamException {
        Reader reader = inputManager.getReader();
        int lineCount = 0;
        try {
            FileBufferedReader scriptReader = new FileBufferedReader(scriptPath, new XMLParser(scriptPath, collectionManager));
            inputManager.setReader(scriptReader);
            while (scriptReader.hasNextLine()) {
                try {
                    String commandName = inputManager.parseCommand();
                    if (commandName == null || commandName.isEmpty()) continue;
                    invoker.runCommand(commandName);

                } catch (Exception e) {

                    System.err.println("  " + e.getMessage());
                }
            }
            scriptReader.close();
        }
        finally{
            inputManager.setReader(reader);
                }
        }

}
