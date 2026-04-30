package client.command;

import client.context.ClientContext;
import client.inputWorkers.InputManager;
import client.network.ConnectionManager;
import client.utils.RequestsFactory;
import client.utils.SideFlag;
import client.utils.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.models.SpaceMarine;

import java.io.IOException;
import java.util.UUID;

public class Update implements ClientCommand {
    private static final Logger logger = LoggerFactory.getLogger(Update.class);
    private final InputManager inputManager;
    private final ConnectionManager connection;
    private final ClientContext context;

    public Update(InputManager inputManager, ConnectionManager connectionManager, ClientContext context) {
        this.inputManager = inputManager;
        this.connection = connectionManager;
        this.context = context;
    }

    @Override
    public CommandRequest execute(SideFlag flag) {
        long id;
        if (flag == SideFlag.FORWARDED) {
            System.out.println("Please enter the ID of the element to update:");
            try {
                id = inputManager.getNewLong();
            } catch (IOException e) {
                logger.error("Failed to read ID from input: {}", e.getMessage());
                System.err.println("Error: Could not read ID. Please check your input stream.");
                return null;
            }
        } else {
            id = inputManager.getLastLong();
            logger.debug("Using cached ID for update command: {}", id);
        }
        if (id <= 0) {
            System.err.println("Error: Valid positive ID required for update command.");
            logger.debug("Update command aborted: ID is invalid ({}).", id);
            return null;
        }
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        CommandRequest checkRequest = RequestsFactory.withLongArg("could_be_updated", id);
        logger.info("Sending could_be_updated check for ID: {}", id);
        try {
            connection.sendRequest(checkRequest);
        } catch (IOException e) {
            logger.error("Failed to send update check request: {}", e.getMessage());
            System.err.println("Error: Could not communicate with server. Please check your connection.");
            return null;
        }
        CommandResponse response = connection.readResponse();
        if (response == null) {
            logger.warn("No response received for could_be_updated check");
            System.err.println("Error: Server did not respond to update eligibility check.");
            return null;
        }
        if (!response.success()) {
            String errorMsg = response.message();
            if (errorMsg == null || errorMsg.trim().isEmpty()) {
                errorMsg = "The element cannot be updated (access denied or not found).";
            }
            logger.debug("Update check failed: {}", errorMsg);
            System.err.println("Error: " + errorMsg);
            return null;
        }
        Object result = response.result();
        if (result instanceof Boolean canUpdate && canUpdate) {
            System.out.println("Element is eligible for update. Please provide the new SpaceMarine data:");
            SpaceMarine marine = inputManager.getInputSpaceMarine();
            if (marine == null) {
                System.err.println("Error: Failed to parse SpaceMarine from input. Please ensure valid format.");
                logger.debug("Update command aborted: SpaceMarine parsing failed.");
                return null;
            }
            logger.debug("Validating updated SpaceMarine data...");
            Validator.spaceMarineValidate(marine);
            logger.info("Preparing update request for ID: {}", id);
            return RequestsFactory.createTwoArgs("update", id, marine);
        } else {
            logger.debug("Update check returned unexpected result: {}", result);
            System.err.println("Error: Server returned an unexpected response during update check.");
            return null;
        }
    }
}