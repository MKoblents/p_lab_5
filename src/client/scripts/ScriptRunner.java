package client.scripts;

import client.handlers.ResponseHandler;
import client.inputWorkers.InputManager;
import client.inputWorkers.Invoker;
import client.io.ConsoleBufferedScanner;
import client.io.FileBufferedReader;
import client.io.Reader;
import client.network.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.utils.XMLParser;

import java.io.IOException;
import java.util.*;

public class ScriptRunner {
    private static final Logger logger = LoggerFactory.getLogger(ScriptRunner.class);
    private final InputManager inputManager;
    private final ConnectionManager connectionManager;
    private final ResponseHandler responseHandler;
    private Invoker invoker;
    private final FileManager fileManager;
    private static final ThreadLocal<Deque<String>> executingScripts =
            ThreadLocal.withInitial(ArrayDeque::new);
    private static final int MAX_SCRIPT_DEPTH = 5;

    public ScriptRunner(InputManager inputManager,
                        ConnectionManager connectionManager,
                        ResponseHandler responseHandler, Invoker invoker,
                        FileManager fileManager) {
        this.inputManager = inputManager;
        this.connectionManager = connectionManager;
        this.responseHandler = responseHandler;
        this.invoker = invoker;
        this.fileManager = fileManager;
    }

