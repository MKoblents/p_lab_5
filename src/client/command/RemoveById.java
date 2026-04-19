package client.command;

import client.inputWorkers.InputManager;
import client.utils.RequestsFactory;
import client.utils.SideFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shared.dto.CommandRequest;

import java.io.IOException;

public class RemoveById implements ClientCommand {
    private static final Logger logger = LoggerFactory.getLogger(RemoveById.class);
    private final InputManager inputManager;

    public RemoveById(InputManager inputManager) {
        this.inputManager = inputManager;
    }

    @Override
    public CommandRequest execute(SideFlag flag) {
        long id;
        if (flag == SideFlag.FORWARDED) {
            System.out.println("Please enter the ID of the element to remove:");
            try {
                id = inputManager.getNewLong();
            } catch (IOException e) {
                logger.error("Failed to read ID from input: {}", e.getMessage());
                System.err.println("Error: Could not read ID. Please check your input stream.");
                return null;
            }
        } else {
            id = inputManager.getLastLong();
            logger.debug("Using cached ID for remove_by_id command: {}", id);
        }
        if (id <= 0) {
            System.err.println("Error: Valid positive ID required for remove_by_id command.");
            logger.debug("RemoveById command aborted: ID is invalid ({}).", id);
            return null;
        }
        logger.info("Preparing remove_by_id request for element ID: {}", id);
        return RequestsFactory.withLongArg("remove_by_id", id);
    }
}