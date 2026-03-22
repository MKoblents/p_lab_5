package server.commands;

import server.manager.CollectionManager;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.models.SpaceMarine;

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
    public CommandResponse execute(CommandRequest commandRequest) {
        for (SpaceMarine spaceMarine:collectionManager.getSpaceMarines()){
            System.out.println(spaceMarine);
        }
    }
}
