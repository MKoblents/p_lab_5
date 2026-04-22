package client.command;

import client.utils.SideFlag;
import shared.dto.CommandRequest;

public interface ClientCommand {
    CommandRequest execute(SideFlag flag);
}
