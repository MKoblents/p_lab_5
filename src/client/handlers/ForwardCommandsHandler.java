package client.handlers;

import client.hierarchy.PeerConnection;
import client.inputWorkers.Invoker;
import client.network.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;

import java.util.concurrent.atomic.AtomicBoolean;

public class ForwardCommandsHandler {
    private static final Logger logger = LoggerFactory.getLogger(ForwardCommandsHandler.class);
    private final ConnectionManager connection;
    private final Invoker invoker;
    private final PeerConnection peerConnection;
    private final AtomicBoolean processingFlag = new AtomicBoolean(false);

    public ForwardCommandsHandler(Invoker invoker,
                                  ConnectionManager connection,
                                  PeerConnection peerConnection) {
        this.invoker = invoker;
        this.connection = connection;
        this.peerConnection = peerConnection;
    }

    public void handleForwardCommand(String message, PeerConnection peerConnection) {
        if (!processingFlag.compareAndSet(false, true)) {
            System.out.println("Another command is being processed, ignoring...");
            return;
        }
        try {
            ForwardMessage parsed = parseForwardMessage(message);
            if (parsed == null ){
                logger.warn("Malformed FORWARD message: {}", message);
                return;
            }
            executeForwardMessage(parsed);
        } finally {
            processingFlag.set(false);
        }
    }

    public void executeForwardMessage(ForwardMessage fm) {
        logger.info("Received forwarded command from {}: {}", fm.fromClientId(), fm.command());
        System.out.println("\n Received command from client " + fm.fromClientId() + ": " + fm.command());

        try {
            CommandRequest request = invoker.runServerCommand(fm.command());
            CommandResponse response = null;
            String resultMsg;
            boolean success;

            if (request != null) {
                connection.sendRequest(request);
                response = connection.readResponse();
                success = response != null && response.success();
                resultMsg = response != null ? response.message() : "No response from server";
            } else {
                success = false;
                resultMsg = "Failed to build request for command: " + fm.command();
            }
            String resultPayload = success && response != null && response.result() != null
                    ? response.result().toString()
                    : resultMsg;

            peerConnection.sendForwardResult(
                    fm.fromClientId(),
                    -1, // TODO
                    fm.requestId(),
                    success,
                    resultPayload
            );
            if (success) {
                System.out.println("\nCommand '" + fm.command() + "' executed successfully");
            } else {
                System.err.println("\nCommand '" + fm.command() + "' failed: " + resultMsg);
            }

        } catch (Exception e) {
            logger.error("Error executing forwarded command", e);
            System.err.println("Failed to execute command: " + e.getMessage());
        }
    }
    public record ForwardExecutionResult(boolean success, String message, Object result) {}
    private record ForwardMessage(String fromClientId, String requestId, String command) {}
    private ForwardMessage parseForwardMessage(String message) {
        String[] parts = message.split(":", 4);
        if (parts.length < 4) {
            return null;
        }
        return new ForwardMessage(parts[1], parts[2], parts[3]);
    }

}
