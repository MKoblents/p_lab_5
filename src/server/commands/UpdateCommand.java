package server.commands;

import server.manager.CollectionService;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.models.SpaceMarine;

import java.sql.SQLException;
import java.util.Map;

public class UpdateCommand implements Command {
    private static final String HELP_INFO =
            "update id {element} : обновить значение элемента коллекции, id которого равен заданному";

    private final CollectionService collectionService;

    public UpdateCommand(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @Override
    public String getHelpInformation() {
        return HELP_INFO;
    }

    @Override
    public CommandResponse execute(CommandRequest commandRequest) {
        Object data = commandRequest.args();
        if (!(data instanceof Map)) {
            return new CommandResponse(false,data,
                    "Error: Invalid data format for update command", commandRequest.requestId(), commandRequest.clientId());
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> args = (Map<String, Object>) data;
        if (!args.containsKey("id") || !args.containsKey("marine")) {
            return new CommandResponse(false, args,
                    "Error: Update requires both 'id' and 'marine' arguments", commandRequest.requestId(), commandRequest.clientId());
        }
        Long id = (Long) args.get("id");
        if (id == null || id <= 0) {
            return new CommandResponse(false, null,
                    "Error: Valid ID required for update", commandRequest.requestId(), commandRequest.clientId());
        }
        try {
            if (!collectionService.isIdInCollection(id)) {
                return new CommandResponse(false,  null,
                        "Error: No element found with ID " + id, commandRequest.requestId(), commandRequest.clientId());
            }
            SpaceMarine spaceMarineInput = (SpaceMarine) args.get("marine");
            if (spaceMarineInput == null) {
                return new CommandResponse(false, null,
                        "Error: Invalid SpaceMarine data", commandRequest.requestId(), commandRequest.clientId());
            }
            spaceMarineInput.setId(id);
            collectionService.update(id, spaceMarineInput, commandRequest.userInfo().name());
            collectionService.removeUpdating(id);
            return new CommandResponse(true,
                    spaceMarineInput,
                    "SpaceMarine with ID " + id + " updated successfully",
                    commandRequest.requestId(), commandRequest.clientId()
            );
        }catch (SQLException e){
            return new CommandResponse(false,(SpaceMarine) args.get("marine"),"SpaceMarine updating failed because of db access "+e.getMessage(), commandRequest.requestId(),commandRequest.clientId());
        }

    }
}
