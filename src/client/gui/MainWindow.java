package client.gui;
import client.config.ClientConfig;
import client.context.ClientContext;
import client.gui.buttons.ButtonsHandler;
import client.gui.buttons.SpaceMarineUpdateDialog;
import client.gui.utils.GuiUtils;
import client.gui.window.SpaceMarineCanvas;
import client.gui.window.SpaceMarineTable;
import client.network.AsyncNetworkReader;
import client.network.ConnectionManager;
import client.process.ClientProcessManager;
import client.utils.LocaleManager;
import client.utils.RequestsFactory;
import shared.dto.CommandRequest;
import shared.dto.ForwardCommandObject;
import shared.models.SpaceMarine;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class MainWindow {
    private JFrame frame;
    private JLabel statusLabel;
    private JLabel userLabel;
    private JComboBox<GuiUtils.LocaleOption> localeCombo;
    private JPanel controlPanel;
    private JPanel contentPanel;
    private SpaceMarineTable tableModel;
    private JTable tableView;
    private ButtonsHandler buttonsHandler;
    private SpaceMarineCanvas canvas;
    private CardLayout cardLayout;
    private JButton switchButton;
    private ConnectionManager connection;
    private JButton btnAdd, btnRemove, btnExecuteScript, btnRemoveAll, btnShowMine;
    private JButton btnUpdate, btnInfo, btnSpawnClient, btnKillClient, btnHelp, btnExit;
    private final AsyncNetworkReader networkReader;
    private Dimension originalSize;
    private double scaleFactor = 1.0;
    private ClientContext context;
    private final ClientConfig config;
    private ClientProcessManager processManager;
    private JComboBox<String> forwardCommandCombo;
    private JButton btnForwardCommand;

    public void setDependencies(ConnectionManager connection, ClientContext context, ClientProcessManager processManager) {
        this.connection = connection;
        this.context = context;
        this.processManager = processManager;
    }
    public ClientConfig getConfig() { return config; }

    public MainWindow(ConnectionManager connection, ClientConfig config, AsyncNetworkReader networkReader) {
        this.config = config;
        this.connection = connection;
        this.networkReader = networkReader;
        initializeFrame();
        initializeComponents();
        setupLayout();
        buttonsHandler = new ButtonsHandler(connection, this);
        originalSize = new Dimension(1200, 800);
        frame.setMinimumSize(originalSize);
        frame.setLocationRelativeTo(null);
        GuiUtils.addResizeListener(frame.getContentPane(), originalSize, this::onResize);
        startForwardCommandListener();
        frame.setVisible(true);
    }
    public AsyncNetworkReader getNetworkReader() { return networkReader; }

    private void onResize(double newScaleFactor) {
        this.scaleFactor = newScaleFactor;
        resizeComponents();
    }
    public double getScaleFactor() { return scaleFactor; }

    private void resizeComponents() {
        float scaledFontSize = (float) (GuiUtils.BASE_FONT_SIZE * scaleFactor);
        float scaledTitleFontSize = (float) (GuiUtils.BASE_TITLE_FONT_SIZE * scaleFactor);
        float scaledButtonFontSize = (float) (GuiUtils.BASE_BUTTON_FONT_SIZE * scaleFactor);
        Font regularFont = new Font("Segoe UI", Font.PLAIN, (int) scaledFontSize);
        Font boldFont = new Font("Segoe UI", Font.BOLD, (int) scaledFontSize);
        Font titleFont = new Font("Segoe UI", Font.BOLD, (int) scaledTitleFontSize);
        Font buttonFont = new Font("Segoe UI", Font.BOLD, (int) scaledButtonFontSize);

        statusLabel.setFont(regularFont);
        userLabel.setFont(regularFont);
        localeCombo.setFont(regularFont);
        if (tableView != null) {
            tableView.setFont(regularFont);
            tableView.setRowHeight((int) (28 * scaleFactor));
            tableView.getTableHeader().setFont(boldFont);
        }
        updateButtonFonts(controlPanel, buttonFont);
        int scaledButtonWidth = (int) (190 * scaleFactor);
        int scaledButtonHeight = (int) (40 * scaleFactor);
        int scaledPanelWidth = (int) (220 * scaleFactor);
        int scaledTopPanelHeight = (int) (50 * scaleFactor);
        int scaledButtonGap = (int) (8 * scaleFactor);
        int scaledBorderInset = (int) (15 * scaleFactor);

        controlPanel.setPreferredSize(new Dimension(scaledPanelWidth, 0));
        controlPanel.setMaximumSize(new Dimension(scaledPanelWidth, Integer.MAX_VALUE));
        Component[] components = frame.getContentPane().getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel && comp.getName() != null && comp.getName().equals("topPanel")) {
                comp.setPreferredSize(new Dimension(0, scaledTopPanelHeight));
            }
        }
        updateButtonSizes(controlPanel, scaledButtonWidth, scaledButtonHeight, scaledButtonGap);
        if (switchButton != null) {
            switchButton.setPreferredSize(new Dimension((int) (140 * scaleFactor), (int) (28 * scaleFactor)));
            switchButton.setFont(buttonFont);
        }
        contentPanel.setBorder(BorderFactory.createEmptyBorder(scaledBorderInset, scaledBorderInset, scaledBorderInset, scaledBorderInset));
        frame.revalidate();
        frame.repaint();
    }

    private void updateButtonFonts(Container container, Font buttonFont) {
        for (Component c : container.getComponents()) {
            if (c instanceof JButton button) button.setFont(buttonFont);
            else if (c instanceof Container subContainer) updateButtonFonts(subContainer, buttonFont);
        }
    }
    private void updateButtonSizes(Container container, int width, int height, int gap) {
        for (Component c : container.getComponents()) {
            if (c instanceof JPanel buttonPanel) {
                for (Component panelComp : buttonPanel.getComponents()) {
                    if (panelComp instanceof JButton button) {
                        button.setPreferredSize(new Dimension(width, height));
                        button.setMaximumSize(new Dimension(width, height));
                    }
                }
                buttonPanel.setMaximumSize(new Dimension(width, height + (int) (16 * scaleFactor)));
                buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, gap));
            } else if (c instanceof Container subContainer) updateButtonSizes(subContainer, width, height, gap);
        }
    }

    private void initializeFrame() {
        frame = new JFrame(LocaleManager.getAppTitle());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(0, 0));
        frame.setBackground(GuiUtils.BACKGROUND_COLOR);
        frame.getContentPane().setBackground(GuiUtils.BACKGROUND_COLOR);
    }

    private void initializeComponents() {
        statusLabel = new JLabel(LocaleManager.get("main.status.connecting"));
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, (int) GuiUtils.BASE_FONT_SIZE));
        userLabel = new JLabel(LocaleManager.get("main.user.guest"));
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, (int) GuiUtils.BASE_FONT_SIZE));
        localeCombo = GuiUtils.createLocaleComboBox(selected -> {
            String[] parts = selected.code().split("_");
            if (parts.length == 2) {
                LocaleManager.setLocale(parts[0], parts[1]);
                updateUITexts();
            }
        });

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        contentPanel.setBackground(GuiUtils.BACKGROUND_COLOR);

        tableModel = new SpaceMarineTable();
        tableView = new JTable(tableModel);
        tableView.setFillsViewportHeight(true);
        tableView.setRowHeight(28);
        tableView.setFont(new Font("Segoe UI", Font.PLAIN, (int) GuiUtils.BASE_FONT_SIZE));
        tableView.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, (int) GuiUtils.BASE_FONT_SIZE));
        tableView.setSelectionBackground(GuiUtils.PRIMARY_LIGHT);
        tableView.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = tableView.getSelectedRow();
                    if (selectedRow == -1) return;
                    int modelRow = tableView.convertRowIndexToModel(selectedRow);
                    SpaceMarine marine = tableModel.getMarineAtRow(modelRow);
                    if (marine == null) return;
                    String currentOwner = (context != null && context.getUserInfo() != null) ? context.getUserInfo().name() : null;
                    if (currentOwner == null || !currentOwner.equals(marine.getOwner())) {
                        GuiUtils.showMessageDialog(frame,
                                "Permission Denied",
                                "You can only edit your own objects.",
                                GuiUtils.MessageType.WARNING);
                        return;
                    }
                    SpaceMarineUpdateDialog dialog = new SpaceMarineUpdateDialog(frame, tableModel, context.getUserInfo().name());
                    dialog.setSelectedMarine(marine);
                    dialog.setVisible(true);
                    buttonsHandler.handleRequest(RequestsFactory.createTwoArgs("update", dialog.getUpdatedSpaceMarine().getId(), dialog.getUpdatedSpaceMarine()), "Updated successfully!");
                }
            }
        });

        TableRowSorter<SpaceMarineTable> rowSorter = new TableRowSorter<>(tableModel);
        tableView.setRowSorter(rowSorter);
        tableView.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int column = tableView.columnAtPoint(e.getPoint());
                    if (column >= 0) showFilterMenu(tableView, rowSorter, column, e.getX(), e.getY());
                }
            }
        });
        rowSorter.addRowSorterListener(e -> tableView.getTableHeader().repaint());

        JScrollPane tableScroll = new JScrollPane(tableView);
        tableScroll.setBorder(BorderFactory.createLineBorder(GuiUtils.PRIMARY_LIGHT, 1));
        contentPanel.add(tableScroll, "TABLE");

        canvas = new SpaceMarineCanvas();
        canvas.setOnMarineDoubleClick(marine -> {
            SpaceMarineUpdateDialog dialog = new SpaceMarineUpdateDialog(frame, tableModel, context.getUserInfo().name());
            dialog.setSelectedMarine(marine);
            dialog.setVisible(true);
            if (dialog.getUpdatedSpaceMarine() != null) {
                buttonsHandler.handleRequest(RequestsFactory.createTwoArgs("update", dialog.getUpdatedSpaceMarine().getId(), dialog.getUpdatedSpaceMarine()), "SpaceMarine updated successfully!");
            }
        });
        JScrollPane canvasScroll = new JScrollPane(canvas);
        canvasScroll.setBorder(BorderFactory.createLineBorder(GuiUtils.PRIMARY_LIGHT, 1));
        contentPanel.add(canvasScroll, "CANVAS");

        switchButton = new JButton(LocaleManager.get("view.switch.to_map"));
        switchButton.setFont(new Font("Segoe UI", Font.BOLD, (int) GuiUtils.BASE_BUTTON_FONT_SIZE));
        switchButton.setBackground(GuiUtils.PRIMARY_COLOR);
        switchButton.setForeground(Color.WHITE);
        switchButton.setFocusPainted(false);
        switchButton.setBorderPainted(false);
        switchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        switchButton.setPreferredSize(new Dimension(140, 28));
        switchButton.addActionListener(e -> toggleView());
    }

    private void showFilterMenu(JTable table, TableRowSorter<SpaceMarineTable> sorter, int column, int x, int y) {
        JPopupMenu filterMenu = new JPopupMenu();
        JMenuItem clearFilter = new JMenuItem("Clear Filter");
        clearFilter.addActionListener(e -> sorter.setRowFilter(null));
        filterMenu.add(clearFilter);
        filterMenu.addSeparator();
        if (table.getModel().getColumnClass(column) == String.class) {
            JPanel filterPanel = new JPanel(new BorderLayout(5, 0));
            filterPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            JTextField filterText = new JTextField(15);
            filterText.setToolTipText("Type to filter...");
            JButton applyBtn = new JButton("Apply");
            applyBtn.addActionListener(ev -> {
                String text = filterText.getText().trim();
                if (text.isEmpty()) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text), column));
            });
            filterPanel.add(filterText, BorderLayout.CENTER);
            filterPanel.add(applyBtn, BorderLayout.EAST);
            filterMenu.add(filterPanel);
        } else if (Number.class.isAssignableFrom(table.getModel().getColumnClass(column))) {
            JPanel filterPanel = new JPanel(new GridLayout(2, 2, 5, 2));
            filterPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            JTextField minField = new JTextField(8);
            JTextField maxField = new JTextField(8);
            minField.setToolTipText("Min value");
            maxField.setToolTipText("Max value");
            JButton applyBtn = new JButton("Apply");
            applyBtn.addActionListener(ev -> {
                try {
                    Number min = minField.getText().trim().isEmpty() ? null : Double.valueOf(minField.getText());
                    Number max = maxField.getText().trim().isEmpty() ? null : Double.valueOf(maxField.getText());
                    sorter.setRowFilter(new RowFilter<SpaceMarineTable, Integer>() {
                        @Override
                        public boolean include(Entry<? extends SpaceMarineTable, ? extends Integer> entry) {
                            Object value = entry.getValue(column);
                            if (value == null) return false;
                            double numValue = ((Number) value).doubleValue();
                            if (min != null && numValue < min.doubleValue()) return false;
                            return max == null || numValue <= max.doubleValue();
                        }
                    });
                } catch (NumberFormatException ex) {
                    GuiUtils.showMessageDialog(frame, "Input Error", "Please enter valid numbers", GuiUtils.MessageType.ERROR);
                }
            });
            filterPanel.add(new JLabel("Min:"));
            filterPanel.add(minField);
            filterPanel.add(new JLabel("Max:"));
            filterPanel.add(maxField);
            filterMenu.add(filterPanel);
            filterMenu.add(applyBtn);
        }
        filterMenu.show(table.getTableHeader(), x, y);
    }

    private void setupLayout() {
        JPanel topPanel = createTopPanel();
        topPanel.setName("topPanel");
        frame.add(topPanel, BorderLayout.NORTH);
        controlPanel = createControlPanel();
        frame.add(controlPanel, BorderLayout.WEST);
        frame.add(contentPanel, BorderLayout.CENTER);
        cardLayout.show(contentPanel, "TABLE");
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(GuiUtils.PRIMARY_COLOR);
        panel.setBorder(new EmptyBorder(10, 15, 10, 15));
        panel.setPreferredSize(new Dimension(0, 50));
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setOpaque(false); leftPanel.add(statusLabel); statusLabel.setForeground(Color.WHITE);
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centerPanel.setOpaque(false); centerPanel.add(userLabel); userLabel.setForeground(Color.WHITE);
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false); rightPanel.add(localeCombo); rightPanel.add(switchButton);
        panel.add(leftPanel, BorderLayout.WEST); panel.add(centerPanel, BorderLayout.CENTER); panel.add(rightPanel, BorderLayout.EAST);
        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(GuiUtils.PANEL_COLOR);
        panel.setBorder(new EmptyBorder(20, 15, 20, 15));
        panel.setPreferredSize(new Dimension(220, 0));
        panel.setMaximumSize(new Dimension(220, Integer.MAX_VALUE));
        JLabel titleLabel = new JLabel("Commands", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, (int) GuiUtils.BASE_TITLE_FONT_SIZE));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(titleLabel);
        int buttonWidth = 190, buttonHeight = 40;
        panel.add(createStyledButton("btn.add", buttonWidth, buttonHeight, () -> System.out.println("Add clicked")));
        panel.add(createStyledButton("btn.remove", buttonWidth, buttonHeight, () -> System.out.println("Remove clicked")));
        panel.add(createStyledButton("btn.execute_script", buttonWidth, buttonHeight, () -> System.out.println("Script clicked")));
        panel.add(createStyledButton("btn.remove_all", buttonWidth, buttonHeight, () -> System.out.println("Clear clicked")));
        panel.add(createStyledButton("btn.show_mine", buttonWidth, buttonHeight, () -> System.out.println("Show mine clicked")));
        panel.add(createStyledButton("btn.update", buttonWidth, buttonHeight, () -> System.out.println("Update clicked")));
        panel.add(createStyledButton("btn.info", buttonWidth, buttonHeight, () -> System.out.println("Info clicked")));
        panel.add(createStyledButton("btn.spawn_client", buttonWidth, buttonHeight, () -> System.out.println("Spawn clicked")));
        panel.add(createStyledButton("btn.kill_client", buttonWidth, buttonHeight, () -> System.out.println("Kill clicked")));
        panel.add(createStyledButton("btn.help", buttonWidth, buttonHeight, () -> System.out.println("Help clicked")));
        panel.add(createStyledButton("btn.forward", buttonWidth, buttonHeight, () -> buttonsHandler.handleForwardCommand()));
        panel.add(Box.createVerticalGlue());
        panel.add(createStyledButton("btn.exit", buttonWidth, buttonHeight, () -> System.out.println("Log out clicked")));
        btnAdd.addActionListener(e -> buttonsHandler.handleAdd());
        btnRemove.addActionListener(e -> buttonsHandler.handleRemove());
        btnExecuteScript.addActionListener(e -> buttonsHandler.handleExecuteScript());
        btnRemoveAll.addActionListener(e -> buttonsHandler.handleClear());
        btnUpdate.addActionListener(e -> buttonsHandler.handleUpdate());
        btnHelp.addActionListener(e -> buttonsHandler.handleHelp());
        btnInfo.addActionListener(e -> buttonsHandler.handleInfo());
        btnSpawnClient.addActionListener(e -> { try { buttonsHandler.handleSpawn(); } catch (IOException ex) { throw new RuntimeException(ex); } });
        btnKillClient.addActionListener(e -> buttonsHandler.handleKill());
        btnExit.addActionListener(e -> buttonsHandler.handleLogOut());
        return panel;
    }

    private void startForwardCommandListener() {
        new Thread(() -> {
            while (true) {
                try {
                    CommandRequest fwd = networkReader.getForwardQueue().poll();
                    if (fwd != null && "forward_command".equals(fwd.commandType()) && fwd.args() instanceof ForwardCommandObject fco) {
                        SwingUtilities.invokeLater(() -> executeForwardedCommand(fco.commandKey()));
                    }
                    Thread.sleep(50);
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
            }
        }, "forward-command-listener").start();
    }

    public void executeForwardedCommand(String commandKey) {
        if (buttonsHandler == null) return;
        SwingUtilities.invokeLater(() -> {
            switch (commandKey.toLowerCase()) {
                case "add" -> buttonsHandler.handleAdd();
                case "remove" -> buttonsHandler.handleRemove();
                case "update" -> buttonsHandler.handleUpdate();
                case "clear" -> buttonsHandler.handleClear();
                case "info" -> buttonsHandler.handleInfo();
                case "help" -> buttonsHandler.handleHelp();
                case "execute_script" -> buttonsHandler.handleExecuteScript();
                default -> GuiUtils.showMessageDialog(frame, "Warning", "Received unknown command: " + commandKey, GuiUtils.MessageType.WARNING);
            }
        });
    }

    private JPanel createStyledButton(String localeKey, int width, int height, Runnable defaultAction) {
        JButton button = GuiUtils.createStyledButton(LocaleManager.get(localeKey), width, height, defaultAction);
        button.setActionCommand(localeKey);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        buttonPanel.setOpaque(false);
        buttonPanel.setMaximumSize(new Dimension(width, height + 16));
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
            contentPanel.revalidate(); contentPanel.repaint();
        }
    }
    public void switchToTableView() {
        if (cardLayout != null) {
            cardLayout.show(contentPanel, "TABLE");
            switchButton.setText(LocaleManager.get("view.switch.to_map"));
            contentPanel.revalidate(); contentPanel.repaint();
        }
    }
    public void switchToMapView() {
        if (cardLayout != null) {
            cardLayout.show(contentPanel, "CANVAS");
            switchButton.setText(LocaleManager.get("view.switch.to_table"));
            contentPanel.revalidate(); contentPanel.repaint();
        }
    }
    public void updateMapView(List<SpaceMarine> marines) { if (canvas != null) SwingUtilities.invokeLater(() -> canvas.setMarines(marines)); }
    public void updateAllViews(List<SpaceMarine> marines) {
        if (tableModel != null) SwingUtilities.invokeLater(() -> { tableModel.setData(marines); if (canvas != null) canvas.setMarines(marines); });
    }
    public void updateUITexts() {
        frame.setTitle(LocaleManager.getAppTitle());
        statusLabel.setText(LocaleManager.get("main.status.connecting"));
        userLabel.setText(LocaleManager.get("main.user.guest"));
        if (switchButton != null) {
            boolean isMapView = switchButton.getText().contains(LocaleManager.get("view.switch.to_table").substring(0, 3));
            switchButton.setText(isMapView ? LocaleManager.get("view.switch.to_table") : LocaleManager.get("view.switch.to_map"));
        }
        updateButtonTexts(controlPanel);
        if (tableModel != null) tableModel.fireTableStructureChanged();
        frame.revalidate(); frame.repaint();
    }
    private void updateButtonTexts(Container container) {
        for (Component c : container.getComponents()) {
            if (c instanceof JButton btn) {
                String key = btn.getActionCommand();
                if (key != null && !key.isEmpty()) {
                    String text = LocaleManager.get(key);
                    if (!text.startsWith("!")) btn.setText(text);
                }
            } else if (c instanceof Container subContainer) updateButtonTexts(subContainer);
        }
    }
    private void setButtonAction(String localeKey, Runnable action) {
        JButton btn = findButtonByKey(controlPanel, localeKey);
        if (btn != null) {
            for (var listener : btn.getActionListeners()) btn.removeActionListener(listener);
            btn.addActionListener(e -> action.run());
        }
    }
    private JButton findButtonByKey(Container container, String key) {
        for (Component c : container.getComponents()) {
            if (c instanceof JButton btn && key.equals(btn.getActionCommand())) return btn;
            else if (c instanceof Container sub) { JButton found = findButtonByKey(sub, key); if (found != null) return found; }
        }
        return null;
    }
    public JButton getBtnAdd() { return btnAdd; } public JButton getBtnRemove() { return btnRemove; }
    public JButton getBtnExecuteScript() { return btnExecuteScript; } public JButton getBtnRemoveAll() { return btnRemoveAll; }
    public JButton getBtnShowMine() { return btnShowMine; } public JButton getBtnUpdate() { return btnUpdate; }
    public JButton getBtnInfo() { return btnInfo; } public JButton getBtnSpawnClient() { return btnSpawnClient; }
    public JButton getBtnKillClient() { return btnKillClient; } public JButton getBtnHelp() { return btnHelp; }
    public JButton getBtnExit() { return btnExit; }
    public void setStatus(String message) {
        if (SwingUtilities.isEventDispatchThread()) statusLabel.setText(LocaleManager.get("main.status") + ": " + message);
        else SwingUtilities.invokeLater(() -> statusLabel.setText(LocaleManager.get("main.status") + ": " + message));
    }
    public void setUserName(String name) {
        if (SwingUtilities.isEventDispatchThread()) userLabel.setText(LocaleManager.get("main.user") + ": " + (name != null ? name : LocaleManager.get("main.user.guest")));
        else SwingUtilities.invokeLater(() -> userLabel.setText(LocaleManager.get("main.user") + ": " + (name != null ? name : LocaleManager.get("main.user.guest"))));
    }
    public GuiUtils.LocaleOption getSelectedLocale() { return (GuiUtils.LocaleOption) localeCombo.getSelectedItem(); }
    public void addLocaleChangeListener(java.util.function.Consumer<GuiUtils.LocaleOption> listener) { localeCombo.addActionListener(e -> { GuiUtils.LocaleOption selected = (GuiUtils.LocaleOption) localeCombo.getSelectedItem(); if (selected != null) listener.accept(selected); }); }
    public void close() { frame.dispose(); }
    public JFrame getFrame() { return frame; }
    public SpaceMarineTable getTableModel() { return tableModel; }
    public void setContext(ClientContext context) { this.context = context; }
    public ClientContext getContext() { return context; }
    public SpaceMarineCanvas getCanvasModel() { return canvas; }
}