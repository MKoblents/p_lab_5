package commands;

import inputWorkers.InputManager;
import manager.CollectionManager;

public class removeByIdCommand implements  Command{
    private String helpInformation = "remove_by_id id : удалить элемент из коллекции по его id";
    private InputManager inputManager;
    private CollectionManager collectionManager;
    public removeByIdCommand(CollectionManager collectionManager, InputManager inputManager){
        this.inputManager = inputManager;
        this.collectionManager = collectionManager;
    }

    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    @Override
    public void execute() {
        long id = inputManager.getLastLong();
        collectionManager.remove(id);
    }
}
