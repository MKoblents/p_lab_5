package server.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.db.provider.DbProvider;

import java.nio.file.Path;
import java.sql.Connection;

public class DbInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DbInitializer.class);
    private final DbProvider provider;

    public DbInitializer(DbProvider provider) {
        this.provider = provider;
    }

    public void start(Path sqlDir) {
        try {
            provider.initialize();
            logger.info("Database connection pool initialized successfully.");
            try (Connection conn = provider.getConnection()) {
                conn.setAutoCommit(false);
                new DbSchemaRunner(conn).ensureSchemaExists(sqlDir);
                conn.commit();
            }
        } catch (Exception e) {
            logger.error("Failed to initialize database: {}", e.getMessage(), e);
            provider.shutdown();
            throw new RuntimeException("Server startup aborted due to database connection failure", e);
        }
    }

    public DbProvider getProvider() {
        return provider;
    }
}