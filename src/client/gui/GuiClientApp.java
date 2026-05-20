package client.gui;

import client.config.ClientConfig;

import javax.swing.*;

public class GuiClientApp {
    // to run:  mvn exec:java -Dexec.mainClass="client.gui.GuiClientApp"
    public static void main(String[] args){
        ClientConfig config = ClientConfig.parse(args);
        System.out.println("Config: " + config);
        SwingUtilities.invokeLater(GuiClientApp::createAndShowGui);
    }
    private static void createAndShowGui() {
        JFrame frame = new JFrame("spaceMarine Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1024, 768);
        frame.setLocationRelativeTo(null);
        frame.setResizable(true);
        frame.setVisible(true);
        System.out.println("GUI Client started on EDT: " +
                SwingUtilities.isEventDispatchThread());
    }
}
