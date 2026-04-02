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
        String result ="";
        for (Command command: invoker.getCommandMap().values()){
            result = result + command.getHelpInformation()+"\n";
        }
        return  new CommandResponse(true, result, "Helped success", commandRequest.requestId(), commandRequest.clientId());

    }
}
