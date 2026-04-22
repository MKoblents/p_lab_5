package client.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ClientConfig {
    private static final Logger logger = LoggerFactory.getLogger(ClientConfig.class);
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 12345;
    private static final String DEFAULT_LOG_LEVEL = "INFO";
    private static final Set<String> VALID_LOG_LEVELS = new HashSet<>(Arrays.asList("ERROR", "WARN", "INFO", "DEBUG", "TRACE"));

    private final String host;
    private final int port;
    private final String logLevel;
    private final String clientId;
    private final String parentClientId;
    private final int parentPeerPort;
    public ClientConfig(String host, int port, String logLevel, String clientId, String parentClientId, int parentPeerPort) {
        this.host = host;
        this.port = port;
        this.logLevel = logLevel;
        this.clientId = clientId;
        this.parentClientId = parentClientId;
        this.parentPeerPort = parentPeerPort;
    }

    public static ClientConfig parse(String[] args) {
        logger.debug("Parsing command-line arguments: {}", Arrays.toString(args));
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;
        String logLevel = DEFAULT_LOG_LEVEL;
        String clientId = null;
        String parentClientId = null;
        int parentPeerPort = -1;
        for (int i = 0; i < args.length; i++) {
            try {
                if (args[i].equals("--host") && i + 1 < args.length) {
                    host = args[++i];
                    logger.debug("Host set to: {}", host);
                } else if (args[i].equals("--port") && i + 1 < args.length) {
                    port = Integer.parseInt(args[++i]);
                    if (port < 1 || port > 65535) {
                        System.err.println("Error: Port must be between 1 and 65535. Using default: " + DEFAULT_PORT);
                        logger.warn("Invalid port value: {}. Reverting to default: {}", port, DEFAULT_PORT);
                        port = DEFAULT_PORT;
                    }
                    logger.debug("Port set to: {}", port);
                } else if (args[i].equals("--log-level") && i + 1 < args.length) {
                    logLevel = args[++i].toUpperCase();
                    if (!VALID_LOG_LEVELS.contains(logLevel)) {
                        System.err.println("Error: Invalid log level '" + logLevel + "'. Valid options: " + VALID_LOG_LEVELS + ". Using default: " + DEFAULT_LOG_LEVEL);
                        logger.warn("Invalid log level: {}. Reverting to default: {}", logLevel, DEFAULT_LOG_LEVEL);
                        logLevel = DEFAULT_LOG_LEVEL;
                    }
                    logger.debug("Log level set to: {}", logLevel);
                } else if (args[i].equals("--client-id") && i + 1 < args.length) {
                    clientId = args[++i];
                    logger.debug("Client ID set to: {}", clientId);
                } else if (args[i].equals("--parent-id") && i + 1 < args.length) {
                    parentClientId = args[++i];
                    logger.debug("Parent client ID set to: {}", parentClientId);
                } else if (args[i].equals("--parent-peer-port") && i + 1 < args.length) {
                    parentPeerPort = Integer.parseInt(args[++i]);
                    logger.debug("Parent peer port set to: {}", parentPeerPort);
                }
            } catch (NumberFormatException e) {
                System.err.println("Error: Invalid numeric value for argument '" + args[i] + "'. Please provide a valid integer.");
                logger.error("Failed to parse numeric argument: {} | Error: {}", args[i], e.getMessage());
                return null;
            }
        }
        if (clientId == null) {
            clientId = UUID.randomUUID().toString().substring(0, 8);
            logger.debug("Auto-generated client ID: {}", clientId);
        }
        ClientConfig config = new ClientConfig(host, port, logLevel, clientId, parentClientId, parentPeerPort);
        logger.info("Configuration loaded: host={}, port={}, logLevel={}, clientId={}",
                host, port, logLevel, clientId);
        return config;
    }
    public String getHost() { return host; }
    public int getPort() { return port; }
    public String getLogLevel() { return logLevel; }

    public String getClientId() {
        return clientId;
    }

    public String getParentClientId() {
        return parentClientId;
    }

    public int getParentPeerPort() {
        return parentPeerPort;
    }
}
