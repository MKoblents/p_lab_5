package manager;

import java.io.File;
/**
 * Utility class for file path validation and access checks.
 * Tracks the last successfully validated path.
 */
public class FileManager {
    /** Path of the last successfully validated file. */
    private String lastValidatedPath;
    /**
     * Checks if path points to an existing regular file.
     * @param path the file path to check
     * @return true if file exists and is not a directory
     */
    public boolean fileExists(String path) {
        if (path == null || path.isEmpty()) return false;
        File file = new File(path);
        return file.exists() && file.isFile();
    }
    /**
     * Checks if existing file has read permissions.
     * @param path the file path to check
     * @return true if file exists and is readable
     */
    public boolean canReadFile(String path) {
        if (!fileExists(path)) return false;
        return new File(path).canRead();
    }
    /**
     * Checks write access: for existing file or parent directory if creating new.
     * @param path the file path to check
     * @return true if write operation is permitted
     */
    public boolean canWrite(String path) {
        if (path == null || path.isEmpty()) return false;
        File file = new File(path);
        if (file.exists()) {
            return file.canWrite();
        }
        File parent = file.getParentFile();
        return parent != null && parent.exists() && parent.canWrite();
    }
    /**
     * Validates path for specified operation and updates lastValidatedPath if successful.
     * @param path the file path to validate
     * @param operation the intended file operation
     * @return true if path is valid for the operation
     * @see Operation
     */
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
    /**
     * Returns the last path that passed validation.
     * @return validated path or null if none
     */
    public String getLastValidatedPath() {
        return lastValidatedPath;
    }
    /**
     * Supported file operations for validation.
     */
    public enum Operation {
        READ,
        WRITE,
        READ_WRITE,
        CREATE
    }
}