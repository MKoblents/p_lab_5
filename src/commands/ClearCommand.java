package commands;

import manager.ProgramManager;

public class ClearCommand implements Command{
    private final ProgramManager programManager;
    private String helpInformation = "очистить коллекцию";
    public ClearCommand(ProgramManager programManager){
        this.programManager = programManager;
    }

    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    @Override
    public void execute() {
        programManager.getCollectionManager().clear();
    }
}
