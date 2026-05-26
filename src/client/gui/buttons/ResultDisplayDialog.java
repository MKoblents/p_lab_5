package client.gui.buttons;

import client.gui.utils.GuiUtils;
import javax.swing.*;
import java.awt.*;

public class ResultDisplayDialog extends AbstractStyledDialog {
    private JTextArea textArea;
    private final String content;

    public ResultDisplayDialog(Frame owner, String titleKey, String content) {
        super(owner, titleKey, true, 600, 500);
        this.content = content;
        textArea.setText(content);
    }

    @Override
    protected void initComponents() {
        textArea = GuiUtils.createStyledTextArea("");
    }

    @Override
    protected void layoutComponents() {
        add(GuiUtils.createStyledScrollPane(textArea), BorderLayout.CENTER);
        createStandardButtons(() -> dispose(), null);
        if (cancelButton != null) cancelButton.setVisible(false); // Для инфо-окон кнопка отмена не нужна
    }

    @Override
    protected void resizeComponents() {
        float scaledFont = scaleFontSize(14);
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, (int) scaledFont));
    }
}