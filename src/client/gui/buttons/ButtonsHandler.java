package client.gui.buttons;

import client.network.ConnectionManager;
import client.utils.RequestsFactory;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.models.SpaceMarine;

import javax.swing.*;
import java.io.IOException;

public class ButtonsHandler {
    private ConnectionManager connection;
    private JFrame frame;
    public ButtonsHandler(ConnectionManager connection, JFrame frame){
        this.connection = connection;
        this.frame=frame;
    }
    public void handleAdd() {
        SpaceMarineInputDialog dialog = new SpaceMarineInputDialog(frame);
        dialog.setVisible(true);

        SpaceMarine marine = dialog.getSpaceMarine();
        if (marine != null) {
            CommandRequest request = RequestsFactory.withMarine("add", marine);

            try {
                connection.sendRequest(request);
                CommandResponse response = connection.readResponse();

                if (response.success()) {
                    JOptionPane.showMessageDialog(frame,
                            "Space Marine added successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
//                    refreshTable(); // Обновить таблицу
                } else {
                    JOptionPane.showMessageDialog(frame,
                            "Error: " + response.message(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame,
                        "Network error: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
