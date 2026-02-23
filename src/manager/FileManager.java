package manager;

import java.io.File;

public class FileManager {
    private String lastValidatedPath;

    public boolean fileExists(String path) {
        if (path == null || path.isEmpty()) return false;
        File file = new File(path);
        return file.exists() && file.isFile();
    }
    public boolean canReadFile(String path) {
        if (!fileExists(path)) return false;
        return new File(path).canRead();
    }
    public boolean canWrite(String path) {
        if (path == null || path.isEmpty()) return false;
        File file = new File(path);
        if (file.exists()) {
            return file.canWrite();
        }
        File parent = file.getParentFile();
        return parent != null && parent.exists() && parent.canWrite();
    }
    public boolean validate(String path, Operation operation) {
        if (path == null || path.isEmpty()) {
            lastValidatedPath = null;
            return false;
        }
        boolean valid = switch (operation) {
            case READ -> fileExists(path) && canReadFile(path);
            case WRITE, CREATE -> canWrite(path);
            case READ_WRITE -> fileExists(path) && canReadFile(path) && canWrite(path);
        };
        if (valid) {
            lastValidatedPath = path;
        }return valid;
    }
    public String getLastValidatedPath() {
        return lastValidatedPath;
    }
    public enum Operation {
        READ,
        WRITE,
        READ_WRITE,
        CREATE
    }
}