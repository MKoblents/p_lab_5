package client.command;

import client.utils.RequestsFactory;
import shared.dto.CommandRequest;

public class Info implements ClientCommand{
    @Override
    public CommandRequest execute() {
        return RequestsFactory.createSimple("info");
    }
}
