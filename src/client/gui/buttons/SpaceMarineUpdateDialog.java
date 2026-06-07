package client.gui.buttons;

import client.gui.utils.GuiUtils;
import client.gui.window.SpaceMarineTable;
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

public class SpaceMarineUpdateDialog extends AbstractStyledDialog {

    private static final Logger logger = LoggerFactory.getLogger(SpaceMarineUpdateDialog.class);

    private static final int FIELD_HEIGHT = 45;
    private static final int COMBO_HEIGHT = 60;
    private static final int BASE_LABEL_FONT_SIZE = 25;
    private static final int BASE_FIELD_FONT_SIZE = 14;
    private static final int STRUT_SIZE = 15;
    private static final int CHAPTER_STRUT_SIZE = 20;

    private SpaceMarineSelector selector;
    private JTextField nameField, xField, yField, healthField;
    private JComboBox<MeleeWeapon> meleeWeaponCombo;
    private JComboBox<Weapon> weaponCombo;
    private JComboBox<AstartesCategory> categoryCombo;

    private final SpaceMarineTable tableModel;
    private ChapterInputPanel chapterPanel;
    private final String currentUsername;
    private SpaceMarine result;

    private JLabel nameLabel, xLabel, yLabel, healthLabel, meleeLabel, weaponLabel, categoryLabel;

    public SpaceMarineUpdateDialog(JFrame parent, SpaceMarineTable tableModel, String currentUsername) {
        super(parent, "dialog.update.title", true, 800, 800);
        this.tableModel = tableModel;
        this.currentUsername = currentUsername;
        refreshSelector(tableModel);
    }

    @Override
    protected void initComponents() {
        selector = new SpaceMarineSelector();

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

        selector.getComboBox().addActionListener(e -> {
            SpaceMarine selected = selector.getSelectedSpaceMarine();
            if (selected != null) {
                populateFields(selected);
            }
        });
    }

    @Override
    protected void layoutComponents() {
        JPanel selectorPanel = GuiUtils.createPanel(new FlowLayout(FlowLayout.CENTER));
        selectorPanel.setOpaque(false);
        selectorPanel.add(selector);
        add(selectorPanel, BorderLayout.NORTH);

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

        double currentScaleFactor = (double) getWidth() / originalSize.width;
        chapterPanel.scaleFonts(currentScaleFactor);

        meleeWeaponCombo.setFont(fieldFont);
        weaponCombo.setFont(fieldFont);
        categoryCombo.setFont(fieldFont);
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

    public void refreshSelector(SpaceMarineTable tableModel) {
        selector.setCurrentUsername(currentUsername);
        selector.refreshData(tableModel);
    }

    private void populateFields(SpaceMarine marine) {
        setFieldText(nameField, marine.getName(), LocaleManager.get("dialog.field.name"));
        setFieldText(xField, String.valueOf(marine.getCoordinates().getX()), LocaleManager.get("dialog.field.x"));
        setFieldText(yField, String.valueOf(marine.getCoordinates().getY()), LocaleManager.get("dialog.field.y"));
        setFieldText(healthField, String.valueOf(marine.getHealth()), LocaleManager.get("dialog.field.health"));

        meleeWeaponCombo.setSelectedItem(marine.getMeleeWeapon());
        weaponCombo.setSelectedItem(marine.getWeaponType());
        categoryCombo.setSelectedItem(marine.getCategory());

        chapterPanel.setChapter(marine.getChapter());
    }

    private void setFieldText(JTextField field, String value, String placeholder) {
        if (value != null && !value.isEmpty() && !value.equals(placeholder)) {
            field.setText(value);
            field.setForeground(Color.BLACK);
        } else {
            field.setText(placeholder);
            field.setForeground(Color.GRAY);
        }
    }

    private void onOK() {
        SpaceMarine selected = selector.getSelectedSpaceMarine();
        if (selected == null) {
            GuiUtils.showMessageDialog(null,
                    LocaleManager.get("dialog.error.title"),
                    LocaleManager.get("dialog.error.select"),
                    GuiUtils.MessageType.WARNING);
            return;
        }
        try {
            result = buildUpdatedSpaceMarine(selected.getId());
            if (result != null) {
                logger.info("Successfully prepared update for SpaceMarine ID: {}", selected.getId());
                dispose();
            }
        } catch (NumberFormatException ex) {
            logger.warn("Number format error during SpaceMarine update", ex);
            GuiUtils.showMessageDialog(null,
                    LocaleManager.get("dialog.error.title"),
                    LocaleManager.get("dialog.error.number"),
                    GuiUtils.MessageType.ERROR);
        } catch (ValidationException ex) {
            logger.warn("Validation failed during SpaceMarine update: {}", ex.getMessage());
            GuiUtils.showMessageDialog(null,
                    LocaleManager.get("dialog.error.title"),
                    ex.getMessage(),
                    GuiUtils.MessageType.ERROR);
        }
    }

    private void onCancel() {
        logger.debug("SpaceMarine update cancelled by user.");
        result = null;
        dispose();
    }

    private SpaceMarine buildUpdatedSpaceMarine(long id) throws ValidationException {
        SpaceMarine updated = new SpaceMarine();
        updated.setId(id);
        updated.setName(parseStringField(nameField, "validation.name.empty"));

        Coordinates coords = new Coordinates();
        coords.setX(parseLongField(xField, "validation.x.required"));
        coords.setY(parseLongField(yField, "validation.y.required"));
        updated.setCoordinates(coords);

        updated.setHealth(parseDoubleField(healthField, "validation.health.required", true));

        updated.setMeleeWeapon((MeleeWeapon) meleeWeaponCombo.getSelectedItem());
        updated.setWeaponType((Weapon) weaponCombo.getSelectedItem());
        updated.setCategory((AstartesCategory) categoryCombo.getSelectedItem());
        updated.setChapter(chapterPanel.getChapter());

        return updated;
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

    public void setSelectedMarine(SpaceMarine marine) {
        if (marine == null) return;
        selector.refreshData(tableModel);
        selector.getComboBox().setSelectedItem(marine);
        populateFields(marine);
    }

    public SpaceMarine getUpdatedSpaceMarine() {
        return result;
    }
}