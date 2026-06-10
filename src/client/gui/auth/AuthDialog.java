package client.gui.auth;

import client.gui.GuiClientApp;
import client.gui.utils.GuiUtils;
import client.network.ConnectionManager;
import client.utils.LocaleManager;
import client.utils.RequestsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.dto.UserInfo;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;

public class AuthDialog extends JDialog {

    private static final Logger logger = LoggerFactory.getLogger(AuthDialog.class);

    private final ConnectionManager connection;
    private boolean isRegisterMode = false;
    private boolean success = false;
    private UserInfo loggedInUser = null;

    private JLabel titleLabel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton primaryButton;
    private JButton switchModeButton;
    private JLabel errorLabel;
    private JButton langButton;

    private Dimension originalSize;
    private double scaleFactor = 1.0;

    public AuthDialog(JFrame parent, ConnectionManager connection) {
        super(parent, LocaleManager.get("auth.title"), true);
        this.connection = connection;

        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                logger.info("Auth dialog closed by user before completion.");
                System.exit(0);
            }
        });

        setupUI();
        updateLocaleTexts();

        originalSize = new Dimension(550, 650);
        Rectangle endBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setBounds(endBounds);

        double initialScaleFactor = (double) endBounds.width / originalSize.width;
        onResize(initialScaleFactor);

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

    public Rectangle getFinalBounds() {
        return getBounds();
    }

    private void setupUI() {
        JPanel bgPanel = GuiUtils.createStrippedPanel(new GridBagLayout(), 550);

        JPanel formContainer = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 250, 252, 245));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 25, 25));
                g2.setColor(new Color(255, 150, 170));
                g2.setStroke(new BasicStroke(2));
                g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 25, 25));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        formContainer.setOpaque(false);
        formContainer.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(12, 15, 12, 15);
        gbc.gridx = 0;

        JPanel headerPanel = GuiUtils.createPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        headerPanel.setOpaque(false);
        titleLabel = new JLabel(LocaleManager.get("auth.title"));
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(60, 40, 50));

        langButton = GuiUtils.createLanguageSwitchButton(
                new GuiUtils.LocaleOption("ru_RU", "Русский"),
                selected -> {
                    String[] parts = selected.code().split("_");
                    if (parts.length == 2) {
                        LocaleManager.setLocale(parts[0], parts[1]);
                        updateLocaleTexts();
                    }
                }
        );
        langButton.setFont(new Font("Segoe UI", Font.BOLD, 30));
        langButton.setPreferredSize(new Dimension(140, 70));

        headerPanel.add(titleLabel);
        headerPanel.add(langButton);
        gbc.gridy = 0;
        formContainer.add(headerPanel, gbc);

        usernameField = createRoundedField("auth.username");
        passwordField = createRoundedPasswordField("auth.password");
        confirmPasswordField = createRoundedPasswordField("auth.confirm_password");
        confirmPasswordField.setVisible(false);

        gbc.gridy++; formContainer.add(usernameField, gbc);
        gbc.gridy++; formContainer.add(passwordField, gbc);
        gbc.gridy++; formContainer.add(confirmPasswordField, gbc);

        JPanel btnPanel = GuiUtils.createPanel(new GridLayout(2, 1, 10, 10));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        primaryButton = createRoundedButton("auth.login", new Color(255, 105, 135), Color.WHITE);
        switchModeButton = createRoundedButton("auth.switch_to_signup", new Color(255, 255, 255), new Color(200, 80, 110));

        primaryButton.addActionListener(e -> handleAuth());
        switchModeButton.addActionListener(e -> toggleMode());

        btnPanel.add(primaryButton);
        btnPanel.add(switchModeButton);
        gbc.gridy++; gbc.insets = new Insets(20, 15, 10, 15);
        formContainer.add(btnPanel, gbc);

        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        errorLabel.setForeground(Color.RED);
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy++; gbc.insets = new Insets(10, 15, 15, 15);
        formContainer.add(errorLabel, gbc);

        GridBagConstraints bgGbc = new GridBagConstraints();
        bgGbc.weightx = 1.0; bgGbc.weighty = 1.0;
        bgGbc.insets = new Insets(30, 40, 30, 40);
        bgPanel.add(formContainer, bgGbc);

        setContentPane(bgPanel);
    }

    private JTextField createRoundedField(String placeholderKey) {
        JTextField field = new JTextField(LocaleManager.get(placeholderKey)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 220));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 15, 15));
                g2.dispose();
                super.paintComponent(g);
            }
        };

        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(Color.GRAY);
        field.setHorizontalAlignment(JTextField.CENTER);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 150, 170), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        field.setBackground(new Color(255, 255, 255, 220));
        field.setOpaque(false);
        field.setPreferredSize(new Dimension(0, 45));

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

    private JPasswordField createRoundedPasswordField(String placeholderKey) {
        JPasswordField field = new JPasswordField(LocaleManager.get(placeholderKey)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 220));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 15, 15));
                g2.dispose();
                super.paintComponent(g);
            }
        };

        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(Color.GRAY);
        field.setHorizontalAlignment(JPasswordField.CENTER);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 150, 170), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        field.setBackground(new Color(255, 255, 255, 220));
        field.setOpaque(false);
        field.setPreferredSize(new Dimension(0, 45));
        field.setEchoChar((char) 0);

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
                    field.setEchoChar((char) 0);
                }
            }
        });

        return field;
    }

    private JButton createRoundedButton(String key, Color bg, Color fg) {
        JButton btn = new JButton(LocaleManager.get(key)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 15, 15));
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setName(key);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 42));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg);
            }
        });

        return btn;
    }

    private void updateLocaleTexts() {
        if (usernameField.getForeground() == Color.GRAY) {
            usernameField.setText(LocaleManager.get("auth.username"));
        }
        if (passwordField.getForeground() == Color.GRAY) {
            passwordField.setText(LocaleManager.get("auth.password"));
            passwordField.setEchoChar((char) 0);
        }
        if (confirmPasswordField.isVisible() && confirmPasswordField.getForeground() == Color.GRAY) {
            confirmPasswordField.setText(LocaleManager.get("auth.confirm_password"));
            confirmPasswordField.setEchoChar((char) 0);
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
                    while (!connection.isConnected()) {
                        try {
                            if (GuiClientApp.attemptReconnect()) break;
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {}
                    }
                    UserInfo ui = new UserInfo(user, pass);
                    String cmd = isRegisterMode ? "sign_in" : "log_in";
                    CommandRequest req = RequestsFactory.creatLogRequest(cmd, ui);

                    connection.sendRequest(req);
                    CommandResponse resp = connection.readResponse();

                    if (resp != null && resp.success() && resp.result() instanceof UserInfo resultUser) {
                        loggedInUser = resultUser;
                        logger.info("Authentication successful for user: {}", user);
                        return true;
                    }
                    logger.warn("Authentication failed for user: {}. Server response: {}", user, resp != null ? resp.message() : "null");
                    return false;
                } catch (Exception e) {
                    logger.error("Network or serialization error during authentication for user: {}", user, e);
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
                    logger.error("Unexpected error during authentication UI update", e);
                    showError(LocaleManager.get("auth.error.network"));
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
        float newFont = (float) (14 * scaleFactor);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, (int) newFont));
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, (int) newFont));
        confirmPasswordField.setFont(new Font("Segoe UI", Font.PLAIN, (int) newFont));

        float btnFont = (float) (13 * scaleFactor);
        primaryButton.setFont(new Font("Segoe UI", Font.BOLD, (int) btnFont));
        switchModeButton.setFont(new Font("Segoe UI", Font.BOLD, (int) btnFont));

        float titleFont = (float) (22 * scaleFactor);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, (int) titleFont));

        Dimension fieldSize = new Dimension(0, (int)(45 * scaleFactor));
        usernameField.setPreferredSize(fieldSize);
        passwordField.setPreferredSize(fieldSize);
        confirmPasswordField.setPreferredSize(fieldSize);

        Dimension btnSize = new Dimension(0, (int) (42 * scaleFactor));
        primaryButton.setPreferredSize(btnSize);
        switchModeButton.setPreferredSize(btnSize);

        revalidate();
        repaint();
    }
}