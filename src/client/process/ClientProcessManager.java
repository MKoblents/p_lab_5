package client.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ClientProcessManager {
    private static final Logger logger = LoggerFactory.getLogger(ClientProcessManager.class);
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
                           String parentClientId) throws IOException {
        logger.info("Spawning child client: {} (parent: {})", childClientId, parentClientId);
//        String jarPath = System.getProperty("java.class.path");
//        if (jarPath == null || jarPath.isEmpty()) {
//            jarPath = "target/p_lab_5-client.jar";
//        }

//        List<String> command = new ArrayList<>();
//        command.add("tmux");
//        command.add("new-window");
//        command.add("-n");
//        command.add("child_" + childClientId);
//        command.add("-P");
//        command.add("java");
//        command.add("-jar");
//        command.add(jarPath);
//        command.add("--host");
//        command.add(host);
//        command.add("--port");
//        command.add(String.valueOf(serverPort));
//        command.add("--client-id");
//        command.add(childClientId);
//        command.add("--parent-id");
//        command.add(parentClientId);
        String jarPath = System.getProperty("java.class.path");
        if (jarPath == null || jarPath.isEmpty()) {
            jarPath = "target/p_lab_5-client-gui.jar";
        }
        System.out.println(jarPath);

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


        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        pb.redirectError(ProcessBuilder.Redirect.DISCARD);
        Process process = pb.start();

        logger.info("Child process started with PID: {}", process.pid());

    }

}