package commands;

import manager.CollectionManager;

public class SumOfHealthCommand implements Command{
    private CollectionManager collectionManager;
    private String helpInformation = "sum of health : вывести сумму значений поля health для всех элементов коллекции";
    public SumOfHealthCommand(CollectionManager collectionManager){
        this.collectionManager = collectionManager;
    }

    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    @Override
    public void execute() {
        System.out.println(collectionManager.getSumOfHealth());
    }
}
