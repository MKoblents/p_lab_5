package client.gui.auth;

import client.gui.utils.GuiUtils;
import client.network.ConnectionManager;
import client.utils.LocaleManager;
import client.utils.RequestsFactory;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.dto.UserInfo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class AuthDialog extends JDialog {

    private final ConnectionManager connection;
    private boolean isRegisterMode = false;
    private boolean success = false;
    private UserInfo loggedInUser = null;

    private JLabel titleLabel;
    private JComboBox<GuiUtils.LocaleOption> localeCombo;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton primaryButton;
    private JButton switchModeButton;
    private JLabel errorLabel;

    private Dimension originalSize;
    private double scaleFactor = 1.0;

    public AuthDialog(JFrame parent, ConnectionManager connection) {
        super(parent, "Auth", true);
        this.connection = connection;

        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        setupUI();
        updateLocaleTexts();

        originalSize = new Dimension(420, 480);
        setSize(originalSize);
        setLocationRelativeTo(parent);

        GuiUtils.addResizeListener(getContentPane(), originalSize, this::onResize);
    }

    private void onResize(double newScaleFactor) {
        this.scaleFactor = newScaleFactor;
        resizeComponents();
    }

    public boolean isSuccess() {
        return success;
    }

    public UserInfo getLoggedInUser() {
        return loggedInUser;
    }

    private void setupUI() {
        JPanel bgPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                int stripeWidth = (int)(25 * scaleFactor);
                Color c1 = new Color(255, 230, 235);
                Color c2 = new Color(255, 245, 248);
                for (int x = 0; x < getWidth(); x += stripeWidth * 2) {
                    g2.setColor(c1); g2.fillRect(x, 0, stripeWidth, getHeight());
                    g2.setColor(c2); g2.fillRect(x + stripeWidth, 0, stripeWidth, getHeight());
                }
            }
        };

        JPanel formContainer = new JPanel(new GridBagLayout());
        formContainer.setOpaque(false);
        formContainer.setBorder(BorderFactory.createLineBorder(new Color(255, 150, 170), 1));
        formContainer.setBackground(new Color(255, 250, 252, 230));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 20, 8, 20);
        gbc.gridx = 0;

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        headerPanel.setOpaque(false);
        titleLabel = new JLabel("SpaceMarine Client");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(60, 40, 50));

        localeCombo = GuiUtils.createLocaleComboBox(selected -> {
            String[] parts = selected.code().split("_");
            if (parts.length == 2) {
                LocaleManager.setLocale(parts[0], parts[1]);
                updateLocaleTexts();
            }
        });

        headerPanel.add(titleLabel);
        headerPanel.add(localeCombo);
        gbc.gridy = 0;
        formContainer.add(headerPanel, gbc);

        usernameField = GuiUtils.createPlaceholderField("auth.username");
        passwordField = GuiUtils.createPasswordField("auth.password");
        confirmPasswordField = GuiUtils.createPasswordField("auth.confirm_password");
        confirmPasswordField.setVisible(false);

        gbc.gridy++; formContainer.add(usernameField, gbc);
        gbc.gridy++; formContainer.add(passwordField, gbc);
        gbc.gridy++; formContainer.add(confirmPasswordField, gbc);

        JPanel btnPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        btnPanel.setOpaque(false);
        primaryButton = createStyledButton("auth.signup", new Color(255, 100, 130), Color.WHITE);
        switchModeButton = createStyledButton("auth.switch_to_login", Color.WHITE, new Color(200, 80, 110));

        primaryButton.addActionListener(e -> handleAuth());
        switchModeButton.addActionListener(e -> toggleMode());

        btnPanel.add(primaryButton);
        btnPanel.add(switchModeButton);
        gbc.gridy++; gbc.insets = new Insets(15, 20, 5, 20);
        formContainer.add(btnPanel, gbc);

        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        errorLabel.setForeground(Color.RED);
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy++; gbc.insets = new Insets(5, 20, 15, 20);
        formContainer.add(errorLabel, gbc);

        GridBagConstraints bgGbc = new GridBagConstraints();
        bgGbc.weightx = 1.0; bgGbc.weighty = 1.0;
        bgPanel.add(formContainer, bgGbc);

        setContentPane(bgPanel);
    }

    private JButton createStyledButton(String key, Color bg, Color fg) {
        JButton btn = new JButton(LocaleManager.get(key));
        btn.setName(key);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        return btn;
    }

    private void updateLocaleTexts() {
        GuiUtils.LocaleOption selected = (GuiUtils.LocaleOption) localeCombo.getSelectedItem();
        localeCombo.removeAllItems();
        for (GuiUtils.LocaleOption option : GuiUtils.createLocaleOptions()) {
            localeCombo.addItem(option);
            if (selected != null && selected.code().equals(option.code())) {
                localeCombo.setSelectedItem(option);
            }
        }

        if (usernameField.getForeground() == Color.GRAY) {
            usernameField.setText(LocaleManager.get("auth.username"));
        }
        if (passwordField.getForeground() == Color.GRAY) {
            passwordField.setText(LocaleManager.get("auth.password"));
            passwordField.setEchoChar((char)0);
        }
        if (confirmPasswordField.isVisible() && confirmPasswordField.getForeground() == Color.GRAY) {
            confirmPasswordField.setText(LocaleManager.get("auth.confirm_password"));
            confirmPasswordField.setEchoChar((char)0);
        }

        if (isRegisterMode) {
            primaryButton.setText(LocaleManager.get("auth.signup"));
            switchModeButton.setText(LocaleManager.get("auth.switch_to_login"));
        } else {
            primaryButton.setText(LocaleManager.get("auth.login"));
            switchModeButton.setText(LocaleManager.get("auth.switch_to_signup"));
        }

        titleLabel.setText(LocaleManager.get("app.title"));

        errorLabel.setText(" ");
        revalidate();
        repaint();
    }

    private void toggleMode() {
        isRegisterMode = !isRegisterMode;
        confirmPasswordField.setVisible(isRegisterMode);
        updateLocaleTexts();
    }

    private void handleAuth() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());
        String confirm = new String(confirmPasswordField.getPassword());

        if (usernameField.getForeground() == Color.GRAY || passwordField.getForeground() == Color.GRAY ||
                (isRegisterMode && confirmPasswordField.getForeground() == Color.GRAY)) {
            showError(LocaleManager.get("auth.error.empty_fields"));
            return;
        }
        if (isRegisterMode && !pass.equals(confirm)) {
            showError(LocaleManager.get("auth.error.passwords_mismatch"));
            return;
        }

        primaryButton.setEnabled(false);
        switchModeButton.setEnabled(false);

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                try {
                    UserInfo ui = new UserInfo(user, pass);
                    String cmd = isRegisterMode ? "sign_in" : "log_in";
                    CommandRequest req = RequestsFactory.creatLogRequest(cmd, ui);

                    connection.sendRequest(req);
                    CommandResponse resp = connection.readResponse();

                    if (resp != null && resp.success() && resp.result() instanceof UserInfo resultUser) {
                        loggedInUser = resultUser;
                        return true;
                    }
                    return false;
                } catch (Exception e) {
                    return false;
                }
            }
            @Override
            protected void done() {
                primaryButton.setEnabled(true);
                switchModeButton.setEnabled(true);
                try {
                    if (get()) {
                        success = true;
                        dispose();
                    } else {
                        showError(LocaleManager.get("auth.error.network"));
                    }
                } catch (Exception e) {
                    showError("Error: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setForeground(Color.RED);
        Timer timer = new Timer(3000, e -> errorLabel.setText(" "));
        timer.setRepeats(false);
        timer.start();
    }

    private void resizeComponents() {
        float newFont = (float)(14 * scaleFactor);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, (int)newFont));
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, (int)newFont));
        confirmPasswordField.setFont(new Font("Segoe UI", Font.PLAIN, (int)newFont));

        float btnFont = (float)(13 * scaleFactor);
        primaryButton.setFont(new Font("Segoe UI", Font.BOLD, (int)btnFont));
        switchModeButton.setFont(new Font("Segoe UI", Font.BOLD, (int)btnFont));

        float titleFont = (float)(18 * scaleFactor);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, (int)titleFont));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets((int)(8 * scaleFactor), (int)(20 * scaleFactor),
                (int)(8 * scaleFactor), (int)(20 * scaleFactor));
    }
}