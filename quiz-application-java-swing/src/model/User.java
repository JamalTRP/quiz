package model;

/**
 * User.java
 *
 * Represents a registered system user (Admin or Student).
 * Plain Old Java Object (POJO) representing the 'users' table.
 */
public class User {
    private int id;
    private String username;
    private String passwordHash;
    private String role; // "ADMIN" or "STUDENT"

    public User() {}

    public User(int id, String username, String passwordHash, String role) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public User(String username, String passwordHash, String role) {
        this(-1, username, passwordHash, role);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    @Override
    public String toString() {
        return username + " (" + role + ")";
    }
}
