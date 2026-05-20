package client.gui;

import javax.swing.*;
import java.awt.*;

public class MainWindow {
    private  final JFrame frame;
    private final JLabel label;
    private final JLabel userLabel;
    private final JComboBox<LocaleOption> localeCombo;
    public record LocaleOption(String code, String displayName){
        @Override
        public String toString() {
            return displayName + " (" + code + ")";
        }
    }
    public MainWindow(){
        frame = new JFrame("SpaceMarine Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200,800);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(5,5));

        label = new JLabel("Status: connecting...");
        userLabel = new JLabel("User: guest"); //todo name
        localeCombo =new JComboBox<>(createLocaleOptions());
        localeCombo.setMaximumSize(new Dimension(150,25));

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,10,2));
        statusPanel.add(label);
        statusPanel.add(Box.createHorizontalGlue());
        statusPanel.add(userLabel);
        statusPanel.add(Box.createRigidArea(new Dimension(20,0)));
        statusPanel.add(localeCombo);

        JMenuBar menuBar = createMenuBar();
        frame.setJMenuBar(menuBar);
        //todo

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        contentPanel.add(new JLabel("GUI loading", SwingConstants.CENTER), BorderLayout.CENTER);

        frame.add(contentPanel, BorderLayout.CENTER);
        frame.add(statusPanel,BorderLayout.SOUTH);

        frame.setVisible(true);



    }

    private JMenuBar createMenuBar() {
        //todo
        JMenuBar menuBar = new JMenuBar();
        JMenu commandsMenu = new JMenu("Commands");
        commandsMenu.add(new JMenuItem("ADD"));
        menuBar.add(commandsMenu);
        return menuBar;
    }

    private LocaleOption[] createLocaleOptions() {
        return new LocaleOption[]{
                new LocaleOption("ru_RU", "Русский"),
                new LocaleOption("de_DE", "Deutsch"),
                new LocaleOption("sv_SE", "Svenska"),
                new LocaleOption("es_ES", "Español")
        };
    }
}
