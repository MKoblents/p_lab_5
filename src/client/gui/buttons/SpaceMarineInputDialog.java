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
    private JTextField chapterNameField, chapterParentLegionField, chapterWorldField;

    private SpaceMarine result;
    private final JFrame parent;

    public SpaceMarineInputDialog(JFrame parent) {
        super(parent, "dialog.add.title", true, 500, 700);
        this.parent = parent;
        }

    @Override
    protected void initComponents() {
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
    }

    @Override
    protected void layoutComponents() {
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
        float labelSize = scaleFontSize(12);

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
            marine.setChapter(chapter);
        }
        return marine;
    }

    public SpaceMarine getSpaceMarine() {
        return result;
    }
}