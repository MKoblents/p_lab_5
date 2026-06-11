package client.utils;

import shared.dto.CommandRequest;
import shared.dto.UserInfo;
import shared.enums.MeleeWeapon;
import shared.models.SpaceMarine;
import java.util.Map;

public class RequestsFactory {
    private static String clientId = null;
    private static UserInfo userInfo = null;

    public static void setClientId(String clientId) {
        RequestsFactory.clientId = clientId;
    }
    public static void  setUserInfo(UserInfo userInfo){
        RequestsFactory.userInfo = userInfo;
    }

    public static CommandRequest creatLogRequest(String commandName, UserInfo userInfo){
        return new CommandRequest(commandName,userInfo,generateRequestId(),clientId,RequestsFactory.userInfo);
    }
    public static CommandRequest createSimple(String commandName) {
        return new CommandRequest(commandName, null, generateRequestId(), clientId, userInfo);
    }
    public static CommandRequest withLongArg(String commandName, Long arg) {
        return new CommandRequest(commandName, arg, generateRequestId(), clientId, userInfo);
    }
    public static CommandRequest withMarine(String commandName, SpaceMarine marine) {
        return new CommandRequest(commandName, marine, generateRequestId(), clientId, userInfo);
    }
    public static CommandRequest withStringArg(String commandName, String arg) {
        return new CommandRequest(commandName, arg, generateRequestId(), clientId, userInfo);
    }
    public static CommandRequest createTwoArgs(String commandName, Long id, SpaceMarine marine) {
        Map<String, Object> args = Map.of("id", id, "marine", marine);
        return new CommandRequest(commandName, args, generateRequestId(), clientId, userInfo);
    }
    public static CommandRequest withMeleeWeapon(String commandName, MeleeWeapon meleeWeapon){
        return new CommandRequest(commandName, meleeWeapon, generateRequestId(), clientId, userInfo);
    }
    public static CommandRequest createIdMarine(String commandName, int index, SpaceMarine spaceMarine){
        Map<String, Object> args = Map.of("index", index, "marine", spaceMarine);
        return new CommandRequest(commandName, args, generateRequestId(), clientId, userInfo);
    }
    private static String generateRequestId() {
        return java.util.UUID.randomUUID().toString().substring(0, 8);
    }
}