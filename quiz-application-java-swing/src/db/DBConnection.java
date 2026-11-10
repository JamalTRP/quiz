package db;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DBConnection.java
 *
 * Centralized singleton utility for connecting to MySQL via JDBC.
 * Configured with default connection parameters (modifiable via db.properties or constants).
 * Includes SHA-256 password hashing helper used throughout the application.
 *
 * Note: Never store plain-text passwords in databases. We use standard SHA-256 hashing.
 */
public class DBConnection {

    // Database connection credentials
    private static final String URL = "jdbc:mysql://localhost:3306/quiz_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "admin123"; // Local MySQL root password (reset 2026-11-10)

    // Ensure the MySQL 8 Connector/J driver is loaded once
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("CRITICAL: MySQL JDBC Driver not found! Ensure mysql-connector-j-8.x.jar is added to the classpath.");
            e.printStackTrace();
        }
    }

    // Private constructor to prevent direct instantiation
    private DBConnection() {}

    /**
     * Obtains a new connection to the MySQL database.
     * Always use with try-with-resources in DAO classes!
     *
     * @return active java.sql.Connection
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Utility method to compute SHA-256 hash of a plain text string.
     * Converts raw bytes to a 64-character lowercase hexadecimal string.
     *
     * @param input Plain text password
     * @return 64-character hex hash string
     */
    public static String hashPassword(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Fatal Error: SHA-256 algorithm not available in this JVM environment.", e);
        }
    }
}
