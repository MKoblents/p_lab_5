package commands;

import enums.MeleeWeapon;
import inputWorkers.InputManager;
import manager.CollectionManager;
import manager.SpaceMarine;

import java.util.List;

public class FilterLessThanMeleeWeaponCommand implements Command{
    private String helpInfprmation = "filter_less_than_melee_weapon meleeWeapon : вывести элементы, значение поля meleeWeapon которых меньше заданного";
    private CollectionManager collectionManager;
    private InputManager inputManager;
    public FilterLessThanMeleeWeaponCommand(CollectionManager collectionManager, InputManager inputManager){
        this.collectionManager = collectionManager;
        this.inputManager = inputManager;
    }
    @Override
    public String getHelpInformation() {
        return helpInfprmation;
    }

    @Override
    public void execute(){
        MeleeWeapon meleeWeapon = inputManager.getInputMeleeWeapon();
        System.out.println(meleeWeapon);
        List<SpaceMarine> spaceMarinesList = collectionManager.filterLessThanMeleeWeapon(meleeWeapon);
        for (SpaceMarine spaceMarine : spaceMarinesList) {
            System.out.println(spaceMarine);
        }


    }
}
