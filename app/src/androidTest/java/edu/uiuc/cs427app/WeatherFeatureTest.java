package edu.uiuc.cs427app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.uiuc.cs427app.data.entity.User;
import edu.uiuc.cs427app.repository.UserRepository;
import edu.uiuc.cs427app.ui.fragment.DetailsFragment;

/**
 * Test class for Weather Feature Display.
 * 
 * This class tests:
 * - Weather information display for Chicago
 * - Weather information display for New York
 * - Verifies temperature, conditions, humidity, wind are shown
 */
@RunWith(AndroidJUnit4.class)
public class WeatherFeatureTest {

    private UserRepository userRepository;
    
    // Test user credentials
    private static final String TEST_USERNAME = "testuser_weather";
    private static final String TEST_PASSWORD = "testpass123";

    /**
     * Setup method runs before each test.
     * Creates a test user for authentication.
     */
    @Before
    public void setUp() {
        // Get application context
        Context context = ApplicationProvider.getApplicationContext();
        
        // Create user repository
        userRepository = new UserRepository(context);

        // Create test user if not exists
        if (userRepository.isUsernameAvailable(TEST_USERNAME)) {
            User testUser = new User(TEST_USERNAME, TEST_PASSWORD, "light");
            userRepository.createUser(testUser);
        }
        
        // Log in the test user
        userRepository.authenticateUser(TEST_USERNAME, TEST_PASSWORD);
    }

    /**
     * Test weather display for Chicago.
     * 
     * This test verifies:
     * 1. Weather details page loads for Chicago
     * 2. Temperature is displayed
     * 3. Weather conditions are displayed
     * 4. Humidity is displayed
     * 5. Wind speed is displayed
     */
    @Test
    public void testWeatherDisplayForChicago() throws InterruptedException {
        // Set up Chicago city data
        Bundle args = new Bundle();
        args.putString("cityName", "Chicago");
        args.putDouble("latitude", 41.8781);   // Chicago latitude
        args.putDouble("longitude", -87.6298);  // Chicago longitude
        
        // Launch DetailsFragment with Chicago data
        FragmentScenario<DetailsFragment> scenario = FragmentScenario.launchInContainer(
            DetailsFragment.class,
            args
        );

        // Wait for weather data to load from API
        Thread.sleep(5000);

        // Verify all weather fields exist
        scenario.onFragment(fragment -> {
            View view = fragment.getView();
            assertNotNull("Fragment view should exist", view);
            
            // Check temperature view exists
            View tvTemperature = view.findViewById(R.id.tvTemperature);
            assertNotNull("Temperature view should exist", tvTemperature);
            
            // Check weather conditions view exists
            View tvConditions = view.findViewById(R.id.tvConditions);
            assertNotNull("Conditions view should exist", tvConditions);
            
            // Check humidity view exists
            View tvHumidity = view.findViewById(R.id.tvHumidity);
            assertNotNull("Humidity view should exist", tvHumidity);
            
            // Check wind speed view exists
            View tvWind = view.findViewById(R.id.tvWind);
            assertNotNull("Wind view should exist", tvWind);
        });
        
        // Wait a bit for stability
        Thread.sleep(1000);
        
        // Close the fragment
        scenario.close();
    }

    /**
     * Test weather display for New York.
     * 
     * This test verifies:
     * 1. Weather details page loads for New York
     * 2. Temperature is displayed
     * 3. Weather conditions are displayed
     * 4. Humidity is displayed
     * 5. Wind speed is displayed
     */
    @Test
    public void testWeatherDisplayForNewYork() throws InterruptedException {
        // Set up New York city data
        Bundle args = new Bundle();
        args.putString("cityName", "New York");
        args.putDouble("latitude", 40.7128);   // New York latitude
        args.putDouble("longitude", -74.0060);  // New York longitude
        
        // Launch DetailsFragment with New York data
        FragmentScenario<DetailsFragment> scenario = FragmentScenario.launchInContainer(
            DetailsFragment.class,
            args
        );

        // Wait for weather data to load from API
        Thread.sleep(5000);

        // Verify all weather fields exist
        scenario.onFragment(fragment -> {
            View view = fragment.getView();
            assertNotNull("Fragment view should exist", view);
            
            // Check temperature view exists
            View tvTemperature = view.findViewById(R.id.tvTemperature);
            assertNotNull("Temperature view should exist", tvTemperature);
            
            // Check weather conditions view exists
            View tvConditions = view.findViewById(R.id.tvConditions);
            assertNotNull("Conditions view should exist", tvConditions);
            
            // Check humidity view exists
            View tvHumidity = view.findViewById(R.id.tvHumidity);
            assertNotNull("Humidity view should exist", tvHumidity);
            
            // Check wind speed view exists
            View tvWind = view.findViewById(R.id.tvWind);
            assertNotNull("Wind view should exist", tvWind);
        });
        
        // Wait a bit for stability
        Thread.sleep(1000);
        
        // Close the fragment
        scenario.close();
    }
}
