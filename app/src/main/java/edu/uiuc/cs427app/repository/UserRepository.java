package edu.uiuc.cs427app.repository;

import android.content.Context;
import android.content.SharedPreferences;
import edu.uiuc.cs427app.data.AppDatabase;
import edu.uiuc.cs427app.data.dao.UserDao;
import edu.uiuc.cs427app.data.entity.User;

/**
 * Repository for User-related data operations.
 *
 * Mediates between ViewModels and data sources (UserDao, SharedPreferences).
 * Following Iteration 2 Class Diagram design.
 *
 * TODO: Team member assigned to Authentication:
 * - Implement authenticateUser() method with password validation
 * - Implement createUser() with duplicate username check
 * - Implement session management with SharedPreferences
 * - Add logout() method to clear session
 * - Handle edge cases (null inputs, database errors)
 *
 * @author CS427 Team 407
 */
public class UserRepository {

    private final UserDao userDao;
    private final SharedPreferences sharedPreferences;

    private static final String PREF_NAME = "weather_app_prefs";
    private static final String KEY_CURRENT_USER_ID = "current_user_id";

    /**
     * Constructor with dependency injection.
     *
     * @param context Application context for database and SharedPreferences
     */
    public UserRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.userDao = database.userDao();
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Authenticate a user with username and password.
     *
     * Steps:
     * 1. Get user by username from database
     * 2. If user not found, return null
     * 3. Validate password using user.validatePassword(plainPassword)
     * 4. If password matches, save user session and return user
     * 5. If password doesn't match, return null
     *
     * @param username Username to authenticate
     * @param password Plain-text password
     * @return User object if authentication successful, null otherwise
     */
    public User authenticateUser(String username, String password) {
        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            return null;
        }
        
        String normalizedUsername = normalizeUsername(username);
        User user = userDao.getUserByUsername(normalizedUsername);
        if (user != null && user.validatePassword(password)) {
            saveCurrentUser(user);
            return user;
        }
        return null;
    }

    /**
     * Create a new user account.
     *
     * Steps:
     * 1. Check if username is available using isUsernameAvailable()
     * 2. If username taken, return -1
     * 3. Insert user into database
     * 4. Return user ID
     *
     * @param user User object to create
     * @return User ID if successful, -1 if username already exists
     */
    public long createUser(User user) {
        if (user == null || user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return -1;
        }
        
        String normalizedUsername = normalizeUsername(user.getUsername());
        
        if (!isUsernameAvailable(normalizedUsername)) {
            return -1;
        }
        
        user.setUsername(normalizedUsername);
        return userDao.insert(user);
    }

    /**
     * Get user by ID.
     *
     * @param userId User ID
     * @return User object if found, null otherwise
     */
    public User getUserById(Long userId) {
        return userDao.getUserById(userId);
    }

    /**
     * Check if a username is available for registration.
     *
     * @param username Username to check
     * @return true if available, false if taken
     */
    public boolean isUsernameAvailable(String username) {
        String normalizedUsername = normalizeUsername(username);
        return userDao.countByUsername(normalizedUsername) == 0;
    }

    /**
     * Save the current user's ID to SharedPreferences (for session management).
     *
     * Store user ID in SharedPreferences so they stay logged in.
     *
     * @param user User to save as current user
     */
    public void saveCurrentUser(User user) {
        if (user != null && user.getId() != null) {
            sharedPreferences.edit()
                .putLong(KEY_CURRENT_USER_ID, user.getId())
                .apply();
        }
    }

    /**
     * Get the currently logged-in user.
     *
     * Retrieve user ID from SharedPreferences and get user from database.
     *
     * @return Current user if logged in, null otherwise
     */
    public User getCurrentUser() {
        long userId = sharedPreferences.getLong(KEY_CURRENT_USER_ID, -1);
        if (userId != -1) {
            return getUserById(userId);
        }
        return null;
    }

    /**
     * Log out the current user.
     *
     * Clear user session from SharedPreferences.
     */
    public void logout() {
        sharedPreferences.edit()
            .remove(KEY_CURRENT_USER_ID)
            .apply();
    }

    /**
     * Update user information (e.g., theme preference).
     *
     * @param user User object with updated information
     */
    public void updateUser(User user) {
        userDao.updateUser(user);
    }

    /**
     * Normalize username to lowercase for case-insensitive comparison.
     *
     * @param username The username to normalize
     * @return Lowercase username, or null if input is null
     */
    private String normalizeUsername(String username) {
        return username != null ? username.toLowerCase().trim() : null;
    }
}
