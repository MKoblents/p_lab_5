package commands;

import inputWorkers.InputManager;
import manager.CollectionManager;
import manager.SpaceMarine;

public class RemoveGreaterCommand implements Command{
    private final InputManager inputManager;
    private final CollectionManager collectionManager;
    private String helpInformation = "remove greater {element} : удалить из коллекции все элементы, превышающие заданный";
    public RemoveGreaterCommand(CollectionManager collectionManager,InputManager inputManager){
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
        collectionManager.removeGreater(spaceMarine);
    }
}
