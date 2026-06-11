package client.gui;

import client.config.ClientConfig;
import client.context.ClientContext;
import client.gui.auth.AuthDialog;
import client.gui.utils.ReconnectScheduler;
import client.network.AsyncNetworkReader;
import client.network.ConnectionManager;
import client.network.PollingService;
import client.utils.LocaleManager;
import client.utils.RequestsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shared.dto.CommandResponse;
import shared.dto.HandshakeRequest;
import shared.dto.UserInfo;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class GuiClientApp {

    private static final Logger logger = LoggerFactory.getLogger(GuiClientApp.class);

    private static AsyncNetworkReader networkReader;
    private static Thread readerThread;
    private static ClientContext context;
    private static ConnectionManager connection;
    private static PollingService polling;
    private static ClientConfig config;
    private static MainWindow mainWindow;

    public static void main(String[] args) {
        config = ClientConfig.parse(args);
        RequestsFactory.setClientId(config.getClientId());
        SwingUtilities.invokeLater(() -> createAndShowGui(config));
    }

    private static void createAndShowGui(ClientConfig config) {
        new Thread(() -> {
            SwingUtilities.invokeLater(() -> {
                connection = new ConnectionManager();
                AuthDialog authDialog = new AuthDialog(null, connection);
                authDialog.setVisible(true);

                if (!authDialog.isSuccess() || authDialog.getLoggedInUser() == null) {
                    logger.info("Authentication failed or cancelled by user. Exiting application.");
                    System.exit(0);
                    return;
                }

                Rectangle authBounds = authDialog.getFinalBounds();
                UserInfo user = authDialog.getLoggedInUser();
                RequestsFactory.setUserInfo(user);

                mainWindow = new MainWindow(connection, config, networkReader);
                mainWindow.setUserName(user.name());
                mainWindow.setStatus(LocaleManager.get("status.connected_as") + " " + user.name());
                mainWindow.getFrame().setBounds(authBounds);

                String clientId = config.getClientId();
                String parentClientId = config.getParentClientId();
                boolean isRoot = (parentClientId == null);

                context = new ClientContext(clientId, parentClientId, connection, isRoot, user);
                mainWindow.setContext(context);

                ReconnectScheduler reconnectScheduler = new ReconnectScheduler(connection, config, networkReader);
                reconnectScheduler.start();

                polling = new PollingService(connection, mainWindow.getTableModel(), mainWindow.getCanvasModel(), mainWindow);
                polling.start();

                logger.info("GUI initialization and background services started successfully for client: {}", clientId);
            });
        }, "Connection-Init-Thread").start();
    }

    public static void restartNetworkReader() {
        if (networkReader != null) {
            networkReader.stop();
        }
        if (readerThread != null && readerThread.isAlive()) {
            try {
                readerThread.join(500);
            } catch (InterruptedException e) {
                logger.warn("Interrupted while waiting for old reader thread to die.", e);
                Thread.currentThread().interrupt();
            }
        }

        networkReader = new AsyncNetworkReader(connection.getSocketChannel(),
                reason -> logger.error("Network disconnected: {}", reason));

        readerThread = new Thread(networkReader);
        readerThread.setDaemon(true);
        readerThread.start();
        logger.info("Network reader restarted successfully.");
    }

    public static boolean attemptReconnect() {
        if (mainWindow != null){
            mainWindow.setStatus("Client disconnected from server");
        }
        if (networkReader != null) {
            networkReader.stop();
        }
        if (readerThread != null && readerThread.isAlive()) {
            try {
                readerThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        String host = config.getHost();
        int port = config.getPort();

        if (!connection.connect(host, port)) {
            logger.warn("Failed to connect to {}:{}, will retry...", host, port);
            return false;
        }

        if (polling != null) {
            polling.stop();
        }

        try {
            connection.getSocketChannel().configureBlocking(true);
        } catch (IOException e) {
            logger.error("Failed to configure socket to blocking mode.", e);
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
                logger.info("Handshake successful. Connection established for client: {}", clientId);
                return true;
            } else {
                String errorMsg = handshakeResponse != null ? handshakeResponse.message() : "No response";
                logger.error("Handshake failed: {}", errorMsg);
                connection.disconnect();
                return false;
            }
        } catch (IOException e) {
            logger.error("Handshake failed due to communication error.", e);
            connection.disconnect();
            return false;
        }
    }

    public static AsyncNetworkReader getNetworkReader() {
        return networkReader;
    }

    public static void updateViews() {
        if (polling != null) {
            polling.pollServer();
        } else {
            logger.warn("updateViews() called, but PollingService is not initialized yet.");
        }
    }
}