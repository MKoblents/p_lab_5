package commands;

import manager.CollectionManager;
import outputWorkers.CollectionSaver;

public class SaveCommand implements Command{
    private final CollectionSaver collectionSaver;
    private CollectionManager collectionManager;
    private String helpInformation ="save : сохранить коллекцию в файл";
    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    public SaveCommand(CollectionManager collectionManager, CollectionSaver collectionSaver){
        this.collectionManager = collectionManager;
        this.collectionSaver = collectionSaver;
    }
    @Override
    public void execute() {
        try {
            collectionSaver.save(collectionManager, "new.xml");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }
}
