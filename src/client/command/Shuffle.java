package client.command;

import client.utils.RequestsFactory;
import client.utils.SideFlag;
import shared.dto.CommandRequest;

public class Shuffle implements ClientCommand{
    @Override
    public CommandRequest execute(SideFlag flag) {
        return RequestsFactory.createSimple("shuffle");
    }
}
