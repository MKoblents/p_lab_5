package commands;

import manager.CollectionManager;

public class MinByMeleeWeaponCommand implements Command{
    private CollectionManager collectionManager;
    private String helpinformation = "вывести любой объект из коллекции, значение поля meleeWeapon которого является минимальным";
    public MinByMeleeWeaponCommand(CollectionManager collectionManager){
        this.collectionManager= collectionManager;
    }
    @Override
    public String getHelpInformation() {
        return "";
    }

    @Override
    public void execute() {
        System.out.println(collectionManager.getMinByMeleeWeapon());
    }
}
