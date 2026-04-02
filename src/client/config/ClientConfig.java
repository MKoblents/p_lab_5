package client.config;

import java.util.UUID;

public class ClientConfig {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 12345;
    private static final String DEFAULT_LOG_LEVEL = "INFO";
    private String host;
    private int port;
    private String logLevel;
    private String clientId;
    private String parentClientId;
    private int parentPeerPort;

    public ClientConfig(String host, int port, String loglevel, String clientId, String parentClientId, int parentPeerPort){
    this.host = host;
    this.port = port;
    this.logLevel = loglevel;
    this.clientId = clientId;
    this.parentClientId = parentClientId;
    this.parentPeerPort = parentPeerPort;
}

    public static ClientConfig parse(String[] args) {
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;
        String logLevel = DEFAULT_LOG_LEVEL;
        String clientId = null;
        String parentClientId = null;
        int parentPeerPort = -1;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--host") && i + 1 < args.length) {
                host = args[++i];
            } else if (args[i].equals("--port") && i + 1 < args.length) {
                port = Integer.parseInt(args[++i]);
            } else if (args[i].equals("--log-level") && i + 1 < args.length) {
                logLevel = args[++i].toUpperCase();
            }
            else if (args[i].equals("--client-id") && i + 1 < args.length) {
                clientId = args[++i];
            }
            else if (args[i].equals("--parent-id") && i + 1 < args.length) {
                parentClientId = args[++i];
            }
            else if (args[i].equals("--parent-peer-port") && i + 1 < args.length) {
                parentPeerPort = Integer.parseInt(args[++i]);
            }
        }
        if (clientId == null) {
            clientId = UUID.randomUUID().toString().substring(0, 8);
        }
        return new ClientConfig(host, port, logLevel,clientId, parentClientId, parentPeerPort );
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
