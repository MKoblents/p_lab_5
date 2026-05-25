package client.gui.buttons;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class KillClientDialog extends JDialog {
    private JComboBox<String> clientCombo;
    private JButton killButton;
    private JButton cancelButton;
    private Consumer<String> onKillRequested;

    public KillClientDialog(JFrame parent, List<String> availableClients) {
        super(parent, "Завершить клиент", true);
        setupUI(availableClients);
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    public void setOnKillRequested(Consumer<String> callback) {
        this.onKillRequested = callback;
    }

    private void setupUI(List<String> clients) {
        setLayout(new BorderLayout(10, 10));
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);

        JLabel titleLabel = new JLabel("Выберите дочерний клиент:", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        clientCombo = new JComboBox<>(clients.toArray(new String[0]));
        clientCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridy = 1; gbc.gridwidth = 1;
        mainPanel.add(new JLabel("ID клиента:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(clientCombo, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        killButton = new JButton("Завершить");
        cancelButton = new JButton("Отмена");
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
            JOptionPane.showMessageDialog(this, "Клиент не выбран.", "Внимание", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Вы уверены, что хотите завершить клиент: " + selectedId + "?\n" +
                        "Это отключит его и всех его потомков.",
                "Подтверждение завершения",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (onKillRequested != null) {
                onKillRequested.accept(selectedId);
            }
            dispose();
        }
    }
}