package server.commands;
import server.manager.CollectionManager;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;

public class RemoveByIdCommand implements  Command{
    private String helpInformation = "remove_by_id id : удалить элемент из коллекции по его id";
    private CollectionManager collectionManager;
    public RemoveByIdCommand(CollectionManager collectionManager){
        this.collectionManager = collectionManager;
    }

    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    @Override
    public CommandResponse execute(CommandRequest commandRequest) {
        Long id = (Long) commandRequest.args();
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Valid ID required for remove_by_id, got: " + id);
        }
        if (!collectionManager.isIdInCollection(id)) {
            return new CommandResponse(
                    false,id,
                    "Element with ID " + id + " does not exist", commandRequest.requestId(), commandRequest.clientId());
        }
        collectionManager.remove(id);
        return new CommandResponse(
                true,id,
                "Element with ID " + id + " removed successfully",
                commandRequest.requestId(),
                commandRequest.clientId()

        );
    }
}
