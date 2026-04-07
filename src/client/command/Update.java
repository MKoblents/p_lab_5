package client.command;

import client.context.ClientContext;
import client.inputWorkers.InputManager;
import client.network.ConnectionManager;
import client.utils.RequestsFactory;
import client.utils.SideFlag;
import client.utils.Validator;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.models.SpaceMarine;

import java.io.IOException;
import java.util.UUID;

public class Update implements ClientCommand{
    private InputManager inputManager;
    private ConnectionManager connection;
    private ClientContext context;
    public Update(InputManager inputManager, ConnectionManager connectionManager, ClientContext context){
        this.inputManager = inputManager;
        this.connection = connectionManager;
        this.context = context;
    }

    @Override
    public CommandRequest execute(SideFlag flag) {
        long id;
        if (flag == SideFlag.FORWARDED){
            try {
                id = inputManager.getNewLong();
            } catch (IOException e) {
                id = -1;
            }
        }else {
            id = inputManager.getLastLong();
        }
        if (id <= 0) {
            System.err.println("Error: Valid ID required for update");
            return null;
        }
        CommandRequest request = new CommandRequest("could_be_updated", id, UUID.randomUUID().toString().substring(0,8),context.getClientId());

        try {
            connection.sendRequest(request);
        } catch (IOException e) {
            return null;
//                    TODO
        }
        CommandResponse response = connection.readResponse();
        System.out.println(response);
        if (response != null){
            if ((boolean) response.result()){
                SpaceMarine marine = inputManager.getInputSpaceMarine();
                if (marine == null) {
                    System.err.println("Error: Failed to parse SpaceMarine from XML");
                    return null;
                }
                Validator.spaceMarineValidate(marine);
                return RequestsFactory.createTwoArgs("update", id, marine);
            }else {
                return null;
            }
        }else {
            System.out.println("You can update this spaceMarine. It's now updating.");
            return null;
        }
    }
}
