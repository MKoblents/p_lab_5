package client.gui.buttons;

import client.gui.MainWindow;
import client.handlers.ResponseHandler;
import client.inputWorkers.CommandParser;
import client.inputWorkers.InputManager;
import client.inputWorkers.Invoker;
import client.io.ConsoleBufferedScanner;
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

            try {
                connection.sendRequest(request);
                CommandResponse response = connection.readResponse();

                if (response.success()) {
                    JOptionPane.showMessageDialog(mainWindow.getFrame(),
                            "Space Marine added successfully!",
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
    }
    public void handleRemove(){
        RemoveSpaceMarineDialog dialog = new RemoveSpaceMarineDialog(mainWindow.getFrame(), mainWindow.getTableModel());
        dialog.setVisible(true);

        if (dialog.isSuccess()) {
            SpaceMarine marineToRemove = dialog.getSelectedSpaceMarine();
            CommandRequest request = RequestsFactory.withLongArg("remove_by_id", marineToRemove.getId());
            try{
                connection.sendRequest(request);
                CommandResponse response = connection.readResponse();

                if (response.success()) {
                    JOptionPane.showMessageDialog(mainWindow.getFrame(),
                            "Space Marine deleted successfully!",
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
            }catch (IOException ex) {
                JOptionPane.showMessageDialog(mainWindow.getFrame(),
                        "Network error: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

            System.out.println("Запрос на удаление ID: " + marineToRemove.getId());
        }
    }

    public void handleExecuteScript(){
        File scriptFile = ExecuteScriptDialog.showScriptFileChooser(mainWindow.getFrame());
        //TODO
    }
}
