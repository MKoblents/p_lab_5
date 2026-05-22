package client.gui;

import client.gui.buttons.ButtonsHandler;
import client.gui.buttons.SpaceMarineInputDialog;
import client.gui.window.SpaceMarineCanvas;
import client.gui.window.SpaceMarineTable;
import client.network.ConnectionManager;
import client.utils.LocaleManager;
import client.utils.RequestsFactory;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.models.SpaceMarine;

import java.io.IOException;
import java.util.List;

import javax.swing.*;
import java.awt.*;

public class MainWindow {
    private final JFrame frame;
    private final JLabel statusLabel;
    private final JLabel userLabel;
    private final JComboBox<LocaleOption> localeCombo;
    private JPanel controlPanel;
    private JPanel contentPanel;
    private SpaceMarineTable tableModel;
    private JTable tableView;
    private ButtonsHandler buttonsHandler;

    private SpaceMarineCanvas canvas;
    private CardLayout cardLayout;
    private JButton switchButton;

    private JButton btnAdd;
    private JButton btnRemove;
    private JButton btnExecuteScript;
    private JButton btnRemoveAll;
    private JButton btnShowMine;
    private JButton btnUpdate;
    private JButton btnInfo;
    private JButton btnSpawnClient;
    private JButton btnKillClient;
    private JButton btnHelp;
    private JButton btnExit;
    private ConnectionManager connection;

    public record LocaleOption(String code, String displayName) {
        @Override
        public String toString() {
            return displayName + " (" + code + ")";
        }
    }

    public MainWindow(ConnectionManager connection) {
        this.connection = connection;
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
        frame.add(statusPanel, BorderLayout.NORTH);

        controlPanel = createControlPanel();
        frame.add(controlPanel, BorderLayout.WEST);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tableModel = new SpaceMarineTable();
        tableView = new JTable(tableModel);
        JScrollPane tableScroll = new JScrollPane(tableView);
        contentPanel.add(tableScroll, "TABLE");

        canvas = new SpaceMarineCanvas();
        JScrollPane canvasScroll = new JScrollPane(canvas);
        contentPanel.add(canvasScroll, "CANVAS");

        frame.add(contentPanel, BorderLayout.CENTER);
        switchButton = new JButton(LocaleManager.get("view.switch.to_map"));
        switchButton.setActionCommand("btn.switch_view");
        switchButton.addActionListener(e -> toggleView());
        statusPanel.add(switchButton);

        cardLayout.show(contentPanel, "TABLE");
        buttonsHandler = new ButtonsHandler(connection,this);

        frame.setVisible(true);
    }
    private void toggleView() {
        if (cardLayout != null) {
            String currentText = switchButton.getText();
            String mapText = LocaleManager.get("view.switch.to_table");

            if (currentText.equals(mapText) || currentText.contains("Table")) {
                cardLayout.show(contentPanel, "TABLE");
                switchButton.setText(LocaleManager.get("view.switch.to_map"));
                setStatus("Table view");
            } else {
                cardLayout.show(contentPanel, "CANVAS");
                switchButton.setText(LocaleManager.get("view.switch.to_table"));
                setStatus("Map view");
            }
            contentPanel.revalidate();
            contentPanel.repaint();
        }
    }

    public void switchToTableView() {
        if (cardLayout != null) {
            cardLayout.show(contentPanel, "TABLE");
            switchButton.setText(LocaleManager.get("view.switch.to_map"));
            contentPanel.revalidate();
            contentPanel.repaint();
        }
    }

    public void switchToMapView() {
        if (cardLayout != null) {
            cardLayout.show(contentPanel, "CANVAS");
            switchButton.setText(LocaleManager.get("view.switch.to_table"));
            contentPanel.revalidate();
            contentPanel.repaint();
        }
    }

    public void updateMapView(List<SpaceMarine> marines) {
        if (canvas != null) {
            SwingUtilities.invokeLater(() -> {
                canvas.setMarines(marines);
            });
        }
    }

