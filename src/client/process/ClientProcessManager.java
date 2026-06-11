package client.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientProcessManager {
    private static final Logger logger = LoggerFactory.getLogger(ClientProcessManager.class);

    private final Map<String, Process> runningProcesses = new ConcurrentHashMap<>();

    private final String host;
    private final int serverPort;
    private final String jarPath;

    public ClientProcessManager(String host, int serverPort, String jarPath) {
        this.host = host;
        this.serverPort = serverPort;
        this.jarPath = jarPath != null ? jarPath : "target/p_lab_5-client-gui.jar";
    }

    /**
     * Spawns a new client process and stores its reference.
     * @return true if process started successfully
     */
    public boolean spawnChild(String childClientId, String parentClientId) {
        logger.info("Spawning child client: {} (parent: {})", childClientId, parentClientId);
        System.out.println("in process manager: "+parentClientId);

        try {
            List<String> command = new ArrayList<>();
            command.add("java");
            command.add("-jar");
            command.add(jarPath);
            command.add("--host");
            command.add(host);
            command.add("--port");
            command.add(String.valueOf(serverPort));
            command.add("--client-id");
            command.add(childClientId);
            command.add("--parent-id");
            command.add(parentClientId);
            System.out.println(command);

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            pb.redirectError(ProcessBuilder.Redirect.DISCARD);

            Process process = pb.start();
            runningProcesses.put(childClientId, process);

            logger.info("Child process started with PID: {}", process.pid());
            return true;

        } catch (IOException e) {
            logger.error("Failed to spawn child {}: {}", childClientId, e.getMessage());
            return false;
        }
    }

    /**
     * Terminates a spawned client process.
     * @return true if process was terminated
     */
    public boolean killChild(String clientId) {
        Process process = runningProcesses.remove(clientId);
        if (process == null) {
            logger.warn("No running process found for client: {}", clientId);
            return false;
        }

        logger.info("Terminating process for client: {} (PID: {})", clientId, process.pid());

        // Graceful shutdown first
        process.destroy();

        try {
            // Wait up to 3 seconds for graceful termination
            if (!process.waitFor(3, java.util.concurrent.TimeUnit.SECONDS)) {
                // Force kill if still running
                logger.warn("Process did not terminate gracefully, forcing: {}", clientId);
                process.destroyForcibly();
                process.waitFor(2, java.util.concurrent.TimeUnit.SECONDS);
            }
            logger.info("Process for client {} terminated", clientId);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted while waiting for process termination: {}", clientId);
            process.destroyForcibly();
            return false;
        }
    }

    /**
     * Check if a client process is still running.
     */
    public boolean isRunning(String clientId) {
        Process process = runningProcesses.get(clientId);
        return process != null && process.isAlive();
    }

    /**
     * Clean up all tracked processes (called on server shutdown).
     */
    public void shutdown() {
        logger.info("Shutting down {} tracked client processes", runningProcesses.size());
        for (String clientId : List.copyOf(runningProcesses.keySet())) {
            killChild(clientId);
        }
    }
}