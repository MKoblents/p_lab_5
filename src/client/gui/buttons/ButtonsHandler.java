package client.gui.buttons;
import client.command.SpawnClient;
import client.gui.MainWindow;
import client.gui.auth.AuthDialog;
import client.gui.utils.GuiUtils;
import client.handlers.ResponseHandler;
import client.inputWorkers.CommandParser;
import client.inputWorkers.InputManager;
import client.inputWorkers.Invoker;
import client.network.AsyncNetworkReader;
import client.network.ConnectionManager;
import client.process.ClientProcessManager;
import client.scripts.FileManager;
import client.scripts.ScriptRunner;
import client.utils.RequestsFactory;
import client.utils.SideFlag;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.dto.ForwardCommandObject;
import shared.dto.UserInfo;
import shared.models.SpaceMarine;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ButtonsHandler {
    private final ConnectionManager connection;
    private final MainWindow mainWindow;
    private final AsyncNetworkReader networkReader;
    private final ClientProcessManager processManager;
    private SpawnClient spawnClient;

    public ButtonsHandler(ConnectionManager connection, MainWindow mainWindow) {
        this.connection = connection;
        this.mainWindow = mainWindow;
        this.networkReader = mainWindow.getNetworkReader();
        this.processManager = new ClientProcessManager(mainWindow.getConfig().getHost(), mainWindow.getConfig().getPort(), null);
    }

    public void handleAdd() {
        SpaceMarineInputDialog dialog = new SpaceMarineInputDialog(mainWindow.getFrame());
        dialog.setVisible(true);
        SpaceMarine marine = dialog.getSpaceMarine();
        if (marine != null) handleRequest(RequestsFactory.withMarine("add", marine), "Space Marine added successfully!");
    }

    public void handleRemove() {
        String username = mainWindow.getContext().getUserInfo().name();
        RemoveSpaceMarineDialog dialog = new RemoveSpaceMarineDialog(mainWindow.getFrame(), mainWindow.getTableModel(), username);
        dialog.setVisible(true);
        if (dialog.isSuccess()) {
            SpaceMarine marineToRemove = dialog.getSelectedSpaceMarine();
            handleRequest(RequestsFactory.withLongArg("remove_by_id", marineToRemove.getId()), "Space Marine deleted successfully!");
        }
    }

    public void handleExecuteScript() {
        ExecuteScriptDialog executeScriptDialog = new ExecuteScriptDialog(mainWindow.getFrame());
        executeScriptDialog.setVisible(true);
        File scriptFile = executeScriptDialog.getSelectedFile();
        if (scriptFile == null) return;

        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                InputManager inputManager = new InputManager(null, new CommandParser());
                ScriptRunner scriptRunner = new ScriptRunner(inputManager, connection, new ResponseHandler(mainWindow.getContext()), null, new FileManager());
                Invoker invoker = new Invoker(inputManager, mainWindow.getContext(), connection, processManager, scriptRunner);
                scriptRunner.setInvoker(invoker);
                boolean success = scriptRunner.executeScript(scriptFile.getPath());
                publish(success ? "Script completed successfully\n" + scriptRunner.getRes() : "Script completed with errors");
                return null;
            }
            @Override
            protected void process(List<String> chunks) {
                String message = chunks.get(chunks.size() - 1);
                GuiUtils.showMessageDialog(mainWindow.getFrame(), "Script Result", message, GuiUtils.MessageType.INFO);
            }
        };
        worker.execute();
    }

    public void handleClear() {
        boolean confirm = GuiUtils.showConfirmDialog(null,
                "Are you sure you want to clear your collection?",
                "Confirm Action");
        if (!confirm) return;
        handleRequest(RequestsFactory.createSimple("clear"), "Collection cleared successfully!");
    }

    public void handleUpdate() {
        String username = mainWindow.getContext().getUserInfo().name();
        SpaceMarineUpdateDialog updateDialog = new SpaceMarineUpdateDialog(mainWindow.getFrame(), mainWindow.getTableModel(), username);
        updateDialog.setVisible(true);
        SpaceMarine updateMarine = updateDialog.getUpdatedSpaceMarine();
        if (updateMarine != null) handleRequest(RequestsFactory.createTwoArgs("update", updateMarine.getId(), updateMarine), "SpaceMarine updated successfully!");
    }

    public void handleInfo() { showSimple("info"); }
    public void handleHelp() { showSimple("help"); }

    private void showSimple(String commandKey) {
        CommandRequest request = RequestsFactory.createSimple(commandKey);
        try {
            connection.sendRequest(request);
            CommandResponse response = connection.readResponse();
            if (response != null && response.result() instanceof String result) {
                GuiUtils.showMessageDialog(mainWindow.getFrame(), "Success", result, GuiUtils.MessageType.INFO);
            } else {
                GuiUtils.showMessageDialog(mainWindow.getFrame(), "Info", "Command executed successfully.", GuiUtils.MessageType.INFO);
            }
        } catch (IOException ex) {
            GuiUtils.showMessageDialog(mainWindow.getFrame(), "Error", "Network error: " + ex.getMessage(), GuiUtils.MessageType.ERROR);
        }
    }

    public void handleSpawn() throws IOException {
        if (spawnClient == null) spawnClient = new SpawnClient(mainWindow.getContext(), processManager);
        CommandRequest request = spawnClient.execute(SideFlag.SELF);
        CommandResponse response = handleRequest(request, "New window opened!");
        if (response != null) spawnClient.handleResponse(response, mainWindow.getContext());
    }

    public void handleKill() {
        List<String> availableClients = mainWindow.getContext().getChildClientIds();
        if (availableClients.isEmpty()) {
            GuiUtils.showMessageDialog(mainWindow.getFrame(), "Info", "No active child clients found.", GuiUtils.MessageType.INFO);
            return;
        }
        KillClientDialog dialog = new KillClientDialog(mainWindow.getFrame(), availableClients);
        dialog.setOnKillRequested(this::sendKillCommand);
        dialog.setVisible(true);
    }

    private void sendKillCommand(String clientId) {
        CommandRequest request = RequestsFactory.withStringArg("kill_client", clientId);
        CommandResponse response = handleRequest(request, "Client terminated");
        if (response != null && response.success()) {
            processManager.killChild(clientId);
            mainWindow.getContext().removeChild(clientId);
            GuiUtils.showMessageDialog(mainWindow.getFrame(), "Success", "Client " + clientId + " terminated successfully.", GuiUtils.MessageType.INFO);
        } else {
            GuiUtils.showMessageDialog(mainWindow.getFrame(), "Error", "Failed to terminate client: " + (response != null ? response.message() : "Unknown error"), GuiUtils.MessageType.ERROR);
        }
    }

    public void handleLogOut() {
        mainWindow.getFrame().setVisible(false);
        AuthDialog authDialog = new AuthDialog(mainWindow.getFrame(), connection);
        authDialog.setVisible(true);
        if (!authDialog.isSuccess() || authDialog.getLoggedInUser() == null) {
            System.exit(0);
            return;
        }
        UserInfo user = authDialog.getLoggedInUser();
        RequestsFactory.setClientId(mainWindow.getConfig().getClientId());
        RequestsFactory.setUserInfo(user);
        mainWindow.setUserName(user.name());
        mainWindow.getFrame().setVisible(true);
    }

    public void handleForwardCommand() {
        List<String> childClients = mainWindow.getContext().getChildClientIds();
        if (childClients.isEmpty()) {
            GuiUtils.showMessageDialog(mainWindow.getFrame(), "No Clients", "No child clients available for forwarding.", GuiUtils.MessageType.WARNING);
            return;
        }
        ForwardCommandDialog dialog = new ForwardCommandDialog(mainWindow.getFrame(), childClients);
        dialog.setVisible(true);
        if (dialog.isConfirmed() && dialog.getResult() != null) {
            ForwardCommandObject fco = dialog.getResult();
            String parentId = mainWindow.getContext().getClientId();
            ForwardCommandObject finalFco = new ForwardCommandObject(parentId, fco.childId(), fco.commandKey());
            CommandRequest request = new CommandRequest("forward_command", finalFco, UUID.randomUUID().toString().substring(0, 8), parentId, mainWindow.getContext().getUserInfo());
            try {
                connection.sendRequest(request);
                GuiUtils.showMessageDialog(mainWindow.getFrame(), "Success", "Command forwarded to " + finalFco.childId(), GuiUtils.MessageType.INFO);
            } catch (IOException ex) {
                GuiUtils.showMessageDialog(mainWindow.getFrame(), "Error", "Failed to forward command: " + ex.getMessage(), GuiUtils.MessageType.ERROR);
            }
        }
    }

    public CommandResponse handleRequest(CommandRequest request, String successMessage) {
        try {
            String expectedRequestId = request.requestId();
            connection.sendRequest(request);
            CommandResponse response = null;
            long startTime = System.currentTimeMillis();
            long timeout = 5000;
            while (response == null && (System.currentTimeMillis() - startTime) < timeout) {
                CommandResponse candidate = networkReader.getResponseQueue().poll();
                if (candidate != null && expectedRequestId.equals(candidate.requestId())) {
                    response = candidate;
                    break;
                }
                Thread.sleep(50);
            }
            if (response == null) {
                GuiUtils.showMessageDialog(mainWindow.getFrame(), "Error", "Timeout waiting for response", GuiUtils.MessageType.ERROR);
                return null;
            }
            if (response.success()) GuiUtils.showMessageDialog(mainWindow.getFrame(), "Success", successMessage, GuiUtils.MessageType.INFO);
            else GuiUtils.showMessageDialog(mainWindow.getFrame(), "Error", "Error: " + response.message(), GuiUtils.MessageType.ERROR);
            return response;
        } catch (IOException ex) {
            GuiUtils.showMessageDialog(mainWindow.getFrame(), "Error", "Network error: " + ex.getMessage(), GuiUtils.MessageType.ERROR);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            GuiUtils.showMessageDialog(mainWindow.getFrame(), "Error", "Request interrupted", GuiUtils.MessageType.ERROR);
        }
        return null;
    }
}