package dao;

import db.DBConnection;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * UserDAO.java
 *
 * Handles database operations for the 'users' table using PreparedStatements.
 * Ensures SQL injection prevention and secure password verification.
 */
public class UserDAO {

    /**
     * Authenticates a user by username and plain-text password.
     * The password is encrypted with SHA-256 before comparison.
     *
     * @param username The entered username
     * @param plainPassword The plain text password
     * @return User object if credentials match; null otherwise
     * @throws SQLException On database failure
     */
    public User login(String username, String plainPassword) throws SQLException {
        String sql = "SELECT id, username, password_hash, role FROM users WHERE username = ? AND password_hash = ?";
        String hash = DBConnection.hashPassword(plainPassword);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, hash);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("role")
                    );
                }
            }
        }
        return null; // Invalid credentials
    }

    /**
     * Checks if a given username already exists in the database.
     *
     * @param username Username to check
     * @return true if taken, false if available
     * @throws SQLException On database failure
     */
    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Registers a new user with role 'STUDENT' or 'ADMIN'.
     *
     * @param username Desired username
     * @param plainPassword Plain-text password to hash
     * @param role Either "STUDENT" or "ADMIN"
     * @return true if successfully inserted
     * @throws SQLException On database failure
     */
    public boolean register(String username, String plainPassword, String role) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?)";
        String hash = DBConnection.hashPassword(plainPassword);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, hash);
            stmt.setString(3, role.toUpperCase());

            return stmt.executeUpdate() > 0;
        }
    }
}
