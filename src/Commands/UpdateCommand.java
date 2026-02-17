package Commands;

import manager.ProgramManager;

public class UpdateCommand implements Command{
    private String helpInformation = "обновить значение элемента коллекции, id которого равен заданному";
    private ProgramManager programManager;
    public UpdateCommand(ProgramManager programManager){
        this.programManager=programManager;
    }

    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    @Override
    public void execute() {
        int id = programManager.getInputManager().getLastInt();
        //#TODO something like addCommand but with choosing change or not
    }
}
