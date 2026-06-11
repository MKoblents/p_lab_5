package server.db.provider;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import server.db.config.DbConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class HikariDbProvider implements DbProvider{
    private final DbConfig config;
    private HikariDataSource dataSource;
    public HikariDbProvider(DbConfig config){
        this.config = config;
    }

    @Override
    public void initialize() throws SQLException {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getUrl());
        hikariConfig.setUsername(config.getUser());
        hikariConfig.setPassword(config.getPassword());
        hikariConfig.setMaximumPoolSize(config.getMaxPoolSize());
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setConnectionTimeout(5000);
        hikariConfig.setValidationTimeout(3000);

        dataSource = new HikariDataSource(hikariConfig);

        try (Connection test = dataSource.getConnection()){
            PreparedStatement preparedStatement = test.prepareStatement("select 1");
            preparedStatement.executeQuery();
        } catch (SQLException e) {
            shutdown();
            throw new SQLException("Database connection test failed. Verify URL, credentials, or Docker status.", e);
        }
    }

    @Override
    public void shutdown() {
        if (dataSource != null &&  !dataSource.isClosed()){
            dataSource.close();
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (dataSource == null){
            throw new IllegalStateException("Connection pool has not been initialized. Call initialize() first.");
        }
        return dataSource.getConnection();
    }
}
