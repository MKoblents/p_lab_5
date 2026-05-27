package client.gui.buttons;

import client.gui.utils.GuiUtils;
import client.utils.LocaleManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;

public class ExecuteScriptDialog extends JDialog {
    private File selectedFile;
    private JTextField filePathField;
    private JLabel titleLabel, infoLabel, fileLabel;
    private JButton browseButton, executeButton, cancelButton;
    private JFrame parent;

    private final Dimension originalSize = new Dimension(650, 320);

    public ExecuteScriptDialog(JFrame parent) {
        super(parent, true);
        this.parent = parent;
        initComponents();
        layoutComponents();
        applyTheme();
        setSize(originalSize);
        setLocationRelativeTo(parent);
        setMinimumSize(new Dimension(500, 280));

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeComponents();
            }
        });
    }

    private void initComponents() {
        titleLabel = new JLabel(LocaleManager.get("dialog.script.title"), SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));

        infoLabel = new JLabel(LocaleManager.get("dialog.script.info"), SwingConstants.CENTER);
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoLabel.setForeground(GuiUtils.PRIMARY_DARK);

        fileLabel = new JLabel(LocaleManager.get("dialog.script.file_path"), SwingConstants.RIGHT);
        fileLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        fileLabel.setForeground(GuiUtils.TEXT_COLOR);

        filePathField = new JTextField();
        filePathField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        filePathField.setEditable(false);
        filePathField.setBackground(Color.WHITE);
        filePathField.setBorder(BorderFactory.createLineBorder(GuiUtils.PRIMARY_LIGHT, 1));
        filePathField.setPreferredSize(new Dimension(0, 40));

        browseButton = GuiUtils.createStyledButton(LocaleManager.get("dialog.script.browse"), 0, 0, this::openFileChooser);
        browseButton.setPreferredSize(new Dimension(120, 40));
        browseButton.setMaximumSize(new Dimension(120, 40));

        executeButton = GuiUtils.createStyledButton(LocaleManager.get("dialog.script.execute"), 0, 0, () -> {
            if (selectedFile == null) {
                JOptionPane.showMessageDialog(this,
                        LocaleManager.get("dialog.script.no_file"),
                        LocaleManager.get("dialog.script.title_error"),
                        JOptionPane.WARNING_MESSAGE);
            } else {
                dispose();
            }
        });
        executeButton.setPreferredSize(new Dimension(130, 45));
        executeButton.setMaximumSize(new Dimension(130, 45));

        cancelButton = GuiUtils.createStyledButton(LocaleManager.get("dialog.script.cancel"), 0, 0, this::dispose);
        cancelButton.setPreferredSize(new Dimension(130, 45));
        cancelButton.setMaximumSize(new Dimension(130, 45));
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(GuiUtils.BACKGROUND_COLOR);

        // Title Panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        add(titlePanel, BorderLayout.NORTH);

        // Info Label
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        infoPanel.setOpaque(false);
        infoPanel.add(infoLabel);
        add(infoPanel, BorderLayout.NORTH);

        // File Selection Panel
        JPanel filePanel = new JPanel(new GridBagLayout());
        filePanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 25, 10, 25);

        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        filePanel.add(fileLabel, gbc);

        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        filePanel.add(filePathField, gbc);

        gbc.gridy = 1;
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(10, 10, 10, 25);
        filePanel.add(browseButton, gbc);

        add(filePanel, BorderLayout.CENTER);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 20));
        buttonPanel.setOpaque(false);
        buttonPanel.add(executeButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void applyTheme() {
        getContentPane().setBackground(GuiUtils.BACKGROUND_COLOR);
    }

    private void resizeComponents() {
        double scaleFactor = (double) getWidth() / originalSize.width;
        float titleSize = (float) (24 * scaleFactor);
        float infoSize = (float) (14 * scaleFactor);
        float fieldSize = (float) (14 * scaleFactor);
        float labelSize = (float) (12 * scaleFactor);

        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, (int) titleSize));
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, (int) infoSize));
        fileLabel.setFont(new Font("Segoe UI", Font.BOLD, (int) labelSize));
        filePathField.setFont(new Font("Segoe UI", Font.PLAIN, (int) fieldSize));
        browseButton.setFont(new Font("Segoe UI", Font.BOLD, (int) (12 * scaleFactor)));
        executeButton.setFont(new Font("Segoe UI", Font.BOLD, (int) (14 * scaleFactor)));
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, (int) (14 * scaleFactor)));

        int scaledFieldHeight = (int) (40 * scaleFactor);
        int scaledButtonWidth = (int) (120 * scaleFactor);
        int scaledButtonHeight = (int) (40 * scaleFactor);

        filePathField.setPreferredSize(new Dimension(0, scaledFieldHeight));
        filePathField.setMaximumSize(new Dimension(Short.MAX_VALUE, scaledFieldHeight));
        browseButton.setPreferredSize(new Dimension(scaledButtonWidth, scaledButtonHeight));
        browseButton.setMaximumSize(new Dimension(scaledButtonWidth, scaledButtonHeight));
        executeButton.setPreferredSize(new Dimension((int)(130 * scaleFactor), scaledButtonHeight + 5));
        executeButton.setMaximumSize(new Dimension((int)(130 * scaleFactor), scaledButtonHeight + 5));
        cancelButton.setPreferredSize(new Dimension((int)(130 * scaleFactor), scaledButtonHeight + 5));
        cancelButton.setMaximumSize(new Dimension((int)(130 * scaleFactor), scaledButtonHeight + 5));
    }

    private void openFileChooser() {
        StyledFileChooser fileChooser = new StyledFileChooser(null);
        if (fileChooser.showDialog() == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            if (selectedFile != null) {
                filePathField.setText(selectedFile.getAbsolutePath());
            }
        }
    }

    public File getSelectedFile() {
        return selectedFile;
    }

    /**
     * Custom styled file chooser dialog that matches the app theme
     * and supports resizing.
     */
    private static class StyledFileChooser {
        private final JDialog dialog;
        private JFileChooser fileChooser;
        private File selectedFile;
        private int result = JFileChooser.CANCEL_OPTION;

        public StyledFileChooser(Frame parent) {
            dialog = new JDialog(parent, LocaleManager.get("dialog.script.chooser_title"), true);
            initFileChooser();
            buildDialog();
        }

        private void initFileChooser() {
            fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.addChoosableFileFilter(new NoExtensionFileFilter());
            fileChooser.setFileFilter(new NoExtensionFileFilter());

            applyFileChooserStyling(fileChooser);

            // ⭐ Apply font to file list AFTER components are fully created
            SwingUtilities.invokeLater(() -> {
                styleFileNameList(fileChooser, 40);
                // Fallback: also try styling via UIManager for this instance
                if (fileChooser.isShowing()) {
                    forceStyleFileList(fileChooser, 40);
                }
            });
        }

        /**
         * First attempt: recursive search for JList
         */
        private void styleFileNameList(Component comp, int fontSize) {
            if (comp instanceof JList<?> list) {
                // Check if this JList is likely the file list by checking its parent hierarchy
                Component parent = list.getParent();
                if (parent instanceof JScrollPane) {
                    Component scrollParent = parent.getParent();
                    // File lists are usually inside a JScrollPane inside a JSplitPane or JPanel
                    if (scrollParent instanceof JSplitPane ||
                            scrollParent instanceof JPanel ||
                            list.getName().contains("file")) {
                        list.setFont(new Font("Segoe UI", Font.PLAIN, fontSize));
                        list.setFixedCellHeight((int)(fontSize * 1.8));
                        list.repaint();
                    }
                }
            }
            if (comp instanceof Container container) {
                for (Component child : container.getComponents()) {
                    styleFileNameList(child, fontSize);
                }
            }
        }

        /**
         * Fallback: direct access via component hierarchy traversal
         */
        private void forceStyleFileList(JFileChooser fc, int fontSize) {
            // Try to find the file list by traversing known JFileChooser structure
            for (Component comp : fc.getComponents()) {
                if (comp instanceof JSplitPane splitPane) {
                    // Left side usually contains the file list
                    Component left = splitPane.getLeftComponent();
                    if (left instanceof JScrollPane scrollPane) {
                        Component view = scrollPane.getViewport().getView();
                        if (view instanceof JList<?> fileList) {
                            fileList.setFont(new Font("Segoe UI", Font.PLAIN, fontSize));
                            fileList.setFixedCellHeight((int)(fontSize * 1.8));
                            fileList.repaint();
                            return; // Found it, stop searching
                        }
                    }
                } else if (comp instanceof JScrollPane scrollPane) {
                    // Some L&F put the list directly in a JScrollPane
                    Component view = scrollPane.getViewport().getView();
                    if (view instanceof JList<?> fileList) {
                        fileList.setFont(new Font("Segoe UI", Font.PLAIN, fontSize));
                        fileList.setFixedCellHeight((int)(fontSize * 1.8));
                        fileList.repaint();
                        return;
                    }
                }
                // Recurse into nested containers
                if (comp instanceof Container) {
                    forceStyleFileListRecursive(comp, fontSize);
                }
            }
        }

        private void forceStyleFileListRecursive(Component comp, int fontSize) {
            if (comp instanceof JList<?> list &&
                    list.getCellRenderer() != null &&
                    list.getCellRenderer().getClass().getName().contains("File")) {
                list.setFont(new Font("Segoe UI", Font.PLAIN, fontSize));
                list.setFixedCellHeight((int)(fontSize * 1.8));
                list.repaint();
                return;
            }
            if (comp instanceof Container container) {
                for (Component child : container.getComponents()) {
                    forceStyleFileListRecursive(child, fontSize);
                }
            }
        }

        private void applyFileChooserStyling(JFileChooser fc) {
            fc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            fc.setBackground(GuiUtils.BACKGROUND_COLOR);
            fc.setForeground(GuiUtils.TEXT_COLOR);

            // Style the approve/cancel buttons if accessible
            for (Component comp : fc.getComponents()) {
                if (comp instanceof JPanel) {
                    stylePanel((JPanel) comp);
                }
            }
        }

        private void stylePanel(JPanel panel) {
            panel.setBackground(GuiUtils.BACKGROUND_COLOR);
            for (Component comp : panel.getComponents()) {
                if (comp instanceof JButton btn) {
                    btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    btn.setBackground(GuiUtils.PRIMARY_COLOR);
                    btn.setForeground(Color.WHITE);
                    btn.setFocusPainted(false);
                    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else if (comp instanceof JComboBox<?> combo) {
                    combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    combo.setBackground(Color.WHITE);
                } else if (comp instanceof JTextField field) {
                    field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    field.setBackground(Color.WHITE);
                } else if (comp instanceof JPanel subPanel) {
                    stylePanel(subPanel);
                }
            }
        }
        private void buildDialog() {
            dialog.setLayout(new BorderLayout(10, 10));
            dialog.getContentPane().setBackground(GuiUtils.BACKGROUND_COLOR);
            dialog.setMinimumSize(new Dimension(1000, 800));
            dialog.setSize(800, 600);
            dialog.setLocationRelativeTo(null);

            // ⭐ ВАЖНО: Убираем стандартные кнопки JFileChooser
            fileChooser.setControlButtonsAreShown(false);

            // Добавляем file chooser в центр
            dialog.add(fileChooser, BorderLayout.CENTER);

            // Создаём панель с кнопками через GuiUtils
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
            buttonPanel.setOpaque(false);
            buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

            // ⭐ Используем GuiUtils.createStyledButton
            JButton openButton = GuiUtils.createStyledButton(
                    LocaleManager.get("dialog.script.open"),
                    100, 40,  // width, height
                    () -> {
                        selectedFile = fileChooser.getSelectedFile();
                        result = JFileChooser.APPROVE_OPTION;
                        dialog.dispose();
                    }
            );

            JButton cancelButton = GuiUtils.createStyledButton(
                    LocaleManager.get("dialog.script.cancel"),
                    100, 40,  // width, height
                    () -> {
                        result = JFileChooser.CANCEL_OPTION;
                        dialog.dispose();
                    }
            );

            buttonPanel.add(openButton);
            buttonPanel.add(cancelButton);
            dialog.add(buttonPanel, BorderLayout.SOUTH);

            // Добавляем listener для ресайза
            dialog.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    resizeFileChooser();
                    // ⭐ Также ресайзим кнопки
                    double scaleFactor = (double) dialog.getWidth() / 800.0;
                    int scaledButtonWidth = (int)(100 * scaleFactor);
                    int scaledButtonHeight = (int)(40 * scaleFactor);
                    openButton.setPreferredSize(new Dimension(scaledButtonWidth, scaledButtonHeight));
                    openButton.setMaximumSize(new Dimension(scaledButtonWidth, scaledButtonHeight));
                    cancelButton.setPreferredSize(new Dimension(scaledButtonWidth, scaledButtonHeight));
                    cancelButton.setMaximumSize(new Dimension(scaledButtonWidth, scaledButtonHeight));
                    dialog.revalidate();
                    dialog.repaint();
                }
            });
        }
        private JButton createDialogButton(String text) {
            JButton btn = new JButton(text);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btn.setBackground(GuiUtils.PRIMARY_COLOR);
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setPreferredSize(new Dimension(100, 35));
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    btn.setBackground(GuiUtils.PRIMARY_DARK);
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    btn.setBackground(GuiUtils.PRIMARY_COLOR);
                }
            });
            return btn;
        }

        private void resizeFileChooser() {
            double scaleFactor = (double) dialog.getWidth() / 800.0;
            float scaledFont = (float) (13 * scaleFactor);

            fileChooser.setFont(new Font("Segoe UI", Font.PLAIN, (int) scaledFont));

            // Recursively resize components in file chooser
            resizeComponent(fileChooser, scaleFactor);
        }

        private void resizeComponent(Component comp, double scaleFactor) {
            if (comp instanceof Container container) {
                for (Component child : container.getComponents()) {
                    if (child instanceof JButton btn) {
                        btn.setFont(new Font("Segoe UI", Font.BOLD, (int) (12 * scaleFactor)));
                    } else if (child instanceof JTextField field) {
                        field.setFont(new Font("Segoe UI", Font.PLAIN, (int) (12 * scaleFactor)));
                    } else if (child instanceof JComboBox<?> combo) {
                        combo.setFont(new Font("Segoe UI", Font.PLAIN, (int) (12 * scaleFactor)));
                    } else if (child instanceof JLabel label) {
                        label.setFont(new Font("Segoe UI", Font.PLAIN, (int) (12 * scaleFactor)));
                    }
                    if (child instanceof Container) {
                        resizeComponent(child, scaleFactor);
                    }
                }
            }
        }

        public int showDialog() {
            dialog.setVisible(true);
            return result;
        }

        public File getSelectedFile() {
            return selectedFile;
        }
    }

    /**
     * File filter for files without extension (as per original requirement)
     */
    static class NoExtensionFileFilter extends FileFilter {
        @Override
        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            }
            return hasNoExtension(file);
        }

        @Override
        public String getDescription() {
            return LocaleManager.get("dialog.script.filter_no_ext");
        }

        private boolean hasNoExtension(File file) {
            String fileName = file.getName();
            int lastDotIndex = fileName.lastIndexOf('.');
            return lastDotIndex == -1 ||
                    (lastDotIndex == 0 && fileName.substring(1).lastIndexOf('.') == -1);
        }
    }
}