    public void updateAllViews(List<SpaceMarine> marines) {
        if (tableModel != null) {
            SwingUtilities.invokeLater(() -> {
                tableModel.setData(marines);
                if (canvas != null) canvas.setMarines(marines);
            });
        }
    }

    private JPanel createButton(String localeKey, Runnable action) {
        JButton button = new JButton(LocaleManager.get(localeKey));
        button.setActionCommand(localeKey);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(160, 40));
        button.setBackground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.addActionListener(e -> action.run());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
        buttonPanel.setOpaque(false);
        buttonPanel.add(button);

        switch (localeKey) {
            case "btn.add" -> btnAdd = button;
            case "btn.remove" -> btnRemove = button;
            case "btn.execute_script" -> btnExecuteScript = button;
            case "btn.remove_all" -> btnRemoveAll = button;
            case "btn.show_mine" -> btnShowMine = button;
            case "btn.update" -> btnUpdate = button;
            case "btn.info" -> btnInfo = button;
            case "btn.spawn_client" -> btnSpawnClient = button;
            case "btn.kill_client" -> btnKillClient = button;
            case "btn.help" -> btnHelp = button;
            case "btn.exit" -> btnExit = button;
        }

        return buttonPanel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(255, 105, 180));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(200, 600));

        panel.add(createButton("btn.add", () -> System.out.println("Add clicked")));
        panel.add(createButton("btn.remove", () -> System.out.println("Remove clicked")));
        panel.add(createButton("btn.execute_script", () -> System.out.println("Script clicked")));
        panel.add(createButton("btn.remove_all", () -> System.out.println("Clear clicked")));
        panel.add(createButton("btn.show_mine", () -> System.out.println("Show mine clicked")));
        panel.add(createButton("btn.update", () -> System.out.println("Update clicked")));
        panel.add(createButton("btn.info", () -> System.out.println("Info clicked")));
        panel.add(createButton("btn.spawn_client", () -> System.out.println("Spawn clicked")));
        panel.add(createButton("btn.kill_client", () -> System.out.println("Kill clicked")));
        panel.add(createButton("btn.help", () -> System.out.println("Help clicked")));

        panel.add(Box.createVerticalGlue()); // Прижать кнопку выхода вниз
        panel.add(createButton("btn.exit", () -> System.exit(0)));
        btnAdd.addActionListener(e -> buttonsHandler.handleAdd());
        btnRemove.addActionListener(e -> buttonsHandler.handleRemove());
        btnExecuteScript.addActionListener(e->buttonsHandler.handleExecuteScript());

        return panel;
    }

    private LocaleOption[] createLocaleOptions() {
        return new LocaleOption[]{
                new LocaleOption("ru_RU", LocaleManager.get("locale.ru")),
                new LocaleOption("de_DE", LocaleManager.get("locale.de")),
                new LocaleOption("sv_SE", LocaleManager.get("locale.sv")),
                new LocaleOption("es_ES", LocaleManager.get("locale.es"))
        };
    }

    public void updateUITexts() {
        frame.setTitle(LocaleManager.getAppTitle());
        statusLabel.setText(LocaleManager.get("main.status.connecting"));
        userLabel.setText(LocaleManager.get("main.user.guest"));

        if (switchButton != null) {
            String currentView = cardLayout != null ?
                    (contentPanel.isShowing() ? "map" : "table") : "table";
            boolean isMapView = switchButton.getText().contains(LocaleManager.get("view.switch.to_table").substring(0, 3));
            switchButton.setText(isMapView ?
                    LocaleManager.get("view.switch.to_table") :
                    LocaleManager.get("view.switch.to_map"));
        }
        updateButtonTexts(controlPanel);

        if (tableModel != null) tableModel.fireTableStructureChanged();

        frame.revalidate();
        frame.repaint();
    }

    private void updateButtonTexts(Container container) {
        for (Component c : container.getComponents()) {
            if (c instanceof JButton btn) {
                String key = btn.getActionCommand();
                if (key != null && !key.isEmpty()) {
                    String text = LocaleManager.get(key);
                    if (!text.startsWith("!")) {
                        btn.setText(text);
                    }
                }
            } else if (c instanceof Container subContainer) {
                updateButtonTexts(subContainer);
            }
        }
    }

    public void setAddAction(Runnable action) { setButtonAction("btn.add", action); }
    public void setRemoveAction(Runnable action) { setButtonAction("btn.remove", action); }
    public void setScriptAction(Runnable action) { setButtonAction("btn.execute_script", action); }
    public void setClearAction(Runnable action) { setButtonAction("btn.remove_all", action); }
    public void setShowMineAction(Runnable action) { setButtonAction("btn.show_mine", action); }
    public void setUpdateAction(Runnable action) { setButtonAction("btn.update", action); }
    public void setInfoAction(Runnable action) { setButtonAction("btn.info", action); }
    public void setSpawnAction(Runnable action) { setButtonAction("btn.spawn_client", action); }
    public void setKillAction(Runnable action) { setButtonAction("btn.kill_client", action); }
    public void setHelpAction(Runnable action) { setButtonAction("btn.help", action); }

    private void setButtonAction(String localeKey, Runnable action) {
        JButton btn = findButtonByKey(controlPanel, localeKey);
        if (btn != null) {
            for (var listener : btn.getActionListeners()) {
                btn.removeActionListener(listener);
            }
            btn.addActionListener(e -> action.run());
        }
    }

    private JButton findButtonByKey(Container container, String key) {
        for (Component c : container.getComponents()) {
            if (c instanceof JButton btn && key.equals(btn.getActionCommand())) {
                return btn;
            } else if (c instanceof Container sub) {
                JButton found = findButtonByKey(sub, key);
                if (found != null) return found;
            }
        }
        return null;
    }

    public JButton getBtnAdd() { return btnAdd; }
    public JButton getBtnRemove() { return btnRemove; }
    public JButton getBtnExecuteScript() { return btnExecuteScript; }
    public JButton getBtnRemoveAll() { return btnRemoveAll; }
    public JButton getBtnShowMine() { return btnShowMine; }
    public JButton getBtnUpdate() { return btnUpdate; }
    public JButton getBtnInfo() { return btnInfo; }
    public JButton getBtnSpawnClient() { return btnSpawnClient; }
    public JButton getBtnKillClient() { return btnKillClient; }
    public JButton getBtnHelp() { return btnHelp; }
    public JButton getBtnExit() { return btnExit; }

    public void setStatus(String message) {
        if (SwingUtilities.isEventDispatchThread()) {
            statusLabel.setText(LocaleManager.get("main.status") + ": " + message);
        } else {
            SwingUtilities.invokeLater(() ->
                    statusLabel.setText(LocaleManager.get("main.status") + ": " + message));
        }
    }

    public void setUserName(String name) {
        if (SwingUtilities.isEventDispatchThread()) {
            userLabel.setText(LocaleManager.get("main.user") + ": " +
                    (name != null ? name : LocaleManager.get("main.user.guest")));
        } else {
            SwingUtilities.invokeLater(() ->
                    userLabel.setText(LocaleManager.get("main.user") + ": " +
                            (name != null ? name : LocaleManager.get("main.user.guest"))));
        }
    }

    public LocaleOption getSelectedLocale() {
        return (LocaleOption) localeCombo.getSelectedItem();
    }

    public void addLocaleChangeListener(java.util.function.Consumer<LocaleOption> listener) {
        localeCombo.addActionListener(e -> {
            LocaleOption selected = (LocaleOption) localeCombo.getSelectedItem();
            if (selected != null) listener.accept(selected);
        });
    }


    public void close() { frame.dispose(); }
    public JFrame getFrame() { return frame; }
    public SpaceMarineTable getTableModel() { return tableModel; }
}