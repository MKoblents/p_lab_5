package client.command;

import client.inputWorkers.InputManager;
import client.scripts.ScriptRunner;
import client.utils.SideFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shared.dto.CommandRequest;

import java.io.IOException;

public class ExecuteScript implements ClientCommand {
    private static final Logger logger = LoggerFactory.getLogger(ExecuteScript.class);
    private final InputManager inputManager;
    private final ScriptRunner runner;

    public ExecuteScript(InputManager inputManager, ScriptRunner runner) {
        this.inputManager = inputManager;
        this.runner = runner;
    }
    @Override
    public CommandRequest execute(SideFlag flag) {
        String path;

        if (flag == SideFlag.FORWARDED) {
            try {
                System.out.println("Please, enter the script name.");
                path = inputManager.getNewString();
            } catch (IOException e) {
                logger.error("Failed to read script path from input: {}", e.getMessage());
                System.err.println("Error: Could not read script path. Please check input stream.");
                return null;
            }
        } else {
            path = inputManager.getLastPath();
        }

        if (path == null || path.trim().isEmpty()) {
            System.err.println("Error: Script path is not specified. Use 'execute_script <path>' to provide a valid file path.");
            logger.debug("ExecuteScript command aborted: path is null or empty.");
            return null;
        }
        logger.info("Executing script: {}", path);
        System.out.println("Running script: " + path);
        runner.executeScript(path);
        logger.debug("Script execution initiated: {}", path);

        return null;
    }
}