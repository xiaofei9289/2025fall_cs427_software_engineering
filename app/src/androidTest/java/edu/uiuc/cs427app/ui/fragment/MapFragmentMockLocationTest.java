package edu.uiuc.cs427app.ui.fragment;

import android.os.Bundle;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import edu.uiuc.cs427app.R;

/**
 * Instrumented test for MapFragment that tests mocking location data.
 *
 * This test class validates that MapFragment correctly receives and uses mocked location data.
 *
 * @author CS427 Team 407
 */
@RunWith(AndroidJUnit4.class)
public class MapFragmentMockLocationTest {

    // Mocked location data for Chicago
    private static final String CHICAGO_NAME = "Chicago";
    private static final double CHICAGO_LATITUDE = 41.8781;
    private static final double CHICAGO_LONGITUDE = -87.6298;

    // Mocked location data for Champaign
    private static final String CHAMPAIGN_NAME = "Champaign";
    private static final double CHAMPAIGN_LATITUDE = 40.1164;
    private static final double CHAMPAIGN_LONGITUDE = -88.2434;

    private FragmentScenario<MapFragment> scenario;

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
    }

    /**
     * Tests that location parameters can be mocked.
     */
    @Test
    public void testMockLocationChicago_DisplaysCorrectCoordinates() {
        // ACTION: Launch fragment with mocked Chicago location data
        Bundle args = new Bundle();
        args.putDouble("latitude", CHICAGO_LATITUDE);
        args.putDouble("longitude", CHICAGO_LONGITUDE);
        args.putString("cityName", CHICAGO_NAME);

        scenario = FragmentScenario.launchInContainer(MapFragment.class, args);

        // ASSERTION: Verify mocked Chicago city name is displayed in UI
        Espresso.onView(withId(R.id.tvCityName))
                .check(ViewAssertions.matches(withText(CHICAGO_NAME)));

        // ASSERTION: Verify mocked Chicago coordinates are displayed in UI
        String expectedCoordinates = String.format("Lat: %.4f, Lng: %.4f",
                CHICAGO_LATITUDE, CHICAGO_LONGITUDE);
        Espresso.onView(withId(R.id.tvCoordinates))
                .check(ViewAssertions.matches(withText(expectedCoordinates)));
    }

    /**
     * Test that location mocking works for changing cities.
     */
    @Test
    public void testMockLocationChange_ChicagoToChampaign() {
        // ACTION 1: Launch fragment with mocked Chicago location
        Bundle chicagoArgs = new Bundle();
        chicagoArgs.putDouble("latitude", CHICAGO_LATITUDE);
        chicagoArgs.putDouble("longitude", CHICAGO_LONGITUDE);
        chicagoArgs.putString("cityName", CHICAGO_NAME);

        scenario = FragmentScenario.launchInContainer(MapFragment.class, chicagoArgs);

        // ASSERTION 1: Verify Chicago location is displayed
        Espresso.onView(withId(R.id.tvCityName))
                .check(ViewAssertions.matches(withText(CHICAGO_NAME)));

        String expectedChicagoCoords = String.format("Lat: %.4f, Lng: %.4f",
                CHICAGO_LATITUDE, CHICAGO_LONGITUDE);
        Espresso.onView(withId(R.id.tvCoordinates))
                .check(ViewAssertions.matches(withText(expectedChicagoCoords)));

        // ACTION 2: Close Chicago fragment and launch with mocked Champaign location
        scenario.close();

        Bundle champaignArgs = new Bundle();
        champaignArgs.putDouble("latitude", CHAMPAIGN_LATITUDE);
        champaignArgs.putDouble("longitude", CHAMPAIGN_LONGITUDE);
        champaignArgs.putString("cityName", CHAMPAIGN_NAME);

        scenario = FragmentScenario.launchInContainer(MapFragment.class, champaignArgs);

        // ASSERTION 2: Verify Champaign location is now displayed (location changed)
        Espresso.onView(withId(R.id.tvCityName))
                .check(ViewAssertions.matches(withText(CHAMPAIGN_NAME)));

        String expectedChampaignCoords = String.format("Lat: %.4f, Lng: %.4f",
                CHAMPAIGN_LATITUDE, CHAMPAIGN_LONGITUDE);
        Espresso.onView(withId(R.id.tvCoordinates))
                .check(ViewAssertions.matches(withText(expectedChampaignCoords)));
    }
}
