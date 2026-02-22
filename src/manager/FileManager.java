package manager;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileManager {
    private String lastPath;
    public boolean fileExists(String path) {
        if (path == null || path.isEmpty()) return false;
        File file = new File(path);
        return file.exists() && file.isFile();
    }
    public boolean directoryExists(String path) {
        if (path == null || path.isEmpty()) return false;
        Path filePath = Paths.get(path);
        File file = new File(path);
        return file.exists() && file.isDirectory();
    }
    public boolean canRead(String path) {
        if (path == null || path.isEmpty()) return false;
        Path filePath = Paths.get(path);
        File file = new File(filePath.toUri());
        return file.canRead();
    }
    public boolean canWrite(String path) {
        if (path == null || path.isEmpty()) return false;
        File file = new File(path);
        // For new files, check parent directory writability
        if (!file.exists()) {
            File parent = file.getParentFile();
            return parent != null && parent.canWrite();
        }
        return file.canWrite();
    }

}
