package client.scripts;

public interface ScriptExecutionListener {
    void onCommandExecuted(String commandName, boolean success, String message);
    void onScriptFinished(int totalCommands, int successCount, int errorCount);
}