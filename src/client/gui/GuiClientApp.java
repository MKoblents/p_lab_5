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
        mainWindow.setStatus("Connected as " + user.name());
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

//        ScheduledExecutorService heartbeatScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
//            Thread t = new Thread(r, "heartbeat-scheduler");
//            t.setDaemon(true);
//            return t;
//        });
//        Runnable heartbeatTask = () -> {
//            try {
//                CommandRequest request = new CommandRequest(
//                        CommandRequest.CMD_HEARTBEAT,
//                        null,
//                        UUID.randomUUID().toString().substring(0, 8),
//                        context.getClientId(),
//                        context.getUserInfo());
//                connection.sendRequest(request);
//            } catch (IOException | RuntimeException e) {
//                // Ignore heartbeat errors to avoid UI spam
//            }
//        };
//        heartbeatScheduler.scheduleWithFixedDelay(heartbeatTask, 0, 5, TimeUnit.SECONDS);

        polling = new PollingService(connection, mainWindow.getTableModel(), mainWindow.getCanvasModel(), mainWindow, networkReader);
        System.out.println("polling started");
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
    // В GuiClientApp.attemptReconnect()
    public static boolean attemptReconnect(){
        // 1. Остановить старого читателя
        if (networkReader != null) networkReader.stop();
        if (readerThread != null && readerThread.isAlive()) {
            try { readerThread.join(1000); } catch (InterruptedException ignored) {}
        }

        // 2. Переподключиться
        String host = config.getHost();
        int port = config.getPort();
        if (!connection.connect(host, port)) {
            return false;
        }

        if (polling != null) {
            polling.stop();
        }

        // 3. ⚡ ВАЖНО: Переключить канал в блокирующий режим для хендшейка
        try {
            connection.getSocketChannel().configureBlocking(true);
        } catch (IOException e) {
            return false;
        }

        try {
            // 4. Отправить хендшейк
            String clientId = (context != null) ? context.getClientId() : config.getClientId();
            String parentClientId = (context != null) ? context.getParentClientId() : null;
            HandshakeRequest handshake = new HandshakeRequest(clientId, parentClientId);
            connection.sendHandshake(handshake);

            // 5. ⚡ Прочитать ответ БЛОКИРУЮЩЕ
            // ВАЖНО: AsyncNetworkReader НЕ запущен, поэтому байты не будут перехвачены фоновым потоком!
            CommandResponse handshakeResponse = connection.readResponse();

            if (handshakeResponse != null && handshakeResponse.success()) {
                // 6. ⚡ Вернуть канал в неблокирующий режим для асинхронного чтения
                connection.getSocketChannel().configureBlocking(false);

                // 7. Теперь безопасно запускаем асинхронный читатель
                restartNetworkReader();

                if (polling != null) {
                    polling.setNetworkReader(networkReader);
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
}