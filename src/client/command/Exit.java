package client.command;

import shared.dto.CommandRequest;

public class Exit implements ClientCommand{
    @Override
    public CommandRequest execute() {
        System.exit(1);
        return null;
    }
}
