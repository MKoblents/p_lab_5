package server.console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.manager.ClientRegistry;
import server.manager.CollectionService;
import server.outputWorkers.CollectionSaver;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConsoleHandler {
    private static final Logger logger = LoggerFactory.getLogger(ConsoleHandler.class);
    private CollectionService collectionService;
    private CollectionSaver collectionSaver;
    private String dataFile;
    private AtomicBoolean running;
    private ClientRegistry clientRegistry;
    public ConsoleHandler(CollectionService collectionService,
                          CollectionSaver collectionSaver,
                          String dataFile,
                          AtomicBoolean running,
                          ClientRegistry clientRegistry){
        this.collectionService = collectionService;
        this.collectionSaver = collectionSaver;
        this.dataFile = dataFile;
        this.running = running;
        this.clientRegistry = clientRegistry;

    }
    public void handleConsoleInput(String command) {
        switch (command) {
            case "save":
                logger.info("Manual save triggered from console");
                try {
                    collectionSaver.save(collectionService.getSpaceMarines(), dataFile);
                    logger.info("Collection saved to {}", dataFile);
                    System.out.println(" Collection saved successfully!");
                } catch (Exception e) {
                    logger.error("Save failed: {}", e.getMessage(), e);
                    System.err.println(" Save failed: " + e.getMessage());
                }
                break;

            case "exit":
                logger.info("Exit command received from console");
                System.out.println("Shutting down server...");
                running.set(false);
                handleConsoleInput("save");
                System.exit(0);
                return ;
            case "help":
                System.out.println("Available server commands:");
                System.out.println("  save - Save collection to file");
                System.out.println("  exit - Stop server");
                System.out.println("  help - Show this help");
                System.out.println("  status - Clients status");
                break;
            case "status":
                logger.info("Client status requested from console");
                clientRegistry.printStatusToConsole();
                break;
            case "":
                break;
            default:
                logger.warn("Unknown server command: {}", command);
                System.err.println("Unknown command: " + command + ". Type 'help' for list.");
        }
    }
}
