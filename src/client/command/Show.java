package client.command;

import client.utils.RequestsFactory;
import shared.dto.CommandRequest;

public class Show implements ClientCommand{
    @Override
    public CommandRequest execute() {
        return RequestsFactory.createSimple("show");
    }
}
