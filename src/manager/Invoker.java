package manager;

import Commands.Command;

import java.util.HashMap;
import java.util.Map;

public class Invoker {
    private Map<String, Command> commandMap = new HashMap<>();

    public void registerCommand(String name, Command command){
        commandMap.put(name, command);
    }
    public boolean runCommand(String key){
        commandMap.get(key).execute();
    }

    public Map<String, Command> getCommandMap() {
        return commandMap;
    }
}
