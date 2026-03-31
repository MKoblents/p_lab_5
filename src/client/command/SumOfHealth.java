package client.command;

import client.utils.RequestsFactory;
import shared.dto.CommandRequest;

public class SumOfHealth implements ClientCommand{
    @Override
    public CommandRequest execute() {
        return RequestsFactory.createSimple("sum_of_health");
    }
}
