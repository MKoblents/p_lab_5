package client.gui.utils;

import client.utils.LocaleManager;

import javax.swing.*;
import java.awt.*;

public class LocalePanel extends JPanel {
    private final JComboBox<GuiUtils.LocaleOption> localeCombo;

    public LocalePanel(Color backgroundColor, boolean showLabel) {
        setOpaque(false);
        setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        if (showLabel) {
            JLabel langLabel = new JLabel("🌐");
            add(langLabel);
        }

        localeCombo = GuiUtils.createLocaleComboBox(selected -> {
            String[] parts = selected.code().split("_");
            LocaleManager.setLocale(parts[0], parts[1]);
            // Здесь нужен триггер обновления всего UI (через callback)
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window instanceof UpdatableUI) {
                ((UpdatableUI) window).updateUITexts();
            }
        });
        add(localeCombo);
    }

    public JComboBox<GuiUtils.LocaleOption> getCombo() { return localeCombo; }
}

// Интерфейс для окон, которые умеют обновлять тексты
interface UpdatableUI {
    void updateUITexts();
}