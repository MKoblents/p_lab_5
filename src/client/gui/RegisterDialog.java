package client.gui;

import client.utils.LocaleManager;

import javax.swing.*;
import java.awt.*;


public class RegisterDialog extends JDialog {

    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JPasswordField confirmPasswordField;
    private String registeredUsername;
    private boolean success;

    public RegisterDialog(JFrame parent) {
        super(parent, LocaleManager.get("register.title"), true);
        setSize(400, 300);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        setResizable(false);


        JLabel titleLabel = new JLabel(LocaleManager.get("register.title"), SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);


        JPanel fieldsPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        fieldsPanel.add(new JLabel(LocaleManager.get("register.username")));
        usernameField = new JTextField();
        fieldsPanel.add(usernameField);

        fieldsPanel.add(new JLabel(LocaleManager.get("register.password")));
        passwordField = new JPasswordField();
        fieldsPanel.add(passwordField);

        fieldsPanel.add(new JLabel(LocaleManager.get("register.confirm_password")));
        confirmPasswordField = new JPasswordField();
        fieldsPanel.add(confirmPasswordField);

        add(fieldsPanel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        JButton registerButton = new JButton(LocaleManager.get("register.button"));
        registerButton.addActionListener(e -> attemptRegister());

        JButton cancelButton = new JButton(LocaleManager.get("register.cancel"));
        cancelButton.addActionListener(e -> cancel());

        buttonsPanel.add(registerButton);
        buttonsPanel.add(cancelButton);

        add(buttonsPanel, BorderLayout.SOUTH);
    }

    private void attemptRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    LocaleManager.get("register.error.empty_fields"),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this,
                    LocaleManager.get("register.error.passwords_mismatch"),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (password.length() < 4) {
            JOptionPane.showMessageDialog(this,
                    LocaleManager.get("register.error.password_length"),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // TODO: Отправка на сервер
        this.registeredUsername = username;
        this.success = true;
        JOptionPane.showMessageDialog(this,
                LocaleManager.get("register.success.message"),
                LocaleManager.get("register.success.title"),
                JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    private void cancel() {
        this.success = false;
        this.registeredUsername = null;
        dispose();
    }

    public String getUsername() {
        return registeredUsername;
    }

    public boolean isSuccess() {
        return success;
    }
}