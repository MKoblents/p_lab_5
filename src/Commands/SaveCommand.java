package Commands;

import manager.ProgramManager;

public class SaveCommand implements Command{
    private final ProgramManager programManager;
    private String helpInformation;
    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    public SaveCommand(ProgramManager programManager){
        this.programManager = programManager;
    }
    @Override
    public void execute() {//#TODO IOException
        programManager.getCollectionSaver().save();

    }
}
