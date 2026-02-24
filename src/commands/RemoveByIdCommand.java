package commands;

import inputWorkers.InputManager;
import manager.CollectionManager;

public class RemoveByIdCommand implements  Command{
    private String helpInformation = "remove_by_id id : удалить элемент из коллекции по его id";
    private InputManager inputManager;
    private CollectionManager collectionManager;
    public RemoveByIdCommand(CollectionManager collectionManager, InputManager inputManager){
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
        if (!collectionManager.isIdInCollection(id)) {
            System.err.println("Your id is missing.");
        }collectionManager.remove(id);
    }
}
