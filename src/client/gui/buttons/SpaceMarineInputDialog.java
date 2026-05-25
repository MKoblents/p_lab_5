package client.gui.buttons;

import client.gui.utils.GuiUtils;
import client.utils.LocaleManager;
import shared.enums.AstartesCategory;
import shared.enums.MeleeWeapon;
import shared.enums.Weapon;
import shared.models.Coordinates;
import shared.models.SpaceMarine;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.xml.bind.ValidationException;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class SpaceMarineInputDialog extends JDialog {
    private JTextField nameField;
    private JTextField xField, yField;
    private JTextField healthField;
    private JComboBox<MeleeWeapon> meleeWeaponCombo;
    private JComboBox<Weapon> weaponCombo;
    private JComboBox<AstartesCategory> categoryCombo;
    private JButton okButton, cancelButton;
    private JLabel titleLabel;
    private JFrame parent;

    private SpaceMarine result;
    private final Dimension originalSize = new Dimension(500, 700);

    public SpaceMarineInputDialog(JFrame parent) {
        super(parent, true);
        this.parent = parent;
        initComponents();
        layoutComponents();
        applyTheme();
        pack();
        setSize(originalSize);
        setLocationRelativeTo(parent);
        setMinimumSize(new Dimension(600, 800));

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeComponents();
            }
        });
    }

    private void initComponents() {
        titleLabel = new JLabel(LocaleManager.get("dialog.add.title"), SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));

        nameField = createStyledTextField(LocaleManager.get("dialog.field.name"));
        xField = createStyledTextField(LocaleManager.get("dialog.field.x"));
        yField = createStyledTextField(LocaleManager.get("dialog.field.y"));
        healthField = createStyledTextField(LocaleManager.get("dialog.field.health"));

        meleeWeaponCombo = createStyledComboBox(MeleeWeapon.values());
        weaponCombo = createStyledComboBox(Weapon.values());
        categoryCombo = createStyledComboBox(AstartesCategory.values());

        okButton = createStyledButton(LocaleManager.get("button.ok"));
        cancelButton = createStyledButton(LocaleManager.get("button.cancel"));

        okButton.addActionListener(e -> onOK());
        cancelButton.addActionListener(e -> onCancel());

        getRootPane().setDefaultButton(okButton);
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField(placeholder);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(Color.GRAY);
        field.setHorizontalAlignment(JTextField.CENTER);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GuiUtils.PRIMARY_COLOR, 2),
                new EmptyBorder(10, 15, 10, 15)
        ));
        field.setBackground(Color.WHITE);
        field.setPreferredSize(new Dimension(0, 45));
        field.setMaximumSize(new Dimension(Short.MAX_VALUE, 45));

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                }
            }
        });

        return field;
    }

    private <T> JComboBox<T> createStyledComboBox(T[] items) {
        JComboBox<T> combo = new JComboBox<>(items);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBackground(Color.WHITE);
        combo.setForeground(Color.BLACK);
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GuiUtils.PRIMARY_COLOR, 2),
                new EmptyBorder(8, 10, 8, 10)
        ));
        combo.setPreferredSize(new Dimension(0, 45));
        combo.setMaximumSize(new Dimension(Short.MAX_VALUE, 45));
        combo.setCursor(new Cursor(Cursor.HAND_CURSOR));

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setFont(new Font("Segoe UI", Font.PLAIN, 14));
                if (isSelected) {
                    setBackground(GuiUtils.PRIMARY_COLOR);
                    setForeground(Color.WHITE);
                } else {
                    setBackground(Color.WHITE);
                    setForeground(Color.BLACK);
                }
                setBorder(new EmptyBorder(5, 10, 5, 10));
                return this;
            }
        });

        return combo;
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
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(GuiUtils.BACKGROUND_COLOR);
//        setBorder(new EmptyBorder(20, 30, 20, 30));

        // Title Panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        add(titlePanel, BorderLayout.NORTH);

        // Fields Panel
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setOpaque(false);
        fieldsPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        fieldsPanel.add(createLabeledField(LocaleManager.get("dialog.field.name"), nameField));
        fieldsPanel.add(Box.createVerticalStrut(15));
        fieldsPanel.add(createLabeledField(LocaleManager.get("dialog.field.x"), xField));
        fieldsPanel.add(Box.createVerticalStrut(15));
        fieldsPanel.add(createLabeledField(LocaleManager.get("dialog.field.y"), yField));
        fieldsPanel.add(Box.createVerticalStrut(15));
        fieldsPanel.add(createLabeledField(LocaleManager.get("dialog.field.health"), healthField));
        fieldsPanel.add(Box.createVerticalStrut(15));
        fieldsPanel.add(createLabeledCombo(LocaleManager.get("dialog.field.melee"), meleeWeaponCombo));
        fieldsPanel.add(Box.createVerticalStrut(15));
        fieldsPanel.add(createLabeledCombo(LocaleManager.get("dialog.field.weapon"), weaponCombo));
        fieldsPanel.add(Box.createVerticalStrut(15));
        fieldsPanel.add(createLabeledCombo(LocaleManager.get("dialog.field.category"), categoryCombo));

        JScrollPane scrollPane = new JScrollPane(fieldsPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createLabeledField(String label, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setOpaque(false);

        JLabel labelComponent = new JLabel(label, SwingConstants.CENTER);
        labelComponent.setFont(new Font("Segoe UI", Font.BOLD, 12));
        labelComponent.setForeground(GuiUtils.PRIMARY_DARK);

        panel.add(labelComponent, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLabeledCombo(String label, JComboBox<?> combo) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setOpaque(false);

        JLabel labelComponent = new JLabel(label, SwingConstants.CENTER);
        labelComponent.setFont(new Font("Segoe UI", Font.BOLD, 12));
        labelComponent.setForeground(GuiUtils.PRIMARY_DARK);

        panel.add(labelComponent, BorderLayout.NORTH);
        panel.add(combo, BorderLayout.CENTER);

        return panel;
    }

    private void applyTheme() {
        getContentPane().setBackground(GuiUtils.BACKGROUND_COLOR);
    }

    private void resizeComponents() {
        double scaleFactor = (double) getWidth() / originalSize.width;

        float titleSize = (float) (24 * scaleFactor);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, (int) titleSize));

        float fieldSize = (float) (14 * scaleFactor);
        float labelSize = (float) (12 * scaleFactor);

        nameField.setFont(new Font("Segoe UI", Font.PLAIN, (int) fieldSize));
        xField.setFont(new Font("Segoe UI", Font.PLAIN, (int) fieldSize));
        yField.setFont(new Font("Segoe UI", Font.PLAIN, (int) fieldSize));
        healthField.setFont(new Font("Segoe UI", Font.PLAIN, (int) fieldSize));

        meleeWeaponCombo.setFont(new Font("Segoe UI", Font.PLAIN, (int) fieldSize));
        weaponCombo.setFont(new Font("Segoe UI", Font.PLAIN, (int) fieldSize));
        categoryCombo.setFont(new Font("Segoe UI", Font.PLAIN, (int) fieldSize));

        okButton.setFont(new Font("Segoe UI", Font.BOLD, (int) fieldSize));
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, (int) fieldSize));
    }

    private void onOK() {
        try {
            result = createSpaceMarineFromInput();
            if (result != null) {
                setVisible(false);
            }
        } catch (NumberFormatException ex) {
            GuiUtils.showMessageDialog(parent,
                    LocaleManager.get("dialog.error.title"),
                    LocaleManager.get("dialog.error.number"),
                    GuiUtils.MessageType.ERROR);
        } catch (ValidationException ex) {
            GuiUtils.showMessageDialog(parent,
                    LocaleManager.get("dialog.error.title"),
                    ex.getMessage(),
                    GuiUtils.MessageType.ERROR);
        }
    }

    private void onCancel() {
        result = null;
        setVisible(false);
    }

    private SpaceMarine createSpaceMarineFromInput() throws ValidationException {
        String name = nameField.getText().trim();
        String placeholder = LocaleManager.get("dialog.field.name");
        if (name.isEmpty() || name.equals(placeholder)) {
            throw new ValidationException(LocaleManager.get("validation.name.empty"));
        }

        SpaceMarine marine = new SpaceMarine();
        marine.setName(name);

        Coordinates coords = new Coordinates();

        String xText = xField.getText().trim();
        String xPlaceholder = LocaleManager.get("dialog.field.x");
        if (xText.isEmpty() || xText.equals(xPlaceholder)) {
            throw new ValidationException(LocaleManager.get("validation.x.required"));
        }
        coords.setX(Long.parseLong(xText));

        String yText = yField.getText().trim();
        String yPlaceholder = LocaleManager.get("dialog.field.y");
        if (yText.isEmpty() || yText.equals(yPlaceholder)) {
            throw new ValidationException(LocaleManager.get("validation.y.required"));
        }
        coords.setY(Long.parseLong(yText));

        marine.setCoordinates(coords);

        String healthText = healthField.getText().trim();
        String healthPlaceholder = LocaleManager.get("dialog.field.health");
        if (healthText.isEmpty() || healthText.equals(healthPlaceholder)) {
            throw new ValidationException(LocaleManager.get("validation.health.required"));
        }

        double health = Double.parseDouble(healthText);
        if (health <= 0) {
            throw new ValidationException(LocaleManager.get("validation.health.positive"));
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