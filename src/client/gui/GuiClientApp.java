package client.gui;

import client.config.ClientConfig;
import client.network.ConnectionManager;
import client.utils.RequestsFactory;
import shared.dto.CommandResponse;
import shared.dto.HandshakeRequest;
import shared.dto.UserInfo;

import javax.swing.*;
import java.io.IOException;

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
        } else {
            mainWindow.setStatus("Login cancelled or failed");
            System.out.println("Login flow finished without success.");
        }

        System.out.println("GUI Client started on EDT: " + SwingUtilities.isEventDispatchThread());
    }
}