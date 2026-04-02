package server.commands;

import shared.dto.CommandRequest;
import shared.dto.CommandResponse;

public class ExitCommand implements Command{
    public String helpInformation = "exit : завершить программу (без сохранения в файл)";

    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    @Override
    public CommandResponse execute(CommandRequest commandRequest) {
        System.exit(0);
        return new CommandResponse(true, null,"Exited successfully", commandRequest.requestId(), commandRequest.clientId());
    }
}
