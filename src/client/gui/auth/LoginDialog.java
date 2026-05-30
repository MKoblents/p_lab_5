package client.gui.auth;

import client.gui.utils.GuiUtils;
import client.network.ConnectionManager;
import client.utils.LocaleManager;
import client.utils.RequestsFactory;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.dto.UserInfo;

import javax.swing.*;
import java.awt.*;

public class LoginDialog extends JDialog {
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final ConnectionManager connection;
    private JButton loginButton;
    private JButton registerButton;
    private JButton cancelButton;
    private UserInfo loggedInUser;
    private JFrame parent;
    private boolean success = false;

    public LoginDialog(JFrame parent, ConnectionManager connection) {
        super(parent, LocaleManager.get("login.title"), true);
        this.connection = connection;
        this.parent = parent;
        setSize(400, 250);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        setResizable(false);

        JLabel titleLabel = new JLabel(LocaleManager.get("app.title"), SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        JPanel fieldsPanel = GuiUtils.createPanel(new GridLayout(2, 2, 5, 5));
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        fieldsPanel.add(new JLabel(LocaleManager.get("login.username")));
        usernameField = new JTextField();
        fieldsPanel.add(usernameField);

        fieldsPanel.add(new JLabel(LocaleManager.get("login.password")));
        passwordField = new JPasswordField();
        fieldsPanel.add(passwordField);

        add(fieldsPanel, BorderLayout.CENTER);

        JPanel buttonsPanel = GuiUtils.createPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        loginButton = new JButton(LocaleManager.get("login.signin"));
        loginButton.addActionListener(e -> attemptLogin());

        registerButton = new JButton(LocaleManager.get("login.signup"));
        registerButton.addActionListener(e -> openRegistration());

        cancelButton = new JButton(LocaleManager.get("login.cancel"));
        cancelButton.addActionListener(e -> cancel());

        buttonsPanel.add(loginButton);
        buttonsPanel.add(registerButton);
        buttonsPanel.add(cancelButton);

        add(buttonsPanel, BorderLayout.SOUTH);
        passwordField.addActionListener(e -> attemptLogin());
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            GuiUtils.showMessageDialog(parent,
                    LocaleManager.get("login.title"),
                    LocaleManager.get("login.error.empty_fields"),
                    GuiUtils.MessageType.ERROR);
            return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        loginButton.setEnabled(false);
        registerButton.setEnabled(false);
        cancelButton.setEnabled(false);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            private String errorMessage = null;

            @Override
            protected Boolean doInBackground() {
                try {
                    UserInfo userInfo = new UserInfo(username, password);
                    CommandRequest request = RequestsFactory.creatLogRequest("log_in", userInfo);
                    connection.sendRequest(request);
                    CommandResponse response = connection.readResponse();

                    if (response != null && response.success() && response.result() instanceof UserInfo ui) {
                        loggedInUser = ui;
                        return true;
                    }
                    errorMessage = response != null ? response.message() : "Login failed";
                    return false;
                } catch (Exception e) {
                    errorMessage = "Network error: " + e.getMessage();
                    return false;
                }
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                loginButton.setEnabled(true);
                registerButton.setEnabled(true);
                cancelButton.setEnabled(true);
                try {
                    if (get()) {
                        success = true;
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(LoginDialog.this, errorMessage,
                                LocaleManager.get("login.title"), JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(LoginDialog.this, "Unexpected error",
                            LocaleManager.get("login.title"), JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void openRegistration() {
        RegisterDialog registerDialog = new RegisterDialog((JFrame) SwingUtilities.getWindowAncestor(this), connection);
        registerDialog.setVisible(true);
        if (registerDialog.isSuccess()) {
            usernameField.setText(registerDialog.getUsername());
            passwordField.requestFocus();
        }
    }

    private void cancel() {
        success = false;
        dispose();
    }

    public UserInfo getLoggedInUser() { return loggedInUser; }
    public boolean isSuccess() { return success; }
}