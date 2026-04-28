package server.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.db.provider.DbProvider;

public class DbInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DbInitializer.class);
    private final DbProvider provider;

    public DbInitializer(DbProvider provider) {
        this.provider = provider;
    }

    public void start() {
        try {
            provider.initialize();
            logger.info("Database connection pool initialized successfully.");
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