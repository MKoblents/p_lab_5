package server.commands;

import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.enums.MeleeWeapon;
import client.inputWorkers.InputManager;
import server.manager.CollectionManager;
import shared.models.SpaceMarine;

import java.util.List;

public class FilterLessThanMeleeWeaponCommand implements Command{
    private String helpInfprmation = "filter_less_than_melee_weapon meleeWeapon : вывести элементы, значение поля meleeWeapon которых меньше заданного";
    private CollectionManager collectionManager;
    public FilterLessThanMeleeWeaponCommand(CollectionManager collectionManager){
        this.collectionManager = collectionManager;
    }
    @Override
    public String getHelpInformation() {
        return helpInfprmation;
    }

    @Override
    public CommandResponse execute(CommandRequest commandRequest){
        MeleeWeapon meleeWeapon = (MeleeWeapon) commandRequest.getData();
        System.out.println(meleeWeapon);
        List<SpaceMarine> spaceMarinesList = collectionManager.filterLessThanMeleeWeapon(meleeWeapon);
        for (SpaceMarine spaceMarine : spaceMarinesList) {
            System.out.println(spaceMarine);
        }
        return new CommandResponse(true, "Filterred success", null);


    }
}
