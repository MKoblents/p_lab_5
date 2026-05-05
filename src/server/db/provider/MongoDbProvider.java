package server.db.provider;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class MongoDbProvider implements MongoProvider{
    private static final Logger logger = LoggerFactory.getLogger(MongoDbProvider.class);

    private MongoClient client;
    private MongoDatabase database;
    private final String uri;
    private final String dbName;
    public MongoDbProvider(String uri, String dbName) {
        if (uri == null || uri.trim().isEmpty()) throw new IllegalArgumentException("MongoDB URI is empty");
        if (dbName == null || dbName.trim().isEmpty()) throw new IllegalArgumentException("Database name is empty");
        this.uri = uri;
        this.dbName = dbName;
    }
    @Override
    public void initialize() {
        logger.info("Initializing MongoDB client for URI: {}", maskUri(uri));
        client = MongoClients.create(uri);
        database = client.getDatabase(dbName);
        logger.info("MongoDB client initialized successfully.");
    }
    @Override
    public void start() {
        if (client == null) {
            throw new IllegalStateException("MongoDB client not initialized. Call initialize() first.");
        }
        try {
            database.runCommand(new Document("ping", 1));
            logger.info("MongoDB connection verified. Connected to database: {}", dbName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to establish MongoDB connection: " + e.getMessage(), e);
        }
    }
    @Override
    public MongoDatabase getDb() {
        if (database == null) throw new IllegalStateException("Database not initialized. Call start() first.");
        return database;
    }

    @Override
    public MongoClient getClient() {
        if (client == null) throw new IllegalStateException("Client not initialized. Call initialize() first.");
        return client;
    }
    @Override
    public void shutdown() {
        if (client != null) {
            client.close();
            logger.info("MongoDB connection pool closed.");
        }
    }

    private String maskUri(String uri) {
        if (uri.contains("@")) {
            String[] parts = uri.split("@");
            return "mongodb://***@" + parts[1];
        }
        return uri;
    }


}
