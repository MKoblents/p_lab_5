package client.command;

import client.inputWorkers.InputManager;
import client.utils.RequestsFactory;
import shared.dto.CommandRequest;
import shared.enums.MeleeWeapon;

public class FilterLessThanMeleeWeapon implements ClientCommand{
    private InputManager inputManager;
    public FilterLessThanMeleeWeapon(InputManager inputManager){
        this.inputManager = inputManager;
    }
    @Override
    public CommandRequest execute() {
        MeleeWeapon weapon = inputManager.getInputMeleeWeapon();
        if (weapon == null) {
            System.err.println("Error: Valid MeleeWeapon required");
            return  null;
        }
        return RequestsFactory.withMeleeWeapon("filter_less_than_melee_weapon", weapon);
    }
}
