package server.commands;

import server.manager.CollectionManager;
import server.validator.Validator;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.models.SpaceMarine;

import java.util.Map;

public class UpdateCommand implements Command {
    private static final String HELP_INFO =
            "update id {element} : обновить значение элемента коллекции, id которого равен заданному";

    private final CollectionManager collectionManager;
    private final Validator validator;

    public UpdateCommand(CollectionManager collectionManager, Validator validator) {
        this.collectionManager = collectionManager;
        this.validator = validator;
    }

    @Override
    public String getHelpInformation() {
        return HELP_INFO;
    }

    @Override
    public CommandResponse execute(CommandRequest commandRequest) {
        Object data = commandRequest.getData();
        if (!(data instanceof Map)) {
            return new CommandResponse(false,
                    "Error: Invalid data format for update command", null);
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> args = (Map<String, Object>) data;
        if (!args.containsKey("id") || !args.containsKey("marine")) {
            return new CommandResponse(false,
                    "Error: Update requires both 'id' and 'marine' arguments", null);
        }
        Long id = (Long) args.get("id");
        if (id == null || id <= 0) {
            return new CommandResponse(false,
                    "Error: Valid ID required for update", null);
        }
        if (!collectionManager.isIdInCollection(id)) {
            return new CommandResponse(false,
                    "Error: No element found with ID " + id, null);
        }
        SpaceMarine spaceMarineInput = (SpaceMarine) args.get("marine");
        if (spaceMarineInput == null) {
            return new CommandResponse(false,
                    "Error: Invalid SpaceMarine data", null);
        }
        validator.spaceMarineValidate(spaceMarineInput);
        spaceMarineInput.setId(id);
        collectionManager.update(id, spaceMarineInput);
        return new CommandResponse(true,
                "SpaceMarine with ID " + id + " updated successfully",
                spaceMarineInput.toString());

    }
}
