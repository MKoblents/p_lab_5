package client.gui;

import client.utils.LocaleManager;

import javax.swing.*;
import java.awt.*;

public class MainWindow {
    private  final JFrame frame;
    private final JLabel statusLabel;
    private final JLabel userLabel;
    private final JComboBox<LocaleOption> localeCombo;
    public record LocaleOption(String code, String displayName){
        @Override
        public String toString() {
            return displayName + " (" + code + ")";
        }
    }
    public MainWindow(){
        frame = new JFrame(LocaleManager.getAppTitle());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200,800);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(5,5));

        statusLabel = new JLabel(LocaleManager.get("main.status.connecting"));
        userLabel = new JLabel(LocaleManager.get("main.user.guest")); //todo name
        localeCombo =new JComboBox<>(createLocaleOptions());
        localeCombo.setMaximumSize(new Dimension(150,25));

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,10,2));
        statusPanel.add(statusLabel);
        statusPanel.add(Box.createHorizontalGlue());
        statusPanel.add(userLabel);
        statusPanel.add(Box.createRigidArea(new Dimension(20,0)));
        statusPanel.add(localeCombo);

        JMenuBar menuBar = createMenuBar();
        frame.setJMenuBar(menuBar);
        //todo

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        contentPanel.add(new JLabel(LocaleManager.get("gui.loading"), SwingConstants.CENTER), BorderLayout.CENTER);

        frame.add(contentPanel, BorderLayout.CENTER);
        frame.add(statusPanel,BorderLayout.SOUTH);

        frame.setVisible(true);



    }

    private JMenuBar createMenuBar() {
        //todo
        JMenuBar menuBar = new JMenuBar();
        JMenu commandsMenu = new JMenu(LocaleManager.get("menu.commands"));
        commandsMenu.add(new JMenuItem(LocaleManager.get("menu.add")));
        menuBar.add(commandsMenu);
        return menuBar;
    }

    private LocaleOption[] createLocaleOptions() {
        return new LocaleOption[]{
                new LocaleOption("ru_RU", LocaleManager.get("locale.ru")),
                new LocaleOption("de_DE", LocaleManager.get("locale.de")),
                new LocaleOption("sv_SE", LocaleManager.get("locale.sv")),
                new LocaleOption("es_ES", LocaleManager.get("locale.es")),
                new LocaleOption("en_US", LocaleManager.get("locale.en"))
        };
    }
    public String showLoginDialog() {
        LoginDialog loginDialog = new LoginDialog(frame);
        loginDialog.setVisible(true);

        if (loginDialog.isSuccess()) {
            return loginDialog.getAuthenticatedUser();
        }
        return null;
    }
    public void setStatus(String message) {
        if (SwingUtilities.isEventDispatchThread()) {
            statusLabel.setText(LocaleManager.get("main.status") + ": " + message);
        } else {
            SwingUtilities.invokeLater(() -> statusLabel.setText(LocaleManager.get("main.status") + ": " + message));
        }
    }
    public void setUserName(String name) {
        if (SwingUtilities.isEventDispatchThread()) {
            userLabel.setText(LocaleManager.get("main.user") + ": " + (name != null ? name : LocaleManager.get("main.user.guest")));
        } else {
            SwingUtilities.invokeLater(() ->
                    userLabel.setText(LocaleManager.get("main.user") + ": " + (name != null ? name : LocaleManager.get("main.user.guest"))));
        }
    }

    public LocaleOption getSelectedLocale() {
        return (LocaleOption) localeCombo.getSelectedItem();
    }

    public void addLocaleChangeListener(java.util.function.Consumer<LocaleOption> listener) {
        localeCombo.addActionListener(e -> {
            LocaleOption selected = (LocaleOption) localeCombo.getSelectedItem();
            if (selected != null) {
                listener.accept(selected);
            }
        });
    }

    public void close() {
        frame.dispose();
    }
    public JFrame getFrame() {
        return frame;
    }
}