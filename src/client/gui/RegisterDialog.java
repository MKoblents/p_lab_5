package client.gui;

import javax.swing.*;
import java.awt.*;


public class RegisterDialog extends JDialog {

    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JPasswordField confirmPasswordField;
    private String registeredUsername;
    private boolean success;

    public RegisterDialog(JFrame parent) {
        super(parent, "Sign In", true);
        setSize(400, 300);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        setResizable(false);


        JLabel titleLabel = new JLabel("Sign In", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);


        JPanel fieldsPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        fieldsPanel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        fieldsPanel.add(usernameField);

        fieldsPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        fieldsPanel.add(passwordField);

        fieldsPanel.add(new JLabel("Confirm password:"));
        confirmPasswordField = new JPasswordField();
        fieldsPanel.add(confirmPasswordField);

        add(fieldsPanel, BorderLayout.CENTER);

        // Кнопки
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        JButton registerButton = new JButton("Sign In");
        registerButton.addActionListener(e -> attemptRegister());

        JButton cancelButton = new JButton("Cancel");
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
                    "All fields must be init",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this,
                    "Passwords aren't equal",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (password.length() < 4) {
            JOptionPane.showMessageDialog(this,
                    "Password length must be more than 8",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // TODO: Отправка на сервер
        this.registeredUsername = username;
        this.success = true;
        JOptionPane.showMessageDialog(this,
                "Регистрация успешна!\nТеперь войдите с вашим логином.",
                "Успех",
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