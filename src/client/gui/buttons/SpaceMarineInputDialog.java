package client.gui.buttons;

import client.gui.utils.GuiUtils;
import client.utils.LocaleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(SpaceMarineInputDialog.class);

    private static final int FIELD_HEIGHT = 45;
    private static final int COMBO_HEIGHT = 60;
    private static final int BASE_LABEL_FONT_SIZE = 25;
    private static final int BASE_FIELD_FONT_SIZE = 14;
    private static final int STRUT_SIZE = 15;
    private static final int CHAPTER_STRUT_SIZE = 20;

    private JTextField nameField;
    private JTextField xField, yField;
    private JTextField healthField;
    private JComboBox<MeleeWeapon> meleeWeaponCombo;
    private JComboBox<Weapon> weaponCombo;
    private JComboBox<AstartesCategory> categoryCombo;
    private ChapterInputPanel chapterPanel;

    private JLabel nameLabel, xLabel, yLabel, healthLabel, meleeLabel, weaponLabel, categoryLabel;

    private SpaceMarine result;
    private final JFrame parent;

    public SpaceMarineInputDialog(JFrame parent) {
        super(parent, "dialog.add.title", true, 800, 700);
        this.parent = parent;
    }

    @Override
    protected void initComponents() {
        nameField = GuiUtils.createStyledPlaceholderField("dialog.field.name", FIELD_HEIGHT);
        xField = GuiUtils.createStyledPlaceholderField("dialog.field.x", FIELD_HEIGHT);
        yField = GuiUtils.createStyledPlaceholderField("dialog.field.y", FIELD_HEIGHT);
        healthField = GuiUtils.createStyledPlaceholderField("dialog.field.health", FIELD_HEIGHT);

        meleeWeaponCombo = GuiUtils.createStyledComboBox(MeleeWeapon.values(), COMBO_HEIGHT);
        weaponCombo = GuiUtils.createStyledComboBox(Weapon.values(), COMBO_HEIGHT);
        categoryCombo = GuiUtils.createStyledComboBox(AstartesCategory.values(), COMBO_HEIGHT);

        chapterPanel = new ChapterInputPanel();

        nameLabel = createStyledLabel("dialog.field.name", BASE_LABEL_FONT_SIZE);
        xLabel = createStyledLabel("dialog.field.x", BASE_LABEL_FONT_SIZE);
        yLabel = createStyledLabel("dialog.field.y", BASE_LABEL_FONT_SIZE);
        healthLabel = createStyledLabel("dialog.field.health", BASE_LABEL_FONT_SIZE);
        meleeLabel = createStyledLabel("dialog.field.melee", BASE_LABEL_FONT_SIZE);
        weaponLabel = createStyledLabel("dialog.field.weapon", BASE_LABEL_FONT_SIZE);
        categoryLabel = createStyledLabel("dialog.field.category", BASE_LABEL_FONT_SIZE);
    }

    @Override
    protected void layoutComponents() {
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setOpaque(false);
        fieldsPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        fieldsPanel.add(createLabeledPanel(nameLabel, nameField));
        fieldsPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        fieldsPanel.add(createLabeledPanel(xLabel, xField));
        fieldsPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        fieldsPanel.add(createLabeledPanel(yLabel, yField));
        fieldsPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        fieldsPanel.add(createLabeledPanel(healthLabel, healthField));
        fieldsPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        fieldsPanel.add(createLabeledPanel(meleeLabel, meleeWeaponCombo));
        fieldsPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        fieldsPanel.add(createLabeledPanel(weaponLabel, weaponCombo));
        fieldsPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        fieldsPanel.add(createLabeledPanel(categoryLabel, categoryCombo));
        fieldsPanel.add(Box.createVerticalStrut(CHAPTER_STRUT_SIZE));
        fieldsPanel.add(chapterPanel);

        add(createScrollableContentPanel(fieldsPanel), BorderLayout.CENTER);

        createStandardButtons(this::onOK, this::onCancel);
    }

    @Override
    protected void resizeComponents() {
        float scaledSize = scaleFontSize(BASE_FIELD_FONT_SIZE);
        float scaledLabelSize = scaleFontSize(BASE_LABEL_FONT_SIZE);

        Font labelFont = new Font("Segoe UI", Font.BOLD, (int) scaledLabelSize);
        nameLabel.setFont(labelFont);
        xLabel.setFont(labelFont);
        yLabel.setFont(labelFont);
        healthLabel.setFont(labelFont);
        meleeLabel.setFont(labelFont);
        weaponLabel.setFont(labelFont);
        categoryLabel.setFont(labelFont);

        Font fieldFont = new Font("Segoe UI", Font.PLAIN, (int) scaledSize);
        nameField.setFont(fieldFont);
        xField.setFont(fieldFont);
        yField.setFont(fieldFont);
        healthField.setFont(fieldFont);
        meleeWeaponCombo.setFont(fieldFont);
        weaponCombo.setFont(fieldFont);
        categoryCombo.setFont(fieldFont);

        double currentScaleFactor = (double) getWidth() / originalSize.width;
        chapterPanel.scaleFonts(currentScaleFactor);
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
                logger.info("Successfully created new SpaceMarine: {}", result.getName());
                dispose();
            }
        } catch (NumberFormatException ex) {
            logger.warn("Number format error during SpaceMarine creation", ex);
            GuiUtils.showMessageDialog(parent,
                    LocaleManager.get("dialog.error.title"),
                    LocaleManager.get("dialog.error.number"),
                    GuiUtils.MessageType.ERROR);
        } catch (ValidationException ex) {
            logger.warn("Validation failed during SpaceMarine creation: {}", ex.getMessage());
            GuiUtils.showMessageDialog(parent,
                    LocaleManager.get("dialog.error.title"),
                    ex.getMessage(),
                    GuiUtils.MessageType.ERROR);
        }
    }

    private void onCancel() {
        logger.debug("SpaceMarine creation cancelled by user.");
        result = null;
        dispose();
    }

    private SpaceMarine createSpaceMarineFromInput() throws ValidationException {
        SpaceMarine marine = new SpaceMarine();
        marine.setName(parseStringField(nameField, "validation.name.empty"));

        Coordinates coords = new Coordinates();
        coords.setX(parseLongField(xField, "validation.x.required"));
        coords.setY(parseLongField(yField, "validation.y.required"));
        marine.setCoordinates(coords);

        marine.setHealth(parseDoubleField(healthField, "validation.health.required", true));

        marine.setMeleeWeapon((MeleeWeapon) meleeWeaponCombo.getSelectedItem());
        marine.setWeaponType((Weapon) weaponCombo.getSelectedItem());
        marine.setCategory((AstartesCategory) categoryCombo.getSelectedItem());
        marine.setChapter(chapterPanel.getChapter());

        return marine;
    }

    private String parseStringField(JTextField field, String emptyKey) throws ValidationException {
        if (field.getForeground() == Color.GRAY || field.getText().trim().isEmpty()) {
            throw new ValidationException(LocaleManager.get(emptyKey));
        }
        return field.getText().trim();
    }

    private long parseLongField(JTextField field, String emptyKey) throws ValidationException {
        if (field.getForeground() == Color.GRAY || field.getText().trim().isEmpty()) {
            throw new ValidationException(LocaleManager.get(emptyKey));
        }
        try {
            return Long.parseLong(field.getText().trim());
        } catch (NumberFormatException e) {
            throw new ValidationException(LocaleManager.get("dialog.error.number"));
        }
    }

    private double parseDoubleField(JTextField field, String emptyKey, boolean mustBePositive) throws ValidationException {
        if (field.getForeground() == Color.GRAY || field.getText().trim().isEmpty()) {
            throw new ValidationException(LocaleManager.get(emptyKey));
        }
        try {
            double val = Double.parseDouble(field.getText().trim());
            if (mustBePositive && val <= 0) {
                throw new ValidationException(LocaleManager.get("validation.health.positive"));
            }
            return val;
        } catch (NumberFormatException e) {
            throw new ValidationException(LocaleManager.get("dialog.error.number"));
        }
    }

    public SpaceMarine getSpaceMarine() {
        return result;
    }
}