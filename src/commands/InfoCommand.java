package commands;

import manager.CollectionManager;
import manager.ProgramManager;

public class InfoCommand implements Command{
    private CollectionManager collectionManager;
    private String helpInformation = "info : вывести в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество элементов и т.д.)";
    public InfoCommand(CollectionManager collectionManager){
        this.collectionManager= collectionManager;
    }

    @Override
    public void execute() {
        System.out.println(collectionManager.getSpaceMarines().getClass());
        System.out.println(collectionManager.getCreationData());
        System.out.println(collectionManager.getSpaceMarines().size());
    }

    @Override
    public String getHelpInformation() {
        return helpInformation;
    }
}
