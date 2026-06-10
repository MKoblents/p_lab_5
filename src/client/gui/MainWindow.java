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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.regex.Pattern;

public class MainWindow {
    private static final Logger logger = LoggerFactory.getLogger(MainWindow.class);
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
    private JButton btnAdd, btnRemove, btnExecuteScript, btnRemoveAll, btnShowMine, btnShuffle;
    private JButton btnUpdate, btnInfo, btnSpawnClient, btnKillClient, btnHelp, btnExit;
    private final AsyncNetworkReader networkReader;
    private Dimension originalSize;
    private double scaleFactor = 1.0;
    public ClientContext context;
    private final ClientConfig config;
    private ClientProcessManager processManager;
    private JButton langButton;

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
        logger.info("MainWindow initialized and displayed successfully.");
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
        Font buttonFont = new Font("Segoe UI", Font.BOLD, (int) scaledButtonFontSize);

        statusLabel.setFont(regularFont);
        userLabel.setFont(regularFont);

        if (tableView != null) {
            tableView.setFont(regularFont);
            tableView.setRowHeight((int) (28 * scaleFactor));
            tableView.getTableHeader().setFont(boldFont);
        }

        if (langButton != null) {
            int scaledLangBtnWidth = (int) (100 * scaleFactor);
            int scaledLangBtnHeight = (int) (28 * scaleFactor);
            langButton.setPreferredSize(new Dimension(scaledLangBtnWidth, scaledLangBtnHeight));
            langButton.setMaximumSize(new Dimension(scaledLangBtnWidth, scaledLangBtnHeight));
            langButton.setFont(new Font("Segoe UI", Font.BOLD, (int) scaledButtonFontSize));
        }

        int scaledPanelWidth = (int) (220 * scaleFactor);
        controlPanel.setPreferredSize(new Dimension(scaledPanelWidth, 0));
        controlPanel.setMaximumSize(new Dimension(scaledPanelWidth, Integer.MAX_VALUE));
        updateButtonFonts(controlPanel, buttonFont);

        int scaledButtonWidth = (int) (190 * scaleFactor);
        int scaledButtonHeight = (int) (40 * scaleFactor);
        int scaledButtonGap = (int) (8 * scaleFactor);
        int scaledTopPanelHeight = (int) (50 * scaleFactor);
        int scaledBorderInset = (int) (15 * scaleFactor);

        updateButtonSizes(controlPanel, scaledButtonWidth, scaledButtonHeight, scaledButtonGap);

