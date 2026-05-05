package server.commands;

import server.manager.ClientRegistry;
import server.service.AuthService;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.dto.UserInfo;

import java.sql.SQLException;
import java.util.Optional;

public class LogInCommand implements Command{
    private final AuthService authService;
    private final ClientRegistry clientRegistry;
    public LogInCommand(AuthService authService, ClientRegistry clientRegistry){
        this.authService = authService;
        this.clientRegistry =clientRegistry;
    }
    @Override
    public String getHelpInformation() {
        return "log_in <username> <password> : войти в систему";
    }

    @Override
    public CommandResponse execute(CommandRequest commandRequest) {
        UserInfo userInfo = (UserInfo)commandRequest.args();
        if (userInfo == null || userInfo.name() == null || userInfo.password() == null) {
            return new CommandResponse(false, null, "Log in failed: credentials missing", commandRequest.requestId(), commandRequest.clientId());
        }
        try {
            Optional<String> validUser = authService.validate(userInfo);
            if (validUser.isPresent()) {
                clientRegistry.bindUserToClient(commandRequest.clientId(), userInfo.name());
                return new CommandResponse(true, userInfo, "Log in success", commandRequest.requestId(), commandRequest.clientId());
            } else {
                return new CommandResponse(false, null, "Log in failed: invalid username or password", commandRequest.requestId(), commandRequest.clientId());
            }
        } catch (SQLException e){
            return new CommandResponse(false,userInfo,"Log in failed because of db access "+e.getMessage(), commandRequest.requestId(),commandRequest.clientId());

        }
    }
}
