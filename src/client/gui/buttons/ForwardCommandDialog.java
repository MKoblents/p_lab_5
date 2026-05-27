package client.gui.buttons;

import client.gui.utils.GuiUtils;
import client.utils.LocaleManager;
import shared.dto.CommandRequest;
import shared.dto.ForwardCommandObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;

public class ForwardCommandDialog extends JDialog {
    private JComboBox<String> commandCombo;
    private JComboBox<String> clientCombo;
    private JButton okButton, cancelButton;

    private ForwardCommandObject result;
    private boolean confirmed = false;

    private final Dimension originalSize = new Dimension(500, 300);

    // List of forwardable commands
    private static final String[] FORWARDABLE_COMMANDS = {
            "add", "remove_by_id", "update", "clear",
            "show", "info", "help", "shuffle"
    };

    public ForwardCommandDialog(Frame parent, List<String> availableClients) {
        super(parent, LocaleManager.get("dialog.forward.title"), true);
        initComponents(availableClients);
        layoutComponents();
        applyTheme();
        setSize(originalSize);
        setLocationRelativeTo(parent);
        setMinimumSize(new Dimension(1000, 700));
        setResizable(true);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeComponents();
            }
        });
    }

    private void initComponents(List<String> clients) {
        commandCombo = GuiUtils.createStyledComboBox(FORWARDABLE_COMMANDS, 80);
        clientCombo = GuiUtils.createStyledComboBox(clients.toArray(new String[0]), 80);

        okButton = GuiUtils.createStyledDialogButton("button.ok", 120, 40, () -> {
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

        cancelButton = GuiUtils.createStyledDialogButton("button.cancel", 120, 40, this::dispose);
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(GuiUtils.BACKGROUND_COLOR);

        // Title Label
        JLabel titleLabel = new JLabel(LocaleManager.get("dialog.forward.info"), SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(GuiUtils.PRIMARY_DARK);
        titleLabel.setBorder(new EmptyBorder(15, 15, 15, 15));
        add(titleLabel, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(20, 40, 20, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Command Label
        JLabel commandLabel = new JLabel(LocaleManager.get("dialog.forward.command"));
        commandLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        commandLabel.setForeground(GuiUtils.TEXT_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        formPanel.add(commandLabel, gbc);

        // Command Combo
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(commandCombo, gbc);

        // Client Label
        JLabel clientLabel = new JLabel(LocaleManager.get("dialog.forward.client"));
        clientLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        clientLabel.setForeground(GuiUtils.TEXT_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        formPanel.add(clientLabel, gbc);

        // Client Combo
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(clientCombo, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(0, 20, 20, 20));
        buttonPanel.add(okButton);
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

        // Update fonts
        Component[] components = getContentPane().getComponents();
        for (Component comp : components) {
            if (comp instanceof JLabel label) {
                if (label.getFont().getSize() == 16) {
                    label.setFont(new Font("Segoe UI", Font.BOLD, (int) scaledTitleSize));
                } else {
                    label.setFont(new Font("Segoe UI", Font.BOLD, (int) scaledFontSize));
                }
            } else if (comp instanceof JPanel panel) {
                resizePanel(panel, scaleFactor);
            }
        }

        // Update combo box sizes
        int scaledComboHeight = (int) (40 * scaleFactor);
        commandCombo.setPreferredSize(new Dimension(0, scaledComboHeight));
        commandCombo.setMaximumSize(new Dimension(Short.MAX_VALUE, scaledComboHeight));
        commandCombo.setFont(new Font("Segoe UI", Font.PLAIN, (int) scaledFontSize));

        clientCombo.setPreferredSize(new Dimension(0, scaledComboHeight));
        clientCombo.setMaximumSize(new Dimension(Short.MAX_VALUE, scaledComboHeight));
        clientCombo.setFont(new Font("Segoe UI", Font.PLAIN, (int) scaledFontSize));

        // Update button sizes
        int scaledButtonWidth = (int) (120 * scaleFactor);
        int scaledButtonHeight = (int) (40 * scaleFactor);
        okButton.setPreferredSize(new Dimension(scaledButtonWidth, scaledButtonHeight));
        okButton.setMaximumSize(new Dimension(scaledButtonWidth, scaledButtonHeight));
        okButton.setFont(new Font("Segoe UI", Font.BOLD, (int) scaledFontSize));

        cancelButton.setPreferredSize(new Dimension(scaledButtonWidth, scaledButtonHeight));
        cancelButton.setMaximumSize(new Dimension(scaledButtonWidth, scaledButtonHeight));
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, (int) scaledFontSize));

        revalidate();
        repaint();
    }

    private void resizePanel(JPanel panel, double scaleFactor) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JLabel label) {
                label.setFont(new Font("Segoe UI", Font.BOLD, (int) (13 * scaleFactor)));
            } else if (comp instanceof JComboBox<?> combo) {
                combo.setFont(new Font("Segoe UI", Font.PLAIN, (int) (13 * scaleFactor)));
                int scaledHeight = (int) (40 * scaleFactor);
                combo.setPreferredSize(new Dimension(0, scaledHeight));
                combo.setMaximumSize(new Dimension(Short.MAX_VALUE, scaledHeight));
            }
        }
    }

    public ForwardCommandObject getResult() {
        return result;
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}