package Commands;

import inputWorkers.InputManager;
import manager.CollectionManager;
import manager.ProgramManager;
import outputWorkers.CollectionSaver;

public class InfoCommand implements Command{
    private ProgramManager programManager;
    private String helpInformation = "вывести в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество элементов и т.д.)";
    public InfoCommand(ProgramManager programManager){
        this.programManager= programManager;
    }

    @Override
    public void execute() {
        CollectionManager collectionManager = programManager.getCollectionManager();
        System.out.println(collectionManager.getSpaceMarines().getClass());
        System.out.println(collectionManager.getCreationData());
        System.out.println(collectionManager.getSpaceMarines().size());
        //#TODO more information
    }

    @Override
    public String getHelpInformation() {
        return helpInformation;
    }
}
