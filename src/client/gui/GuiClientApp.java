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
       MainWindow mainWindow = new MainWindow();

        System.out.println("GUI Client started on EDT: " +
                SwingUtilities.isEventDispatchThread());
    }
}
