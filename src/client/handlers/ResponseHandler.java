package client.handlers;

import client.scripts.ScriptRunner;
import shared.dto.CommandResponse;
import shared.models.SpaceMarine;

import java.util.List;

public class ResponseHandler {

    /**
     * Handles server response and displays to user.
     * @param response the CommandResponse from server
     */
    public void handle(CommandResponse response) {
        if (response == null) {
            System.err.println("Error: Null response from server");
            return;
        }
        if (response.isSuccess()) {
            System.out.println(response.getMessage());
        } else {
            System.err.println("Error: " + response.getMessage());
        }
        if (response.getResult() != null) {
            handleResult(response.getResult());
        }
    }

    /**
     * Handles different result types from server.
     * Extend this method when adding new commands.
     */
    private void handleResult(Object result) {
        if (result instanceof SpaceMarine marine) {
            System.out.println("  → " + marine);
        } else if (result instanceof List<?> list) {
            handleSpaceMarineList(list);
        } else if (result instanceof Double value) {
            System.out.println("  → Value: " + String.format("%.2f", value));
        } else if (result instanceof Long id) {
            System.out.println("  → ID: " + id);
        } else if (result instanceof Integer count) {
            System.out.println("  → Count: " + count);
        } else if (result instanceof ScriptRunner.ExecutionResult scriptResult) {
            handleScriptResult(scriptResult);

        } else if (result instanceof String text) {
            System.out.println("  → " + text);

        } else {
            System.out.println("  → Result: " + result);
        }
    }

    /**
     * Handles list of SpaceMarines with formatting.
     */
    private void handleSpaceMarineList(List<?> list) {
        if (list.isEmpty()) {
            System.out.println("  → Collection is empty");
        } else {
            System.out.println("  → Collection (" + list.size() + " elements, sorted by name):");
            for (Object item : list) {
                if (item instanceof SpaceMarine marine) {
                    System.out.println("    • " + marine);
                }
            }
        }
    }

    /**
     * Handles script execution result.
     */
    private void handleScriptResult(ScriptRunner.ExecutionResult result) {
        System.out.println("  → Script execution summary:");
        System.out.println("    • Success: " + result.successCount());
        System.out.println("    • Errors: " + result.errorCount());
        if (result.details().size() <= 10) {
            for (String detail : result.details()) {
                System.out.println("    " + detail);
            }
        } else {
            System.out.println("    (details omitted - too many lines)");
        }
    }
}