package client.gui.buttons;

import client.network.ConnectionManager;
import client.utils.RequestsFactory;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class KillClientDialog extends JDialog {
    private final ConnectionManager connection;
    private  JComboBox<String> clientCombo;
    private JButton killButton;
    private JButton cancelButton;

    public KillClientDialog(JFrame parent, ConnectionManager connection, List<String> availableClients) {
        super(parent, "Kill Client", true);
        this.connection = connection;
        setupUI(availableClients);
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void setupUI(List<String> clients) {
        setLayout(new BorderLayout(10, 10));
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);

        // Заголовок
        JLabel titleLabel = new JLabel("Select client to terminate:", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        // Выпадающий список с ID клиентов
        clientCombo = new JComboBox<>(clients.toArray(new String[0]));
        clientCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridy = 1; gbc.gridwidth = 1;
        mainPanel.add(new JLabel("Client ID:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(clientCombo, gbc);

        // Кнопки
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        killButton = new JButton("Terminate");
        cancelButton = new JButton("Cancel");

        killButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cancelButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        killButton.addActionListener(e -> attemptKill());
        cancelButton.addActionListener(e -> dispose());

        gbc.gridy = 2; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE;
        btnPanel.add(killButton);
        btnPanel.add(cancelButton);
        mainPanel.add(btnPanel, gbc);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void attemptKill() {
        String selectedId = (String) clientCombo.getSelectedItem();
        if (selectedId == null || selectedId.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No client selected.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to terminate client: " + selectedId + "?\nThis will disconnect it and all its children.",
                "Confirm Termination",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            performKill(selectedId);
        }
    }

    private void performKill(String clientId) {
        try {
            CommandRequest request = RequestsFactory.withStringArg("kill_client", clientId);
            connection.sendRequest(request);
            CommandResponse response = connection.readResponse();

            if (response != null && response.success()) {
                JOptionPane.showMessageDialog(this,
                        "Client " + clientId + " terminated successfully.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed: " + (response != null ? response.message() : "Unknown error"),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Network error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}