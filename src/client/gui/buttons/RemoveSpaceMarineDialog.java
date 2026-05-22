package client.gui.buttons;

import client.gui.window.SpaceMarineTable;
import client.gui.buttons.SpaceMarineSelector;
import shared.models.SpaceMarine;

import javax.swing.*;
import java.awt.*;

public class RemoveSpaceMarineDialog extends JDialog {

    private final SpaceMarineSelector selector;
    private boolean success = false;

    public RemoveSpaceMarineDialog(Frame owner, SpaceMarineTable tableModel) {
        super(owner, "Удаление SpaceMarine", true);
        setLayout(new BorderLayout(10, 10));
        setSize(400, 250);
        setLocationRelativeTo(owner);

        selector = new SpaceMarineSelector();
        selector.refreshData(tableModel);
        add(selector, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton removeBtn = new JButton("Удалить");
        JButton cancelBtn = new JButton("Отмена");

        removeBtn.addActionListener(e -> {
            SpaceMarine selected = selector.getSelectedSpaceMarine();
            if (selected != null) {
                // Здесь можно добавить подтверждение
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Вы уверены, что хотите удалить " + selected.getName() + "?",
                        "Подтверждение", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    success = true;
                    dispose();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Сначала выберите элемент из списка!");
            }
        });

        cancelBtn.addActionListener(e -> dispose());

        buttonPanel.add(removeBtn);
        buttonPanel.add(cancelBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public SpaceMarine getSelectedSpaceMarine() {
        return selector.getSelectedSpaceMarine();
    }

    public boolean isSuccess() {
        return success;
    }
}