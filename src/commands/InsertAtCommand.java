package commands;

import inputWorkers.InputManager;
import manager.CollectionManager;
import manager.SpaceMarine;

public class InsertAtCommand implements Command{
    private CollectionManager collectionManager;
    private String helpInformation = "insert_at index {element} : добавить новый элемент в заданную позицию";
    private InputManager inputManager;
    public InsertAtCommand(CollectionManager collectionManager, InputManager inputManager){
        this.collectionManager = collectionManager;
        this.inputManager = inputManager;
    }

    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    @Override
    public void execute() {
        SpaceMarine spaceMarine = inputManager.getInputSpaceMarine();
        inputManager.getValidator().spaceMarineValidate(spaceMarine);
        if (inputManager.getLastInt()>collectionManager.size()){
            collectionManager.addItem(spaceMarine);
            System.err.println("Your index was out of range, so element added to the end of collection.");
        }
        collectionManager.addItem(inputManager.getLastInt(), spaceMarine);

    }
}
