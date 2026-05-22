package client.gui.buttons;

import client.gui.window.SpaceMarineTable;
import shared.enums.AstartesCategory;
import shared.enums.MeleeWeapon;
import shared.enums.Weapon;
import shared.models.Coordinates;
import shared.models.SpaceMarine;

import javax.swing.*;
import javax.xml.bind.ValidationException;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class SpaceMarineUpdateDialog extends JDialog {
    private SpaceMarineSelector selector;
    private JTextField nameField, xField, yField, healthField;
    private JComboBox<MeleeWeapon> meleeWeaponCombo;
    private JComboBox<Weapon> weaponCombo;
    private JComboBox<AstartesCategory> categoryCombo;
    private JButton okButton, cancelButton;
    private SpaceMarine result;

    public SpaceMarineUpdateDialog(JFrame parent, SpaceMarineTable tableModel) {
        super(parent, "Update Space Marine", true);
        initComponents();
        layoutComponents();
        refreshSelector(tableModel);
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        selector = new SpaceMarineSelector();

        nameField = createPlaceholderField("Enter name");
        xField = createPlaceholderField("Enter X coordinate");
        yField = createPlaceholderField("Enter Y coordinate");
        healthField = createPlaceholderField("Enter health");

        meleeWeaponCombo = new JComboBox<>(MeleeWeapon.values());
        weaponCombo = new JComboBox<>(Weapon.values());
        categoryCombo = new JComboBox<>(AstartesCategory.values());

        okButton = new JButton("Update");
        cancelButton = new JButton("Cancel");

        okButton.addActionListener(e -> onOK());
        cancelButton.addActionListener(e -> onCancel());

        getRootPane().setDefaultButton(okButton);

        // Автозаполнение полей при выборе элемента в селекторе
        selector.getComboBox().addActionListener(e -> {
            SpaceMarine selected = selector.getSelectedSpaceMarine();
            if (selected != null) {
                populateFields(selected);
            }
        });
    }

    private JTextField createPlaceholderField(String placeholder) {
        JTextField field = new JTextField(20);
        field.setText(placeholder);
        field.setForeground(Color.GRAY);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                }
            }
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }
        });
        return field;
    }

    private void populateFields(SpaceMarine marine) {
        setFieldText(nameField, marine.getName(), "Enter name");
        setFieldText(xField, String.valueOf(marine.getCoordinates().getX()), "Enter X coordinate");
        setFieldText(yField, String.valueOf(marine.getCoordinates().getY()), "Enter Y coordinate");
        setFieldText(healthField, String.valueOf(marine.getHealth()), "Enter health");

        meleeWeaponCombo.setSelectedItem(marine.getMeleeWeapon());
        weaponCombo.setSelectedItem(marine.getWeaponType());
        categoryCombo.setSelectedItem(marine.getCategory());
    }

    private void setFieldText(JTextField field, String value, String placeholder) {
        if (!value.equals(placeholder)) {
            field.setText(value);
            field.setForeground(Color.BLACK);
        }
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        selector.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Верх: Селектор
        JPanel selectorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        selectorPanel.add(selector);
        add(selectorPanel, BorderLayout.NORTH);

        // Центр: Поля ввода
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        int row = 0;

        gbc.gridy = row++; fieldsPanel.add(nameField, gbc);
        gbc.gridy = row++; fieldsPanel.add(xField, gbc);
        gbc.gridy = row++; fieldsPanel.add(yField, gbc);
        gbc.gridy = row++; fieldsPanel.add(healthField, gbc);
        gbc.gridy = row++; fieldsPanel.add(meleeWeaponCombo, gbc);
        gbc.gridy = row++; fieldsPanel.add(weaponCombo, gbc);
        gbc.gridy = row++; fieldsPanel.add(categoryCombo, gbc);

        add(fieldsPanel, BorderLayout.CENTER);

        // Низ: Кнопки
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void refreshSelector(SpaceMarineTable tableModel) {
        selector.refreshData(tableModel);
    }

    private void onOK() {
        SpaceMarine selected = selector.getSelectedSpaceMarine();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a SpaceMarine to update.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            result = buildUpdatedSpaceMarine(selected.getId());
            if (result != null) {
                setVisible(false);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number format for coordinates or health.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onCancel() {
        result = null;
        setVisible(false);
    }

    private SpaceMarine buildUpdatedSpaceMarine(long id) throws ValidationException {
        String name = nameField.getText().trim();
        if (name.isEmpty() || name.equals("Enter name")) throw new ValidationException("Name cannot be empty");

        String xText = xField.getText().trim();
        if (xText.isEmpty() || xText.equals("Enter X coordinate")) throw new ValidationException("X coordinate is required");
        long x = Long.parseLong(xText);

        String yText = yField.getText().trim();
        if (yText.isEmpty() || yText.equals("Enter Y coordinate")) throw new ValidationException("Y coordinate is required");
        long y = Long.parseLong(yText);

        String healthText = healthField.getText().trim();
        if (healthText.isEmpty() || healthText.equals("Enter health")) throw new ValidationException("Health is required");
        double health = Double.parseDouble(healthText);
        if (health <= 0) throw new ValidationException("Health must be greater than 0");

        SpaceMarine updated = new SpaceMarine();
        updated.setId(id); // 🔑 Сохраняем ID для команды обновления
        updated.setName(name);

        Coordinates coords = new Coordinates();
        coords.setX(x);
        coords.setY(y);
        updated.setCoordinates(coords);

        updated.setHealth(health);
        updated.setMeleeWeapon((MeleeWeapon) meleeWeaponCombo.getSelectedItem());
        updated.setWeaponType((Weapon) weaponCombo.getSelectedItem());
        updated.setCategory((AstartesCategory) categoryCombo.getSelectedItem());

        return updated;
    }

    public SpaceMarine getUpdatedSpaceMarine() {
        return result;
    }
}