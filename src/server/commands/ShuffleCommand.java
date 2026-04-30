package server.commands;

import server.manager.CollectionService;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;

public class ShuffleCommand implements Command{
    private CollectionService collectionService;
    private String helpInformation = "shuffle : перемешать элементы коллекции в случайном порядке";

    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    @Override
    public CommandResponse execute(CommandRequest commandRequest) {
        collectionService.shuffle();
        return new CommandResponse(true, null, "Shuffle completed.", commandRequest.requestId(), commandRequest.clientId());
    }
    public ShuffleCommand(CollectionService collectionService){
        this.collectionService = collectionService;
    }
}