    /**
     * Executes script file with recursion support.
     * @param scriptPath path to local script file
     * @return true if script completed without errors
     */
    public boolean executeScript(String scriptPath) {
        String normalizedPath = normalizePath(scriptPath);
        if (!fileManager.validate(normalizedPath, FileManager.Operation.READ)) {
            System.err.println("Error: Script file is not accessible. Please check the path and file permissions.");
            logger.warn("Script access denied: {}", normalizedPath);
            return false;
        }
        if (executingScripts.get().contains(normalizedPath)) {
            System.err.println("Error: Circular script inclusion detected: " + scriptPath);
            logger.error("Circular dependency in script execution: {}", normalizedPath);
            return false;
        }
        if (executingScripts.get().size() >= MAX_SCRIPT_DEPTH) {
            System.err.println("Error: Maximum script nesting depth exceeded (" + MAX_SCRIPT_DEPTH + ").");
            logger.error("Script nesting limit reached for: {}", scriptPath);
            return false;
        }
        executingScripts.get().push(normalizedPath);
        logger.info("Executing script: {} (depth: {})", scriptPath, executingScripts.get().size());
        System.out.println("Executing script: " + scriptPath + " (depth: " + executingScripts.get().size() + ")");
        Reader originalReader = inputManager.getReader();
        try {
            FileBufferedReader scriptReader = new FileBufferedReader(scriptPath, new XMLParser(scriptPath));
            inputManager.setReader(scriptReader);
            logger.debug("Reader switched to file: {}", scriptPath);
            ExecutionResult result = executeScriptInternal(scriptPath);
            printExecutionSummary(result);
            return result.success();
        } catch (IOException e) {
            logger.error("IO error while reading script {}: {}", scriptPath, e.getMessage());
            System.err.println("Error: Could not read script file. Details: " + e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error while executing script {}: {}", scriptPath, e.getMessage(), e);
            System.err.println("Error: Script execution failed. Details: " + e.getMessage());
            return false;
        } finally {
            try {
                if (originalReader instanceof ConsoleBufferedScanner) {
                    originalReader.clearBuffer();
                }
                inputManager.setReader(originalReader);
                logger.debug("Reader restored to original source");
            } catch (IOException e) {
                logger.warn("Failed to restore original reader: {}", e.getMessage());
                System.err.println("Warning: Could not restore input reader: " + e.getMessage());
            }
            executingScripts.get().pop();
            logger.info("Finished executing script: {}", scriptPath);
        }
    }

    /**
     * Prints execution summary to console.
     */
    private void printExecutionSummary(ExecutionResult result) {
        System.out.println("Script completed: " +
                result.successCount() + " succeeded, " +
                result.errorCount() + " failed");
        if (!result.details().isEmpty() && result.details().size() <= 10) {
            System.out.println("Details:");
            for (String detail : result.details()) {
                System.out.println("  " + detail);
            }
        } else if (!result.details().isEmpty()) {
            System.out.println("(details omitted - " + result.details().size() + " lines)");
        }
        logger.debug("Script execution summary: {} success, {} errors", result.successCount(), result.errorCount());
    }
    /**
     * Internal execution logic (after Reader is switched).
     * @return ExecutionResult with summary
     */
    private ExecutionResult executeScriptInternal(String scriptPath) throws IOException {
        List<String> details = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;
        while (inputManager.getReader().hasNextLine()) {
            try {
                String commandName = inputManager.parseCommand();
                if (commandName == null || commandName.isEmpty()) {
                    continue;
                }
                System.out.println("  " + commandName);
                logger.debug("Processing script command: {}", commandName);
                if (commandName.equals("execute_script")) {
                    String nestedPath = inputManager.getLastPath();
                    if (nestedPath != null && !nestedPath.isEmpty()) {
                        if (executeScript(nestedPath)) {
                            successCount++;
                            details.add("execute_script " + nestedPath + " done");
                        } else {
                            errorCount++;
                            details.add("execute_script " + nestedPath + " failed");
                        }
                    } else {
                        errorCount++;
                        String msg = "Script path required for execute_script command";
                        details.add(msg);
                        System.err.println("Error: " + msg);
                        logger.debug("Script command failed: {}", msg);
                    }
                    continue;
                }
                CommandRequest request = invoker.runCommand(commandName);
                if (request == null) {
                    errorCount++;
                    String msg = "Failed to build request for command: " + commandName;
                    details.add(msg);
                    System.err.println("Error: " + msg);
                    logger.debug("Request build failed: {}", commandName);
                    continue;
                }
                connectionManager.sendRequest(request);
                CommandResponse response = connectionManager.readResponse();
                if (response != null && response.success()) {
                    if ("spawn_client".equals(response.requestId()) ||
                            (response.message() != null && response.message().contains("Child client created"))) {
                        invoker.getCommand("spawn_client").handleResponse(response, invoker.getContext());
                    }
                    successCount++;
                    String responseMsg = response.message();
                    if (responseMsg == null || responseMsg.trim().isEmpty()) {
                        responseMsg = "Operation completed successfully";
                    }
                    String detail = commandName + " - " + responseMsg;
                    if (response.result() != null) {
                        detail += " | Result: " + safeToString(response.result());
                    }
                    details.add(detail);
                } else {
                    errorCount++;
                    String msg = response != null ? response.message() : "No response received";
                    if (msg == null || msg.trim().isEmpty()) {
                        msg = response != null && !response.success()
                                ? "Operation failed with no details"
                                : "Server returned no response";
                    }
                    details.add(commandName + " - " + msg);
                    System.err.println("Error: " + msg);
                    logger.debug("Command failed in script: {} - {}", commandName, msg);
                }
            } catch (Exception e) {
                errorCount++;
                String msg = "Exception during command execution: " + e.getMessage();
                details.add(msg);
                System.err.println("Error: " + e.getMessage());
                logger.warn("Exception in script execution", e);
            }
        }
        return ExecutionResult.of(
                errorCount == 0,
                successCount,
                errorCount,
                details
        );
    }

    /**
     * Safely converts an object to string, handling nulls and avoiding exceptions.
     */
    private String safeToString(Object obj) {
        if (obj == null) return "null";
        try {
            return obj.toString();
        } catch (Exception e) {
            logger.debug("Failed to convert result to string: {}", e.getMessage());
            return "[unable to display result]";
        }
    }

    /**
     * Normalizes path for consistent comparison.
     */
    private String normalizePath(String scriptPath) {
        try {
            return java.nio.file.Paths.get(scriptPath).toAbsolutePath().normalize().toString();
        } catch (Exception e) {
            logger.debug("Path normalization failed for '{}', using original: {}", scriptPath, e.getMessage());
            return scriptPath;
        }
    }

    /**
     * Record for script execution summary.
     * Immutable, serializable, with auto-generated getters.
     */
    public record ExecutionResult(
            boolean success,
            int successCount,
            int errorCount,
            List<String> details
    ) implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        public static ExecutionResult of(boolean success, int successCount, int errorCount, List<String> details) {
            return new ExecutionResult(success, successCount, errorCount, details);
        }
    }

    public void setInvoker(Invoker invoker) {
        this.invoker = invoker;
    }
}