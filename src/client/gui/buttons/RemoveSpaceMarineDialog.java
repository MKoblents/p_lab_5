package client.gui.buttons;

import client.gui.utils.GuiUtils;
import client.gui.window.SpaceMarineTable;
import client.utils.LocaleManager;
import shared.models.SpaceMarine;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RemoveSpaceMarineDialog extends AbstractStyledDialog {

    private SpaceMarineSelector selector;
    private JLabel infoLabel;
    private boolean success = false;
    private final String currentUsername;

    public RemoveSpaceMarineDialog(Frame owner, SpaceMarineTable tableModel, String currentUsername) {
        super(owner, "dialog.remove.title", true, 500, 400);
        this.currentUsername = currentUsername;
        refreshSelector(tableModel);
    }

    @Override
    protected void initComponents() {
        infoLabel = new JLabel(LocaleManager.get("dialog.remove.info"), SwingConstants.CENTER);
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoLabel.setForeground(GuiUtils.PRIMARY_DARK);

        selector = new SpaceMarineSelector();
    }

    @Override
    protected void layoutComponents() {
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        infoPanel.setOpaque(false);
        infoPanel.add(infoLabel);
        add(infoPanel, BorderLayout.NORTH);
        JPanel selectorPanel = new JPanel(new BorderLayout());
        selectorPanel.setOpaque(false);
        selectorPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        selectorPanel.add(selector, BorderLayout.CENTER);
        add(selectorPanel, BorderLayout.CENTER);
        createStandardButtons(this::onRemove, this::onCancel);
    }

    @Override
    protected void resizeComponents() {
        float scaledInfoSize = scaleFontSize(14);
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, (int) scaledInfoSize));
    }

    private void onRemove() {
        SpaceMarine selected = selector.getSelectedSpaceMarine();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                    LocaleManager.get("dialog.error.select"),
                    LocaleManager.get("dialog.remove.title"),
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

    public void refreshSelector(SpaceMarineTable tableModel) {
        selector.setCurrentUsername(currentUsername);
        selector.refreshData(tableModel);
    }

    public SpaceMarine getSelectedSpaceMarine() {
        return selector.getSelectedSpaceMarine();
    }

    public boolean isSuccess() {
        return success;
    }
}