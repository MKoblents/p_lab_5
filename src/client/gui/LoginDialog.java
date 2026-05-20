package client.gui;

import javax.swing.*;
import java.awt.*;

public class LoginDialog extends JDialog {
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private String authenticatedUser;
    private boolean success;

    public LoginDialog(JFrame parent){
        super(parent,"Auth", true);
        setSize(400,250);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10,10));
        setResizable(false);

        JLabel titleLabel = new JLabel("SpaceMarine Client",SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel(new GridLayout(2,2,5,5));
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        fieldsPanel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        fieldsPanel.add(usernameField);

        fieldsPanel.add(new JLabel("Password"));
        passwordField = new JPasswordField();
        fieldsPanel.add(passwordField);

        add(fieldsPanel, BorderLayout.CENTER);


        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        JButton loginButton = new JButton("Enter");
        loginButton.addActionListener(e -> attemptLogin());

        JButton registerButton = new JButton("Sign In");
        registerButton.addActionListener(e -> openRegistration(parent));

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> cancel());

        buttonsPanel.add(loginButton);
        buttonsPanel.add(registerButton);
        buttonsPanel.add(cancelButton);

        add(buttonsPanel, BorderLayout.SOUTH);

        // Enter для быстрой авторизации
        passwordField.addActionListener(e -> attemptLogin());
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Enter user name",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Enter pssword",
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

        // Если регистрация успешна — подставить username
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
