package server.db.dao;

import server.db.provider.DbProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    private final DbProvider provider;
    public UserDAO(DbProvider provider){
        this.provider = provider;
    }
    public void register(String user, String password) throws SQLException {
        String sql = "insert into Users (name, password) values (?,?);";
        try (Connection connection = provider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1, user);
            preparedStatement.setString(2, password);
            preparedStatement.executeUpdate();
        }
    }
    public String getHashPassword(String user) throws  SQLException{
        try (Connection connection = provider.getConnection();
            PreparedStatement ps = connection.prepareStatement("select password from Users where name = ?")){
            ps.setString(1, user);
            try (ResultSet resultSet = ps.executeQuery()){
                if (resultSet.next()){
                    return resultSet.getString("password");
                }
                return null;

            }
        }
    }
}
