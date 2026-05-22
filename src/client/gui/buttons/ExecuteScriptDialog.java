package client.gui.buttons;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;

public class ExecuteScriptDialog {

    public static File showScriptFileChooser(JFrame parent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Выберите файл скрипта");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        fileChooser.setFileFilter(new NoExtensionFileFilter());
        fileChooser.setAcceptAllFileFilterUsed(false);

        int result = fileChooser.showOpenDialog(parent);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();


            if (hasNoExtension(selectedFile)) {
                return selectedFile;
            } else {
                JOptionPane.showMessageDialog(parent,
                        "Ошибка: файл должен быть без расширения!",
                        "Неверный формат файла",
                        JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }

        return null;
    }


    private static boolean hasNoExtension(File file) {
        String fileName = file.getName();
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex == -1 ||
                (lastDotIndex == 0 && fileName.substring(1).lastIndexOf('.') == -1);
    }


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
            return "Файлы без расширения (*.*)";
        }
    }
}