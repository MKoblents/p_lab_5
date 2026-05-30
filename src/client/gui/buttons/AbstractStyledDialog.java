package client.gui.buttons;

import client.gui.utils.GuiUtils;
import client.utils.LocaleManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Base class for styled dialogs with common behavior:
 * - Theme application
 * - Responsive resizing
 * - Standard layout structure
 */
public abstract class AbstractStyledDialog extends JDialog {

    protected final Dimension originalSize;
    protected  JPanel contentPanel;
    protected  JPanel buttonPanel;

    protected JButton okButton;
    protected JButton cancelButton;

    public AbstractStyledDialog(Frame owner, String titleKey, boolean modal,
                                int originalWidth, int originalHeight) {
        super(owner, LocaleManager.get(titleKey), modal);

        this.originalSize = new Dimension(originalWidth, originalHeight);

        setupDialog();
        initComponents();
        layoutComponents();
        applyTheme();

        pack();
        setSize(originalSize);
        setLocationRelativeTo(owner);

        addResizeListener();
    }

    private void setupDialog() {
        setLayout(new BorderLayout(15, 15));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(
                (int)(originalSize.width * 0.8),
                (int)(originalSize.height * 0.8)
        ));
    }

    /**
     * Override to create dialog-specific components.
     */
    protected abstract void initComponents();

    /**
     * Override to layout components using BorderLayout regions.
     * Use contentPanel for scrollable content, buttonPanel for actions.
     */
    protected abstract void layoutComponents();

    /**
     * Override to add custom theme elements (called after base theme).
     */
    protected void applyTheme() {
        getContentPane().setBackground(GuiUtils.BACKGROUND_COLOR);
    }

    /**
     * Creates standard button panel with OK/Cancel.
     * Override okAction/cancelAction for custom behavior.
     */
    protected void createStandardButtons(Runnable okAction, Runnable cancelAction) {
        buttonPanel = GuiUtils.createPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);

        okButton = GuiUtils.createStyledDialogButton("button.ok", 150, 45, okAction);
        cancelButton = GuiUtils.createStyledDialogButton("button.cancel", 150, 45, cancelAction);

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        getRootPane().setDefaultButton(okButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Creates titled content panel with scroll support.
     * @param components components to add vertically
     * @return JScrollPane ready to add to CENTER
     */
    protected JScrollPane createScrollableContentPanel(Component... components) {
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        for (Component c : components) {
            contentPanel.add(c);
            contentPanel.add(Box.createVerticalStrut(15));
        }

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    /**
     * Adds resize listener that calls resizeComponents() on scale change.
     */
    private void addResizeListener() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeComponents();
            }
        });
    }

    /**
     * Override to scale fonts and sizes proportionally.
     * Default implementation does nothing.
     */
    protected void resizeComponents() {
        // Override in subclasses if responsive scaling is needed
    }

    /**
     * Helper: scales a font size proportionally.
     */
    protected float scaleFontSize(float baseSize) {
        double scaleFactor = (double) getWidth() / originalSize.width;
        return (float) (baseSize * scaleFactor);
    }

    /**
     * Helper: creates a titled section panel.
     */
    protected JPanel createTitledSection(String titleKey, Component... components) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(GuiUtils.PRIMARY_COLOR, 1),
                LocaleManager.get(titleKey),
                SwingConstants.LEFT,
                SwingConstants.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                GuiUtils.PRIMARY_DARK
        ));

        for (int i = 0; i < components.length; i++) {
            section.add(components[i]);
            if (i < components.length - 1) {
                section.add(Box.createVerticalStrut(10));
            }
        }
        return section;
    }
}