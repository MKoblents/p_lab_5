package client.gui.buttons;

import client.gui.MainWindow;
import client.network.ConnectionManager;
import client.scripts.ScriptRunner;
import client.utils.RequestsFactory;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.models.SpaceMarine;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class ButtonsHandler {
    private ConnectionManager connection;
    private MainWindow mainWindow;
    private ScriptRunner scriptRunner;
    public ButtonsHandler(ConnectionManager connection, MainWindow mainWindow){
        this.connection = connection;
        this.mainWindow=mainWindow;
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
        RemoveSpaceMarineDialog dialog = new RemoveSpaceMarineDialog(mainWindow.getFrame(), mainWindow.getTableModel());
        dialog.setVisible(true);

        if (dialog.isSuccess()) {
            SpaceMarine marineToRemove = dialog.getSelectedSpaceMarine();
            CommandRequest request = RequestsFactory.withLongArg("remove_by_id", marineToRemove.getId());
            handleRequest(request, "Space Marine deleted successfully!");

            System.out.println("Запрос на удаление ID: " + marineToRemove.getId());
        }
    }

    public void handleExecuteScript(){
        File scriptFile = ExecuteScriptDialog.showScriptFileChooser(mainWindow.getFrame());
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

    private void handleRequest(CommandRequest request, String successMessage){
        try {
            connection.sendRequest(request);
            CommandResponse response = connection.readResponse();

            if (response.success()) {
                JOptionPane.showMessageDialog(mainWindow.getFrame(),
                        successMessage,
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                //todo
//                    refreshTable(); // Обновить таблицу
            } else {
                JOptionPane.showMessageDialog(mainWindow.getFrame(),
                        "Error: " + response.message(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(mainWindow.getFrame(),
                    "Network error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    public void handleUpdate(){
        SpaceMarineUpdateDialog updateDialog = new SpaceMarineUpdateDialog(mainWindow.getFrame(),mainWindow.getTableModel());
        updateDialog.setVisible(true);
        SpaceMarine updateMarine = updateDialog.getUpdatedSpaceMarine();
        if (updateMarine != null){
            CommandRequest request = RequestsFactory.createTwoArgs("update", updateMarine.getId(), updateMarine);
            handleRequest(request, "SpaceMarine updated successfully!");
        }
    }
}
