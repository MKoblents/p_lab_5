package client.gui.auth;

import client.network.ConnectionManager;
import client.utils.LocaleManager;
import client.utils.RequestsFactory;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.dto.UserInfo;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class AuthDialog extends JDialog {
    private final ConnectionManager connection;
    private boolean isLoginMode = true;
    private boolean success = false;
    private UserInfo loggedInUser;

    // UI Components
    private JPanel mainPanel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton actionButton;
    private JButton switchButton;
    private JLabel messageLabel;

    public AuthDialog(Frame owner, ConnectionManager connection) {
        super(owner, LocaleManager.get("app.title"), true);
        this.connection = connection;
        setSize(1440, 1040);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        initComponents();
        setupLayout();
        updateModeUI();
    }

    private void initComponents() {
        mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                int stripeWidth = 30;
                Color c1 = new Color(255, 230, 235);
                Color c2 = new Color(255, 245, 248);
                for (int x = 0; x < getWidth(); x += stripeWidth * 2) {
                    g2.setColor(c1);
                    g2.fillRect(x, 0, stripeWidth, getHeight());
                    g2.setColor(c2);
                    g2.fillRect(x + stripeWidth, 0, stripeWidth, getHeight());
                }
            }
        };
        mainPanel.setLayout(new GridBagLayout());

        // Form container
        JPanel formBox = new JPanel();
        formBox.setLayout(new BoxLayout(formBox, BoxLayout.Y_AXIS));
        formBox.setOpaque(false);
        formBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 150, 170), 1, true),
                BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));

        // Title
        JLabel titleLabel = new JLabel(LocaleManager.get("app.title"), SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formBox.add(titleLabel);
        formBox.add(Box.createVerticalStrut(20));

        // Fields
        usernameField = createPlaceholderField(LocaleManager.get("auth.username"));
        passwordField = createPlaceholderPasswordField(LocaleManager.get("auth.password"));
        confirmPasswordField = createPlaceholderPasswordField(LocaleManager.get("auth.confirm_password"));
        confirmPasswordField.setVisible(false);

        formBox.add(usernameField);
        formBox.add(Box.createVerticalStrut(12));
        formBox.add(passwordField);
        formBox.add(Box.createVerticalStrut(12));
        formBox.add(confirmPasswordField);
        formBox.add(Box.createVerticalStrut(20));

        // Buttons
        actionButton = new JButton();
        actionButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        styleButton(actionButton, new Color(255, 100, 130), Color.WHITE);
        formBox.add(actionButton);

        switchButton = new JButton();
        switchButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        styleButton(switchButton, new Color(255, 240, 245), new Color(180, 60, 90));
        switchButton.addActionListener(e -> toggleMode());
        formBox.add(Box.createVerticalStrut(8));
        formBox.add(switchButton);

        // Message label
        messageLabel = new JLabel(" ", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        messageLabel.setForeground(Color.RED);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formBox.add(Box.createVerticalStrut(10));
        formBox.add(messageLabel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(formBox, gbc);
    }

    private void setupLayout() {
        setContentPane(mainPanel);
    }

    private void updateModeUI() {
        if (isLoginMode) {
            actionButton.setText(LocaleManager.get("auth.login"));
            switchButton.setText(LocaleManager.get("auth.switch_to_signup"));
            confirmPasswordField.setVisible(false);
            messageLabel.setText(" ");
        } else {
            actionButton.setText(LocaleManager.get("auth.signup"));
            switchButton.setText(LocaleManager.get("auth.switch_to_login"));
            confirmPasswordField.setVisible(true);
            messageLabel.setText(" ");
        }
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void toggleMode() {
        isLoginMode = !isLoginMode;
        usernameField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
        updateModeUI();
    }

    private void attemptAuth() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmPasswordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showMessage(LocaleManager.get("auth.error.empty_fields"), Color.RED);
            return;
        }
        if (!isLoginMode && !password.equals(confirm)) {
            showMessage(LocaleManager.get("auth.error.passwords_mismatch"), Color.RED);
            return;
        }
        if (!isLoginMode && password.length() < 4) {
            showMessage(LocaleManager.get("auth.error.password_length"), Color.RED);
            return;
        }

        actionButton.setEnabled(false);
        switchButton.setEnabled(false);
        showMessage(LocaleManager.get("auth.processing"), new Color(80, 80, 80));

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                try {
                    UserInfo userInfo = new UserInfo(username, password);
                    String cmd = isLoginMode ? "log_in" : "sign_in";
                    CommandRequest request = RequestsFactory.creatLogRequest(cmd, userInfo);

                    connection.sendRequest(request);
                    CommandResponse response = connection.readResponse();

                    if (response != null && response.success() && response.result() instanceof UserInfo ui) {
                        loggedInUser = ui;
                        return true;
                    }
                    return false;
                } catch (Exception e) {
                    return false;
                }
            }

            @Override
            protected void done() {
                actionButton.setEnabled(true);
                switchButton.setEnabled(true);
                try {
                    if (get()) {
                        success = true;
                        dispose();
                    } else {
                        showMessage(LocaleManager.get("auth.error.failed"), Color.RED);
                    }
                } catch (Exception e) {
                    showMessage(LocaleManager.get("auth.error.network"), Color.RED);
                }
            }
        }.execute();
    }

    private void showMessage(String msg, Color color) {
        messageLabel.setText(msg);
        messageLabel.setForeground(color);
    }

    // --- Helper Methods ---
    private JTextField createPlaceholderField(String placeholder) {
        JTextField field = new JTextField(placeholder);
        styleTextField(field, placeholder);
        return field;
    }

    private JPasswordField createPlaceholderPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField(placeholder);
        styleTextField(field, placeholder);
        return field;
    }

    private void styleTextField(JTextField field, String placeholder) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(Color.GRAY);
        field.setHorizontalAlignment(JTextField.CENTER);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 180, 200), 1, true),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        field.setMaximumSize(new Dimension(300, 40));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Placeholder logic
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                }
            }
        });

        field.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { check(); }
            public void removeUpdate(DocumentEvent e) { check(); }
            public void insertUpdate(DocumentEvent e) { check(); }
            private void check() {
                if (!field.getText().equals(placeholder) && !field.getText().isEmpty()) {
                    field.setForeground(Color.BLACK);
                }
            }
        });

        // Enter key triggers auth
        field.addActionListener(e -> attemptAuth());
    }

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 150, 170), 1, true),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        btn.setMaximumSize(new Dimension(300, 40));
        btn.addActionListener(e -> attemptAuth());
    }

    public UserInfo getLoggedInUser() { return loggedInUser; }
    public boolean isSuccess() { return success; }
}