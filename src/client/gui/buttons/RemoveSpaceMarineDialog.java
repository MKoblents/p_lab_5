package client.gui.buttons;

import client.gui.utils.GuiUtils;
import client.gui.window.SpaceMarineTable;
import client.utils.LocaleManager;
import shared.models.SpaceMarine;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class RemoveSpaceMarineDialog extends JDialog {
    private  SpaceMarineSelector selector;
    private JButton removeButton, cancelButton;
    private JLabel titleLabel, infoLabel;
    private boolean success = false;
    private  String currentUsername;
    private final Dimension originalSize = new Dimension(500, 400);

    public RemoveSpaceMarineDialog(Frame owner, SpaceMarineTable tableModel,  String currentUsername) {
        super(owner, true);
        this.currentUsername =currentUsername;
        initComponents();
        layoutComponents();
        applyTheme();
        refreshSelector(tableModel);
        pack();
        setSize(originalSize);
        setLocationRelativeTo(owner);
        setMinimumSize(new Dimension(400, 350));

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeComponents();
            }
        });
    }

    private void initComponents() {
        titleLabel = new JLabel(LocaleManager.get("dialog.remove.title"), SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));

        infoLabel = new JLabel(LocaleManager.get("dialog.remove.info"), SwingConstants.CENTER);
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoLabel.setForeground(GuiUtils.PRIMARY_DARK);

        selector = new SpaceMarineSelector();

        removeButton = createStyledButton(LocaleManager.get("button.remove"));
        cancelButton = createStyledButton(LocaleManager.get("button.cancel"));

        removeButton.addActionListener(e -> onRemove());
        cancelButton.addActionListener(e -> onCancel());
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(GuiUtils.PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(150, 45));
        button.setMaximumSize(new Dimension(150, 45));
        button.setBorder(new EmptyBorder(10, 20, 10, 20));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(GuiUtils.PRIMARY_DARK);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(GuiUtils.PRIMARY_COLOR);
            }
        });

        return button;
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(20, 20));
        getContentPane().setBackground(GuiUtils.BACKGROUND_COLOR);
//        setBorder(new EmptyBorder(30, 30, 30, 30));

        // Title Panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        add(titlePanel, BorderLayout.NORTH);

        // Info Label
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        infoPanel.setOpaque(false);
        infoPanel.add(infoLabel);
        add(infoPanel, BorderLayout.NORTH);

        // Selector Panel
        JPanel selectorPanel = new JPanel(new BorderLayout());
        selectorPanel.setOpaque(false);
        selectorPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        selectorPanel.add(selector, BorderLayout.CENTER);
        add(selectorPanel, BorderLayout.CENTER);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        buttonPanel.setOpaque(false);
        buttonPanel.add(removeButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void applyTheme() {
        getContentPane().setBackground(GuiUtils.BACKGROUND_COLOR);
    }

    private void resizeComponents() {
        double scaleFactor = (double) getWidth() / originalSize.width;

        float titleSize = (float) (24 * scaleFactor);
        float infoSize = (float) (14 * scaleFactor);

        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, (int) titleSize));
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, (int) infoSize));

        removeButton.setFont(new Font("Segoe UI", Font.BOLD, (int) (14 * scaleFactor)));
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, (int) (14 * scaleFactor)));
    }

    public void refreshSelector(SpaceMarineTable tableModel) {
        selector.setCurrentUsername(currentUsername);
        selector.refreshData(tableModel);
    }

    private void onRemove() {
        SpaceMarine selected = selector.getSelectedSpaceMarine();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                    LocaleManager.get("dialog.error.select"),
                    LocaleManager.get("dialog.error.title"),
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                LocaleManager.get("dialog.remove.confirm").replace("{name}", selected.getName()),
                LocaleManager.get("dialog.remove.title"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            success = true;
            dispose();
        }
    }

    private void onCancel() {
        success = false;
        dispose();
    }

    public SpaceMarine getSelectedSpaceMarine() {
        return selector.getSelectedSpaceMarine();
    }

    public boolean isSuccess() {
        return success;
    }
}