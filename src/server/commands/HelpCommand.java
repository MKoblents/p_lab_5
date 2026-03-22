package server.commands;

import server.manager.Invoker;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;

public class HelpCommand implements Command {
    private String helpInformation = "help : вывести справку по доступным командам";
    private final Invoker invoker;
    public HelpCommand(Invoker invoker){
        this.invoker = invoker;
    }
    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    @Override
    public CommandResponse execute(CommandRequest commandRequest) {
        for (Command command: invoker.getCommandMap().values()){
            System.out.println(command.getHelpInformation());
        }

    }
}
