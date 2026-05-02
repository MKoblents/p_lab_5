package server.commands;

import client.context.ClientContext;
import client.network.ConnectionManager;
import server.service.AuthService;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.dto.UserInfo;

import java.sql.SQLException;
import java.util.Optional;

public class LogInCommand implements Command{
    private final AuthService authService;
    public LogInCommand(AuthService authService){
        this.authService = authService;
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
                return new CommandResponse(true, userInfo, "Log in success", commandRequest.requestId(), commandRequest.clientId());
            } else {
//                return new CommandResponse(false, null, "Log in failed: invalid username or password", commandRequest.requestId(), commandRequest.clientId());
//                TODO sign_in
                authService.register(userInfo.name(), userInfo.password());
                return new CommandResponse(true,userInfo,"Log in  success", commandRequest.requestId(),commandRequest.clientId());
            }
        } catch (SQLException e){
            return new CommandResponse(false,userInfo,"Log in failed because of db access "+e.getMessage(), commandRequest.requestId(),commandRequest.clientId());

        }
    }
}
