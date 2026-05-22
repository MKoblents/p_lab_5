package client.gui.utils;

import client.utils.LocaleManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.function.Consumer;

public class GuiUtils {

    public static final Color PRIMARY_COLOR = new Color(255, 105, 180);
    public static final Color PRIMARY_DARK = new Color(255, 20, 147);
    public static final Color PRIMARY_LIGHT = new Color(255, 182, 193);
    public static final Color BACKGROUND_COLOR = new Color(255, 240, 248);
    public static final Color BUTTON_COLOR = Color.WHITE;
    public static final Color TEXT_COLOR = new Color(33, 33, 33);
    public static final Color PANEL_COLOR = new Color(255, 105, 180);

    public static final float BASE_FONT_SIZE = 12.0f;
    public static final float BASE_TITLE_FONT_SIZE = 16.0f;
    public static final float BASE_BUTTON_FONT_SIZE = 12.0f;

    public record LocaleOption(String code, String displayName) {
        @Override
        public String toString() {
            return displayName + " (" + code + ")";
        }
    }

    public static JComboBox<LocaleOption> createLocaleComboBox(Consumer<LocaleOption> onChange) {
        JComboBox<LocaleOption> combo = new JComboBox<>(createLocaleOptions());
        combo.setFont(new Font("Segoe UI", Font.PLAIN, (int) BASE_FONT_SIZE));
        combo.setMaximumSize(new Dimension(150, 28));
        combo.setPreferredSize(new Dimension(150, 28));
        combo.setBackground(Color.WHITE);

        combo.addActionListener(e -> {
            LocaleOption selected = (LocaleOption) combo.getSelectedItem();
            if (selected != null && onChange != null) {
                onChange.accept(selected);
            }
        });

        return combo;
    }

    public static LocaleOption[] createLocaleOptions() {
        return new LocaleOption[]{
                new LocaleOption("ru_RU", LocaleManager.get("locale.ru")),
                new LocaleOption("en_US", LocaleManager.get("locale.en")),
                new LocaleOption("de_DE", LocaleManager.get("locale.de")),
                new LocaleOption("sv_SE", LocaleManager.get("locale.sv")),
                new LocaleOption("es_ES", LocaleManager.get("locale.es"))
        };
    }

    public static JButton createStyledButton(String text, int width, int height, Runnable action) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, (int) BASE_BUTTON_FONT_SIZE));
        button.setBackground(BUTTON_COLOR);
        button.setForeground(TEXT_COLOR);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(width, height));
        button.setMaximumSize(new Dimension(width, height));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_DARK, 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_LIGHT);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(BUTTON_COLOR);
            }
        });

        if (action != null) {
            button.addActionListener(e -> action.run());
        }

        return button;
    }

    public static void addResizeListener(Component component, Dimension originalSize, Consumer<Double> onResize) {
        component.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                double scaleFactor = (double) component.getWidth() / originalSize.width;
                onResize.accept(scaleFactor);
            }
        });
    }

    public static Font scaleFont(Font baseFont, double scaleFactor) {
        return baseFont.deriveFont((float) (baseFont.getSize() * scaleFactor));
    }

    public static JTextField createPlaceholderField(String placeholderKey) {
        JTextField field = new JTextField(LocaleManager.get(placeholderKey));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(Color.GRAY);
        field.setHorizontalAlignment(JTextField.CENTER);
        field.setBorder(BorderFactory.createLineBorder(new Color(255, 180, 200), 1));
        field.setBackground(Color.WHITE);

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (field.getText().equals(LocaleManager.get(placeholderKey))) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(LocaleManager.get(placeholderKey));
                    field.setForeground(Color.GRAY);
                }
            }
        });
        return field;
    }

    public static JPasswordField createPasswordField(String placeholderKey) {
        JPasswordField field = new JPasswordField(LocaleManager.get(placeholderKey));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(Color.GRAY);
        field.setHorizontalAlignment(JPasswordField.CENTER);
        field.setBorder(BorderFactory.createLineBorder(new Color(255, 180, 200), 1));
        field.setBackground(Color.WHITE);
        field.setEchoChar((char)0);

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (String.valueOf(field.getPassword()).equals(LocaleManager.get(placeholderKey))) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                    field.setEchoChar('\u2022');
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (field.getPassword().length == 0) {
                    field.setText(LocaleManager.get(placeholderKey));
                    field.setForeground(Color.GRAY);
                    field.setEchoChar((char)0);
                }
            }
        });
        return field;
    }
}