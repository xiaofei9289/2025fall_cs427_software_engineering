package edu.uiuc.cs427app.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.mindrot.jbcrypt.BCrypt;

import java.util.Date;

/**
 * User entity for Room database.
 * Represents a user account in the weather app.
 *
 * Following Iteration 2 Class Diagram design.
 *
 * @author CS427 Team 407
 */
@Entity(tableName = "users",
        indices = {@Index(value = {"username"}, unique = true)})
public class User {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private Long id;

    @ColumnInfo(name = "username")
    private String username;

    @ColumnInfo(name = "password_hash")
    private String passwordHash;

    @ColumnInfo(name = "theme_preference")
    private String themePreference;

    @ColumnInfo(name = "created_at")
    private Date createdAt;

    // Constructors
    /**
     * Constructor.
     */
    public User() {
        this.createdAt = new Date();
        this.themePreference = "light";
    }

    /**
     * Constructor.
     */
    public User(String username, String password, String themeDescription) {
        this();
        this.username = normalizeUsername(username);
        this.passwordHash = hashPassword(password);
        this.themePreference = themeDescription;
    }

    // Getters and Setters
    /**
     * Get the Id.
     *
     * @return Id
     */
    public Long getId() {
        return id;
    }

    /**
     * Set the Id.
     *
     * @param id Id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get the Username.
     *
     * @return Username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set the Username.
     *
     * @param username Username
     */
    public void setUsername(String username) {
        this.username = normalizeUsername(username);
    }

    /**
     * Get the PasswordHash.
     *
     * @return PasswordHash
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Set the PasswordHash.
     *
     * @param passwordHash PasswordHash
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Get the ThemePreference.
     *
     * @return ThemePreference
     */
    public String getThemePreference() {
        return themePreference;
    }

    /**
     * Set the ThemePreference.
     *
     * @param themePreference ThemePreference
     */
    public void setThemePreference(String themePreference) {
        this.themePreference = themePreference;
    }

    /**
     * Get the CreatedAt.
     *
     * @return CreatedAt
     */
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * Set the CreatedAt.
     *
     * @param createdAt CreatedAt
     */
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    // Business Logic Methods

    /**
     * Normalize username to lowercase for case-insensitive comparison.
     *
     * @param username The username to normalize
     * @return Lowercase username, or null if input is null
     */
    private String normalizeUsername(String username) {
        return username != null ? username.toLowerCase().trim() : null;
    }

    /**
     * Hash a plain-text password using BCrypt.
     *
     * @param plainPassword The plain-text password
     * @return BCrypt hashed password
     */
    private String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    /**
     * Validate a plain-text password against the stored hash.
     *
     * @param plainPassword The password to validate
     * @return true if password matches, false otherwise
     */
    public boolean validatePassword(String plainPassword) {
        return BCrypt.checkpw(plainPassword, this.passwordHash);
    }

    /**
     * Set a new theme for the user.
     *
     * @param theme Theme name ("light", "dark", or "blue")
     */
    /**
     * Set the Theme.
     *
     * @param theme Theme
     */
    public void setTheme(String theme) {
        if (theme != null && (theme.equals("light") || theme.equals("dark") || theme.equals("blue"))) {
            this.themePreference = theme;
        }
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", themePreference='" + themePreference + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
