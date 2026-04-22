package client.command;

import client.utils.RequestsFactory;
import client.utils.SideFlag;
import shared.dto.CommandRequest;

public class Clear implements ClientCommand{
    @Override
    public CommandRequest execute(SideFlag flag) {
        return RequestsFactory.createSimple("clear");
    }
}
