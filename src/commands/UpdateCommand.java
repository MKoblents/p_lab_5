package commands;

import inputWorkers.InputManager;
import manager.CollectionManager;
import manager.ProgramManager;
import manager.SpaceMarine;

public class UpdateCommand implements Command{
    private String helpInformation = "обновить значение элемента коллекции, id которого равен заданному";
    private CollectionManager collectionManager;
    private InputManager inputManager;
    public UpdateCommand(CollectionManager collectionManager, InputManager inputManager){
        this.collectionManager = collectionManager;
        this.inputManager = inputManager;
    }

    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    @Override
    public void execute() {
        long id = inputManager.getLastLong();
        SpaceMarine spaceMarine = collectionManager.getSpaceMarineById(id);
        SpaceMarine spaceMarineInput = inputManager.getInputSpaceMarine();
        inputManager.getValidator().spaceMarineValidate(spaceMarineInput);
        spaceMarineInput.setId(id);
        collectionManager.replace(spaceMarine, spaceMarineInput);
    }
}
