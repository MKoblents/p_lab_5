package server.commands;

import server.manager.CollectionService;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.enums.MeleeWeapon;
import client.inputWorkers.InputManager;
import server.manager.CollectionManager;
import shared.models.SpaceMarine;

import java.util.Comparator;
import java.util.List;

public class FilterLessThanMeleeWeaponCommand implements Command{
    private String helpInfprmation = "filter_less_than_melee_weapon meleeWeapon : вывести элементы, значение поля meleeWeapon которых меньше заданного";
    private CollectionService collectionService;
    public FilterLessThanMeleeWeaponCommand(CollectionService collectionService){
        this.collectionService = collectionService;
    }
    @Override
    public String getHelpInformation() {
        return helpInfprmation;
    }

    @Override
    public CommandResponse execute(CommandRequest commandRequest){
        MeleeWeapon meleeWeapon = (MeleeWeapon) commandRequest.args();
//        System.out.println(meleeWeapon);
        List<SpaceMarine> spaceMarinesList = collectionService.filterLessThanMeleeWeapon(meleeWeapon);
        spaceMarinesList.sort(Comparator.comparing(SpaceMarine::getName));
        return new CommandResponse(true, spaceMarinesList, "Filter succsess", commandRequest.requestId(), commandRequest.clientId());


    }
}
