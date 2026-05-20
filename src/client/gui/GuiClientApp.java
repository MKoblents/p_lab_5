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
        String username = mainWindow.showLoginDialog();

        if (username != null) {
            mainWindow.setUserName(username);
            mainWindow.setStatus("Подключено как " + username);
        } else {
            mainWindow.setStatus("Ошибка авторизации");
            // Можно закрыть приложение или показать диалог снова
        }

        System.out.println("GUI Client started on EDT: " +
                SwingUtilities.isEventDispatchThread());

    }
}
