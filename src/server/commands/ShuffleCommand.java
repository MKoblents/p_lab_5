package server.commands;

import server.manager.CollectionManager;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;

public class ShuffleCommand implements Command{
    private CollectionManager collectionManager;
    private String helpInformation = "shuffle : перемешать элементы коллекции в случайном порядке";

    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    @Override
    public CommandResponse execute(CommandRequest commandRequest) {
        collectionManager.shuffle();
        return new CommandResponse(true, null, "Shuffle completed.", commandRequest.requestId());
    }
    public ShuffleCommand(CollectionManager collectionManager){
        this.collectionManager = collectionManager;
    }
}
