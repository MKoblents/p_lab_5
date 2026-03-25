package server.config;

public class ServerConfig {
    private static int DEFAULT_PORT = 12345;
    private static String DEFAULT_DATA_FILE = "collection.xml";
    private static String DEFAULT_LOG_LEVEL = "INFO";
    private int port;
    private  String file;
    private String logLevel;
    public static ServerConfig parse(String[] args) {
        int port = DEFAULT_PORT;
        String dataFile = System.getenv("PLAB5") != null
                ? System.getenv("PLAB5")
                : DEFAULT_DATA_FILE;
        String logLevel = DEFAULT_LOG_LEVEL;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--port") && i + 1 < args.length) {
                port = Integer.parseInt(args[++i]);
            } else if (args[i].equals("--file") && i + 1 < args.length) {
                dataFile = args[++i];
            } else if (args[i].equals("--log-level") && i + 1 < args.length) {
                logLevel = args[++i].toUpperCase();
            }
        }
        return new ServerConfig(port, dataFile, logLevel);
    }
    public ServerConfig(int port, String file, String logLevel){
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
