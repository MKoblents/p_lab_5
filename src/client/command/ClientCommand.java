package client.command;

import shared.dto.CommandRequest;

public interface ClientCommand {
    CommandRequest execute();
}
