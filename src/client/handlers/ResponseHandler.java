package client.handlers;

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
     */
    private void handleResult(Object result) {
        if (result instanceof SpaceMarine marine) {
            System.out.println("  → Added: " + marine.getName() + " (ID: " + marine.getId() + ")");
        } else if (result instanceof List<?> list) {
            if (list.isEmpty()) {
                System.out.println("  → Collection is empty");
            } else {
                System.out.println("  → Collection (" + list.size() + " elements):");
                for (Object item : list) {
                    System.out.println("    • " + item);
                }
            }
        } else if (result instanceof Long id) {
            System.out.println("  → ID: " + id);
        } else if (result instanceof Double value) {
            System.out.println("  → Value: " + value);
        } else {
            System.out.println("  → Result: " + result);
        }
    }
}