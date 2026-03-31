package client.command;

import client.utils.RequestsFactory;
import shared.dto.CommandRequest;

public class MinByMeleeWeapon implements ClientCommand{
    @Override
    public CommandRequest execute() {
        return RequestsFactory.createSimple("min_by_melee_weapon");
    }
}
