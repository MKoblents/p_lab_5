package client.command;

import client.inputWorkers.InputManager;
import client.utils.RequestsFactory;
import client.utils.SideFlag;
import client.utils.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shared.dto.CommandRequest;
import shared.models.SpaceMarine;

public class RemoveGreater implements ClientCommand {
    private static final Logger logger = LoggerFactory.getLogger(RemoveGreater.class);
    private final InputManager inputManager;

    public RemoveGreater(InputManager inputManager) {
        this.inputManager = inputManager;
    }

    @Override
    public CommandRequest execute(SideFlag flag) {
        logger.debug("Executing remove_greater command with flag: {}", flag);
        SpaceMarine spaceMarine = inputManager.getInputSpaceMarine();
        if (spaceMarine == null) {
            System.err.println("Error: Failed to parse SpaceMarine from input. Please ensure valid format.");
            logger.debug("RemoveGreater command aborted: SpaceMarine parsing failed.");
            return null;
        }
        logger.debug("Validating SpaceMarine before remove_greater request...");
        Validator.spaceMarineValidate(spaceMarine);
        logger.info("Preparing remove_greater request with marine ID: {}", spaceMarine.getId());
        return RequestsFactory.withMarine("remove_greater", spaceMarine);
    }
}