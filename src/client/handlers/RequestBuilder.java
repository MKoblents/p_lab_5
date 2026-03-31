package client.handlers;

import client.inputWorkers.InputManager;
import client.utils.Validator;
import client.utils.RequestsFactory;
import shared.dto.CommandRequest;
import shared.enums.MeleeWeapon;
import shared.models.SpaceMarine;

public class RequestBuilder {
    private final InputManager inputManager;
    public RequestBuilder(InputManager inputManager) {
        this.inputManager = inputManager;
    }

    public CommandRequest buildRequest(String commandName) {
        try {
            return switch (commandName) {
                case "help", "info", "clear", "show", "shuffle", "sum_of_health", "min_by_melee_weapon" ->
                        RequestsFactory.createSimple(commandName);
                case "remove_by_id" -> {
                    long id = inputManager.getLastLong();
                    if (id <= 0) {
                        System.err.println("Error: Valid ID required for remove_by_id");
                        yield null;
                    }
                    yield RequestsFactory.withLongArg(commandName, id);
                }
                case "insert_at" -> {
                    int index = inputManager.getLastInt();
                    if (index<0){
                        System.err.println("Error: Valid ID required for remove_by_id");
                        yield null;
                    }
                    SpaceMarine marine = inputManager.getInputSpaceMarine();

                    if (marine == null){
                        System.err.println("Error: Failed to parse SpaceMarine from XML");
                        yield null;
                    }
                    Validator.spaceMarineValidate(marine);
                    yield RequestsFactory.createIdMarine(commandName, index, marine);
                }
                case "update" -> {
                    long id = inputManager.getLastLong();
                    if (id <= 0) {
                        System.err.println("Error: Valid ID required for update");
                        yield null;
                    }
//                    String xml = inputManager.getLastXmlString();
//                    if (id <= 0 || xml == null || xml.isEmpty()) {
//                        System.err.println("Error: ID and XML data required for " + commandName);
//                        yield null;
//                    }
                    SpaceMarine marine = inputManager.getInputSpaceMarine();
                    if (marine == null) {
                        System.err.println("Error: Failed to parse SpaceMarine from XML");
                        yield null;
                    }
                    Validator.spaceMarineValidate(marine);
                    yield RequestsFactory.createTwoArgs(commandName, id, marine);
                }
                case "add", "remove_greater" -> {
                    SpaceMarine marine = inputManager.getInputSpaceMarine();
                    if (marine == null) {
                        System.err.println("Error: Failed to parse SpaceMarine from XML");
                        yield null;
                    }
                    Validator.spaceMarineValidate(marine);
                    yield RequestsFactory.withMarine(commandName, marine);
                }
                case "execute_script" -> {
                    String path = inputManager.getLastPath();
                    if (path == null || path.isEmpty()) {
                        System.err.println("Error: File path required for execute_script");
                        yield null;
                    }
                    yield RequestsFactory.withStringArg(commandName, path);
                }
                case "filter_less_than_melee_weapon" -> {
                    System.out.println("3");
                    MeleeWeapon weapon = inputManager.getInputMeleeWeapon();
                    if (weapon == null) {
                        System.err.println("Error: Valid MeleeWeapon required");
                        yield null;
                    }
                    yield RequestsFactory.withMeleeWeapon(commandName, weapon);
                }
                default -> {
                    System.err.println("Unknown command: " + commandName + ". Type 'help' for list.");
                    yield null;
                }
            };
        } catch (Exception e) {
            System.err.println("Error building request: " + e.getMessage());
            return null;
        }
    }
}