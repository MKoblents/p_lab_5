package manager;

import commands.Command;

import java.util.HashMap;
import java.util.Map;
/**
 * Command pattern invoker: stores and executes registered commands.
 * Maps command names to Command implementations.
 */
public class Invoker {
    /** Registry of available commands by name. */
    private Map<String, Command> commandMap = new HashMap<>();
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
     * @param key the command name to execute
     */
    public void runCommand(String key){
        if (key == null) {
            System.err.println("Unknown command: " + key);
            System.err.println("Available commands: " + commandMap.keySet());
            return;
        }
        commandMap.get(key).execute();
    }
    /**
     * Returns the command registry.
     * @return mutable map of registered commands (use with caution)
     */
    public Map<String, Command> getCommandMap() {
        return commandMap;
    }
}
