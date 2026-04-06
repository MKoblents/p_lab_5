package client.command;

import client.utils.SideFlag;
import shared.dto.CommandRequest;

public class Exit implements ClientCommand{
    @Override
    public CommandRequest execute(SideFlag flag) {
        System.exit(1);
        return null;
    }
}
