package client.gui.buttons;

import client.gui.utils.GuiUtils;
import client.utils.LocaleManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;

public class ExecuteScriptDialog extends JDialog {
    private File selectedFile;
    private JTextField filePathField;
    private JLabel titleLabel, infoLabel, fileLabel;
    private JButton browseButton, executeButton, cancelButton;

    private final Dimension originalSize = new Dimension(600, 280);

    public ExecuteScriptDialog(JFrame parent) {
        super(parent, true);
        initComponents();
        layoutComponents();
        applyTheme();
        setSize(originalSize);
        setLocationRelativeTo(parent);
        setMinimumSize(new Dimension(500, 250));

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

        browseButton = GuiUtils.createStyledButton(LocaleManager.get("dialog.script.browse"), 0, 0, this::openFileChooser);
        browseButton.setPreferredSize(new Dimension(100, 40));
        browseButton.setMaximumSize(new Dimension(100, 40));

        executeButton = GuiUtils.createStyledButton(LocaleManager.get("dialog.script.execute"), 0, 0, () -> {
            if (selectedFile == null) {
                JOptionPane.showMessageDialog(this, LocaleManager.get("dialog.script.no_file"), LocaleManager.get("dialog.script.title_error"), JOptionPane.WARNING_MESSAGE);
            } else {
                dispose();
            }
        });
        executeButton.setPreferredSize(new Dimension(120, 45));
        executeButton.setMaximumSize(new Dimension(120, 45));

        cancelButton = GuiUtils.createStyledButton(LocaleManager.get("dialog.script.cancel"), 0, 0, this::dispose);
        cancelButton.setPreferredSize(new Dimension(120, 45));
        cancelButton.setMaximumSize(new Dimension(120, 45));
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(15, 15));
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
        gbc.insets = new Insets(10, 20, 10, 20);

        gbc.gridy = 0;
        filePanel.add(fileLabel, gbc);

        gbc.gridy = 1;
        gbc.weightx = 1.0;
        filePanel.add(filePathField, gbc);

        gbc.gridy = 2;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.CENTER;
        filePanel.add(browseButton, gbc);

        add(filePanel, BorderLayout.CENTER);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
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

        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, (int) titleSize));
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, (int) infoSize));
        filePathField.setFont(new Font("Segoe UI", Font.PLAIN, (int) fieldSize));
        browseButton.setFont(new Font("Segoe UI", Font.BOLD, (int) (12 * scaleFactor)));
        executeButton.setFont(new Font("Segoe UI", Font.BOLD, (int) (14 * scaleFactor)));
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, (int) (14 * scaleFactor)));
    }

    private void openFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(LocaleManager.get("dialog.script.chooser_title"));
        // No extension filter, as scripts might have no extension or custom ones

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            filePathField.setText(selectedFile.getAbsolutePath());
        }
    }

    public File getSelectedFile() {
        return selectedFile;
    }
}