package client.gui.utils;

import client.utils.LocaleManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
    /**
     * Creates a generically-styled JComboBox with pink theme.
     * @param <T> the type of items in the combo box
     * @return styled JComboBox instance
     */
    public static <T> JComboBox<T> createStyledComboBox() {
        JComboBox<T> combo = new JComboBox<>();
        combo.setFont(new Font("Segoe UI", Font.PLAIN, (int) BASE_FONT_SIZE));
        combo.setBackground(Color.WHITE);
        combo.setForeground(TEXT_COLOR);
        combo.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 1));
        combo.setPreferredSize(new Dimension(300, 30));
        combo.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));

        // Default renderer with selection styling
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    setBackground(PRIMARY_COLOR);
                    setForeground(Color.WHITE);
                } else {
                    setBackground(Color.WHITE);
                    setForeground(TEXT_COLOR);
                }
                return this;
            }
        });

        return combo;
    }
    /**
     * Shows a styled message dialog with the application's pink theme.
     * Uses larger font and themed OK button.
     *
     * @param parent the parent frame for modal behavior
     * @param title the dialog title
     * @param message the message text to display
     * @param messageType type of message (INFO, WARNING, ERROR) for icon selection
     */
    public static void showMessageDialog(Frame parent, String title, String message, MessageType messageType) {
        JDialog dialog = new JDialog(parent, title, true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout(15, 15));
        dialog.getContentPane().setBackground(BACKGROUND_COLOR);

        // Header panel with icon and title
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        headerPanel.setOpaque(false);

        // Optional icon based on message type
        JLabel iconLabel = new JLabel();
        iconLabel.setPreferredSize(new Dimension(32, 32));
        switch (messageType) {
            case INFO -> iconLabel.setIcon(UIManager.getIcon("OptionPane.informationIcon"));
            case WARNING -> iconLabel.setIcon(UIManager.getIcon("OptionPane.warningIcon"));
            case ERROR -> iconLabel.setIcon(UIManager.getIcon("OptionPane.errorIcon"));
        }
        headerPanel.add(iconLabel);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(PRIMARY_DARK);
        headerPanel.add(titleLabel);

        // Message content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(0, 50, 20, 20));

        JTextArea messageArea = new JTextArea(message);
        messageArea.setFont(new Font("Segoe UI", Font.PLAIN, 16)); // Larger font
        messageArea.setForeground(TEXT_COLOR);
        messageArea.setBackground(BACKGROUND_COLOR);
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setBorder(null);

        // Auto-size the dialog based on content
        messageArea.setPreferredSize(new Dimension(400, Math.min(300, message.length() / 2 * 20)));

        JScrollPane scrollPane = new JScrollPane(messageArea);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // OK button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);

        JButton okButton = new JButton("OK");
        okButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        okButton.setBackground(PRIMARY_COLOR);
        okButton.setForeground(Color.WHITE);
        okButton.setFocusPainted(false);
        okButton.setBorderPainted(false);
        okButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        okButton.setPreferredSize(new Dimension(100, 40));
        okButton.addActionListener(e -> dialog.dispose());

        // Hover effect
        okButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                okButton.setBackground(PRIMARY_DARK);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                okButton.setBackground(PRIMARY_COLOR);
            }
        });

        buttonPanel.add(okButton);

        // Assemble dialog
        dialog.add(headerPanel, BorderLayout.NORTH);
        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Size and position
        dialog.pack();
        dialog.setMinimumSize(new Dimension(450, 200));
        dialog.setLocationRelativeTo(parent);

        // Make OK button default (Enter key)
        dialog.getRootPane().setDefaultButton(okButton);

        dialog.setVisible(true);
    }

    /**
     * Simplified overload for info messages.
     */
    public static void showMessageDialog(Frame parent, String title, String message) {
        showMessageDialog(parent, title, message, MessageType.INFO);
    }

    /**
     * Message type enum for icon selection.
     */
    public enum MessageType {
        INFO, WARNING, ERROR
    }
}