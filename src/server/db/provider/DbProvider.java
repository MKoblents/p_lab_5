package server.db.provider;

import java.sql.Connection;
import java.sql.SQLException;

public interface DbProvider {
    Connection getConnection() throws SQLException;
    void shutdown();
    void initialize() throws SQLException;
}
