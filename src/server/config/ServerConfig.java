package server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ServerConfig {
    private static final Logger logger = LoggerFactory.getLogger(ServerConfig.class);
    private static final int DEFAULT_PORT = 12345;
    private static final String DEFAULT_DATA_FILE = "collection.xml";
    private static final String DEFAULT_LOG_LEVEL = "INFO";
    private static final Set<String> VALID_LOG_LEVELS = new HashSet<>(Arrays.asList("ERROR", "WARN", "INFO", "DEBUG", "TRACE"));

    private final int port;
    private final String file;
    private final String logLevel;

    public static ServerConfig parse(String[] args) {
        logger.debug("Parsing server startup arguments: {}", Arrays.toString(args));
        int port = DEFAULT_PORT;
        String dataFile = System.getenv("PLAB5") != null
                ? System.getenv("PLAB5")
                : DEFAULT_DATA_FILE;
        String logLevel = DEFAULT_LOG_LEVEL;
        if (System.getenv("PLAB5") != null) {
            logger.info("Using PLAB5 environment variable for data file path: {}", dataFile);
        }
        for (int i = 0; i < args.length; i++) {
            try {
                if (args[i].equals("--port") && i + 1 < args.length) {
                    port = Integer.parseInt(args[++i]);
                    if (port < 1 || port > 65535) {
                        System.err.println("Error: Port must be between 1 and 65535. Using default: " + DEFAULT_PORT);
                        logger.warn("Invalid port value: {}. Reverting to default: {}", port, DEFAULT_PORT);
                        port = DEFAULT_PORT;
                    }
                    logger.debug("Port configured: {}", port);
                } else if (args[i].equals("--file") && i + 1 < args.length) {
                    dataFile = args[++i];
                    logger.debug("Data file configured: {}", dataFile);
                } else if (args[i].equals("--log-level") && i + 1 < args.length) {
                    logLevel = args[++i].toUpperCase();
                    if (!VALID_LOG_LEVELS.contains(logLevel)) {
                        System.err.println("Error: Invalid log level '" + logLevel + "'. Valid options: " + VALID_LOG_LEVELS + ". Using default: " + DEFAULT_LOG_LEVEL);
                        logger.warn("Invalid log level: {}. Reverting to default: {}", logLevel, DEFAULT_LOG_LEVEL);
                        logLevel = DEFAULT_LOG_LEVEL;
                    }
                    logger.debug("Log level configured: {}", logLevel);
                }
            } catch (NumberFormatException e) {
                System.err.println("Error: Invalid numeric value for argument '" + args[i] + "'. Please provide a valid integer.");
                logger.error("Failed to parse numeric argument: {} | Error: {}", args[i], e.getMessage());
                return null;
            }
        }

        ServerConfig config = new ServerConfig(port, dataFile, logLevel);
        logger.info("Server configuration loaded successfully: port={}, file={}, logLevel={}",
                port, dataFile, logLevel);
        return config;
    }

    public ServerConfig(int port, String file, String logLevel) {
        this.port = port;
        this.file = file;
        this.logLevel = logLevel;
    }

    public int getPort() {
        return port;
    }

    public String getFile() {
        return file;
    }

    public String getLogLevel() {
        return logLevel;
    }
}
