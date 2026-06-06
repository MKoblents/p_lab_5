package client.network;

import client.gui.GuiClientApp; // <-- Импортируем GuiClientApp
import client.gui.MainWindow;
import client.gui.window.SpaceMarineCanvas;
import client.gui.window.SpaceMarineTable;
import client.utils.RequestsFactory;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.models.SpaceMarine;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PollingService {
    private final ConnectionManager connection;
    private final SpaceMarineTable tableModel;
    private final SpaceMarineCanvas canvasModel;
    private final MainWindow mainWindow;
    private ScheduledExecutorService scheduler;
    private final long POLL_INTERVAL_MS = 2000;

    public PollingService(ConnectionManager connection, SpaceMarineTable tableModel, SpaceMarineCanvas canvasModel, MainWindow mainWindow) {
        this.connection = connection;
        this.tableModel = tableModel;
        this.mainWindow = mainWindow;
        this.canvasModel = canvasModel;
    }

    public void start() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "polling-thread");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(this::pollServer, 0, POLL_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    private AsyncNetworkReader getReader() {
        return GuiClientApp.getNetworkReader();
    }

    public void pollServer() {
        try {
            CommandRequest request = RequestsFactory.createSimple("show");
            AsyncNetworkReader reader = getReader();
            CompletableFuture<CommandResponse> future = reader.registerRequest(request.requestId(), 1500);
            connection.sendRequest(request);
            CommandResponse response = future.get(1500, TimeUnit.MILLISECONDS);

            if (response != null && response.success() && response.result() instanceof List<?>) {
                List<SpaceMarine> marines = (List<SpaceMarine>) response.result();
                SwingUtilities.invokeLater(() -> {
                    tableModel.setData(marines);
                    canvasModel.setMarines(marines);
                    mainWindow.setStatus("Синхронизировано: " + marines.size() + " объектов");
                });
            }
        } catch (TimeoutException e) {
        } catch (Exception e) {
            System.err.println("Ошибка polling: " + e.getMessage());
        }
    }

    public void stop() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }
}