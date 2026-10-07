package edu.uiuc.cs427app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.uiuc.cs427app.data.AppDatabase;
import edu.uiuc.cs427app.data.entity.City;
import edu.uiuc.cs427app.data.entity.User;
import edu.uiuc.cs427app.repository.CityRepository;
import edu.uiuc.cs427app.repository.UserRepository;

/**
 * Test class for City Management features.
 * 
 * This class tests:
 * - Adding a new city to the user's list
 * - Removing an existing city from the list
 */
@RunWith(AndroidJUnit4.class)
public class CityManagementTest {

    private Context appContext;
    private UserRepository userRepository;
    private CityRepository cityRepository;
    private Long testUserId;
    
    // Test user credentials
    private static final String TEST_USERNAME = "citymgmt_test_user";
    private static final String TEST_PASSWORD = "password123";

    /**
     * Setup method runs before each test.
     * Creates a test user and logs them in.
     */
    @Before
    public void setUp() {
        // Get application context
        appContext = ApplicationProvider.getApplicationContext();
        
        // Create repositories
        userRepository = new UserRepository(appContext);
        cityRepository = new CityRepository(appContext);

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
     * Removes test data and logs out.
     */
    @After
    public void cleanUp() {
        // Log out
        userRepository.logout();
        
        // Clean up test user
        cleanupTestUser();
    }

    /**
     * Helper method to delete test user and their cities.
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
     * Test adding a new city.
     * 
     * This test verifies:
     * 1. "ADD A LOCATION" button is visible
     * 2. City appears in the RecyclerView after being added
     */
    @Test
    public void testAddNewCity() throws InterruptedException {
        // Add a test city to database
        City newCity = new City(testUserId, "Chicago", 41.8781, -87.6298);
        cityRepository.addCity(newCity);

        // Launch the main activity
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        
        // Wait for the app to load
        Thread.sleep(2500);

        // Check that "ADD A LOCATION" button is displayed
        onView(withId(R.id.btnAddLocation)).check(matches(isDisplayed()));
        Thread.sleep(1000);

        // Verify button text is correct
        onView(withId(R.id.btnAddLocation)).check(matches(withText("ADD A LOCATION")));
        Thread.sleep(1000);

        // Check that city list (RecyclerView) is displayed
        onView(withId(R.id.rvCities)).check(matches(isDisplayed()));
        Thread.sleep(1000);

        // Verify the city appears in the list
        onView(withText("Chicago")).check(matches(isDisplayed()));
        Thread.sleep(1000);
        
        // Close the activity
        scenario.close();
    }

    /**
     * Test removing an existing city.
     * 
     * This test verifies:
     * 1. City is displayed in the list
     * 2. Delete button works
     * 3. Confirmation dialog appears
     * 4. City is removed after confirmation
     */
    @Test
    public void testRemoveExistingCity() throws InterruptedException {
        // Add a test city that we will delete
        City cityToDelete = new City(testUserId, "New York", 40.7128, -74.0060);
        cityRepository.addCity(cityToDelete);

        // Launch the main activity
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        
        // Wait for the app to load
        Thread.sleep(2500);

        // Check that the city is displayed
        onView(withText("New York")).check(matches(isDisplayed()));
        Thread.sleep(1000);

        // Verify city list is showing (not empty)
        onView(withId(R.id.rvCities)).check(matches(isDisplayed()));
        Thread.sleep(1000);

        // Click the delete button for this city
        onView(allOf(withId(R.id.btnDelete), hasSibling(withText("New York"))))
                .perform(click());
        Thread.sleep(1500);

        // Check that confirmation dialog appears
        onView(withText("Delete City")).check(matches(isDisplayed()));
        Thread.sleep(1000);

        // Verify dialog message contains city name
        onView(withText("Are you sure you want to remove New York?"))
                .check(matches(isDisplayed()));
        Thread.sleep(1000);

        // Click "Delete" to confirm
        onView(withText("Delete")).perform(click());
        
        // Wait for deletion to complete
        Thread.sleep(8000);

        // Check that empty state message is now shown
        onView(withId(R.id.tvEmptyState)).check(matches(isDisplayed()));
        Thread.sleep(1000);

        // Verify city list is now hidden
        onView(withId(R.id.rvCities))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        Thread.sleep(1000);
        
        // Close the activity
        scenario.close();
    }
}
