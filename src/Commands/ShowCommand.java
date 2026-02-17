package Commands;

import manager.ProgramManager;
import manager.SpaceMarine;

public class ShowCommand implements Command{
    private String helpInformation = "вывести в стандартный поток вывода все элементы коллекции в строковом представлении";
    private ProgramManager programManager;
    public ShowCommand(ProgramManager programManager){
        this.programManager = programManager;
    }

    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    @Override
    public void execute() {
        for (SpaceMarine spaceMarine: programManager.getCollectionManager().getSpaceMarines()){
            System.out.println(spaceMarine);
        }
    }
}
