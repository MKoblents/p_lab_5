package client.gui;

import client.utils.LocaleManager;
import javax.swing.*;
import java.awt.*;

public class MainWindow {
    private final JFrame frame;
    private final JLabel statusLabel;
    private final JLabel userLabel;
    private final JComboBox<LocaleOption> localeCombo;
    private JMenu commandsMenu;
    private JMenuItem addMenuItem;
    private JMenuBar menuBar;
    private SpaceMarineTable tableModel;
    private JTable tableView;

    public record LocaleOption(String code, String displayName) {
        @Override
        public String toString() {
            return displayName + " (" + code + ")";
        }
    }

    public MainWindow() {
        frame = new JFrame(LocaleManager.getAppTitle());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(5, 5));

        statusLabel = new JLabel(LocaleManager.get("main.status.connecting"));
        userLabel = new JLabel(LocaleManager.get("main.user.guest"));
        localeCombo = new JComboBox<>(createLocaleOptions());
        localeCombo.setMaximumSize(new Dimension(150, 25));

        localeCombo.addActionListener(e -> {
            LocaleOption selected = (LocaleOption) localeCombo.getSelectedItem();
            if (selected != null) {
                String[] parts = selected.code().split("_");
                if (parts.length == 2) {
                    LocaleManager.setLocale(parts[0], parts[1]);
                    updateUITexts();
                }
            }
        });

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        statusPanel.add(statusLabel);
        statusPanel.add(Box.createHorizontalGlue());
        statusPanel.add(userLabel);
        statusPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        statusPanel.add(localeCombo);

        menuBar = createMenuBar();
        frame.setJMenuBar(menuBar);
        //todo

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tableModel = new SpaceMarineTable();
        tableView = new JTable(tableModel);
        JScrollPane tableScroll = new JScrollPane(tableView);
        contentPanel.add(tableScroll, BorderLayout.CENTER);

        frame.add(contentPanel, BorderLayout.CENTER);
        frame.add(statusPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private JMenuBar createMenuBar() {
        //todo
        JMenuBar menuBar = new JMenuBar();
        commandsMenu = new JMenu(LocaleManager.get("menu.commands"));
        addMenuItem = new JMenuItem(LocaleManager.get("menu.add"));
        commandsMenu.add(addMenuItem);
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

    public void updateUITexts() {
        frame.setTitle(LocaleManager.getAppTitle());
        statusLabel.setText(LocaleManager.get("main.status.connecting"));
        userLabel.setText(LocaleManager.get("main.user.guest"));

        if (commandsMenu != null) commandsMenu.setText(LocaleManager.get("menu.commands"));
        if (addMenuItem != null) addMenuItem.setText(LocaleManager.get("menu.add"));

        if (tableModel != null) tableModel.fireTableStructureChanged();

        frame.revalidate();
        frame.repaint();
        if (menuBar != null) {
            menuBar.revalidate();
            menuBar.repaint();
        }
    }

//    public String showLoginDialog(ConnectionManager connection) {
//        LoginDialog loginDialog = new LoginDialog(frame, connection);
//        loginDialog.setVisible(true);
//        return loginDialog.isSuccess() ? loginDialog.getLoggedInUser() : null;
//    }

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

    public SpaceMarineTable getTableModel() {
        return tableModel;
    }
}