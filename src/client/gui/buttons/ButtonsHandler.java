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
import shared.dto.UserInfo;
import shared.models.SpaceMarine;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ButtonsHandler {
    private ConnectionManager connection;
    private MainWindow mainWindow;
    private ScriptRunner scriptRunner;
    private final AsyncNetworkReader networkReader;
    private ClientProcessManager processManager;
    private  SpawnClient spawnClient;
    public ButtonsHandler(ConnectionManager connection, MainWindow mainWindow){
        this.connection = connection;
        this.mainWindow=mainWindow;
        this.networkReader=mainWindow.getNetworkReader();
        this.processManager = new ClientProcessManager(mainWindow.getConfig().getHost(), mainWindow.getConfig().getPort(), null);
//        scriptRunner = new ScriptRunner(inputManager,connection,null,new Invoker(null,RequestsFactory.))
    }
    public void handleAdd() {
        SpaceMarineInputDialog dialog = new SpaceMarineInputDialog(mainWindow.getFrame());
        dialog.setVisible(true);

        SpaceMarine marine = dialog.getSpaceMarine();
        if (marine != null) {
            CommandRequest request = RequestsFactory.withMarine("add", marine);
            handleRequest(request, "Space Marine added successfully!");
        }
    }
    public void handleRemove(){
        String username = mainWindow.getContext().getUserInfo().name();  // ← Получаем текущего пользователя
        RemoveSpaceMarineDialog dialog = new RemoveSpaceMarineDialog(mainWindow.getFrame(), mainWindow.getTableModel(), username);
        dialog.setVisible(true);

        if (dialog.isSuccess()) {
            SpaceMarine marineToRemove = dialog.getSelectedSpaceMarine();
            CommandRequest request = RequestsFactory.withLongArg("remove_by_id", marineToRemove.getId());
            handleRequest(request, "Space Marine deleted successfully!");

            System.out.println("Запрос на удаление ID: " + marineToRemove.getId());
        }
    }

    public void handleExecuteScript() {
        ExecuteScriptDialog executeScriptDialog = new ExecuteScriptDialog(mainWindow.getFrame());
        executeScriptDialog.setVisible(true);
        File scriptFile = executeScriptDialog.getSelectedFile();
        SwingWorker<Void, String> worker = new SwingWorker<>() {
            protected Void doInBackground() {
                InputManager inputManager = new InputManager(null, new CommandParser());
                ScriptRunner scriptRunner = new ScriptRunner(inputManager, connection, new ResponseHandler(mainWindow.getContext()), null, new FileManager());
                Invoker invoker = new Invoker(inputManager, mainWindow.getContext(), connection, processManager, scriptRunner);
                scriptRunner.setInvoker(invoker);
                boolean success = scriptRunner.executeScript(scriptFile.getPath());
                String message = success ? "Script completed successfully\n"+ scriptRunner.getRes()  : "Script completed with errors";


                SwingUtilities.invokeLater(() ->
                        GuiUtils.showMessageDialog(mainWindow.getFrame(), "Script Result", message,
                                success ? GuiUtils.MessageType.INFO : GuiUtils.MessageType.ERROR)
                );
               return null;
            }

        };
        worker.execute();


        //TODO
    }
    public void handleClear(){
        int confirm = JOptionPane.showConfirmDialog(null,
                "Вы уверены, что хотите удалить все свои объекты?",
                "Подтверждение", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.NO_OPTION) {
            return;
        }
        CommandRequest request = RequestsFactory.createSimple("clear");

        handleRequest(request,"Space Marine cleared successfully!");
    }

    public CommandResponse handleRequest(CommandRequest request, String successMessage) {
        try {
            String expectedRequestId = request.requestId(); // Сохраняем requestId отправленного запроса
            connection.sendRequest(request);

            CommandResponse response = null;
            long startTime = System.currentTimeMillis();
            long timeout = 5000;
            while (response == null && (System.currentTimeMillis() - startTime) < timeout) {
                CommandResponse candidate = networkReader.getResponseQueue().poll();
                if (candidate != null && expectedRequestId.equals(candidate.requestId())) {
                    response = candidate;
                    break;
                } else if (candidate != null) {
                   System.out.println("Received unexpected response for requestId: " + candidate.requestId());
                    // Можно добавить логику обработки других ответов
                } else {
                    Thread.sleep(50); // Ждем немного перед следующей попыткой
                }
            }

            if (response == null) {
                GuiUtils.showMessageDialog(mainWindow.getFrame(),
                        "Error",
                        "Timeout waiting for response",
                        GuiUtils.MessageType.ERROR);
                return null;
            }

            if (response.success()) {
                GuiUtils.showMessageDialog(mainWindow.getFrame(),
                        "Success",
                        successMessage);
            } else {
                GuiUtils.showMessageDialog(mainWindow.getFrame(),
                        "Error",
                        "Error: " + response.message(),
                        GuiUtils.MessageType.ERROR);
            }
            return response;
        } catch (IOException ex) {
            GuiUtils.showMessageDialog(mainWindow.getFrame(),
                    "Error",
                    "Network error: " + ex.getMessage(),
                    GuiUtils.MessageType.ERROR);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            GuiUtils.showMessageDialog(mainWindow.getFrame(),
                    "Error",
                    "Request interrupted",
                    GuiUtils.MessageType.ERROR);
        }
        return null;
    }
    public void handleUpdate(){
        String username = mainWindow.getContext().getUserInfo().name();  // ← Получаем текущего пользователя
        SpaceMarineUpdateDialog updateDialog = new SpaceMarineUpdateDialog(mainWindow.getFrame(),mainWindow.getTableModel(), username);
        updateDialog.setVisible(true);
        SpaceMarine updateMarine = updateDialog.getUpdatedSpaceMarine();
        if (updateMarine != null){
            CommandRequest request = RequestsFactory.createTwoArgs("update", updateMarine.getId(), updateMarine);
            handleRequest(request, "SpaceMarine updated successfully!");
        }
    }
    public void handleInfo(){
        showSimple("info");
    }
    public void handleHelp(){
        showSimple("help");
    }
    private void showSimple(String commandKey){
        CommandRequest request = RequestsFactory.createSimple(commandKey);
        try {
            connection.sendRequest(request);
            CommandResponse response = connection.readResponse();
            System.out.println(request);
            System.out.println(response);
            GuiUtils.showMessageDialog(mainWindow.getFrame(),
                    "Success",
                    (String) response.result(),
                    GuiUtils.MessageType.INFO);
        } catch (IOException ex) {
            GuiUtils.showMessageDialog(mainWindow.getFrame(),
                    "Error",
                    "Network error: " + ex.getMessage(),
                    GuiUtils.MessageType.ERROR);
        }
    }
    public void handleSpawn() throws IOException {
        if (spawnClient == null) spawnClient  = new SpawnClient(mainWindow.getContext(),processManager);
       CommandRequest request = spawnClient.execute(SideFlag.SELF);
        CommandResponse response = handleRequest(request, "New window opened!");
        String requestId = response.requestId();
        String message = response.message();
        if (spawnClient == null) spawnClient  = new SpawnClient(mainWindow.getContext(),processManager);
        System.out.println(response);
        spawnClient.handleResponse(response, mainWindow.getContext());
//        if (response != null && response.success() && response.clientId() != null) {
//            String childClientId = response.clientId();
//            processManager.spawnChild(childClientId, mainWindow.getContext().getClientId());
//
//            mainWindow.getContext().addChild(childClientId);
//            System.out.println(mainWindow.getContext());
//            System.out.println(childClientId+" "+ mainWindow.getContext().getClientId());
//
//            mainWindow.setStatus("Spawned child: " + childClientId);
//            System.out.println("Spawned child client: " + childClientId);
//        } else {
//            String errorMsg = response != null ? response.message() : "Unknown error";
//            GuiUtils.showMessageDialog(mainWindow.getFrame(),
//                    "Error",
//                    "Network error: " + errorMsg,
//                    GuiUtils.MessageType.ERROR);
//        }


    }
    public void handleKill(){
        List<String> availableClients = mainWindow.getContext().getChildClientIds();

        if (availableClients.isEmpty()) {
            GuiUtils.showMessageDialog(mainWindow.getFrame(),
                    "Success",
                    "No active clients found to terminate.",
                    GuiUtils.MessageType.INFO);
            return;
        }

        KillClientDialog dialog = new KillClientDialog(mainWindow.getFrame(), availableClients);
        dialog.setOnKillRequested(this::sendKillCommand);
        dialog.setVisible(true);
    }
    private void sendKillCommand(String clientId) {
        CommandRequest request = RequestsFactory.withStringArg("kill_client", clientId);
        CommandResponse response =handleRequest(request, "Client killed");
        if (response != null && response.success()) {
            boolean processKilled = processManager.killChild(clientId);
           mainWindow.getContext().removeChild(clientId);
            GuiUtils.showMessageDialog(mainWindow.getFrame(),
                    "Успех", "Клиент " + clientId + " успешно завершен.", GuiUtils.MessageType.INFO);
        } else {
            GuiUtils.showMessageDialog(mainWindow.getFrame(),
                    "Ошибка", "Не удалось завершить клиент: " + (response != null ? response.message() : "Неизвестная ошибка"),
                    GuiUtils.MessageType.ERROR);
        }

    }


    public void handleLogOut(){
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
}