package client.gui;
import client.config.ClientConfig;
import client.context.ClientContext;
import client.gui.auth.AuthDialog;
import client.gui.utils.GuiUtils;
import client.gui.utils.ReconnectScheduler;
import client.network.AsyncNetworkReader;
import client.network.ConnectionManager;
import client.network.PollingService;
import client.utils.RequestsFactory;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.dto.HandshakeRequest;
import shared.dto.UserInfo;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GuiClientApp {
    public static void main(String[] args) {
        ClientConfig config = ClientConfig.parse(args);
        RequestsFactory.setClientId(config.getClientId());
        SwingUtilities.invokeLater(() -> createAndShowGui(config));
    }

    private static void createAndShowGui(ClientConfig config) {
        ConnectionManager connection = new ConnectionManager();
//        if (!connection.connect(config.getHost(), config.getPort())) {
//            GuiUtils.showMessageDialog(null,
//                    "Error",
//                    "Failed to connect to server " + config.getHost() + ":" + config.getPort(),
//                    GuiUtils.MessageType.ERROR);
//            return;
//        }
        AsyncNetworkReader networkReader = new AsyncNetworkReader(
                connection.getSocketChannel(),
                reason -> System.out.println("Network disconnected: " + reason)
        );
        Thread readerThread = new Thread(networkReader, "gui-net-reader");
        readerThread.setDaemon(true);
        readerThread.start();
        ReconnectScheduler reconnectScheduler = new ReconnectScheduler(connection,config,networkReader);
        reconnectScheduler.start();
        try {
            while (!connection.isConnected()){
                reconnectScheduler.attemptConnection();
                Thread.sleep(1000);
            }
            HandshakeRequest handshake = new HandshakeRequest(config.getClientId(), config.getParentClientId());
            connection.sendHandshake(handshake);
            CommandResponse handshakeResponse = connection.readResponse();
            if (!handshakeResponse.success()) {
                GuiUtils.showMessageDialog(null,
                        "Handshake Failed",
                        "Server rejected the handshake. Please check credentials.",
                        GuiUtils.MessageType.ERROR);
                connection.disconnect();
                return;
            }
        } catch (IOException e) {
            GuiUtils.showMessageDialog(null,
                    "Network Error",
                    "Failed during handshake: " + e.getMessage(),
                    GuiUtils.MessageType.ERROR);
            connection.disconnect();
            return;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
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
        mainWindow.setStatus("Connected as " + user.name());
        mainWindow.getFrame().setBounds(authBounds);

        String clientId = config.getClientId();
        String parentClientId = config.getParentClientId();
        boolean isRoot = (parentClientId == null);
        ClientContext context = new ClientContext(
                clientId,
                parentClientId,
                connection,
                isRoot,
                user
        );
        mainWindow.setContext(context);

        ScheduledExecutorService heartbeatScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "heartbeat-scheduler");
            t.setDaemon(true);
            return t;
        });
        Runnable heartbeatTask = () -> {
            try {
                CommandRequest request = new CommandRequest(
                        CommandRequest.CMD_HEARTBEAT,
                        null,
                        UUID.randomUUID().toString().substring(0, 8),
                        context.getClientId(),
                        context.getUserInfo());
                connection.sendRequest(request);
            } catch (IOException | RuntimeException e) {
                // Ignore heartbeat errors to avoid UI spam
            }
        };
        heartbeatScheduler.scheduleWithFixedDelay(heartbeatTask, 0, 5, TimeUnit.SECONDS);

        PollingService polling = new PollingService(connection, mainWindow.getTableModel(), mainWindow.getCanvasModel(), mainWindow, networkReader);
        polling.start();
    }
}