package client.command;

import client.inputWorkers.InputManager;
import client.utils.RequestsFactory;
import client.utils.SideFlag;
import client.utils.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shared.dto.CommandRequest;
import shared.models.SpaceMarine;

import java.io.IOException;

public class InsertAt implements ClientCommand {
    private static final Logger logger = LoggerFactory.getLogger(InsertAt.class);
    private final InputManager inputManager;
    public InsertAt(InputManager inputManager) {
        this.inputManager = inputManager;
    }
    @Override
    public CommandRequest execute(SideFlag flag) {
        int index;
        if (flag == SideFlag.FORWARDED) {
            System.out.println("Please enter the index position for insertion:");
            try {
                index = inputManager.getNewInt();
            } catch (IOException e) {
                logger.error("Failed to read index from input: {}", e.getMessage());
                System.err.println("Error: Could not read index. Please check your input stream.");
                return null;
            }
        } else {
            index = inputManager.getLastInt();
        }
        if (index < 0) {
            System.err.println("Error: Valid non-negative index required for insert_at command.");
            logger.debug("InsertAt command aborted: index is invalid ({}).", index);
            return null;
        }
        System.out.println("Please provide the SpaceMarine:");
        SpaceMarine marine = inputManager.getInputSpaceMarine();
        if (marine == null) {
            System.err.println("Error: Failed to parse SpaceMarine from input.");
            logger.debug("InsertAt command aborted: SpaceMarine parsing failed.");
            return null;
        }
        logger.debug("Validating SpaceMarine before insert_at request...");
        Validator.spaceMarineValidate(marine);
        logger.info("Preparing insert_at request: index={}, marineId={}", index, marine.getId());
        return RequestsFactory.createIdMarine("insert_at", index, marine);
    }
}