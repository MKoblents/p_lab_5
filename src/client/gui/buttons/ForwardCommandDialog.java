package client.gui.buttons;

import client.utils.LocaleManager;
import shared.dto.CommandRequest;
import shared.dto.ForwardCommandObject;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ForwardCommandDialog extends JDialog {
    private JComboBox<String> commandCombo;
    private JComboBox<String> clientCombo;
    private JButton okButton, cancelButton;

    private ForwardCommandObject result;
    private boolean confirmed = false;

    // List of forwardable commands
    private static final String[] FORWARDABLE_COMMANDS = {
            "add", "remove_by_id", "update", "clear",
            "show", "info", "help", "shuffle"
    };

    public ForwardCommandDialog(Frame parent, List<String> availableClients) {
        super(parent, "Forward Command", true);
        initComponents(availableClients);
        layoutComponents();
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents(List<String> clients) {
        commandCombo = new JComboBox<>(FORWARDABLE_COMMANDS);
        clientCombo = new JComboBox<>(clients.toArray(new String[0]));

        okButton = new JButton("OK");
        cancelButton = new JButton("Cancel");

        okButton.addActionListener(e -> {
            String command = (String) commandCombo.getSelectedItem();
            String targetClient = (String) clientCombo.getSelectedItem();
            if (command != null && targetClient != null) {
                result = new ForwardCommandObject(
                        null, // parentId will be set by invoker
                        targetClient,
                        command
                );
                confirmed = true;
                dispose();
            }
        });

        cancelButton.addActionListener(e -> dispose());
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(255, 240, 248));

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        formPanel.setOpaque(false);
        formPanel.add(new JLabel("Command:"));
        formPanel.add(commandCombo);
        formPanel.add(new JLabel("Target Client:"));
        formPanel.add(clientCombo);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.setOpaque(false);
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public ForwardCommandObject getResult() { return result; }
    public boolean isConfirmed() { return confirmed; }
}