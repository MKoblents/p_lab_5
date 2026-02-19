package commands;

import manager.ProgramManager;
import manager.SpaceMarine;

public class RemoveByIdCommand implements Command{
    private final ProgramManager programManager;
    private String helpInformation = "удалить элемент из коллекции по его id";

    public RemoveByIdCommand(ProgramManager programManager){
        this.programManager = programManager;
    }
    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    @Override
    public void execute() {
        long id = programManager.getInputManager().getLastLong();//#TODO do normal getting id
        for (SpaceMarine spaceMarine: programManager.getCollectionManager().getSpaceMarines()){
            if (spaceMarine.getId() == id){
                programManager.getCollectionManager().remove(spaceMarine);
                return;
            }
        }
    }
}
