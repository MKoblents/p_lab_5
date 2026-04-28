package server.db.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class DbConfig {
    private final String url;
    private final String user;
    private final String password;
    private final int maxPoolSize;

    public DbConfig(String url, String user, String password, int maxPoolSize) {
        if (url == null || url.trim().isEmpty()) throw new IllegalArgumentException("DB URL is empty");
        if (user == null || password == null) throw new IllegalArgumentException("DB credentials are missing");
        if (maxPoolSize <= 0) throw new IllegalArgumentException("Pool size must be positive");
        this.url = url; this.user = user; this.password = password; this.maxPoolSize = maxPoolSize;
    }

    public static DbConfig loadFromProperties(Path path) throws IOException {
        Properties props = new Properties();
        try (InputStream is = Files.newInputStream(path)) { props.load(is); }
        return new DbConfig(
                props.getProperty("db.url"),
                props.getProperty("db.user"),
                props.getProperty("db.password"),
                Integer.parseInt(props.getProperty("db.pool.size", "10"))
        );
    }

    public String getUrl() { return url; }
    public String getUser() { return user; }
    public String getPassword() { return password; }
    public int getMaxPoolSize() { return maxPoolSize; }
}