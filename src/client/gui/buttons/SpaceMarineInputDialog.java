package client.gui.buttons;

import client.gui.utils.GuiUtils;
import client.utils.LocaleManager;
import shared.enums.AstartesCategory;
import shared.enums.MeleeWeapon;
import shared.enums.Weapon;
import shared.models.Chapter;
import shared.models.Coordinates;
import shared.models.SpaceMarine;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.xml.bind.ValidationException;
import java.awt.*;

public class SpaceMarineInputDialog extends AbstractStyledDialog {
    private JTextField nameField;
    private JTextField xField, yField;
    private JTextField healthField;
    private JComboBox<MeleeWeapon> meleeWeaponCombo;
    private JComboBox<Weapon> weaponCombo;
    private JComboBox<AstartesCategory> categoryCombo;
    private ChapterInputPanel chapterPanel;

    // Label references for resizing
    private JLabel nameLabel, xLabel, yLabel, healthLabel, meleeLabel, weaponLabel, categoryLabel;

    private SpaceMarine result;
    private final JFrame parent;

    public SpaceMarineInputDialog(JFrame parent) {
        super(parent, "dialog.add.title", true, 800, 700);
        this.parent = parent;
    }

    @Override
    protected void initComponents() {
        nameField = GuiUtils.createStyledPlaceholderField("dialog.field.name", 45);
        xField = GuiUtils.createStyledPlaceholderField("dialog.field.x", 45);
        yField = GuiUtils.createStyledPlaceholderField("dialog.field.y", 45);
        healthField = GuiUtils.createStyledPlaceholderField("dialog.field.health", 45);

        meleeWeaponCombo = GuiUtils.createStyledComboBox(MeleeWeapon.values(), 60);
        weaponCombo = GuiUtils.createStyledComboBox(Weapon.values(), 60);
        categoryCombo = GuiUtils.createStyledComboBox(AstartesCategory.values(), 60);

        chapterPanel = new ChapterInputPanel();

        nameLabel = createStyledLabel("dialog.field.name", 25);
        xLabel = createStyledLabel("dialog.field.x", 25);
        yLabel = createStyledLabel("dialog.field.y", 25);
        healthLabel = createStyledLabel("dialog.field.health", 25);
        meleeLabel = createStyledLabel("dialog.field.melee", 25);
        weaponLabel = createStyledLabel("dialog.field.weapon", 25);
        categoryLabel = createStyledLabel("dialog.field.category", 25);
    }

    @Override
    protected void layoutComponents() {
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setOpaque(false);
        fieldsPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        fieldsPanel.add(createLabeledPanel(nameLabel, nameField));
        fieldsPanel.add(Box.createVerticalStrut(15));
        fieldsPanel.add(createLabeledPanel(xLabel, xField));
        fieldsPanel.add(Box.createVerticalStrut(15));
        fieldsPanel.add(createLabeledPanel(yLabel, yField));
        fieldsPanel.add(Box.createVerticalStrut(15));
        fieldsPanel.add(createLabeledPanel(healthLabel, healthField));
        fieldsPanel.add(Box.createVerticalStrut(15));
        fieldsPanel.add(createLabeledPanel(meleeLabel, meleeWeaponCombo));
        fieldsPanel.add(Box.createVerticalStrut(15));
        fieldsPanel.add(createLabeledPanel(weaponLabel, weaponCombo));
        fieldsPanel.add(Box.createVerticalStrut(15));
        fieldsPanel.add(createLabeledPanel(categoryLabel, categoryCombo));
        fieldsPanel.add(Box.createVerticalStrut(20));
        fieldsPanel.add(chapterPanel);

        add(createScrollableContentPanel(fieldsPanel), BorderLayout.CENTER);

        createStandardButtons(this::onOK, this::onCancel);
    }

    @Override
    protected void resizeComponents() {
        float scaledSize = scaleFontSize(14);
        float scaledLabelSize = scaleFontSize(25);

        // Resize labels
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, (int) scaledLabelSize));
        xLabel.setFont(new Font("Segoe UI", Font.BOLD, (int) scaledLabelSize));
        yLabel.setFont(new Font("Segoe UI", Font.BOLD, (int) scaledLabelSize));
        healthLabel.setFont(new Font("Segoe UI", Font.BOLD, (int) scaledLabelSize));
        meleeLabel.setFont(new Font("Segoe UI", Font.BOLD, (int) scaledLabelSize));
        weaponLabel.setFont(new Font("Segoe UI", Font.BOLD, (int) scaledLabelSize));
        categoryLabel.setFont(new Font("Segoe UI", Font.BOLD, (int) scaledLabelSize));

        // Resize fields
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, (int) scaledSize));
        xField.setFont(new Font("Segoe UI", Font.PLAIN, (int) scaledSize));
        yField.setFont(new Font("Segoe UI", Font.PLAIN, (int) scaledSize));
        healthField.setFont(new Font("Segoe UI", Font.PLAIN, (int) scaledSize));

        chapterPanel.scaleFonts(scaleFontSize(getWidth()/originalSize.width));

        meleeWeaponCombo.setFont(new Font("Segoe UI", Font.PLAIN, (int) scaledSize));
        weaponCombo.setFont(new Font("Segoe UI", Font.PLAIN, (int) scaledSize));
        categoryCombo.setFont(new Font("Segoe UI", Font.PLAIN, (int) scaledSize));
    }

    private JLabel createStyledLabel(String localeKey, int baseFontSize) {
        JLabel label = new JLabel(LocaleManager.get(localeKey), SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, baseFontSize));
        label.setForeground(GuiUtils.PRIMARY_DARK);
        return label;
    }

    private JPanel createLabeledPanel(JLabel label, JComponent field) {
        JPanel panel = GuiUtils.createPanel(new BorderLayout(5, 5));
        panel.setOpaque(false);
        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
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
        marine.setChapter(chapterPanel.getChapter());
        return marine;
    }

    public SpaceMarine getSpaceMarine() {
        return result;
    }
}