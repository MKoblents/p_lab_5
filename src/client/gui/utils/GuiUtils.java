package client.gui.utils;

import client.utils.LocaleManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Consumer;

public class GuiUtils {

    public static final Color PRIMARY_COLOR = new Color(255, 105, 180);
    public static final Color PRIMARY_DARK = new Color(255, 20, 147);
    public static final Color PRIMARY_LIGHT = new Color(255, 182, 193);
    public static final Color BACKGROUND_COLOR = new Color(255, 240, 248);
    public static final Color BUTTON_COLOR = Color.WHITE;
    public static final Color TEXT_COLOR = new Color(33, 33, 33);
    public static final Color PANEL_COLOR = new Color(255, 105, 180);
    public static final Color STRIP_1 = new Color(255, 230, 235);
    public static final Color STRIP_2 = new Color(255, 245, 248);
    public static final Color SEMI_TRANSPARENT_PANEL_COLOR = new Color(255, 105, 180, 180);

    public static final float BASE_FONT_SIZE = 12.0f;
    public static final float BASE_TITLE_FONT_SIZE = 16.0f;
    public static final float BASE_BUTTON_FONT_SIZE = 12.0f;

    public record LocaleOption(String code, String displayName) {
        @Override
        public String toString() {
            return displayName + " (" + code + ")";
        }
    }

