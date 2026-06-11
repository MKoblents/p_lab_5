package client.gui.buttons;

import client.gui.utils.GuiUtils;
import client.gui.window.SpaceMarineTable;
import client.utils.LocaleManager;
import shared.models.SpaceMarine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;
import java.util.function.Consumer;

public class SpaceMarineSelector extends JPanel {

    private final JComboBox<SpaceMarine> comboBox;
    private final JLabel emptyLabel;
    private String currentUsername;
    private final Dimension originalSize = new Dimension(320, 80);

    public SpaceMarineSelector() {
        setBackground(GuiUtils.BACKGROUND_COLOR);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setPreferredSize(originalSize);

        comboBox = GuiUtils.createStyledComboBox(80);
        comboBox.setRenderer(new SpaceMarineListCellRenderer());

        emptyLabel = GuiUtils.createLabel(LocaleManager.get("selector.collection.empty"), 14,false);
        emptyLabel.setVisible(false);
        emptyLabel.setAlignmentX(CENTER_ALIGNMENT);

        add(Box.createVerticalStrut(5));
        add(comboBox);
        add(Box.createVerticalStrut(5));
        add(emptyLabel);
        add(Box.createVerticalStrut(5));
    }


    public void setCurrentUsername(String username) {
        this.currentUsername = username;
    }

    public void refreshData(SpaceMarineTable tableModel) {
        comboBox.removeAllItems();
        List<SpaceMarine> marines = tableModel.getAllMarines();

        List<SpaceMarine> filteredMarines = (currentUsername != null && !currentUsername.isEmpty())
                ? marines.stream()
                  .filter(m -> currentUsername.equals(m.getOwner()))
                  .toList()
                : marines;

        if (filteredMarines.isEmpty()) {
            comboBox.setVisible(false);
            emptyLabel.setVisible(true);
            emptyLabel.setText(currentUsername != null
                    ? LocaleManager.get("selector.no_user_objects")
                    : LocaleManager.get("selector.collection.empty"));
        } else {
            for (SpaceMarine m : filteredMarines) {
                comboBox.addItem(m);
            }
            comboBox.setVisible(true);
            emptyLabel.setVisible(false);
        }
        revalidate();
        repaint();
    }

    public SpaceMarine getSelectedSpaceMarine() {
        return (SpaceMarine) comboBox.getSelectedItem();
    }

    public void addResizeListener(Consumer<Double> onResize) {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                double scaleFactor = (double) getWidth() / originalSize.width;
                onResize.accept(scaleFactor);

                float scaledFontSize = (float) (14 * scaleFactor);
                comboBox.setFont(new Font("Segoe UI", Font.PLAIN, (int) scaledFontSize));
                emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, (int) scaledFontSize));

                int scaledHeight = (int) (30 * scaleFactor);
                comboBox.setPreferredSize(new Dimension(Short.MAX_VALUE, scaledHeight));
                comboBox.setMaximumSize(new Dimension(Short.MAX_VALUE, scaledHeight));
            }
        });
    }

    private static class SpaceMarineListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof SpaceMarine marine) {
                setText(marine.getName() + " (ID: " + marine.getId() + ")");
            }

            if (isSelected) {
                setBackground(GuiUtils.PRIMARY_COLOR);
                setForeground(Color.WHITE);
            } else {
                setBackground(Color.WHITE);
                setForeground(GuiUtils.TEXT_COLOR);
            }

            setFont(new Font("Segoe UI", Font.PLAIN, (int) GuiUtils.BASE_MESSAGE_SIZE));
            setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
            return this;
        }
    }

    public JComboBox<SpaceMarine> getComboBox() {
        return comboBox;
    }
}