package server.commands;

import server.manager.CollectionManager;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;

public class ClearCommand implements Command{
    private final CollectionManager collectionManager;
    private String helpInformation = "clear : очистить коллекцию";
    public ClearCommand(CollectionManager collectionManager){
        this.collectionManager = collectionManager;
    }

    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    @Override
    public CommandResponse execute(CommandRequest commandRequest) {
        collectionManager.clear();
        return  new CommandResponse(true, null,"Cleared successfully", commandRequest.requestId(), commandRequest.clientId());
    }
}
