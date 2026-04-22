package server.commands;

import server.manager.CollectionManager;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.models.SpaceMarine;

import java.util.Map;

public class InsertAtCommand implements Command{
    private CollectionManager collectionManager;
    private String helpInformation = "insert_at index {element} : добавить новый элемент в заданную позицию";
    public InsertAtCommand(CollectionManager collectionManager){
        this.collectionManager = collectionManager;
    }

    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    @Override
    public CommandResponse execute(CommandRequest commandRequest) {
        Object data = commandRequest.args();
        if (!(data instanceof Map)) {
            return new CommandResponse(false,null,
                    "Error: Invalid data format for insert at command", commandRequest.requestId(), commandRequest.clientId());
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> args = (Map<String, Object>) data;
        if (!args.containsKey("index") || !args.containsKey("marine")) {
            return new CommandResponse(false, null,
                    "Error: Insert at requires both 'id' and 'marine' arguments", commandRequest.requestId(), commandRequest.clientId());
        }
        int index = (int) args.get("index");
        if (index < 0) {
            return new CommandResponse(false, null,
                    "Error: Valid ID required for insert at", commandRequest.requestId(), commandRequest.clientId());
        }
        SpaceMarine spaceMarineInput = (SpaceMarine) args.get("marine");
        if (spaceMarineInput == null) {
            return new CommandResponse(false, null,
                    "Error: Invalid SpaceMarine data", commandRequest.requestId(), commandRequest.clientId());
        }
        collectionManager.addItem(index, spaceMarineInput);
        return new CommandResponse(true,
                spaceMarineInput.toString(),
                "SpaceMarine with ID " + index + " inserted successfully", commandRequest.requestId(), commandRequest.clientId()
                );

    }
}
