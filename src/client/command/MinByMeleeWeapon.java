package client.command;

import client.utils.RequestsFactory;
import client.utils.SideFlag;
import shared.dto.CommandRequest;

public class MinByMeleeWeapon implements ClientCommand{
    @Override
    public CommandRequest execute(SideFlag flag) {
        return RequestsFactory.createSimple("min_by_melee_weapon");
    }
}
