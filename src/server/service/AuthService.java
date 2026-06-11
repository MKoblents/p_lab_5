package server.service;

import server.db.dao.UserDAO;
import shared.dto.UserInfo;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Optional;

public class AuthService {
    private final UserDAO userDAO;
    public  AuthService(UserDAO userDAO){
        this.userDAO = userDAO;
    }
    public void register(String user, String password, String parentName) throws SQLException {
        String hashPassword =  Base64.getEncoder().encodeToString(hashPassword(password));
        try {
            userDAO.register(user, hashPassword, parentName);
        } catch (SQLException e) {
            // SQLState 23505 = unique_violation в PostgreSQL
            if ("23505".equals(e.getSQLState())) {
                throw new SQLException("Registration failed: username '" + user + "' is already taken. Please choose another.");
            }
            throw e;
        }

    }
    public Optional<String> validate(UserInfo userInfo) throws SQLException {
        String user = userInfo.name();
        String password = userInfo.password();
        String storedPassword = userDAO.getHashPassword(user);
        if (storedPassword == null){
            return Optional.empty();
        }
        byte[] inputPassword = hashPassword(password);
        byte[] storedPasswordBytes = Base64.getDecoder().decode(storedPassword);
        if (MessageDigest.isEqual(inputPassword, storedPasswordBytes)){
            return Optional.of(user);
        }return Optional.empty();
    }
    private byte[] hashPassword(String password){
        try{
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-384");
            byte[] digest = messageDigest.digest(password.getBytes(StandardCharsets.UTF_8));
            return digest;
        }catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-384 algorithm is not available", e);
        }
    }
}
