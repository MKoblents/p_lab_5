package server.commands;

import client.context.ClientContext;
import client.network.ConnectionManager;
import server.service.AuthService;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.dto.UserInfo;

import java.sql.SQLException;

public class LogInCommand implements Command{
    private final AuthService authService;
    public LogInCommand(AuthService authService){
        this.authService = authService;
    }
    @Override
    public String getHelpInformation() {
        return "";
    }

    @Override
    public CommandResponse execute(CommandRequest commandRequest) {
        UserInfo userInfo = (UserInfo)commandRequest.args();
        try {
            authService.register(userInfo.name(), userInfo.password());
            return new CommandResponse(true,userInfo,"Log in  success", commandRequest.requestId(),commandRequest.clientId());
        } catch (SQLException e){
            return new CommandResponse(false,userInfo,"Log in failed because of db access "+e.getMessage(), commandRequest.requestId(),commandRequest.clientId());

        }
    }
}
