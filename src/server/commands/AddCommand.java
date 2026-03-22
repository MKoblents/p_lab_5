package server.commands;
import client.inputWorkers.InputManager;
import server.manager.CollectionManager;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.models.SpaceMarine;

public class AddCommand implements Command {
    private String helpInformation = "add {element} : добавить новый элемент в коллекцию";
    private final CollectionManager collectionManager;
    public AddCommand(CollectionManager collectionManager{
        this.collectionManager = collectionManager;
    }
    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    @Override
    public CommandResponse execute(CommandRequest commandRequest) {
        SpaceMarine spaceMarine = (SpaceMarine) commandRequest.getData();
//        inputManager.getValidator().spaceMarineValidate(spaceMarine);
//        TODO
        collectionManager.addItem(spaceMarine);
        return  new CommandResponse(true, "Added successfully", null);
    }
}
