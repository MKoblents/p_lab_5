package commands;

import manager.CollectionManager;

public class ShuffleCommand implements Command{
    private CollectionManager collectionManager;
    private String helpInformation = "перемешать элементы коллекции в случайном порядке";

    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    @Override
    public void execute() {
        collectionManager.shuffle();
    }
    public ShuffleCommand(CollectionManager collectionManager){
        this.collectionManager = collectionManager;
    }
}
