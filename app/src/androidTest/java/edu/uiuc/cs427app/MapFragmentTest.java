package edu.uiuc.cs427app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

import android.os.Bundle;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.uiuc.cs427app.ui.fragment.MapFragment;

/**
 * Instrumented test class for MapFragment.
 * 
 * This test class uses Espresso UI testing framework to verify the functionality
 * of the MapFragment, including UI element display, map view rendering,
 * and text content validation with different city coordinates.
 * 
 * Tests are executed on an Android device or emulator.
 */
@RunWith(AndroidJUnit4.class)
public class MapFragmentTest {

    /**
     * Test case to verify that all UI elements in MapFragment are displayed correctly.
     * 
     * This test creates a MapFragment with test city data (Urbana) and verifies that all major
     * UI components (city name, coordinates, map view, info card) are visible on screen.
     * Each verification is followed by a 1-second delay to ensure UI stability.
     * 
     * @throws InterruptedException if the thread sleep is interrupted during test execution
     */
    @Test
    public void testMapFragmentDisplay() throws InterruptedException {
        // Create a Bundle with test city parameters (Urbana coordinates)
        // Bundle is used to pass data to the fragment during initialization
        Bundle args = new Bundle();
        args.putDouble("latitude", 40.1106);
        args.putDouble("longitude", -88.2073);
        args.putString("cityName", "Urbana");

        // Launch the MapFragment in a test container with the arguments
        FragmentScenario<MapFragment> scenario = FragmentScenario.launchInContainer(
            MapFragment.class,
            args
        );

        // Wait for the fragment to fully initialize and render
        Thread.sleep(2000);

        // Verify that the city name text view is displayed on screen
        onView(withId(R.id.tvCityName)).check(matches(isDisplayed()));
        Thread.sleep(1000);

        // Verify that the coordinates text view is displayed on screen
        onView(withId(R.id.tvCoordinates)).check(matches(isDisplayed()));
        Thread.sleep(1000);

        // Verify that the map view is displayed on screen
        onView(withId(R.id.mapView)).check(matches(isDisplayed()));
        Thread.sleep(1000);

        // Verify that the info card is displayed on screen
        onView(withId(R.id.infoCard)).check(matches(isDisplayed()));
        Thread.sleep(1000);
        
        // Close the scenario to clean up resources
        scenario.close();
    }

    /**
     * Test case to verify that text content in MapFragment displays correct values.
     * 
     * This test creates a MapFragment with test city data (Urbana) and validates that
     * the city name and coordinates are displayed correctly. Uses containsString matcher
     * for coordinates to accommodate formatted text.
     * 
     * @throws InterruptedException if the thread sleep is interrupted during test execution
     */
    @Test
    public void testMapFragmentTextContent() throws InterruptedException {
        // Create a Bundle with test city parameters (Urbana coordinates)
        // Bundle is used to pass data to the fragment during initialization
        Bundle args = new Bundle();
        args.putDouble("latitude", 40.1106);
        args.putDouble("longitude", -88.2073);
        args.putString("cityName", "Urbana");

        // Launch the MapFragment in a test container with the arguments
        FragmentScenario<MapFragment> scenario = FragmentScenario.launchInContainer(
            MapFragment.class,
            args
        );

        // Wait for the fragment to fully initialize and render
        Thread.sleep(2000);

        // Verify that the city name text view displays "Urbana"
        onView(withId(R.id.tvCityName)).check(matches(withText("Urbana")));
        Thread.sleep(1000);

        // Verify that the coordinates text contains the latitude value "40.1106"
        onView(withId(R.id.tvCoordinates)).check(matches(withText(containsString("40.1106"))));
        Thread.sleep(1000);

        // Verify that the coordinates text contains the longitude value "-88.2073"
        onView(withId(R.id.tvCoordinates)).check(matches(withText(containsString("-88.2073"))));
        Thread.sleep(1000);
        
        // Close the scenario to clean up resources
        scenario.close();
    }

    /**
     * Test case to verify that MapFragment works correctly with different city data.
     * 
     * This test creates a MapFragment with different city data (Chicago) to ensure the fragment
     * can handle various city coordinates and display the correct information. Validates that
     * city name and coordinates are displayed properly for different locations.
     * 
     * @throws InterruptedException if the thread sleep is interrupted during test execution
     */
    @Test
    public void testMapFragmentWithDifferentCity() throws InterruptedException {
        // Create a Bundle with test city parameters (Chicago coordinates)
        // This tests that the fragment works with different city data
        Bundle args = new Bundle();
        args.putDouble("latitude", 41.8781);
        args.putDouble("longitude", -87.6298);
        args.putString("cityName", "Chicago");

        // Launch the MapFragment in a test container with the arguments
        FragmentScenario<MapFragment> scenario = FragmentScenario.launchInContainer(
            MapFragment.class,
            args
        );

        // Wait for the fragment to fully initialize and render
        Thread.sleep(2000);

        // Verify that the city name text view displays "Chicago"
        onView(withId(R.id.tvCityName)).check(matches(withText("Chicago")));
        Thread.sleep(1000);

        // Verify that the coordinates text view is displayed on screen
        onView(withId(R.id.tvCoordinates)).check(matches(isDisplayed()));
        Thread.sleep(1000);
        
        // Close the scenario to clean up resources
        scenario.close();
    }

}
