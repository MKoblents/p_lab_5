package client.gui.buttons;

import client.gui.utils.GuiUtils;
import client.utils.LocaleManager;
import shared.models.Chapter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Reusable panel for Chapter input with validation support.
 */
public class ChapterInputPanel extends JPanel {

    private final JTextField nameField;
    private final JTextField parentLegionField;
    private final JTextField worldField;

    public ChapterInputPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(GuiUtils.PRIMARY_COLOR, 1),
                LocaleManager.get("dialog.section.chapter"),
                SwingConstants.LEFT,
                SwingConstants.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                GuiUtils.PRIMARY_DARK
        ));

        nameField = GuiUtils.createStyledPlaceholderField("dialog.field.chapter.name", 45);
        parentLegionField = GuiUtils.createStyledPlaceholderField("dialog.field.chapter.parentLegion", 45);
        worldField = GuiUtils.createStyledPlaceholderField("dialog.field.chapter.world", 45);

        add(GuiUtils.createLabeledInputPanel("dialog.field.chapter.name", nameField));
        add(Box.createVerticalStrut(10));
        add(GuiUtils.createLabeledInputPanel("dialog.field.chapter.parentLegion", parentLegionField));
        add(Box.createVerticalStrut(10));
        add(GuiUtils.createLabeledInputPanel("dialog.field.chapter.world", worldField));
    }

    /**
     * Populates fields from Chapter object.
     */
    public void setChapter(Chapter chapter) {
        if (chapter != null) {
            populateField(nameField, chapter.getName(), "dialog.field.chapter.name");
            populateField(worldField, chapter.getWorld(), "dialog.field.chapter.world");

            String parentLegion = chapter.getParentLegion();
            String placeholder = LocaleManager.get("dialog.field.chapter.parentLegion");
            if (parentLegion != null && !parentLegion.isEmpty()) {
                parentLegionField.setText(parentLegion);
                parentLegionField.setForeground(Color.BLACK);
            } else {
                parentLegionField.setText(placeholder);
                parentLegionField.setForeground(Color.GRAY);
            }
        } else {
            clearFields();
        }
    }

    /**
     * Creates Chapter from input, or null if name is empty.
     * @throws javax.xml.bind.ValidationException if name provided but world missing
     */
    public Chapter getChapter() throws javax.xml.bind.ValidationException {
        String name = nameField.getText().trim();
        String namePlaceholder = LocaleManager.get("dialog.field.chapter.name");

        if (name.isEmpty() || name.equals(namePlaceholder)) {
            return null; // Chapter is optional
        }

        String world = worldField.getText().trim();
        String worldPlaceholder = LocaleManager.get("dialog.field.chapter.world");

        if (world.isEmpty() || world.equals(worldPlaceholder)) {
            throw new javax.xml.bind.ValidationException(
                    LocaleManager.get("validation.chapter.world.required"));
        }

        Chapter chapter = new Chapter();
        chapter.setName(name);

        String parentLegion = parentLegionField.getText().trim();
        String parentLegionPlaceholder = LocaleManager.get("dialog.field.chapter.parentLegion");
        chapter.setParentLegion(
                parentLegion.isEmpty() || parentLegion.equals(parentLegionPlaceholder) ? null : parentLegion);

        chapter.setWorld(world);
        return chapter;
    }

    /**
     * Scales fonts for responsive design.
     */
    public void scaleFonts(float fontSize) {
        Font scaledFont = new Font("Segoe UI", Font.PLAIN, (int)fontSize);
        nameField.setFont(scaledFont);
        parentLegionField.setFont(scaledFont);
        worldField.setFont(scaledFont);
    }

    private void populateField(JTextField field, String value, String placeholderKey) {
        String placeholder = LocaleManager.get(placeholderKey);
        if (value != null && !value.isEmpty() && !value.equals(placeholder)) {
            field.setText(value);
            field.setForeground(Color.BLACK);
        } else {
            field.setText(placeholder);
            field.setForeground(Color.GRAY);
        }
    }

    private void clearFields() {
        nameField.setText(LocaleManager.get("dialog.field.chapter.name"));
        nameField.setForeground(Color.GRAY);
        parentLegionField.setText(LocaleManager.get("dialog.field.chapter.parentLegion"));
        parentLegionField.setForeground(Color.GRAY);
        worldField.setText(LocaleManager.get("dialog.field.chapter.world"));
        worldField.setForeground(Color.GRAY);
    }

    public JTextField getNameField() { return nameField; }
    public JTextField getWorldField() { return worldField; }
    public JTextField getParentLegionField() { return parentLegionField; }
}