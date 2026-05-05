package server.commands;

import server.manager.ClientRegistry;
import server.service.AuthService;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.dto.UserInfo;

import java.sql.SQLException;
import java.util.Optional;

public class SignInCommand implements Command{
    private final AuthService authService;
    private final ClientRegistry clientRegistry;
    public SignInCommand(AuthService authService, ClientRegistry clientRegistry){
        this.authService = authService;
        this.clientRegistry =clientRegistry;
    }
    @Override
    public String getHelpInformation() {
        return "sign_in <username> <password> : зарегистрироваться в систему";
    }

    @Override
    public CommandResponse execute(CommandRequest commandRequest) {
        UserInfo userInfo = (UserInfo)commandRequest.args();
        if (userInfo == null || userInfo.name() == null || userInfo.password() == null) {
            return new CommandResponse(false, null, "Sign in failed: credentials missing", commandRequest.requestId(), commandRequest.clientId());
        }
        try {
            Optional<String> validUser = authService.validate(userInfo);
            if (validUser.isPresent()) {
                clientRegistry.bindUserToClient(commandRequest.clientId(), userInfo.name());
                return new CommandResponse(true, userInfo, "User already exists. Log in success", commandRequest.requestId(), commandRequest.clientId());
            } else {
                clientRegistry.bindUserToClient(commandRequest.clientId(), userInfo.name());
                Optional<String> parentName = null;
                parentName = clientRegistry.findParentByChild(commandRequest.clientId());
                String pn = null;
                if (parentName.isPresent()){
                    pn = clientRegistry.getUsernameByClientId(parentName.get());
                }
                authService.register(userInfo.name(), userInfo.password(), pn);
                return new CommandResponse(true,userInfo,"Sign in success", commandRequest.requestId(),commandRequest.clientId());
            }
        } catch (SQLException e){
            return new CommandResponse(false,userInfo,"Sign in failed because of db access "+e.getMessage(), commandRequest.requestId(),commandRequest.clientId());

        }
    }
}
