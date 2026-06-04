//package client.gui.auth;
//
//import client.gui.utils.GuiUtils;
//import client.network.ConnectionManager;
//import client.utils.LocaleManager;
//import client.utils.RequestsFactory;
//import shared.dto.CommandRequest;
//import shared.dto.CommandResponse;
//import shared.dto.UserInfo;
//
//import javax.swing.*;
//import java.awt.*;
//
//public class RegisterDialog extends JDialog {
//    private final JTextField usernameField;
//    private final JPasswordField passwordField;
//    private final JPasswordField confirmPasswordField;
//    private final ConnectionManager connection;
//    private JButton registerButton;
//    private JButton cancelButton;
//    private String registeredUsername;
//    private boolean success = false;
//
//    public RegisterDialog(JFrame parent, ConnectionManager connection) {
//        super(parent, LocaleManager.get("register.title"), true);
//        this.connection = connection;
//        setSize(400, 300);
//        setLocationRelativeTo(parent);
//        setLayout(new BorderLayout(10, 10));
//        setResizable(false);
//
//        JLabel titleLabel = new JLabel(LocaleManager.get("register.title"), SwingConstants.CENTER);
//        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
//        add(titleLabel, BorderLayout.NORTH);
//
//        JPanel fieldsPanel = GuiUtils.createPanel(new GridLayout(3, 2, 5, 5));
//        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
//
//        fieldsPanel.add(new JLabel(LocaleManager.get("register.username")));
//        usernameField = new JTextField();
//        fieldsPanel.add(usernameField);
//
//        fieldsPanel.add(new JLabel(LocaleManager.get("register.password")));
//        passwordField = new JPasswordField();
//        fieldsPanel.add(passwordField);
//
//        fieldsPanel.add(new JLabel(LocaleManager.get("register.confirm_password")));
//        confirmPasswordField = new JPasswordField();
//        fieldsPanel.add(confirmPasswordField);
//
//        add(fieldsPanel, BorderLayout.CENTER);
//
//        JPanel buttonsPanel = GuiUtils.createPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
//
//        registerButton = new JButton(LocaleManager.get("register.button"));
//        registerButton.addActionListener(e -> attemptRegister());
//
//        cancelButton = new JButton(LocaleManager.get("register.cancel"));
//        cancelButton.addActionListener(e -> cancel());
//
//        buttonsPanel.add(registerButton);
//        buttonsPanel.add(cancelButton);
//
//        add(buttonsPanel, BorderLayout.SOUTH);
//    }
//
//    private void attemptRegister() {
//        String username = usernameField.getText().trim();
//        String password = new String(passwordField.getPassword());
//        String confirmPassword = new String(confirmPasswordField.getPassword());
//
//        if (username.isEmpty() || password.isEmpty()) {
//            JOptionPane.showMessageDialog(this, LocaleManager.get("register.error.empty_fields"),
//                    LocaleManager.get("register.title"), JOptionPane.ERROR_MESSAGE);
//            return;
//        }
//        if (!password.equals(confirmPassword)) {
//            JOptionPane.showMessageDialog(this, LocaleManager.get("register.error.passwords_mismatch"),
//                    LocaleManager.get("register.title"), JOptionPane.ERROR_MESSAGE);
//            return;
//        }
//        if (password.length() < 4) {
//            JOptionPane.showMessageDialog(this, LocaleManager.get("register.error.password_length"),
//                    LocaleManager.get("register.title"), JOptionPane.ERROR_MESSAGE);
//            return;
//        }
//
//        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//        registerButton.setEnabled(false);
//        cancelButton.setEnabled(false);
//
//        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
//            private String errorMessage = null;
//
//            @Override
//            protected Boolean doInBackground() {
//                try {
//                    UserInfo userInfo = new UserInfo(username, password);
//                    CommandRequest request = RequestsFactory.creatLogRequest("sign_in", userInfo);
//                    connection.sendRequest(request);
//                    CommandResponse response = connection.readResponse();
//
//                    if (response != null && response.success() && response.result() instanceof UserInfo ui) {
//                        registeredUsername = ui.name();
//                        return true;
//                    }
//                    errorMessage = response != null ? response.message() : "Registration failed";
//                    return false;
//                } catch (Exception e) {
//                    errorMessage = "Network error: " + e.getMessage();
//                    return false;
//                }
//            }
//
//            @Override
//            protected void done() {
//                setCursor(Cursor.getDefaultCursor());
//                registerButton.setEnabled(true);
//                cancelButton.setEnabled(true);
//                try {
//                    if (get()) {
//                        success = true;
//                        JOptionPane.showMessageDialog(RegisterDialog.this,
//                                LocaleManager.get("register.success.message"),
//                                LocaleManager.get("register.success.title"), JOptionPane.INFORMATION_MESSAGE);
//                        dispose();
//                    } else {
//                        JOptionPane.showMessageDialog(RegisterDialog.this, errorMessage,
//                                LocaleManager.get("register.title"), JOptionPane.ERROR_MESSAGE);
//                    }
//                } catch (Exception e) {
//                    JOptionPane.showMessageDialog(RegisterDialog.this, "Unexpected error",
//                            LocaleManager.get("register.title"), JOptionPane.ERROR_MESSAGE);
//                }
//            }
//        };
//        worker.execute();
//    }
//
//    private void cancel() {
//        success = false;
//        dispose();
//    }
//
//    public String getUsername() { return registeredUsername; }
//    public boolean isSuccess() { return success; }
//}