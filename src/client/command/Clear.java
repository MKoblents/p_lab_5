package client.command;

import client.utils.RequestsFactory;
import shared.dto.CommandRequest;

public class Clear implements ClientCommand{
    @Override
    public CommandRequest execute() {
        return RequestsFactory.createSimple("clear");
    }
}
