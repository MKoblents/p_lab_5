package server.db;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.stream.Stream;

public class DbSchemaRunner {
    private static final Logger logger = LoggerFactory.getLogger(DbSchemaRunner.class);
    private final Connection connection;

    public DbSchemaRunner(Connection connection) { this.connection = connection; }

    public void ensureSchemaExists(Path sqlDir) throws SQLException, IOException {
        if (!tableExists("users")) {
            logger.info("Database schema not found. Initializing from SQL scripts...");
            runScripts(sqlDir);
        }
    }

    private boolean tableExists(String tableName) throws SQLException {
        try (ResultSet rs = connection.getMetaData().getTables(null, null, tableName, null)) {
            return rs.next();
        }
    }

    private void runScripts(Path dir) throws IOException, SQLException {
        if (!Files.exists(dir)) {
            logger.warn("SQL directory not found: {}", dir);
            return;
        }
        try (Stream<Path> paths = Files.list(dir)) {
            paths.sorted()
                    .filter(p -> p.toString().endsWith(".sql"))
                    .forEach(this::executeScript);
        }
        connection.commit();
        logger.info("Database schema initialized successfully.");
    }

    private void executeScript(Path sqlFile) {
        logger.info("Running: {}", sqlFile.getFileName());
        try {
            String sql = Files.readString(sqlFile);
            String[] statements = sql.split(";");
            try (Statement stmt = connection.createStatement()) {
                for (String s : statements) {
                    String trimmed = s.trim();
                    if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                        stmt.execute(trimmed);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to execute {}: {}", sqlFile, e.getMessage());
        }
    }
}