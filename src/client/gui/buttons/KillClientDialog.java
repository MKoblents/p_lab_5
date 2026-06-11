package client.gui.buttons;

import client.gui.utils.GuiUtils;
import client.utils.LocaleManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;
import java.util.function.Consumer;

public class KillClientDialog extends JDialog {
    private JComboBox<String> clientCombo;
    private JButton killButton, cancelButton;
    private JLabel titleLabel, infoLabel, clientLabel;
    private Consumer<String> onKillRequested;

    private final Dimension originalSize = new Dimension(500, 500);

    public KillClientDialog(JFrame parent, List<String> availableClients) {
        super(parent, LocaleManager.get("dialog.kill.title"), true);
        initComponents(availableClients);
        layoutComponents();
        applyTheme();
        setSize(originalSize);
        setLocationRelativeTo(parent);
        setMinimumSize(new Dimension(1000, 500));
        setResizable(true);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeComponents();
            }
        });
    }

    public void setOnKillRequested(Consumer<String> callback) {
        this.onKillRequested = callback;
    }

    private void initComponents(List<String> clients) {
        titleLabel = new JLabel(LocaleManager.get("dialog.kill.title"), SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        infoLabel = new JLabel(LocaleManager.get("dialog.kill.info"), SwingConstants.CENTER);
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoLabel.setForeground(GuiUtils.PRIMARY_DARK);

        clientLabel = new JLabel(LocaleManager.get("dialog.kill.client_label"), SwingConstants.RIGHT);
        clientLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        clientLabel.setForeground(GuiUtils.TEXT_COLOR);

        clientCombo = GuiUtils.createStyledComboBox(clients.toArray(new String[0]), 80);

        killButton = GuiUtils.createStyledDialogButton("button.kill", 120, 40, this::attemptKill);
        cancelButton = GuiUtils.createStyledDialogButton("button.cancel", 120, 40, this::dispose);
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(GuiUtils.BACKGROUND_COLOR);

        JPanel titlePanel = GuiUtils.createPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        add(titlePanel, BorderLayout.NORTH);

        JPanel infoPanel = GuiUtils.createPanel(new FlowLayout(FlowLayout.CENTER));
        infoPanel.setOpaque(false);
        infoPanel.add(infoLabel);
        add(infoPanel, BorderLayout.NORTH);

        JPanel formPanel = GuiUtils.createPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(20, 40, 20, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        formPanel.add(clientLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(clientCombo, gbc);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = GuiUtils.createPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(0, 20, 20, 20));
        buttonPanel.add(killButton);
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void applyTheme() {
        getContentPane().setBackground(GuiUtils.BACKGROUND_COLOR);
    }

    private void resizeComponents() {
        double scaleFactor = (double) getWidth() / originalSize.width;

        float scaledFontSize = (float) (13 * scaleFactor);
        float scaledTitleSize = (float) (16 * scaleFactor);
        float scaledInfoSize = (float) (14 * scaleFactor);

        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, (int) scaledTitleSize));
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, (int) scaledInfoSize));
        clientLabel.setFont(new Font("Segoe UI", Font.BOLD, (int) scaledFontSize));

        int scaledComboHeight = (int) (40 * scaleFactor);
        clientCombo.setPreferredSize(new Dimension(0, scaledComboHeight));
        clientCombo.setMaximumSize(new Dimension(Short.MAX_VALUE, scaledComboHeight));
        clientCombo.setFont(new Font("Segoe UI", Font.PLAIN, (int) scaledFontSize));

        int scaledButtonWidth = (int) (120 * scaleFactor);
        int scaledButtonHeight = (int) (40 * scaleFactor);
        killButton.setPreferredSize(new Dimension(scaledButtonWidth, scaledButtonHeight));
        killButton.setMaximumSize(new Dimension(scaledButtonWidth, scaledButtonHeight));
        killButton.setFont(new Font("Segoe UI", Font.BOLD, (int) scaledFontSize));

        cancelButton.setPreferredSize(new Dimension(scaledButtonWidth, scaledButtonHeight));
        cancelButton.setMaximumSize(new Dimension(scaledButtonWidth, scaledButtonHeight));
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, (int) scaledFontSize));

        revalidate();
        repaint();
    }

    private void attemptKill() {
        String selectedId = (String) clientCombo.getSelectedItem();
        if (selectedId == null || selectedId.trim().isEmpty()) {
            GuiUtils.showMessageDialog(null,
                    LocaleManager.get("dialog.kill.title"),
                    LocaleManager.get("dialog.kill.no_client"),
                    GuiUtils.MessageType.WARNING);
            return;
        }

        boolean confirm = GuiUtils.showConfirmDialog(null,
                LocaleManager.get("dialog.kill.confirm").replace("{client_id}", selectedId),
                LocaleManager.get("dialog.kill.confirm_title"));

        if (confirm) {
            if (onKillRequested != null) {
                onKillRequested.accept(selectedId);
            }
            dispose();
        }
    }
}