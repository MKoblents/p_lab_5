package client.gui.utils;

import client.config.ClientConfig;
import client.gui.GuiClientApp;
import client.network.AsyncNetworkReader;
import client.network.ConnectionManager;
import client.gui.MainWindow;
import javax.swing.SwingUtilities;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReconnectScheduler {
    private final ScheduledExecutorService scheduler;
    private final ConnectionManager connection;
    private final ClientConfig config;
    private AsyncNetworkReader reader;

    public ReconnectScheduler(ConnectionManager connection, ClientConfig config, AsyncNetworkReader reader) {
        this.connection = connection;
        this.config = config;
        this.reader = reader;

        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "gui-reconnect-scheduler");
            t.setDaemon(true);
            return t;
        });
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::attemptConnection, 0, 2, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdownNow();
    }

    public void attemptConnection() {
        if (connection.isConnected()) {
            return;
        }

        try {
            GuiClientApp.attemptReconnect();

        } catch (Exception e) {
        }
    }
}