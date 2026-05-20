package client.gui;

import client.utils.LocaleManager;

import javax.swing.*;
import java.awt.*;

public class LoginDialog extends JDialog {
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private String authenticatedUser;
    private boolean success;

    public LoginDialog(JFrame parent){
        super(parent,LocaleManager.get("login.title"), true);
        setSize(400,250);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10,10));
        setResizable(false);

        JLabel titleLabel = new JLabel(LocaleManager.get("app.title"),SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel(new GridLayout(2,2,5,5));
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        fieldsPanel.add(new JLabel(LocaleManager.get("login.username")));
        usernameField = new JTextField();
        fieldsPanel.add(usernameField);

        fieldsPanel.add(new JLabel(LocaleManager.get("login.password")));
        passwordField = new JPasswordField();
        fieldsPanel.add(passwordField);

        add(fieldsPanel, BorderLayout.CENTER);


        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        JButton loginButton = new JButton(LocaleManager.get("login.signin"));
        loginButton.addActionListener(e -> attemptLogin());

        JButton registerButton = new JButton(LocaleManager.get("login.signup"));
        registerButton.addActionListener(e -> openRegistration(parent));

        JButton cancelButton = new JButton(LocaleManager.get("login.cancel"));
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

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    LocaleManager.get("login.error.empty_username"),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    LocaleManager.get("login.error.empty_password"),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // TODO: Здесь будет отправка на сервер
        this.authenticatedUser = username;
        this.success = true;
        dispose();
    }
    private void openRegistration(JFrame parent) {
        RegisterDialog registerDialog = new RegisterDialog(parent);
        registerDialog.setVisible(true);

        if (registerDialog.isSuccess()) {
            usernameField.setText(registerDialog.getUsername());
            passwordField.requestFocus();
        }
    }
    private void cancel() {
        this.success = false;
        this.authenticatedUser = null;
        dispose();
    }
    public String getAuthenticatedUser() {
        return authenticatedUser;
    }
    public boolean isSuccess() {
        return success;
    }

}