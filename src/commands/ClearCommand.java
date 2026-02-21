package commands;

import manager.CollectionManager;
import manager.ProgramManager;

public class ClearCommand implements Command{
    private final CollectionManager collectionManager;
    private String helpInformation = "очистить коллекцию";
    public ClearCommand(CollectionManager collectionManager){
        this.collectionManager = collectionManager;
    }

    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    @Override
    public void execute() {
        collectionManager.clear();
    }
}
