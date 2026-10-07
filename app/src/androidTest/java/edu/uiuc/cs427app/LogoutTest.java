package edu.uiuc.cs427app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.uiuc.cs427app.data.AppDatabase;
import edu.uiuc.cs427app.data.entity.User;
import edu.uiuc.cs427app.repository.UserRepository;

/**
 * Test class for User Logout feature.
 * 
 * This class tests:
 * - Clicking logout button returns to login screen
 */
@RunWith(AndroidJUnit4.class)
public class LogoutTest {

    private Context appContext;
    private UserRepository userRepository;
    private Long testUserId;
    
    // Test user credentials
    private static final String TEST_USERNAME = "logout_test_user";
    private static final String TEST_PASSWORD = "password123";

    /**
     * Setup method runs before each test.
     * Creates a test user and logs them in.
     */
    @Before
    public void setUp() {
        // Get application context
        appContext = ApplicationProvider.getApplicationContext();
        
        // Create user repository
        userRepository = new UserRepository(appContext);

        // Clean up any existing test user
        cleanupTestUser();

        // Create a new test user
        User testUser = new User(TEST_USERNAME, TEST_PASSWORD, "light");
        testUserId = userRepository.createUser(testUser);
        
        // Log in the test user
        userRepository.authenticateUser(TEST_USERNAME, TEST_PASSWORD);
    }

    /**
     * Cleanup method runs after each test.
     * Removes test data.
     */
    @After
    public void cleanUp() {
        cleanupTestUser();
    }

    /**
     * Helper method to delete test user.
     */
    private void cleanupTestUser() {
        User existingUser = userRepository.getUserById(testUserId);
        if (existingUser != null && existingUser.getId() != null) {
            // Get database instance
            AppDatabase database = AppDatabase.getInstance(appContext);
            
            // Delete all cities for this user
            database.cityDao().deleteAllByUserId(existingUser.getId());
            
            // Delete the user
            database.userDao().deleteAll();
        }
    }

    /**
     * Test user logout functionality.
     * 
     * This test verifies:
     * 1. User can click logout button
     * 2. App returns to login screen
     * 3. Login screen elements are displayed
     */
    @Test
    public void testUserLogout() throws InterruptedException {
        // Launch main activity (user is already logged in)
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        
        // Wait for the app to load
        Thread.sleep(2000);

        // Check we're on the city list screen (logged in)
        onView(withId(R.id.btnAddLocation)).check(matches(isDisplayed()));
        Thread.sleep(1000);

        // Check that logout button is displayed
        onView(withId(R.id.btnLogout)).check(matches(isDisplayed()));
        Thread.sleep(1000);

        // Click the logout button
        onView(withId(R.id.btnLogout)).perform(click());

        // Wait for logout and navigation to complete
        Thread.sleep(2000);

        // Verify we're back at login screen - check username field
        onView(withId(R.id.etUsername)).check(matches(isDisplayed()));
        Thread.sleep(500);

        // Verify password field is displayed
        onView(withId(R.id.etPassword)).check(matches(isDisplayed()));
        Thread.sleep(500);

        // Verify login button is displayed
        onView(withId(R.id.btnLogin)).check(matches(isDisplayed()));
        Thread.sleep(500);
        
        // Close the activity
        scenario.close();
    }
}
