package client.gui.buttons;

import client.gui.utils.GuiUtils;
import client.utils.LocaleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public abstract class AbstractStyledDialog extends JDialog {

    private static final Logger logger = LoggerFactory.getLogger(AbstractStyledDialog.class);

    private static final double MIN_SIZE_RATIO = 0.8;
    private static final int SCROLL_UNIT_INCREMENT = 16;
    private static final int PANEL_BORDER_SIZE = 10;
    private static final int PANEL_INNER_BORDER_SIZE = 20;
    private static final int COMPONENT_STRUT_SIZE = 15;

    protected final Dimension originalSize;
    protected JPanel contentPanel;
    protected JPanel buttonPanel;
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

        setSize(originalSize);
        setLocationRelativeTo(owner);

        addResizeListener();
        logger.debug("Initialized styled dialog: {}", titleKey);
    }

    private void setupDialog() {
        setLayout(new BorderLayout(COMPONENT_STRUT_SIZE, COMPONENT_STRUT_SIZE));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(
                (int) (originalSize.width * MIN_SIZE_RATIO),
                (int) (originalSize.height * MIN_SIZE_RATIO)
        ));
    }

    protected abstract void initComponents();

    protected abstract void layoutComponents();

    protected void applyTheme() {
        getContentPane().setBackground(GuiUtils.BACKGROUND_COLOR);
    }

    protected void createStandardButtons(Runnable okAction, Runnable cancelAction) {
        buttonPanel = GuiUtils.createPanel(new FlowLayout(FlowLayout.CENTER, 20, COMPONENT_STRUT_SIZE));
        buttonPanel.setOpaque(false);

        okButton = GuiUtils.createStyledDialogButton("button.ok", 150, 45, okAction);
        cancelButton = GuiUtils.createStyledDialogButton("button.cancel", 150, 45, cancelAction);

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        getRootPane().setDefaultButton(okButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    protected JScrollPane createScrollableContentPanel(Component... components) {
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(PANEL_BORDER_SIZE, PANEL_INNER_BORDER_SIZE, PANEL_BORDER_SIZE, PANEL_INNER_BORDER_SIZE));

        for (Component c : components) {
            contentPanel.add(c);
            contentPanel.add(Box.createVerticalStrut(COMPONENT_STRUT_SIZE));
        }

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_UNIT_INCREMENT);
        return scrollPane;
    }

    private void addResizeListener() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeComponents();
            }
        });
    }

    protected void resizeComponents() {
        logger.trace("Dialog resized. New dimensions: {}x{}", getWidth(), getHeight());
    }

    protected float scaleFontSize(float baseSize) {
        double scaleFactor = (double) getWidth() / originalSize.width;
        return (float) (baseSize * scaleFactor);
    }
}