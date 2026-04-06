package client.command;

import client.inputWorkers.InputManager;
import client.utils.RequestsFactory;
import client.utils.SideFlag;
import shared.dto.CommandRequest;
import shared.enums.MeleeWeapon;

import java.io.IOException;

public class FilterLessThanMeleeWeapon implements ClientCommand{
    private InputManager inputManager;
    public FilterLessThanMeleeWeapon(InputManager inputManager){
        this.inputManager = inputManager;
    }
    @Override
    public CommandRequest execute(SideFlag flag) {
        MeleeWeapon weapon;
        if (flag == SideFlag.FORWARDED){
            try {
                weapon = inputManager.getNewEnumType(MeleeWeapon.class);
            }catch (IOException e){
                weapon = null;
            }
        }else {
            weapon = inputManager.getLastInputMeleeWeapon();
        }
        if (weapon == null) {
            System.err.println("Error: Valid MeleeWeapon required");
            return  null;
        }
        return RequestsFactory.withMeleeWeapon("filter_less_than_melee_weapon", weapon);
    }
}
