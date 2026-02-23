package commands;

import manager.CollectionManager;
import manager.SpaceMarine;

public class ShowCommand implements Command{
    private String helpInformation = "show : вывести в стандартный поток вывода все элементы коллекции в строковом представлении";
    private CollectionManager collectionManager;
    public ShowCommand(CollectionManager collectionManager){
        this.collectionManager=collectionManager;
    }

    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    @Override
    public void execute() {
        for (SpaceMarine spaceMarine:collectionManager.getSpaceMarines()){
            System.out.println(spaceMarine);
        }
    }
}
