package client.config;

public class ClientConfig {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 12345;
    private static final String DEFAULT_LOG_LEVEL = "INFO";
    private String host;
    private int port;
    private String logLevel;

    public ClientConfig(String host, int port, String loglevel){
    this.host = host;
    this.port = port;
    this.logLevel = loglevel;
}

    public static ClientConfig parse(String[] args) {
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;
        String logLevel = DEFAULT_LOG_LEVEL;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--host") && i + 1 < args.length) {
                host = args[++i];
            } else if (args[i].equals("--port") && i + 1 < args.length) {
                port = Integer.parseInt(args[++i]);
            } else if (args[i].equals("--log-level") && i + 1 < args.length) {
                logLevel = args[++i].toUpperCase();
            }
        }
        return new ClientConfig(host, port, logLevel);
    }
    public String getHost() { return host; }
    public int getPort() { return port; }
    public String getLogLevel() { return logLevel; }

}