        Component[] components = frame.getContentPane().getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel && "topPanel".equals(comp.getName())) {
                comp.setPreferredSize(new Dimension(0, scaledTopPanelHeight));
            }
        }

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
                        button.setFont(new Font("Segoe UI", Font.BOLD, (int)(GuiUtils.BASE_BUTTON_FONT_SIZE * scaleFactor)));
                    }
                }
                buttonPanel.setMaximumSize(new Dimension(width, height + (int) (16 * scaleFactor)));
                buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, gap));
            } else if (c instanceof Container subContainer) {
                updateButtonSizes(subContainer, width, height, gap);
            }
        }
    }

    private void initializeFrame() {
        frame = new JFrame(LocaleManager.getAppTitle());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(0, 0));
        JPanel rootPanel = GuiUtils.createStrippedPanel(new BorderLayout(), 1200);
        frame.setContentPane(rootPanel);
    }

    private void initializeComponents() {
        statusLabel = GuiUtils.createLabel(LocaleManager.get("main.status.connecting"), (int) GuiUtils.BASE_FONT_SIZE, false);
        userLabel = GuiUtils.createLabel(LocaleManager.get("main.user.guest"), (int) GuiUtils.BASE_FONT_SIZE, false);

        langButton = GuiUtils.createLanguageSwitchButton(
                new GuiUtils.LocaleOption("ru_RU", "Русский"),
                selected -> {
                    String[] parts = selected.code().split("_");
                    if (parts.length == 2) {
                        LocaleManager.setLocale(parts[0], parts[1]);
                        updateUITexts();
                    }
                }
        );

        cardLayout = new CardLayout();
        contentPanel = GuiUtils.createPanel(cardLayout);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        contentPanel.setOpaque(false);

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
                        GuiUtils.showMessageDialog(frame, LocaleManager.get("status.permission_denied"), LocaleManager.get("status.only_edit_own"), GuiUtils.MessageType.WARNING);
                        return;
                    }

                    SpaceMarineUpdateDialog dialog = new SpaceMarineUpdateDialog(frame, tableModel, context.getUserInfo().name());
                    dialog.setSelectedMarine(marine);
                    dialog.setVisible(true);
                    if (dialog.getUpdatedSpaceMarine() != null) {
                        buttonsHandler.handleRequest(RequestsFactory.createTwoArgs("update", dialog.getUpdatedSpaceMarine().getId(), dialog.getUpdatedSpaceMarine()), LocaleManager.get("status.space_marine_updated"));
                    }
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
        tableScroll.setOpaque(false);
        contentPanel.add(tableScroll, "TABLE");

        canvas = new SpaceMarineCanvas();
        canvas.setOnMarineDoubleClick(marine -> {
            if (marine.getOwner().equals(context.getUserInfo().name())) {
                SpaceMarineUpdateDialog dialog = new SpaceMarineUpdateDialog(frame, tableModel, context.getUserInfo().name());
                dialog.setSelectedMarine(marine);
                dialog.setVisible(true);
                if (dialog.getUpdatedSpaceMarine() != null) {
                    buttonsHandler.handleRequest(RequestsFactory.createTwoArgs("update", dialog.getUpdatedSpaceMarine().getId(), dialog.getUpdatedSpaceMarine()), LocaleManager.get("status.space_marine_updated"));
                }
            } else {
                GuiUtils.showMessageDialog(frame, LocaleManager.get("status.permission_denied"), LocaleManager.get("status.only_edit_own"), GuiUtils.MessageType.WARNING);
            }
        });

        JScrollPane canvasScroll = new JScrollPane(canvas);
        canvasScroll.setBorder(BorderFactory.createLineBorder(GuiUtils.PRIMARY_LIGHT, 1));
        canvasScroll.setOpaque(false);
        contentPanel.add(canvasScroll, "CANVAS");

        switchButton = GuiUtils.createStyledButton(LocaleManager.get("view.switch.to_map"), 140, 28, this::toggleView);
    }

    private void showFilterMenu(JTable table, TableRowSorter<SpaceMarineTable> sorter, int column, int x, int y) {
        JPopupMenu filterMenu = new JPopupMenu();
        JMenuItem clearFilter = new JMenuItem(LocaleManager.get("table.filter.clear"));
        clearFilter.addActionListener(e -> sorter.setRowFilter(null));
        clearFilter.setFont(new Font("Segoe UI", Font.PLAIN, (int) GuiUtils.BASE_MESSAGE_SIZE));
        filterMenu.add(clearFilter);
        filterMenu.addSeparator();

        if (table.getModel().getColumnClass(column) == String.class) {
            JPanel filterPanel = GuiUtils.createPanel(new BorderLayout(5, 0));
            filterPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            JTextField filterText = new JTextField(15);
            filterText.setToolTipText(LocaleManager.get("table.filter.tooltip"));
            filterText.setFont(new Font("Segoe UI", Font.PLAIN, (int) GuiUtils.BASE_MESSAGE_SIZE));

            JButton applyBtn = new JButton(LocaleManager.get("button.apply"));
            applyBtn.addActionListener(ev -> {
                String text = filterText.getText().trim();
                if (text.isEmpty()) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text), column));
            });
            applyBtn.setFont(new Font("Segoe UI", Font.PLAIN, (int) GuiUtils.BASE_MESSAGE_SIZE));

            filterPanel.add(filterText, BorderLayout.CENTER);
            filterPanel.add(applyBtn, BorderLayout.EAST);
            filterMenu.add(filterPanel);
        } else if (Number.class.isAssignableFrom(table.getModel().getColumnClass(column))) {
            JPanel filterPanel = GuiUtils.createPanel(new GridLayout(2, 2, 5, 2));
            filterPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            JTextField minField = new JTextField(8);
            JTextField maxField = new JTextField(8);
            minField.setToolTipText(LocaleManager.get("table.filter.min"));
            maxField.setToolTipText(LocaleManager.get("table.filter.max"));
            maxField.setFont(new Font("Segoe UI", Font.PLAIN, (int) GuiUtils.BASE_MESSAGE_SIZE));
            minField.setFont(new Font("Segoe UI", Font.PLAIN, (int) GuiUtils.BASE_MESSAGE_SIZE));

            JButton applyBtn = new JButton(LocaleManager.get("button.apply"));
            applyBtn.addActionListener(ev -> {
                try {
                    Number min = minField.getText().trim().isEmpty() ? null : Double.valueOf(minField.getText());
                    Number max = maxField.getText().trim().isEmpty() ? null : Double.valueOf(maxField.getText());

                    sorter.setRowFilter(new RowFilter<>() {
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
                    logger.warn("Invalid number format in table filter: {}", ex.getMessage());
                    GuiUtils.showMessageDialog(frame, LocaleManager.get("dialog.error.title"), LocaleManager.get("error.please_enter_valid_numbers"), GuiUtils.MessageType.ERROR);
                }
            });
            applyBtn.setFont(new Font("Segoe UI", Font.PLAIN, (int) GuiUtils.BASE_MESSAGE_SIZE));
            JLabel l1= new JLabel(LocaleManager.get("table.filter.min_label"));
            l1.setFont(new Font("Segoe UI", Font.PLAIN, (int) GuiUtils.BASE_MESSAGE_SIZE));
            filterPanel.add(l1);
            filterPanel.add(minField);
            JLabel l2 =new JLabel(LocaleManager.get("table.filter.max_label"));
            l2.setFont(new Font("Segoe UI", Font.PLAIN, (int) GuiUtils.BASE_MESSAGE_SIZE));
            filterPanel.add(l2);
            filterPanel.add(maxField);
            filterMenu.setFont(new Font("Segoe UI", Font.PLAIN, (int) GuiUtils.BASE_MESSAGE_SIZE));
            filterMenu.add(filterPanel);
            filterMenu.add(applyBtn);
        }
        filterMenu.setPreferredSize(new Dimension(500,250));
        filterMenu.setMinimumSize(new Dimension(1000,500));
        filterMenu.show(table.getTableHeader(), x, y);
    }

    private void setupLayout() {
        JPanel rootPanel = (JPanel) frame.getContentPane();
        JPanel topPanel = createTopPanel();
        topPanel.setName("topPanel");
        topPanel.setOpaque(false);
        rootPanel.add(topPanel, BorderLayout.NORTH);

        controlPanel = createControlPanel();
        rootPanel.add(controlPanel, BorderLayout.WEST);
        rootPanel.add(contentPanel, BorderLayout.CENTER);
        cardLayout.show(contentPanel, "TABLE");
    }

    private JPanel createTopPanel() {
        JPanel panel = GuiUtils.createRoundedPanel(new BorderLayout(10, 0), Color.WHITE, 0, 50);
        panel.setBorder(new EmptyBorder(10, 20, 10, 20));
        panel.setPreferredSize(new Dimension(0, 60));

        JPanel leftPanel = GuiUtils.createPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.add(statusLabel);
        statusLabel.setForeground(Color.BLACK);

        JPanel centerPanel = GuiUtils.createPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centerPanel.add(userLabel);
        userLabel.setForeground(Color.BLACK);

        JPanel rightPanel = GuiUtils.createPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.add(langButton);
        rightPanel.add(switchButton);

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(rightPanel, BorderLayout.EAST);
        panel.setOpaque(false);
        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = GuiUtils.createRoundedPanel(null, GuiUtils.PANEL_COLOR, 25, 25);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 15, 20, 15));
        panel.setPreferredSize(new Dimension(220, 0));
        panel.setMaximumSize(new Dimension(220, Integer.MAX_VALUE));

        int buttonWidth = 190, buttonHeight = 40;

        panel.add(createStyledButton("btn.add", buttonWidth, buttonHeight, () -> buttonsHandler.handleAdd()));
        panel.add(createStyledButton("btn.remove", buttonWidth, buttonHeight, () -> buttonsHandler.handleRemove()));
        panel.add(createStyledButton("btn.execute_script", buttonWidth, buttonHeight, () -> buttonsHandler.handleExecuteScript()));
        panel.add(createStyledButton("btn.remove_all", buttonWidth, buttonHeight, () -> buttonsHandler.handleClear()));
        panel.add(createStyledButton("btn.update", buttonWidth, buttonHeight, () -> buttonsHandler.handleUpdate()));
        panel.add(createStyledButton("btn.info", buttonWidth, buttonHeight, () -> buttonsHandler.handleInfo()));
        panel.add(createStyledButton("btn.spawn_client", buttonWidth, buttonHeight, () -> {
            try { buttonsHandler.handleSpawn(); } catch (IOException ex) {
                logger.error("Failed to spawn client", ex);
                throw new RuntimeException(ex);
            }
        }));
        panel.add(createStyledButton("btn.kill_client", buttonWidth, buttonHeight, () -> buttonsHandler.handleKill()));
        panel.add(createStyledButton("btn.help", buttonWidth, buttonHeight, () -> buttonsHandler.handleHelp()));
        panel.add(createStyledButton("btn.forward", buttonWidth, buttonHeight, () -> buttonsHandler.handleForwardCommand()));
