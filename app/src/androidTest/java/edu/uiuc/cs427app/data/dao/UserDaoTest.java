package edu.uiuc.cs427app.data.dao;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import edu.uiuc.cs427app.data.AppDatabase;
import edu.uiuc.cs427app.data.entity.User;

import static org.junit.Assert.*;

/**
 * Unit tests for UserDao.
 *
 * Tests CRUD operations on User entities using an in-memory Room database.
 * These tests verify that user authentication data is correctly stored and retrieved.
 *
 * @author CS427 Team 407
 */
@RunWith(AndroidJUnit4.class)
public class UserDaoTest {

    private AppDatabase database;
    private UserDao userDao;

    /**
     * Setup in-memory database before each test.
     * Uses allowMainThreadQueries() for testing convenience.
     */
    @Before
    public void setup() {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        userDao = database.userDao();
    }

    /**
     * Close database after each test to prevent memory leaks.
     */
    @After
    public void tearDown() {
        database.close();
    }

    /**
     * Test that inserting a user returns a valid ID.
     */
    @Test
    public void insertUser_ReturnsValidId() {
        User user = new User("testuser", "password123", null);

        long userId = userDao.insert(user);

        assertTrue("User ID should be positive", userId > 0);
    }

    /**
     * Test that inserting and retrieving a user by username works correctly.
     */
    @Test
    public void insertAndGetUserByUsername_Success() {
        User user = new User("testuser", "password123", null);
        user.setTheme("dark");
        userDao.insert(user);

        User retrieved = userDao.getUserByUsername("testuser");

        assertNotNull("Retrieved user should not be null", retrieved);
        assertEquals("Usernames should match", "testuser", retrieved.getUsername());
        assertTrue("Password should validate", retrieved.validatePassword("password123"));
        assertEquals("Theme preferences should match", "dark", retrieved.getThemePreference());
    }

    /**
     * Test that retrieving a user by ID works correctly.
     */
    @Test
    public void insertAndGetUserById_Success() {
        User user = new User("johndoe", "password456", null);
        user.setTheme("blue");
        long userId = userDao.insert(user);

        User retrieved = userDao.getUserById(userId);

        assertNotNull("Retrieved user should not be null", retrieved);
        assertEquals("User IDs should match", userId, (long) retrieved.getId());
        assertEquals("Usernames should match", "johndoe", retrieved.getUsername());
    }

    /**
     * Test that retrieving a non-existent user by username returns null.
     */
    @Test
    public void getUserByUsername_NonExistent_ReturnsNull() {
        User retrieved = userDao.getUserByUsername("nonexistent");

        assertNull("Non-existent user should return null", retrieved);
    }

    /**
     * Test that retrieving a non-existent user by ID returns null.
     */
    @Test
    public void getUserById_NonExistent_ReturnsNull() {
        User retrieved = userDao.getUserById(999L);

        assertNull("Non-existent user ID should return null", retrieved);
    }

    /**
     * Test that inserting multiple users with different usernames works correctly.
     */
    @Test
    public void insertMultipleUsers_Success() {
        User user1 = new User("alice", "pass1", "light");
        User user2 = new User("bob", "pass2", null);
        user2.setTheme("dark");

        long id1 = userDao.insert(user1);
        long id2 = userDao.insert(user2);

        assertTrue("First user ID should be positive", id1 > 0);
        assertTrue("Second user ID should be positive", id2 > 0);
        assertNotEquals("User IDs should be different", id1, id2);

        User retrieved1 = userDao.getUserByUsername("alice");
        User retrieved2 = userDao.getUserByUsername("bob");

        assertNotNull("First user should be retrieved", retrieved1);
        assertNotNull("Second user should be retrieved", retrieved2);
        assertEquals("Alice's theme should match", "light", retrieved1.getThemePreference());
        assertEquals("Bob's theme should match", "dark", retrieved2.getThemePreference());
    }

    /**
     * Test that inserting a user with null theme preference works.
     */
    @Test
    public void insertUser_NullThemePreference_Success() {
        User user = new User("testuser", "password789", null);
        user.setThemePreference(null);

        long userId = userDao.insert(user);
        User retrieved = userDao.getUserById(userId);

        assertNotNull("User should be inserted", retrieved);
        assertNull("Theme preference should be null", retrieved.getThemePreference());
    }

    /**
     * Test that updating a user's information works correctly.
     */
    @Test
    public void updateUser_Success() {
        User user = new User("testuser", "oldPassword", null);
        long userId = userDao.insert(user);

        User retrieved = userDao.getUserById(userId);
        String newHash = org.mindrot.jbcrypt.BCrypt.hashpw("newPassword", org.mindrot.jbcrypt.BCrypt.gensalt());
        retrieved.setPasswordHash(newHash);
        retrieved.setTheme("dark");
        userDao.updateUser(retrieved);

        User updated = userDao.getUserById(userId);

        assertNotNull("Updated user should exist", updated);
        assertTrue("New password should validate", updated.validatePassword("newPassword"));
        assertEquals("Theme should be updated", "dark", updated.getThemePreference());
    }

    /**
     * Test that usernames are normalized to lowercase when stored.
     * The User entity handles normalization, not the DAO.
     */
    @Test
    public void getUserByUsername_CaseSensitive() {
        // User entity normalizes username to lowercase on construction
        User user = new User("TestUser", "password", null);
        userDao.insert(user);

        // Username is stored as "testuser" (normalized by User entity)
        User retrieved = userDao.getUserByUsername("testuser");
        assertNotNull("Normalized username should match", retrieved);
        assertEquals("Username should be normalized to lowercase", "testuser", retrieved.getUsername());

        // DAO performs exact match - won't normalize the query parameter
        // So querying with "TestUser" won't match "testuser" in database
        User retrievedMixedCase = userDao.getUserByUsername("TestUser");
        assertNull("Mixed case query should not match (DAO doesn't normalize query)", retrievedMixedCase);
    }
}
