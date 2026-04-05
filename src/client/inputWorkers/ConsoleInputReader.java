package client.inputWorkers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConsoleInputReader implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ConsoleInputReader.class);
    private final InputManager inputManager;
    private final ConcurrentLinkedQueue<String> commandQueue = new ConcurrentLinkedQueue<>();
    private volatile boolean running = true;

    public ConsoleInputReader(InputManager inputManager) {
        this.inputManager = inputManager;
    }

    @Override
    public void run() {
        logger.debug("Console input reader started");
        while (running) {
            try {
                System.out.print("> ");
                String command = inputManager.parseCommand();
                if (command != null && !command.isEmpty()) {
                    commandQueue.offer(command);
                    logger.debug("Queued command: {}", command);
                }
            } catch (IOException e) {
                logger.error("Error reading console input: {}", e.getMessage());
                break;
            } catch (Exception e) {
                logger.error("Unexpected error in console reader: {}", e.getMessage());
            }
        }
    }

    public String pollCommand() {
        return commandQueue.poll();
    }

    public void stop() {
        running = false;
    }
}