package server.commands;

import server.manager.CollectionService;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;

public class SumOfHealthCommand implements Command{
    private CollectionService collectionService;
    private String helpInformation = "sum_of_health : вывести сумму значений поля health для всех элементов коллекции";
    public SumOfHealthCommand(CollectionService collectionService){
        this.collectionService = collectionService;
    }

    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    @Override
    public CommandResponse execute(CommandRequest commandRequest) {
        double sum = collectionService.getSumOfHealth();
        System.out.println(sum);
        return new CommandResponse(true,
                sum,
                "Sum of health: " + sum,
                commandRequest.requestId(), commandRequest.clientId());
    }
}
