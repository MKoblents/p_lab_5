package client.gui;

import client.config.ClientConfig;
import client.context.ClientContext;
import client.network.ConnectionManager;
import client.utils.RequestsFactory;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.dto.HandshakeRequest;
import shared.dto.UserInfo;

import javax.swing.*;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
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

        MainWindow mainWindow = new MainWindow();

        LoginDialog loginDialog = new LoginDialog(mainWindow.getFrame(), connection);
        loginDialog.setVisible(true);

        if (loginDialog.isSuccess() && loginDialog.getLoggedInUser() != null) {
            UserInfo user = loginDialog.getLoggedInUser();

            RequestsFactory.setClientId(config.getClientId());
            RequestsFactory.setUserInfo(user);

            mainWindow.setUserName(user.name());
            mainWindow.setStatus("Connected as " + user.name());

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
        } else {
            mainWindow.setStatus("Login cancelled or failed");
            System.out.println("Login flow finished without success.");
        }

        System.out.println("GUI Client started on EDT: " + SwingUtilities.isEventDispatchThread());
    }
}