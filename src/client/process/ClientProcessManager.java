package client.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ClientProcessManager {
    private static final Logger logger = LoggerFactory.getLogger(ClientProcessManager.class);
    private final Map<String, Process> childProcesses = new ConcurrentHashMap<>();
    private final String host;
    private final int serverPort;

    public ClientProcessManager(String host, int serverPort) {
        this.host = host;
        this.serverPort = serverPort;
    }

    /**
     * Spawns a new client process with server-assigned clientId.
     */
    public void spawnChild(String childClientId,
                           String parentClientId,
                           int parentPeerPort) throws IOException {
        logger.info("Spawning child client: {} (parent: {})", childClientId, parentClientId);

        List<String> command = new ArrayList<>();
        command.add("tmux");
        command.add("new-window");
        command.add("-n");
        command.add("child_" + childClientId);
        command.add("java");
        command.add("-jar");
        command.add("/home/mkoblents/Yandex.Disk/maria/ITMO/progaaaaaaa/p_lab_5/target/p_lab_5-client.jar");
        command.add("--host");
        command.add(host);
        command.add("--port");
        command.add(String.valueOf(serverPort));
        command.add("--client-id");
        command.add(childClientId);
        command.add("--parent-id");
        command.add(parentClientId);
        command.add("--parent-peer-port");
        command.add(String.valueOf(parentPeerPort));

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        pb.redirectError(ProcessBuilder.Redirect.DISCARD);
        Process process = pb.start();

        childProcesses.put(childClientId, process);
        logger.info("Child process started with PID: {}", process.pid());

        monitorProcess(childClientId, process);
    }

    private void monitorProcess(String clientId, Process process) {
        CompletableFuture.runAsync(() -> {
            try {
                int exitCode = process.waitFor();
                logger.info("Child client {} exited with code {}", clientId, exitCode);
                childProcesses.remove(clientId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Process monitoring interrupted for {}", clientId);
            }
        });
    }

    public void destroyAllChildren() {
        logger.info("Destroying {} child processes", childProcesses.size());
        for (Map.Entry<String, Process> entry : childProcesses.entrySet()) {
            try {
                logger.info("Destroying child process: {}", entry.getKey());
                entry.getValue().destroy();
                if (!entry.getValue().waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    entry.getValue().destroyForcibly();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Interrupted while destroying child {}", entry.getKey());
            }
        }
        childProcesses.clear();
    }

    public boolean isChildAlive(String clientId) {
        Process process = childProcesses.get(clientId);
        return process != null && process.isAlive();
    }
}