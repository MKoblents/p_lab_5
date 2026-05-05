package server.db.dao;

import server.db.provider.DbProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class UserDAO {
    private final DbProvider provider;
    public UserDAO(DbProvider provider){
        this.provider = provider;
    }
    public void register(String user, String password, String parentName) throws SQLException {
        String sql = "insert into Users (name, password, parent) values (?,?, (select id from users where name = ?));";
        try (Connection connection = provider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1, user);
            preparedStatement.setString(2, password);
            preparedStatement.setString(3, parentName);
            preparedStatement.executeUpdate();
        }
    }
    private String getParentName(String username) throws SQLException {
        String sql = "SELECT p.name FROM Users u JOIN Users p ON u.parent = p.id WHERE u.name = ?";
        try (Connection conn = provider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("name") : null;
            }
        }
    }

    /**
     * Checks if potentialAncestor is an ancestor (parent, grandparent, etc.)
     * or the same user as descendant.
     */
    public boolean isAncestorOrSelf(String potentialAncestor, String descendant) throws SQLException {
        if (potentialAncestor == null || descendant == null) return false;
        if (potentialAncestor.equals(descendant)) return true;

        String current = descendant;
        while (current != null) {
            current = getParentName(current);
            if (potentialAncestor.equals(current)) return true;
        }
        return false;
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
    public Map<String, Object> getOwnerInfoBySpaceMarineId(long spaceMarineId) throws SQLException{
        String sql = "SELECT u.id,u.name FROM Space_marines s join Users u on s.owner = u.id WHERE s.id = ?";
        try (Connection connection = provider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, spaceMarineId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()){
                    Map<String, Object> mapa = new HashMap<>();
                    mapa.put("id", rs.getLong("id"));
                    mapa.put("name", rs.getString("name"));
                    return mapa;
                }
            }
        }
        return null;
    }
    public long getId(String userName) throws SQLException{
        String sql = "SELECT u.id FROM Users u  WHERE u.name = ?";
        try (Connection connection = provider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, userName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()){

                    return rs.getLong("id");
                }
            }
        }
        return 0;
    }
}
