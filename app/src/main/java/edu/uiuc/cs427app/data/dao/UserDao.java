package edu.uiuc.cs427app.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import edu.uiuc.cs427app.data.entity.User;

/**
 * Data Access Object (DAO) for User entity.
 * Defines database operations for User table.
 *
 * Following Iteration 2 Class Diagram design.
 *
 * @author CS427 Team 407
 */
@Dao
public interface UserDao {

    /**
     * Insert a new user into the database.
     *
     * @param user User to insert
     * @return Row ID of the inserted user
     */
    @Insert
    long insert(User user);

    /**
     * Get a user by username.
     *
     * Used for login authentication.
     *
     * @param username Username to search for
     * @return User object if found, null otherwise
     */
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User getUserByUsername(String username);

    /**
     * Get a user by ID.
     *
     * @param userId User ID to search for
     * @return User object if found, null otherwise
     */
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    User getUserById(Long userId);

    /**
     * Update an existing user's information.
     *
     * Used for updating theme preferences or password.
     *
     * @param user User object with updated information
     */
    @Update
    void updateUser(User user);

    /**
     * Check if a username already exists in the database.
     *
     * Used for account creation validation.
     *
     * @param username Username to check
     * @return Count of users with this username (0 or 1)
     */
    @Query("SELECT COUNT(*) FROM users WHERE username = :username")
    int countByUsername(String username);

    /**
     * Delete all users (for testing purposes only).
     *
     * WARNING: This will cascade delete all related data (cities, weather).
     */
    @Query("DELETE FROM users")
    void deleteAll();
}
