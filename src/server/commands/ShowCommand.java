package server.commands;

import server.manager.CollectionService;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.models.SpaceMarine;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ShowCommand implements Command{
    private String helpInformation = "show : вывести в стандартный поток вывода все элементы коллекции в строковом представлении";
    private CollectionService collectionService;
    public ShowCommand(CollectionService collectionService){
        this.collectionService=collectionService;
    }

    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    @Override
    public CommandResponse execute(CommandRequest commandRequest) {
        List<SpaceMarine> sortedMarines = collectionService.getSpaceMarines().stream()
                .sorted(Comparator.comparing(SpaceMarine::getName))
                .collect(Collectors.toList());
        return new CommandResponse(
                true,sortedMarines,
                "Showing " + sortedMarines.size() + " SpaceMarine(s)", commandRequest.requestId(), commandRequest.clientId());
    }
}