    public static JPanel createStrippedPanel(LayoutManager layoutManager,double baseWidth){
        JPanel bgPanel = new JPanel(layoutManager) {

            private static final int BASE_STRIPE_WIDTH = 30;
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                double scaleFactor = (double) getWidth() / baseWidth;
                int stripeWidth = (int)(BASE_STRIPE_WIDTH * scaleFactor);
                for (int x = 0; x < getWidth(); x += stripeWidth * 2) {
                    g2.setColor(STRIP_1); g2.fillRect(x, 0, stripeWidth, getHeight());
                    g2.setColor(STRIP_2); g2.fillRect(x + stripeWidth, 0, stripeWidth, getHeight());
                }
            }
        };
        return bgPanel;
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
    public static JPanel createPanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(BACKGROUND_COLOR);
        return panel;
    }

    public static JLabel createLabel(String text, int fontSize, boolean bold) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, fontSize));
        label.setForeground(TEXT_COLOR);
        return label;
    }

    public static JDialog createDialog(JFrame parent, String title, boolean modal) {
        JDialog dialog = new JDialog(parent, title, modal);
        dialog.setLayout(new BorderLayout(15, 15));
        dialog.getContentPane().setBackground(BACKGROUND_COLOR);
        return dialog;
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
    public static JPanel createRoundedPanel(LayoutManager layout, Color backgroundColor, int arcWidth, int arcHeight) {
        JPanel panel = new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(backgroundColor);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arcWidth, arcHeight);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        return panel;
    }

    public static JButton createStyledButton(String text, int width, int height, Runnable action) {
        JButton button = new JButton(text){
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth()-1, getHeight()-1, 15, 15));
                g2.dispose();
                super.paintComponent(g);
            }
        };
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
        button.setOpaque(false);
        button.setContentAreaFilled(false);

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
        JDialog dialog = new JDialog(parent);
        dialog.setModal(true);
        dialog.setUndecorated(true);
        dialog.setLayout(new BorderLayout(0, 0));
        dialog.getContentPane().setBackground(BACKGROUND_COLOR);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // === Custom Title Bar ===
        JPanel titleBarPanel = new JPanel(new BorderLayout());
        titleBarPanel.setBackground(PRIMARY_DARK);
        titleBarPanel.setPreferredSize(new Dimension(0, 45));
        titleBarPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        JLabel titleLabel = new JLabel("  " + title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        titleBarPanel.add(titleLabel, BorderLayout.CENTER);

        JButton closeButton = new JButton("✕");
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        closeButton.setForeground(Color.WHITE);
        closeButton.setBackground(PRIMARY_DARK);
        closeButton.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> dialog.dispose());
        closeButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                closeButton.setBackground(PRIMARY_COLOR.darker());
            }
            public void mouseExited(MouseEvent e) {
                closeButton.setBackground(PRIMARY_DARK);
            }
        });
        titleBarPanel.add(closeButton, BorderLayout.EAST);

        final Point[] dragOffset = new Point[1];
        titleBarPanel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                dragOffset[0] = e.getPoint();
            }
        });
        titleBarPanel.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point curr = e.getLocationOnScreen();
                dialog.setLocation(curr.x - dragOffset[0].x, curr.y - dragOffset[0].y);
            }
        });

        dialog.add(titleBarPanel, BorderLayout.NORTH);

        // === Content Panel ===
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        messagePanel.setOpaque(false);

        JLabel iconLabel = new JLabel();
        iconLabel.setPreferredSize(new Dimension(40, 40));
        switch (messageType) {
            case INFO -> iconLabel.setIcon(UIManager.getIcon("OptionPane.informationIcon"));
            case WARNING -> iconLabel.setIcon(UIManager.getIcon("OptionPane.warningIcon"));
            case ERROR -> iconLabel.setIcon(UIManager.getIcon("OptionPane.errorIcon"));
        }
        messagePanel.add(iconLabel);

        JTextArea messageArea = new JTextArea(message);
        messageArea.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        messageArea.setForeground(TEXT_COLOR);
        messageArea.setBackground(BACKGROUND_COLOR);
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setBorder(null);
        messageArea.setOpaque(false);

        // Remove fixed columns/rows - let it calculate naturally
        messageArea.setColumns(1);  // Minimal columns
        messageArea.setRows(1);     // Minimal rows

        // Wrap in scroll pane with reasonable max size
        JScrollPane scrollPane = new JScrollPane(messageArea);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Set preferred size for scroll pane (not too wide)
        int preferredWidth = Math.min(500, Math.max(300, message.length() / 2));
        int preferredHeight = Math.min(300, Math.max(100, message.length() / 3));
        scrollPane.setPreferredSize(new Dimension(preferredWidth, preferredHeight));

        messagePanel.add(scrollPane);
        contentPanel.add(messagePanel, BorderLayout.CENTER);

        // === OK Button Panel ===
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JButton okButton = new JButton("OK");
        okButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        okButton.setBackground(PRIMARY_COLOR);
        okButton.setForeground(Color.WHITE);
        okButton.setFocusPainted(false);
        okButton.setBorderPainted(false);
        okButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        okButton.setPreferredSize(new Dimension(120, 40));
        okButton.addActionListener(e -> dialog.dispose());

        okButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                okButton.setBackground(PRIMARY_DARK);
            }
            public void mouseExited(MouseEvent evt) {
                okButton.setBackground(PRIMARY_COLOR);
            }
        });

        buttonPanel.add(okButton);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(contentPanel, BorderLayout.CENTER);

        // === Size and Position ===
        dialog.pack();

        // Set reasonable max size
        dialog.setMaximumSize(new Dimension(600, 500));

        dialog.setLocationRelativeTo(parent);
        dialog.getRootPane().setDefaultButton(okButton);
        dialog.setVisible(true);
    }
    /**
     * Shows a styled confirmation dialog with Yes/No buttons.
     *
     * @param parent the parent frame for modal behavior
     * @param title the dialog title
     * @param message the message text to display
     * @return true if user clicked Yes, false otherwise
     */
    public static boolean showConfirmDialog(Frame parent, String title, String message) {
        JDialog dialog = new JDialog(parent);
        dialog.setModal(true);
        dialog.setUndecorated(true); // Remove system title bar
        dialog.setLayout(new BorderLayout(0, 0));
        dialog.getContentPane().setBackground(BACKGROUND_COLOR);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // === Custom Title Bar ===
        JPanel titleBarPanel = new JPanel(new BorderLayout());
        titleBarPanel.setBackground(PRIMARY_DARK);
        titleBarPanel.setPreferredSize(new Dimension(0, 45));
        titleBarPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        JLabel titleLabel = new JLabel("  " + title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        titleBarPanel.add(titleLabel, BorderLayout.CENTER);

        // Close button
        JButton closeButton = new JButton("✕");
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        closeButton.setForeground(Color.WHITE);
        closeButton.setBackground(PRIMARY_DARK);
        closeButton.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> dialog.dispose());
        closeButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                closeButton.setBackground(PRIMARY_COLOR.darker());
            }
            public void mouseExited(MouseEvent e) {
                closeButton.setBackground(PRIMARY_DARK);
            }
        });
        titleBarPanel.add(closeButton, BorderLayout.EAST);

        // Make title bar draggable
        final Point[] dragOffset = new Point[1];
        titleBarPanel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                dragOffset[0] = e.getPoint();
            }
        });
        titleBarPanel.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point curr = e.getLocationOnScreen();
                dialog.setLocation(curr.x - dragOffset[0].x, curr.y - dragOffset[0].y);
            }
        });

        dialog.add(titleBarPanel, BorderLayout.NORTH);

        // === Content Panel ===
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Icon and message
        JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        messagePanel.setOpaque(false);

        JLabel iconLabel = new JLabel(UIManager.getIcon("OptionPane.questionIcon"));
        iconLabel.setPreferredSize(new Dimension(40, 40));
        messagePanel.add(iconLabel);

        JTextArea messageArea = new JTextArea(message);
        messageArea.setFont(new Font("Segoe UI", Font.PLAIN, 18)); // Larger font
        messageArea.setForeground(TEXT_COLOR);
        messageArea.setBackground(BACKGROUND_COLOR);
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setBorder(null);
        messageArea.setOpaque(false);

        // Calculate rows based on message length
        int rows = Math.max(1, (message.length() / 50) + 1);
        messageArea.setRows(Math.min(rows, 10));
        messageArea.setColumns(40);

        messagePanel.add(messageArea);
        contentPanel.add(messagePanel, BorderLayout.CENTER);

        // === Yes/No Button Panel ===
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        final boolean[] result = {false};

        JButton yesButton = new JButton("Yes");
        yesButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        yesButton.setBackground(PRIMARY_COLOR);
        yesButton.setForeground(Color.WHITE);
        yesButton.setFocusPainted(false);
        yesButton.setBorderPainted(false);
        yesButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        yesButton.setPreferredSize(new Dimension(100, 40));
        yesButton.addActionListener(e -> {
            result[0] = true;
            dialog.dispose();
        });
        yesButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                yesButton.setBackground(PRIMARY_DARK);
            }
            public void mouseExited(MouseEvent evt) {
                yesButton.setBackground(PRIMARY_COLOR);
            }
        });

        JButton noButton = new JButton("No");
        noButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        noButton.setBackground(Color.LIGHT_GRAY);
        noButton.setForeground(Color.BLACK);
        noButton.setFocusPainted(false);
        noButton.setBorderPainted(false);
        noButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        noButton.setPreferredSize(new Dimension(100, 40));
        noButton.addActionListener(e -> {
            result[0] = false;
            dialog.dispose();
        });
        noButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                noButton.setBackground(Color.GRAY);
            }
            public void mouseExited(MouseEvent evt) {
                noButton.setBackground(Color.LIGHT_GRAY);
            }
        });

        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(contentPanel, BorderLayout.CENTER);

        // === Size and Position ===
        dialog.pack();

        // Ensure minimum size but allow growth
        int minWidth = Math.max(450, messageArea.getPreferredSize().width + 80);
        int maxWidth = 700;
        dialog.setSize(
                Math.min(maxWidth, Math.max(minWidth, dialog.getWidth())),
                dialog.getHeight()
        );

        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        return result[0];
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
    // === В конец класса GuiUtils ===

    /**
     * Creates a styled text field with placeholder behavior.
     * @param placeholderKey locale key for placeholder text
     * @param preferredHeight preferred height in pixels
     * @return configured JTextField
     */
    public static JTextField createStyledPlaceholderField(String placeholderKey, int preferredHeight) {
        JTextField field = new JTextField(LocaleManager.get(placeholderKey));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(Color.GRAY);
        field.setHorizontalAlignment(JTextField.CENTER);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                new EmptyBorder(10, 15, 10, 15)
        ));
        field.setBackground(Color.WHITE);
        field.setPreferredSize(new Dimension(0, preferredHeight));
        field.setMaximumSize(new Dimension(Short.MAX_VALUE, preferredHeight));

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

    /**
     * Creates a styled JComboBox with generic items.
     * @param items enum values or other items
     * @param <T> type of items
     * @return configured JComboBox
     */
    public static <T> JComboBox<T> createStyledComboBox(T[] items, int preferredHeight) {
        JComboBox<T> combo = new JComboBox<>(items);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBackground(Color.WHITE);
        combo.setForeground(Color.BLACK);
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                new EmptyBorder(8, 10, 8, 10)
        ));
        combo.setPreferredSize(new Dimension(0, preferredHeight));
        combo.setMaximumSize(new Dimension(Short.MAX_VALUE, preferredHeight));
        combo.setCursor(new Cursor(Cursor.HAND_CURSOR));

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setFont(new Font("Segoe UI", Font.PLAIN, 14));
                if (isSelected) {
                    setBackground(PRIMARY_COLOR);
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

    /**
     * Creates a styled button with hover effect.
     * @param textKey locale key for button text
     * @param width preferred width (0 for default)
     * @param height preferred height
     * @param action optional action to execute on click
     * @return configured JButton
     */
    public static JButton createStyledDialogButton(String textKey, int width, int height, Runnable action) {
        JButton button = new JButton(LocaleManager.get(textKey));
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (width > 0) {
            button.setPreferredSize(new Dimension(width, height));
            button.setMaximumSize(new Dimension(width, height));
        }
        button.setBorder(new EmptyBorder(10, 20, 10, 20));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_DARK);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_COLOR);
            }
        });

        if (action != null) {
            button.addActionListener(e -> action.run());
        }
        return button;
    }

    /**
     * Creates a labeled field panel (label above field).
     * @param labelKey locale key for label
     * @param field the input component to label
     * @return JPanel with BorderLayout containing label and field
     */
    public static JPanel createLabeledInputPanel(String labelKey, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setOpaque(false);

        JLabel label = new JLabel(LocaleManager.get(labelKey), SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(PRIMARY_DARK);

        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }
    // Добавьте это в GuiUtils.java
    public static JLabel createStyledLabel(String localeKey, float baseSize, Color color) {
        JLabel label = new JLabel(LocaleManager.get(localeKey));
        label.setFont(new Font("Segoe UI", Font.BOLD, (int) baseSize));
        label.setForeground(color);
        return label;
    }

    public static JTextArea createStyledTextArea(String text) {
        JTextArea area = new JTextArea(text);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        area.setForeground(TEXT_COLOR);
        area.setBackground(Color.WHITE);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_LIGHT, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        return area;
    }

    // Метод для создания красивого скролл-панели для текста
    public static JScrollPane createStyledScrollPane(Component view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 2));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }
}