//        panel.add(Box.createVerticalGlue());
        panel.add(createStyledButton("btn.exit", buttonWidth, buttonHeight, () -> buttonsHandler.handleLogOut()));

        return panel;
    }

    public void startForwardCommandListener() {
        new Thread(() -> {
            while (true) {
                try {
                    CommandRequest fwd = networkReader.getForwardQueue().poll();
                    if (fwd != null) {
                        logger.debug("Forward listener received: type={}, args={}", fwd.commandType(), fwd.args());

                        if ("forward_command".equals(fwd.commandType()) && fwd.args() instanceof ForwardCommandObject fco) {
                            logger.info("Executing forwarded command: {}", fco.commandKey());
                            SwingUtilities.invokeLater(() -> executeForwardedCommand(fco.commandKey()));
                        } else if ("child_disconnected".equals(fwd.commandType())) {
                            String disconnectedChildId = (String) fwd.args();
                            logger.info("Child disconnected signal received: {}", disconnectedChildId);

                            SwingUtilities.invokeLater(() -> {
                                if (context != null) {
                                    boolean removed = context.removeChild(disconnectedChildId);
                                    if (removed) {
                                        setStatus(LocaleManager.get("status.child_disconnected").replace("{client_id}", disconnectedChildId));
                                        logger.debug("Removed child {} from context", disconnectedChildId);
                                        if (processManager != null) {
                                            processManager.killChild(disconnectedChildId);
                                        }
                                    }
                                }
                            });
                        }
                    }
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    logger.warn("Forward command listener interrupted.");
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "forward-command-listener").start();
    }

    public void executeForwardedCommand(String commandKey) {
        if (buttonsHandler == null) {
            logger.warn("Attempted to execute forwarded command '{}' but ButtonsHandler is null.", commandKey);
            return;
        }

        switch (commandKey.toLowerCase()) {
            case "add" -> buttonsHandler.handleAdd();
            case "remove" -> buttonsHandler.handleRemove();
            case "update" -> buttonsHandler.handleUpdate();
            case "clear" -> buttonsHandler.handleClear();
            case "info" -> buttonsHandler.handleInfo();
            case "help" -> buttonsHandler.handleHelp();
            case "execute_script" -> buttonsHandler.handleExecuteScript();
            default -> {
                logger.warn("Received unknown forwarded command: {}", commandKey);
                GuiUtils.showMessageDialog(frame, LocaleManager.get("dialog.warning.title"), LocaleManager.get("error.unknown_forwarded_command") + commandKey, GuiUtils.MessageType.WARNING);
            }
        }
    }

    private JPanel createStyledButton(String localeKey, int width, int height, Runnable action) {
        JButton button = GuiUtils.createStyledButton(LocaleManager.get(localeKey), width, height, action);
        button.setActionCommand(localeKey);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel buttonPanel = GuiUtils.createPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
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
            case "btn.shuffle" -> btnShuffle = button;
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
                setStatus(LocaleManager.get("main.status.table_view"));
            } else {
                cardLayout.show(contentPanel, "CANVAS");
                switchButton.setText(LocaleManager.get("view.switch.to_table"));
                setStatus(LocaleManager.get("main.status.map_view"));
            }
            contentPanel.revalidate();
            contentPanel.repaint();
        }
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
        frame.revalidate();
        frame.repaint();
    }

    private void updateButtonTexts(Container container) {
        for (Component c : container.getComponents()) {
            if (c instanceof JButton btn) {
                String key = btn.getActionCommand();
                if (key != null && !key.isEmpty()) {
                    String text = LocaleManager.get(key);
                    if (!text.startsWith("!")) btn.setText(text);
                }
            } else if (c instanceof Container subContainer) {
                updateButtonTexts(subContainer);
            }
        }
    }

    public void setStatus(String message) {
        String statusText = LocaleManager.get("main.status") + ": " + message;
        if (SwingUtilities.isEventDispatchThread()) {
            statusLabel.setText(statusText);
        } else {
            SwingUtilities.invokeLater(() -> statusLabel.setText(statusText));
        }
    }

    public void setUserName(String name) {
        String userNameText = LocaleManager.get("main.user") + ": " + (name != null ? name : LocaleManager.get("main.user.guest"));
        if (SwingUtilities.isEventDispatchThread()) {
            userLabel.setText(userNameText);
        } else {
            SwingUtilities.invokeLater(() -> userLabel.setText(userNameText));
        }
    }

    public void close() {
        logger.info("Closing MainWindow.");
        frame.dispose();
    }

    public JFrame getFrame() { return frame; }
    public SpaceMarineTable getTableModel() { return tableModel; }
    public void setContext(ClientContext context) { this.context = context; }
    public ClientContext getContext() { return context; }
    public SpaceMarineCanvas getCanvasModel() { return canvas; }
}