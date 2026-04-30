package server.commands;

import server.manager.CollectionManager;
import server.manager.CollectionService;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;

import java.sql.SQLException;

public class ClearCommand implements Command{
    private final CollectionService collectionService;
    private String helpInformation = "clear : очистить коллекцию";
    public ClearCommand(CollectionService collectionService){
        this.collectionService = collectionService;
    }

    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    @Override
    public CommandResponse execute(CommandRequest commandRequest) {
        try {
            collectionService.clear(commandRequest.userInfo().name());
            return  new CommandResponse(true, null,"Cleared successfully", commandRequest.requestId(), commandRequest.clientId());
        }catch (SQLException e){
            return new CommandResponse(false,null,"Clear collection failed because of db access "+ e.getMessage(), commandRequest.requestId(),commandRequest.clientId());
        }
    }
}
