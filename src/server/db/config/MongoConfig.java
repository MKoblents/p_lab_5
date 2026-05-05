package server.db.config;

public class MongoConfig {
    private final String uri;
    private final String dbName;

    public MongoConfig() {
        this.uri = System.getenv("MONGO_URI") != null
                ? System.getenv("MONGO_URI")
                : "mongodb://localhost:27017";
        this.dbName = System.getenv("MONGO_DB") != null
                ? System.getenv("MONGO_DB")
                : "studs_db";

        if (uri.isEmpty()) {
            throw new IllegalArgumentException("MongoDB URI cannot be empty");
        }
    }
    public MongoConfig(String uri, String dbName) {
        this.uri = uri;
        this.dbName = dbName;
    }

    public String getUri() { return uri; }
    public String getDbName() { return dbName; }
}