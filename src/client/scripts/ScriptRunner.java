package client.scripts;

import client.handlers.RequestBuilder;
import client.handlers.ResponseHandler;
import client.inputWorkers.InputManager;
import client.io.FileBufferedReader;
import client.io.Reader;
import shared.utils.XMLParser;
import client.network.ConnectionManager;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;

import java.io.IOException;
import java.util.*;

public class ScriptRunner {
    private final InputManager inputManager;
    private final RequestBuilder requestBuilder;
    private final ConnectionManager connectionManager;
    private final ResponseHandler responseHandler;
//    private final XMLParser xmlParser;
    private static final ThreadLocal<Deque<String>> executingScripts =
            ThreadLocal.withInitial(ArrayDeque::new);
    private static final int MAX_SCRIPT_DEPTH = 5;

    public ScriptRunner(InputManager inputManager,
                        RequestBuilder requestBuilder,
                        ConnectionManager connectionManager,
                        ResponseHandler responseHandler) {
        this.inputManager = inputManager;
        this.requestBuilder = requestBuilder;
        this.connectionManager = connectionManager;
        this.responseHandler = responseHandler;
    }

    /**
     * Executes script file with recursion support.
     * @param scriptPath path to local script file
     * @return true if script completed without errors
     */
    public boolean executeScript(String scriptPath) {
        String normalizedPath = normalizePath(scriptPath);
        if (executingScripts.get().contains(normalizedPath)) {
            System.err.println("Circular script inclusion detected: " + scriptPath);
            return false;
        }
        if (executingScripts.get().size() >= MAX_SCRIPT_DEPTH) {
            System.err.println("Maximum script nesting depth exceeded (" + MAX_SCRIPT_DEPTH + ")");
            return false;
        }
        executingScripts.get().push(normalizedPath);
        System.out.println("📜 Entering script: " + scriptPath + " (depth: " + executingScripts.get().size() + ")");
        Reader originalReader = inputManager.getReader();
        try {
            FileBufferedReader scriptReader = new FileBufferedReader(scriptPath, new XMLParser(scriptPath));
            inputManager.setReader(scriptReader);
            ExecutionResult result = executeScriptInternal(scriptPath);
            printExecutionSummary(result);
            return result.success();
        } catch (IOException e) {
            System.err.println("Error reading script: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Error executing script: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (originalReader instanceof client.io.ConsoleBufferedScanner) {
                    originalReader.clearBuffer();
                }
                inputManager.setReader(originalReader);
            } catch (IOException e) {
                System.err.println("Error restoring reader: " + e.getMessage());
            }
            executingScripts.get().pop();
            System.out.println("📜 Exiting script: " + scriptPath);
        }
    }

    /**
     * Prints execution summary to console.
     */
    private void printExecutionSummary(ExecutionResult result) {
        System.out.println("\n📊 Script completed: " +
                result.successCount() + " succeeded, " +
                result.errorCount() + " failed");
        if (!result.details().isEmpty() && result.details().size() <= 10) {
            System.out.println("  Details:");
            for (String detail : result.details()) {
                System.out.println("    " + detail);
            }
        } else if (!result.details().isEmpty()) {
            System.out.println("  (details omitted - " + result.details().size() + " lines)");
        }
    }
    /**
     * Internal execution logic (after Reader is switched).
     * @return ExecutionResult with summary
     */
    private ExecutionResult executeScriptInternal(String scriptPath) throws IOException {
        List<String> details = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;
        Reader reader = inputManager.getReader();
        while (reader.hasNextLine()) {
            try {
                String commandName = inputManager.parseCommand();
                if (commandName == null || commandName.isEmpty()) {
                    continue;
                }
                System.out.println("  → " + commandName);
                if (commandName.equals("execute_script")) {
                    String nestedPath = inputManager.getLastPath();
                    if (nestedPath != null && !nestedPath.isEmpty()) {
                        ExecutionResult nestedResult = executeScriptInternal(nestedPath);
                        if (nestedResult.success()) {
                            successCount += nestedResult.successCount();
                            details.addAll(nestedResult.details());
                            System.out.println("     Nested script executed");
                        } else {
                            errorCount += nestedResult.errorCount();
                            details.addAll(nestedResult.details());
                            System.err.println("     Nested script failed");
                        }
                    } else {
                        errorCount++;
                        details.add("Script path required");
                        System.err.println("     Script path required");
                    }
                    continue;
                }
                CommandRequest request = requestBuilder.buildRequest(commandName);
                if (request == null) {
                    errorCount++;
                    details.add("Line: Failed to build request for " + commandName);
                    System.err.println("     Failed to build request");
                    continue;
                }
                connectionManager.sendRequest(request);
                CommandResponse response = connectionManager.readResponse();
                if (response != null && response.success()) {
                    successCount++;
                    details.add("✓ " + commandName + " - " + response.message());
                    System.out.println("    ✓ " + response.message());
                } else {
                    errorCount++;
                    String msg = response != null ? response.message() : "No response";
                    details.add("✗ " + commandName + " - " + msg);
                    System.err.println("    ✗ Error: " + msg);
                }
            } catch (Exception e) {
                errorCount++;
                details.add("✗ Exception: " + e.getMessage());
                System.err.println("  " + e.getMessage());
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
     * Normalizes path for consistent comparison.
     */
    private String normalizePath(String scriptPath) {
        try {
            return java.nio.file.Paths.get(scriptPath).toAbsolutePath().normalize().toString();
        } catch (Exception e) {
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
}