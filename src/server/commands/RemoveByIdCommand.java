package server.commands;
import server.manager.CollectionManager;
import server.validator.Validator;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;

public class RemoveByIdCommand implements  Command{
    private String helpInformation = "remove_by_id id : удалить элемент из коллекции по его id";
    private CollectionManager collectionManager;
    private Validator validator;
    public RemoveByIdCommand(CollectionManager collectionManager, Validator validator){
        this.validator = validator;
        this.collectionManager = collectionManager;
    }

    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    @Override
    public CommandResponse execute(CommandRequest commandRequest) {
        Long id = (Long) commandRequest.getData();
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Valid ID required for remove_by_id, got: " + id);
        }

        if (!collectionManager.isIdInCollection(id)) {
            return new CommandResponse(
                    false,id,
                    "Element with ID " + id + " does not exist");
        }
        collectionManager.remove(id);
        return new CommandResponse(
                true,id,
                "Element with ID " + id + " removed successfully"

        );
    }
}
