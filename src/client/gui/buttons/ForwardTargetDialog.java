// src/client/gui/buttons/ForwardTargetDialog.java
package client.gui.buttons;

import client.gui.utils.GuiUtils;
import client.utils.LocaleManager;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class ForwardTargetDialog extends JDialog {
    private JComboBox<String> clientCombo;
    private JButton confirmButton;
    private JButton cancelButton;
    private Consumer<String> onConfirm;

    public ForwardTargetDialog(JFrame parent, List<String> availableClients, String commandKey) {
        super(parent, LocaleManager.get("dialog.forward.title") + ": " + commandKey, true);
        setupUI(availableClients, commandKey);
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    public void setOnConfirm(Consumer<String> callback) {
        this.onConfirm = callback;
    }

    private void setupUI(List<String> clients, String commandKey) {
        setLayout(new BorderLayout(10, 10));
        JPanel mainPanel = GuiUtils.createPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);

        JLabel titleLabel = new JLabel("Forward '" + commandKey + "' to:", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        clientCombo = new JComboBox<>(clients.toArray(new String[0]));
        clientCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridy = 1; gbc.gridwidth = 1;
        mainPanel.add(new JLabel(LocaleManager.get("dialog.forward.client")), gbc);
        gbc.gridx = 1;
        mainPanel.add(clientCombo, gbc);

        JPanel btnPanel = GuiUtils.createPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        confirmButton = new JButton(LocaleManager.get("button.forward"));
        cancelButton = new JButton(LocaleManager.get("button.cancel"));
        confirmButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cancelButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        confirmButton.addActionListener(e -> {
            String selectedId = (String) clientCombo.getSelectedItem();
            if (selectedId != null && onConfirm != null) {
                onConfirm.accept(selectedId);
            }
            dispose();
        });
        cancelButton.addActionListener(e -> dispose());

        gbc.gridy = 2; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE;
        btnPanel.add(confirmButton);
        btnPanel.add(cancelButton);
        mainPanel.add(btnPanel, gbc);

        add(mainPanel, BorderLayout.CENTER);
    }
}