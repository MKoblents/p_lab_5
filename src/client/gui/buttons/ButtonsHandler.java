package client.gui.buttons;

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
    public ButtonsHandler(ConnectionManager connection, MainWindow mainWindow){
        this.connection = connection;
        this.mainWindow=mainWindow;
        this.networkReader=mainWindow.getNetworkReader();
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
                Invoker invoker = new Invoker(inputManager, mainWindow.getContext(), connection, new ClientProcessManager(mainWindow.getConfig().getHost(), mainWindow.getConfig().getPort()), scriptRunner);
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

    public CommandResponse handleRequest(CommandRequest request, String successMessage){
        try {
            connection.sendRequest(request);
            CommandResponse response = networkReader.getResponseQueue().poll();

            if (response.success()) {
                GuiUtils.showMessageDialog(mainWindow.getFrame(),
                        "Success",
                        successMessage);
                //todo
//                    refreshTable(); // Обновить таблицу
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
        CommandRequest request = RequestsFactory.createSimple("spawn_client");
        CommandResponse response = handleRequest(request, "New window opened!");
        if (response != null && response.success() && response.clientId() != null) {
            String childClientId = response.clientId();

            String jarPath = System.getProperty("java.class.path");
            if (jarPath == null || jarPath.isEmpty()) {
                jarPath = "target/p_lab_5-client.jar";
            }
            System.out.println(jarPath);

            List<String> command = new ArrayList<>();
            command.add("java");
            command.add("-jar");
            command.add(jarPath);
            command.add("--host");
            command.add(mainWindow.getConfig().getHost());
            command.add("--port");
            command.add(String.valueOf(mainWindow.getConfig().getPort()));
            command.add("--client-id");
            command.add(childClientId);
            command.add("--parent-id");
            command.add(mainWindow.getContext().getClientId());

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.inheritIO(); // Optional: inherit console output
            pb.start();

            // Add child to mainWindow.getContext()
            mainWindow.getContext().addChild(childClientId);

            mainWindow.setStatus("Spawned child: " + childClientId);
            System.out.println("Spawned child client: " + childClientId);
        } else {
            String errorMsg = response != null ? response.message() : "Unknown error";
            GuiUtils.showMessageDialog(mainWindow.getFrame(),
                    "Error",
                    "Network error: " + errorMsg,
                    GuiUtils.MessageType.ERROR);
        }


    }
    public void handleKill(){
        List<String> availableClients = fetchAvailableClients();

        if (availableClients.isEmpty()) {
            GuiUtils.showMessageDialog(mainWindow.getFrame(),
                    "Success",
                    "No active clients found to terminate.",
                    GuiUtils.MessageType.INFO);
            return;
        }

        KillClientDialog dialog = new KillClientDialog(mainWindow.getFrame(), connection, availableClients);
        dialog.setVisible(true);
    }

    // Временный метод для получения списка (замените на реальный запрос к серверу)
    private List<String> fetchAvailableClients() {
        // TODO: Отправьте запрос на сервер, например: "list_clients" или используйте существующую команду
        // Пока возвращаем заглушку для демонстрации UI:
        return List.of("client_01", "client_02", "child_client_01");
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