package client.gui;

import shared.enums.AstartesCategory;
import shared.enums.MeleeWeapon;
import shared.enums.Weapon;
import shared.models.Coordinates;
import shared.models.SpaceMarine;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.xml.bind.ValidationException;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class SpaceMarineInputDialog extends JDialog {
    private JTextField nameField;
    private JTextField xField, yField;
    private JTextField healthField;
    private JComboBox<MeleeWeapon> meleeWeaponCombo;
    private JComboBox<Weapon> weaponCombo;
    private JComboBox<AstartesCategory> categoryCombo;
    private JButton okButton, cancelButton;

    private SpaceMarine result;

    public SpaceMarineInputDialog(JFrame parent) {
        super(parent, "Add Space Marine", true);
        initComponents();
        layoutComponents();
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        nameField = createPlaceholderField("Enter name");
        xField = createPlaceholderField("Enter X coordinate");
        yField = createPlaceholderField("Enter Y coordinate");
        healthField = createPlaceholderField("Enter health");

        meleeWeaponCombo = new JComboBox<>(MeleeWeapon.values());
        weaponCombo = new JComboBox<>(Weapon.values());
        categoryCombo = new JComboBox<>(AstartesCategory.values());

        okButton = new JButton("OK");
        cancelButton = new JButton("Cancel");

        okButton.addActionListener(e -> onOK());
        cancelButton.addActionListener(e -> onCancel());

        getRootPane().setDefaultButton(okButton);
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

        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updatePlaceholder(field, placeholder);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updatePlaceholder(field, placeholder);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updatePlaceholder(field, placeholder);
            }
        });

        return field;
    }

    private void updatePlaceholder(JTextField field, String placeholder) {
        String text = field.getText();
        if (text.isEmpty()) {
            field.setForeground(Color.GRAY);
        } else if (!text.equals(placeholder)) {
            field.setForeground(Color.BLACK);
        }
    }

    private void layoutComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        int row = 0;

        gbc.gridy = row++;
        add(nameField, gbc);

        gbc.gridy = row++;
        add(xField, gbc);

        gbc.gridy = row++;
        add(yField, gbc);

        gbc.gridy = row++;
        add(healthField, gbc);

        gbc.gridy = row++;
        add(meleeWeaponCombo, gbc);

        gbc.gridy = row++;
        add(weaponCombo, gbc);

        gbc.gridy = row++;
        add(categoryCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, gbc);
    }

    private void onOK() {
        try {
            result = createSpaceMarineFromInput();
            if (result != null) {
                setVisible(false);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Please enter valid numbers for coordinates and health",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onCancel() {
        result = null;
        setVisible(false);
    }

    private SpaceMarine createSpaceMarineFromInput() throws ValidationException {
        String name = nameField.getText().trim();
        if (name.isEmpty() || name.equals("Enter name")) {
            throw new ValidationException("Name cannot be empty");
        }

        SpaceMarine marine = new SpaceMarine();
        marine.setName(name);

        Coordinates coords = new Coordinates();

        String xText = xField.getText().trim();
        if (xText.isEmpty() || xText.equals("Enter X coordinate")) {
            throw new ValidationException("X coordinate is required");
        }
        coords.setX(Long.parseLong(xText));

        String yText = yField.getText().trim();
        if (yText.isEmpty() || yText.equals("Enter Y coordinate")) {
            throw new ValidationException("Y coordinate is required");
        }
        coords.setY(Long.parseLong(yText));

        marine.setCoordinates(coords);

        String healthText = healthField.getText().trim();
        if (healthText.isEmpty() || healthText.equals("Enter health")) {
            throw new ValidationException("Health is required");
        }

        double health = Double.parseDouble(healthText);
        if (health <= 0) {
            throw new ValidationException("Health must be greater than 0");
        }
        marine.setHealth(health);

        marine.setMeleeWeapon((MeleeWeapon) meleeWeaponCombo.getSelectedItem());
        marine.setWeaponType((Weapon) weaponCombo.getSelectedItem());
        marine.setCategory((AstartesCategory) categoryCombo.getSelectedItem());

        return marine;
    }

    public SpaceMarine getSpaceMarine() {
        return result;
    }
}