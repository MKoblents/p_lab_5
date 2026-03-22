package server.commands;

import server.manager.CollectionManager;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;

public class SumOfHealthCommand implements Command{
    private CollectionManager collectionManager;
    private String helpInformation = "sum_of_health : вывести сумму значений поля health для всех элементов коллекции";
    public SumOfHealthCommand(CollectionManager collectionManager){
        this.collectionManager = collectionManager;
    }

    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    @Override
    public CommandResponse execute(CommandRequest commandRequest) {
        System.out.println(collectionManager.getSumOfHealth());
    }
}
