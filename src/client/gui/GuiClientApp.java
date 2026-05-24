package client.gui;

import client.config.ClientConfig;
import client.context.ClientContext;
import client.gui.auth.AuthDialog;
import client.network.ConnectionManager;
import client.network.PollingService;
import client.utils.RequestsFactory;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.dto.HandshakeRequest;
import shared.dto.UserInfo;

import javax.swing.*;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GuiClientApp {
    public static void main(String[] args) {
        ClientConfig config = ClientConfig.parse(args);
        System.out.println("Config: " + config);
        SwingUtilities.invokeLater(() -> createAndShowGui(config));
        RequestsFactory.setClientId(config.getClientId());
    }

    private static void createAndShowGui(ClientConfig config) {
        ConnectionManager connection = new ConnectionManager();
        if (!connection.connect(config.getHost(), config.getPort())) {
            JOptionPane.showMessageDialog(null,
                    "Не удалось подключиться к серверу " + config.getHost() + ":" + config.getPort(),
                    "Ошибка подключения", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {

            HandshakeRequest handshake = new HandshakeRequest(config.getClientId(), config.getParentClientId());
            connection.sendHandshake(handshake);
            CommandResponse handshakeResponse = connection.readResponse();
            if (!handshakeResponse.success()) {
                JOptionPane.showMessageDialog(null,
                        "Handshake failed",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
                connection.disconnect();
                return;
            }
        }catch (IOException e){
            //TODO
        }
        System.out.println("Handshake successful");

        AuthDialog authDialog = new AuthDialog(null, connection);
        authDialog.setVisible(true);
        if (!authDialog.isSuccess() || authDialog.getLoggedInUser() == null) {
            System.exit(0); // Exit if auth fails or is cancelled
            return;
        }

        UserInfo user = authDialog.getLoggedInUser();
        RequestsFactory.setClientId(config.getClientId());
        RequestsFactory.setUserInfo(user);

        MainWindow mainWindow = new MainWindow(connection, config);
        mainWindow.setUserName(user.name());
        mainWindow.setStatus("Connected as " + user.name());
//        MainWindow mainWindow = new MainWindow(connection);
//
//        LoginDialog loginDialog = new LoginDialog(mainWindow.getFrame(), connection);
//        loginDialog.setVisible(true);


//            // 🔥 Запускаем polling (после успешной авторизации)
//            PollingService polling = new PollingService(
//                    connection,
//                    mainWindow.getTableModel(),
//                    mainWindow,
//                    config.getClientId()
//            );
//            polling.start();

            System.out.println("User logged in: " + user.name());

            String clientId = config.getClientId();
            String parentClientId = config.getParentClientId();
            RequestsFactory.setClientId(clientId);
            boolean isRoot = (parentClientId == null);
            ClientContext context = new ClientContext(
                    clientId,
                    parentClientId,
                    connection,
                    isRoot,
                    user
            );
            mainWindow.setContext(context);
            RequestsFactory.setClientId(config.getClientId());
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
                }
            };
            heartbeatScheduler.scheduleWithFixedDelay(heartbeatTask, 0, 5, TimeUnit.SECONDS);
            PollingService polling = new PollingService(connection, mainWindow.getTableModel(), mainWindow.getCanvasModel(), mainWindow);
            polling.start();
//        } else {
//            mainWindow.setStatus("Login cancelled or failed");
//            System.out.println("Login flow finished without success.");
//        }

        System.out.println("GUI Client started on EDT: " + SwingUtilities.isEventDispatchThread());
    }

}