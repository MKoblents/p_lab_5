package server.commands;

import server.manager.CollectionManager;
import server.outputWorkers.CollectionSaver;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
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
    public CommandResponse execute(CommandRequest commandRequest) {
        try {
            collectionSaver.save(collectionManager, System.getenv("PLAB5"));
            return new CommandResponse(
                    true,
                    null,
                    "Collection saved to " + System.getenv("PLAB5"),
                    commandRequest.requestId()
            );

        } catch (Exception e) {
            return new CommandResponse(
                    false,
                    null,
                    "Error saving collection: " + e.getMessage(),
                    commandRequest.requestId()
            );
        }


    }
}
