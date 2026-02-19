package commands;

import manager.Invoker;
import manager.ProgramManager;

public class HelpCommand implements Command {
    private String helpInformation = "вывести справку по доступным командам";
    private final ProgramManager programManager;
    private final Invoker invoker;
    public HelpCommand(ProgramManager programManager, Invoker invoker){
        this.programManager = programManager;
        this.invoker = invoker;
    }
    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    @Override
    public void execute() {
        for (Command command: invoker.getCommandMap().values()){
            System.out.println(command.getHelpInformation());
        }

    }
}
