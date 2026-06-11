package server.commands;
import server.manager.CollectionManager;
import server.manager.CollectionService;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.models.SpaceMarine;

import java.sql.SQLException;

public class AddCommand implements Command {
    private String helpInformation = "add {element} : добавить новый элемент в коллекцию";
    private final CollectionService collectionService;
    public AddCommand(CollectionService collectionService){
        this.collectionService = collectionService;
    }
    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    @Override
    public CommandResponse execute(CommandRequest commandRequest) {
        SpaceMarine spaceMarine = (SpaceMarine) commandRequest.args();
        try {
            boolean success = collectionService.addItem(spaceMarine, commandRequest.userInfo().name());
            if (success) {
                return new CommandResponse(true, spaceMarine, "Added successfully",
                        commandRequest.requestId(), commandRequest.clientId());
            } else {
                return new CommandResponse(false, spaceMarine, "Database rejected insert (constraint violation or missing owner)",
                        commandRequest.requestId(), commandRequest.clientId());
            }
        } catch (SQLException e) {
            return new CommandResponse(false, spaceMarine, "SpaceMarine addition failed because of db access: " + e.getMessage(),
                    commandRequest.requestId(), commandRequest.clientId());
        }
    }
}
