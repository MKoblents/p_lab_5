package client.gui.buttons;

import client.gui.utils.GuiUtils;
import client.gui.window.SpaceMarineTable;
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

public class SpaceMarineUpdateDialog extends AbstractStyledDialog {

    private SpaceMarineSelector selector;
    private JTextField nameField, xField, yField, healthField;
    private JComboBox<MeleeWeapon> meleeWeaponCombo;
    private JComboBox<Weapon> weaponCombo;
    private JComboBox<AstartesCategory> categoryCombo;
    private JTextField chapterNameField, chapterParentLegionField, chapterWorldField;

    private final SpaceMarineTable tableModel;
    private final String currentUsername;
    private SpaceMarine result;

    public SpaceMarineUpdateDialog(JFrame parent, SpaceMarineTable tableModel, String currentUsername) {
        super(parent, "dialog.update.title", true, 550, 800);
        this.tableModel = tableModel;
        this.currentUsername = currentUsername;
        refreshSelector(tableModel);
    }

    @Override
    protected void initComponents() {
        selector = new SpaceMarineSelector();

        nameField = GuiUtils.createStyledPlaceholderField("dialog.field.name", 45);
        xField = GuiUtils.createStyledPlaceholderField("dialog.field.x", 45);
        yField = GuiUtils.createStyledPlaceholderField("dialog.field.y", 45);
        healthField = GuiUtils.createStyledPlaceholderField("dialog.field.health", 45);

        meleeWeaponCombo = GuiUtils.createStyledComboBox(MeleeWeapon.values(), 45);
        weaponCombo = GuiUtils.createStyledComboBox(Weapon.values(), 45);
        categoryCombo = GuiUtils.createStyledComboBox(AstartesCategory.values(), 45);

        chapterNameField = GuiUtils.createStyledPlaceholderField("dialog.field.chapter.name", 45);
        chapterParentLegionField = GuiUtils.createStyledPlaceholderField("dialog.field.chapter.parentLegion", 45);
        chapterWorldField = GuiUtils.createStyledPlaceholderField("dialog.field.chapter.world", 45);

        selector.getComboBox().addActionListener(e -> {
            SpaceMarine selected = selector.getSelectedSpaceMarine();
            if (selected != null) {
                populateFields(selected);
            }
        });
    }

