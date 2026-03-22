package client.utils;

import shared.dto.CommandRequest;
import shared.enums.MeleeWeapon;
import shared.models.SpaceMarine;
import java.util.Map;

public class RequestsFactory {
    public static CommandRequest createSimple(String commandName) {
        return new CommandRequest(commandName, null, null);
    }
    public static CommandRequest withLongArg(String commandName, Long arg) {
        return new CommandRequest(commandName, arg, null);
    }
    public static CommandRequest withMarine(String commandName, SpaceMarine marine) {
        return new CommandRequest(commandName, marine, null);
    }
    public static CommandRequest withStringArg(String commandName, String arg) {
        return new CommandRequest(commandName, arg, null);
    }
    public static CommandRequest createTwoArgs(String commandName, Long id, SpaceMarine marine) {
        Map<String, Object> args = Map.of("id", id, "marine", marine);
        return new CommandRequest(commandName, args, null);
    }
    public static CommandRequest withMeleeWeapon(String commandName, MeleeWeapon meleeWeapon){
        return new CommandRequest(commandName, meleeWeapon, null);
    }
}