package server.commands;

import server.manager.CollectionService;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;

public class MinByMeleeWeaponCommand implements Command{
    private CollectionService collectionService;
    private String helpinformation = "min_by_melee_weapon : вывести любой объект из коллекции, значение поля meleeWeapon которого является минимальным";
    public MinByMeleeWeaponCommand(CollectionService collectionService){
        this.collectionService= collectionService;
    }
    @Override
    public String getHelpInformation() {
        return helpinformation;
    }

    @Override
    public CommandResponse execute(CommandRequest commandRequest) {
        return new CommandResponse(true, collectionService.getMinByMeleeWeapon(), "Min element found", commandRequest.requestId(), commandRequest.clientId());
    }
}
