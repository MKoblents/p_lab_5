package server.commands;
import client.inputWorkers.InputManager;
import server.manager.CollectionManager;
import server.validator.Validator;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.models.SpaceMarine;

public class AddCommand implements Command {
    private String helpInformation = "add {element} : добавить новый элемент в коллекцию";
    private final CollectionManager collectionManager;
    private Validator validator;
    public AddCommand(CollectionManager collectionManager, Validator validator){
        this.collectionManager = collectionManager;
        this.validator = validator;
    }
    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    @Override
    public CommandResponse execute(CommandRequest commandRequest) {
        SpaceMarine spaceMarine = (SpaceMarine) commandRequest.getData();
        validator.spaceMarineValidate(spaceMarine);
        collectionManager.addItem(spaceMarine);
        return  new CommandResponse(true, "Added successfully", null);
    }
}
