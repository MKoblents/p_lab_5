package client.gui.buttons;

import client.gui.window.SpaceMarineTable;
import shared.models.SpaceMarine;

import javax.swing.*;
import java.awt.*;
import java.util.List;


public class SpaceMarineSelector extends JPanel {

    private final JComboBox<SpaceMarine> comboBox;
    private final JLabel emptyLabel;

    public SpaceMarineSelector() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        comboBox = new JComboBox<>();
        comboBox.setRenderer(new SpaceMarineListCellRenderer());
        comboBox.setPreferredSize(new Dimension(300, 30));

        emptyLabel = new JLabel("Коллекция пуста");
        emptyLabel.setVisible(false);
        emptyLabel.setAlignmentX(CENTER_ALIGNMENT);

        add(Box.createVerticalStrut(10));
        add(comboBox);
        add(Box.createVerticalStrut(5));
        add(emptyLabel);
    }

    public void refreshData(SpaceMarineTable tableModel) {
        comboBox.removeAllItems();
        List<SpaceMarine> marines = tableModel.getAllMarines();

        if (marines.isEmpty()) {
            comboBox.setVisible(false);
            emptyLabel.setVisible(true);
        } else {
            for (SpaceMarine m : marines) {
                comboBox.addItem(m);
            }
            comboBox.setVisible(true);
            emptyLabel.setVisible(false);
        }
    }

    public SpaceMarine getSelectedSpaceMarine() {
        return (SpaceMarine) comboBox.getSelectedItem();
    }

    /**
     * Кастомный рендерер, чтобы в выпадающем списке отображалось имя SpaceMarine,
     * а не его toString() или ID.
     */
    private static class SpaceMarineListCellRenderer extends DefaultListCellRenderer {
        @Override
        public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof SpaceMarine marine) {
                setText(marine.getName() + " (ID: " + marine.getId() + ")");
            }
            return this;
        }
    }
    public JComboBox<SpaceMarine> getComboBox() {
        return comboBox;
    }
}