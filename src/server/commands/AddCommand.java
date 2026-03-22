package server.commands;
import client.inputWorkers.InputManager;
import server.manager.CollectionManager;
import shared.models.SpaceMarine;

public class AddCommand implements Command {
    private String helpInformation = "add {element} : добавить новый элемент в коллекцию";
    private final InputManager inputManager;
    private final CollectionManager collectionManager;
    public AddCommand(CollectionManager collectionManager,InputManager inputManager){
        this.inputManager = inputManager;
        this.collectionManager = collectionManager;
    }
    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    @Override
    public void execute() {
        SpaceMarine spaceMarine = inputManager.getInputSpaceMarine();
        inputManager.getValidator().spaceMarineValidate(spaceMarine);
        collectionManager.addItem(spaceMarine);
    }
}
