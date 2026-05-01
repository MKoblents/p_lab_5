package client.command;

import client.context.ClientContext;
import client.utils.SideFlag;
import shared.dto.CommandRequest;

public class LogOut implements ClientCommand{
    private final ClientContext context;
    public LogOut(ClientContext context){
        this.context = context;
    }
    @Override
    public CommandRequest execute(SideFlag flag) {
        context.setUserInfo(null);
        return null;
    }
}
