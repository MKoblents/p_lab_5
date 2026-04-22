package client.command;

import client.inputWorkers.InputManager;
import client.utils.RequestsFactory;
import client.utils.SideFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shared.dto.CommandRequest;

import java.io.IOException;

public class KillClient implements ClientCommand {
    private static final Logger logger = LoggerFactory.getLogger(KillClient.class);
    private final InputManager inputManager;
    public KillClient(InputManager inputManager) {
        this.inputManager = inputManager;
    }
    @Override
    public CommandRequest execute(SideFlag flag) {
        String id;
        if (flag == SideFlag.SELF) {
            id = inputManager.getLastString();
            logger.debug("Using cached client ID for kill_client command: {}", id);
        } else {
            System.out.println("Please enter the ID of the client to terminate:");
            try {
                id = inputManager.getNewString();
            } catch (IOException e) {
                logger.error("Failed to read client ID from input: {}", e.getMessage());
                System.err.println("Error: Could not read client ID. Please check your input stream.");
                return null;
            }
        }
        if (id == null || id.trim().isEmpty()) {
            System.err.println("Error: Client ID cannot be empty. Please provide a valid identifier.");
            logger.debug("KillClient command aborted: ID is null or empty.");
            return null;
        }
        logger.info("Preparing kill_client request for target: {}", id);
        return RequestsFactory.withStringArg("kill_client", id);
    }
}