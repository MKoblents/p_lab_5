package server.db.provider;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

public interface MongoProvider {
    void initialize();
    void start();
    MongoDatabase getDb();
    MongoClient getClient();
    void shutdown();
}
