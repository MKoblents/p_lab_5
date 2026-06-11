package client.command;


import client.context.ClientContext;
import client.inputWorkers.InputManager;
import client.network.ConnectionManager;
import client.utils.RequestsFactory;
import client.utils.SideFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.dto.UserInfo;

import java.io.IOException;

public class SignIn implements ClientCommand {
    private static final Logger logger = LoggerFactory.getLogger(SignIn.class);
    private final InputManager inputManager;
    private final ConnectionManager connection;
    private final ClientContext context;
    public SignIn(InputManager inputManager, ConnectionManager connection, ClientContext context){
        this.inputManager = inputManager;
        this.connection = connection;
        this.context = context;
    }
    @Override
    public CommandRequest execute(SideFlag flag) {
        try {
            System.out.println("Enter new username: ");
            String username = inputManager.getNewString();
            System.out.println("Enter password:");
            String password = inputManager.getNewString();
            UserInfo userInfo =new UserInfo(username,password);
            CommandRequest request = RequestsFactory.creatLogRequest("sign_in", userInfo);
            try {
                connection.sendRequest(request);
            } catch (IOException e) {
                logger.error("Failed to send log in request: {}", e.getMessage());
                System.err.println("Error: Could not communicate with server. Please check your connection.");
                return null;
            }
        }catch (IOException e){
            return null;
        }
        return null;
    }
}