    @Override
    protected void layoutComponents() {
        JPanel selectorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        selectorPanel.setOpaque(false);
        selectorPanel.add(selector);
        add(selectorPanel, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setOpaque(false);
        fieldsPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        fieldsPanel.add(GuiUtils.createLabeledInputPanel("dialog.field.name", nameField));
        fieldsPanel.add(Box.createVerticalStrut(15));
        fieldsPanel.add(GuiUtils.createLabeledInputPanel("dialog.field.x", xField));
        fieldsPanel.add(Box.createVerticalStrut(15));
        fieldsPanel.add(GuiUtils.createLabeledInputPanel("dialog.field.y", yField));
        fieldsPanel.add(Box.createVerticalStrut(15));
        fieldsPanel.add(GuiUtils.createLabeledInputPanel("dialog.field.health", healthField));
        fieldsPanel.add(Box.createVerticalStrut(15));
        fieldsPanel.add(GuiUtils.createLabeledInputPanel("dialog.field.melee", meleeWeaponCombo));
        fieldsPanel.add(Box.createVerticalStrut(15));
        fieldsPanel.add(GuiUtils.createLabeledInputPanel("dialog.field.weapon", weaponCombo));
        fieldsPanel.add(Box.createVerticalStrut(15));
        fieldsPanel.add(GuiUtils.createLabeledInputPanel("dialog.field.category", categoryCombo));
        fieldsPanel.add(Box.createVerticalStrut(20));
        fieldsPanel.add(createChapterSection());

        add(createScrollableContentPanel(fieldsPanel), BorderLayout.CENTER);

        createStandardButtons(this::onOK, this::onCancel);
    }

    @Override
    protected void resizeComponents() {
        float scaledSize = scaleFontSize(14);

        nameField.setFont(new Font("Segoe UI", Font.PLAIN, (int) scaledSize));
        xField.setFont(new Font("Segoe UI", Font.PLAIN, (int) scaledSize));
        yField.setFont(new Font("Segoe UI", Font.PLAIN, (int) scaledSize));
        healthField.setFont(new Font("Segoe UI", Font.PLAIN, (int) scaledSize));
        chapterNameField.setFont(new Font("Segoe UI", Font.PLAIN, (int) scaledSize));
        chapterParentLegionField.setFont(new Font("Segoe UI", Font.PLAIN, (int) scaledSize));
        chapterWorldField.setFont(new Font("Segoe UI", Font.PLAIN, (int) scaledSize));

        meleeWeaponCombo.setFont(new Font("Segoe UI", Font.PLAIN, (int) scaledSize));
        weaponCombo.setFont(new Font("Segoe UI", Font.PLAIN, (int) scaledSize));
        categoryCombo.setFont(new Font("Segoe UI", Font.PLAIN, (int) scaledSize));
    }

    private JPanel createChapterSection() {
        return createTitledSection("dialog.section.chapter",
                GuiUtils.createLabeledInputPanel("dialog.field.chapter.name", chapterNameField),
                GuiUtils.createLabeledInputPanel("dialog.field.chapter.parentLegion", chapterParentLegionField),
                GuiUtils.createLabeledInputPanel("dialog.field.chapter.world", chapterWorldField)
        );
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

        Chapter chapter = marine.getChapter();
        if (chapter != null) {
            setFieldText(chapterNameField, chapter.getName(), LocaleManager.get("dialog.field.chapter.name"));
            setFieldText(chapterWorldField, chapter.getWorld(), LocaleManager.get("dialog.field.chapter.world"));

            String parentLegion = chapter.getParentLegion();
            String placeholder = LocaleManager.get("dialog.field.chapter.parentLegion");
            if (parentLegion != null && !parentLegion.isEmpty()) {
                chapterParentLegionField.setText(parentLegion);
                chapterParentLegionField.setForeground(Color.BLACK);
            } else {
                chapterParentLegionField.setText(placeholder);
                chapterParentLegionField.setForeground(Color.GRAY);
            }
        } else {
            clearChapterFields();
        }
    }

    private void clearChapterFields() {
        String namePlaceholder = LocaleManager.get("dialog.field.chapter.name");
        String worldPlaceholder = LocaleManager.get("dialog.field.chapter.world");
        String legionPlaceholder = LocaleManager.get("dialog.field.chapter.parentLegion");

        chapterNameField.setText(namePlaceholder);
        chapterNameField.setForeground(Color.GRAY);
        chapterWorldField.setText(worldPlaceholder);
        chapterWorldField.setForeground(Color.GRAY);
        chapterParentLegionField.setText(legionPlaceholder);
        chapterParentLegionField.setForeground(Color.GRAY);
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
            JOptionPane.showMessageDialog(this,
                    LocaleManager.get("dialog.error.select"),
                    LocaleManager.get("dialog.error.title"),
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            result = buildUpdatedSpaceMarine(selected.getId());
            if (result != null) {
                setVisible(false);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    LocaleManager.get("dialog.error.number"),
                    LocaleManager.get("dialog.error.title"),
                    JOptionPane.ERROR_MESSAGE);
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    LocaleManager.get("dialog.error.title"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onCancel() {
        result = null;
        setVisible(false);
    }

    private SpaceMarine buildUpdatedSpaceMarine(long id) throws ValidationException {
        String name = nameField.getText().trim();
        String placeholder = LocaleManager.get("dialog.field.name");
        if (name.isEmpty() || name.equals(placeholder)) {
            throw new ValidationException(LocaleManager.get("validation.name.empty"));
        }

        String xText = xField.getText().trim();
        String xPlaceholder = LocaleManager.get("dialog.field.x");
        if (xText.isEmpty() || xText.equals(xPlaceholder)) {
            throw new ValidationException(LocaleManager.get("validation.x.required"));
        }
        long x = Long.parseLong(xText);

        String yText = yField.getText().trim();
        String yPlaceholder = LocaleManager.get("dialog.field.y");
        if (yText.isEmpty() || yText.equals(yPlaceholder)) {
            throw new ValidationException(LocaleManager.get("validation.y.required"));
        }
        long y = Long.parseLong(yText);

        String healthText = healthField.getText().trim();
        String healthPlaceholder = LocaleManager.get("dialog.field.health");
        if (healthText.isEmpty() || healthText.equals(healthPlaceholder)) {
            throw new ValidationException(LocaleManager.get("validation.health.required"));
        }
        double health = Double.parseDouble(healthText);
        if (health <= 0) {
            throw new ValidationException(LocaleManager.get("validation.health.positive"));
        }

        SpaceMarine updated = new SpaceMarine();
        updated.setId(id);
        updated.setName(name);

        Coordinates coords = new Coordinates();
        coords.setX(x);
        coords.setY(y);
        updated.setCoordinates(coords);

        updated.setHealth(health);
        updated.setMeleeWeapon((MeleeWeapon) meleeWeaponCombo.getSelectedItem());
        updated.setWeaponType((Weapon) weaponCombo.getSelectedItem());
        updated.setCategory((AstartesCategory) categoryCombo.getSelectedItem());

        String chapterName = chapterNameField.getText().trim();
        String chapterNamePlaceholder = LocaleManager.get("dialog.field.chapter.name");
        if (!chapterName.isEmpty() && !chapterName.equals(chapterNamePlaceholder)) {
            String chapterWorld = chapterWorldField.getText().trim();
            String chapterWorldPlaceholder = LocaleManager.get("dialog.field.chapter.world");

            if (chapterWorld.isEmpty() || chapterWorld.equals(chapterWorldPlaceholder)) {
                throw new ValidationException(LocaleManager.get("validation.chapter.world.required"));
            }

            Chapter chapter = new Chapter();
            chapter.setName(chapterName);

            String parentLegion = chapterParentLegionField.getText().trim();
            String parentLegionPlaceholder = LocaleManager.get("dialog.field.chapter.parentLegion");
            chapter.setParentLegion(parentLegion.isEmpty() || parentLegion.equals(parentLegionPlaceholder) ? null : parentLegion);

            chapter.setWorld(chapterWorld);
            updated.setChapter(chapter);
        } else {
            updated.setChapter(null);
        }

        return updated;
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