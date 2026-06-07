package client.gui;
import client.config.ClientConfig;
import client.context.ClientContext;
import client.gui.auth.AuthDialog;
import client.gui.utils.GuiUtils;
import client.gui.utils.ReconnectScheduler;
import client.network.AsyncNetworkReader;
import client.network.ConnectionManager;
import client.network.PollingService;
import client.utils.LocaleManager;
import client.utils.RequestsFactory;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.dto.HandshakeRequest;
import shared.dto.UserInfo;
import shared.enums.DisconnectReason;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GuiClientApp {
    private static AsyncNetworkReader networkReader;
    private static Thread readerThread;
    private static ClientContext context;
    private static ConnectionManager connection;
    private static PollingService polling;
    private static ClientConfig config;
    public static void main(String[] args) {
        config = ClientConfig.parse(args);
        RequestsFactory.setClientId(config.getClientId());
        SwingUtilities.invokeLater(() -> createAndShowGui(config));
    }

    private static void createAndShowGui(ClientConfig config) {
        GuiClientApp.config = config;
        connection = new ConnectionManager();
        while (!connection.isConnected()) {
            try {
                if (attemptReconnect()) {
                    break;
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }



        AuthDialog authDialog = new AuthDialog(null, connection);
        authDialog.setVisible(true);
        if (!authDialog.isSuccess() || authDialog.getLoggedInUser() == null) {
            System.exit(0);
            return;
        }
        Rectangle authBounds = authDialog.getFinalBounds();
        UserInfo user = authDialog.getLoggedInUser();
        RequestsFactory.setClientId(config.getClientId());
        RequestsFactory.setUserInfo(user);

        MainWindow mainWindow = new MainWindow(connection, config, networkReader);
        mainWindow.setUserName(user.name());
        mainWindow.setStatus(LocaleManager.get("status.connected_as") + user.name());
        mainWindow.getFrame().setBounds(authBounds);

        String clientId = config.getClientId();
        String parentClientId = config.getParentClientId();
        boolean isRoot = (parentClientId == null);
        context = new ClientContext(
                clientId,
                parentClientId,
                connection,
                isRoot,
                user
        );
        mainWindow.setContext(context);
        ReconnectScheduler reconnectScheduler = new ReconnectScheduler(connection,config,networkReader);
        reconnectScheduler.start();

        polling = new PollingService(connection, mainWindow.getTableModel(), mainWindow.getCanvasModel(), mainWindow);
        polling.start();
    }
    public static void restartNetworkReader() {
        if (networkReader != null) networkReader.stop();
        if (readerThread != null && readerThread.isAlive()) {
            try { readerThread.join(500); } catch (InterruptedException ignored) {}
        }

        networkReader = new AsyncNetworkReader(connection.getSocketChannel(),
                reason -> System.out.println("Network disconnected: " + reason));

        readerThread = new Thread(networkReader);
        readerThread.setDaemon(true);
        readerThread.start();
        System.out.println("Network reader restarted successfully.");
    }
    public static boolean attemptReconnect(){
        if (networkReader != null) networkReader.stop();
        if (readerThread != null && readerThread.isAlive()) {
            try { readerThread.join(1000); } catch (InterruptedException ignored) {}
        }

        String host = config.getHost();
        int port = config.getPort();
        if (!connection.connect(host, port)) {
            return false;
        }

        if (polling != null) {
            polling.stop();
        }

        try {
            connection.getSocketChannel().configureBlocking(true);
        } catch (IOException e) {
            return false;
        }

        try {
            String clientId = (context != null) ? context.getClientId() : config.getClientId();

            String parentClientId = (context != null) ? context.getParentClientId() : config.getParentClientId();
            HandshakeRequest handshake = new HandshakeRequest(clientId, parentClientId);
            connection.sendHandshake(handshake);

            CommandResponse handshakeResponse = connection.readResponse();

            if (handshakeResponse != null && handshakeResponse.success()) {
                connection.getSocketChannel().configureBlocking(false);
                restartNetworkReader();

                if (polling != null) {
                    polling.start();
                }
                return true;
            } else {
                String errorMsg = handshakeResponse != null ? handshakeResponse.message() : "No response";
                System.err.println("Handshake failed: " + errorMsg);
                connection.disconnect();
                return false;
            }
        } catch (IOException e) {
            System.err.println("Handshake failed due to communication error: " + e.getMessage());
            connection.disconnect();
            return false;
        }
    }

    public static AsyncNetworkReader getNetworkReader() {
        return  networkReader;
    }

    public static void updateViews() {
        polling.pollServer();
    }
}