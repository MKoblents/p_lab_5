package commands;

import manager.CollectionManager;

public class MinByMeleeWeaponCommand implements Command{
    private CollectionManager collectionManager;
    private String helpinformation = "min_by_melee_weapon : вывести любой объект из коллекции, значение поля meleeWeapon которого является минимальным";
    public MinByMeleeWeaponCommand(CollectionManager collectionManager){
        this.collectionManager= collectionManager;
    }
    @Override
    public String getHelpInformation() {
        return helpinformation;
    }

    @Override
    public void execute() {
        System.out.println(collectionManager.getMinByMeleeWeapon());
    }
}
