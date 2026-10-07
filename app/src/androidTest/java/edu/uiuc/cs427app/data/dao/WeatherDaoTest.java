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
import edu.uiuc.cs427app.data.entity.City;
import edu.uiuc.cs427app.data.entity.User;
import edu.uiuc.cs427app.data.entity.WeatherData;

import static org.junit.Assert.*;

/**
 * Unit tests for WeatherDao.
 *
 * Tests CRUD operations on WeatherData entities using an in-memory Room database.
 * These tests verify that weather caching (with 30-minute expiry) works correctly.
 *
 * @author CS427 Team 407
 */
@RunWith(AndroidJUnit4.class)
public class WeatherDaoTest {

    private AppDatabase database;
    private WeatherDao weatherDao;
    private CityDao cityDao;
    private UserDao userDao;
    private long testCityId;

    /**
     * Setup in-memory database and create test user and city before each test.
     */
    @Before
    public void setup() {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        weatherDao = database.weatherDao();
        cityDao = database.cityDao();
        userDao = database.userDao();

        // Create test user and city
        User testUser = new User("testuser", "password123", "");
        long userId = userDao.insert(testUser);
        City testCity = new City(userId, "Chicago", 41.8781, -87.6298);
        testCityId = cityDao.insert(testCity);
    }

    /**
     * Close database after each test.
     */
    @After
    public void tearDown() {
        database.close();
    }

    /**
     * Test that inserting weather data returns a valid ID.
     */
    @Test
    public void insertWeatherData_ReturnsValidId() {
        WeatherData weather = new WeatherData(testCityId, 72.5, "Sunny", 65, 10.5);

        long weatherId = weatherDao.insert(weather);

        assertTrue("Weather ID should be positive", weatherId > 0);
    }

    /**
     * Test that inserting and retrieving weather data by city ID works correctly.
     */
    @Test
    public void insertAndGetWeatherByCityId_Success() {
        WeatherData weather = new WeatherData(testCityId, 68.3, "Cloudy", 70, 15.2);
        weatherDao.insert(weather);

        WeatherData retrieved = weatherDao.getWeatherByCityId(testCityId);

        assertNotNull("Retrieved weather should not be null", retrieved);
        assertEquals("City IDs should match", testCityId, (long) retrieved.getCityId());
        assertEquals("Temperatures should match", 68.3, retrieved.getTemperature(), 0.01);
        assertEquals("Conditions should match", "Cloudy", retrieved.getConditions());
        assertEquals("Humidity should match", Integer.valueOf(70), retrieved.getHumidity());
        assertEquals("Wind speeds should match", 15.2, retrieved.getWindSpeed(), 0.01);
    }

    /**
     * Test that inserting weather data with REPLACE strategy overwrites old data.
     */
    @Test
    public void insertWeatherData_ReplaceOnConflict_Success() {
        // Insert initial weather data
        WeatherData weather1 = new WeatherData(testCityId, 70.0, "Sunny", 60, 5.0);
        long id1 = weatherDao.insert(weather1);

        // Insert new weather data for same city (should replace)
        WeatherData weather2 = new WeatherData(testCityId, 75.0, "Rainy", 80, 20.0);
        long id2 = weatherDao.insert(weather2);

        WeatherData retrieved = weatherDao.getWeatherByCityId(testCityId);

        assertNotNull("Weather data should exist", retrieved);
        assertEquals("Should have latest temperature", 75.0, retrieved.getTemperature(), 0.01);
        assertEquals("Should have latest conditions", "Rainy", retrieved.getConditions());
        assertEquals("Should have latest humidity", Integer.valueOf(80), retrieved.getHumidity());
    }

    /**
     * Test that getting weather for non-existent city returns null.
     */
    @Test
    public void getWeatherByCityId_NonExistent_ReturnsNull() {
        WeatherData retrieved = weatherDao.getWeatherByCityId(999L);

        assertNull("Non-existent city weather should return null", retrieved);
    }

    /**
     * Test that isStale() method correctly identifies old weather data.
     */
    @Test
    public void isStale_OldData_ReturnsTrue() {
        // Create weather data from 31 minutes ago
        Date oldDate = new Date(System.currentTimeMillis() - (31 * 60 * 1000));
        WeatherData weather = new WeatherData(testCityId, 70.0, "Sunny", 60, 5.0);
        weather.setTimestamp(oldDate);
        weatherDao.insert(weather);

        WeatherData retrieved = weatherDao.getWeatherByCityId(testCityId);

        assertNotNull("Weather data should exist", retrieved);
        assertTrue("Weather data should be stale", retrieved.isStale());
    }

    /**
     * Test that isStale() method correctly identifies fresh weather data.
     */
    @Test
    public void isStale_FreshData_ReturnsFalse() {
        // Create weather data from now
        WeatherData weather = new WeatherData(testCityId, 70.0, "Sunny", 60, 5.0);
        weatherDao.insert(weather);

        WeatherData retrieved = weatherDao.getWeatherByCityId(testCityId);

        assertNotNull("Weather data should exist", retrieved);
        assertFalse("Weather data should not be stale", retrieved.isStale());
    }

    /**
     * Test that deleteOldWeather removes weather data older than cutoff date.
     */
    @Test
    public void deleteOldWeather_RemovesOldData() {
        // Create old weather data (2 hours ago)
        Date oldDate = new Date(System.currentTimeMillis() - (2 * 60 * 60 * 1000));
        WeatherData oldWeather = new WeatherData(testCityId, 70.0, "Sunny", 60, 5.0);
        oldWeather.setTimestamp(oldDate);
        weatherDao.insert(oldWeather);

        // Create another city with fresh weather
        City city2 = new City(testCityId, "Seattle", 47.6062, -122.3321);
        long city2Id = cityDao.insert(city2);
        WeatherData freshWeather = new WeatherData(city2Id, 65.0, "Rainy", 80, 15.0);
        weatherDao.insert(freshWeather);

        // Delete weather older than 1 hour ago
        Date cutoffDate = new Date(System.currentTimeMillis() - (1 * 60 * 60 * 1000));
        weatherDao.deleteOldWeather(cutoffDate);

        // Old weather should be deleted
        WeatherData retrievedOld = weatherDao.getWeatherByCityId(testCityId);
        assertNull("Old weather should be deleted", retrievedOld);

        // Fresh weather should still exist
        WeatherData retrievedFresh = weatherDao.getWeatherByCityId(city2Id);
        assertNotNull("Fresh weather should still exist", retrievedFresh);
    }

    /**
     * Test that weather data stores negative temperatures correctly.
     */
    @Test
    public void insertWeatherData_NegativeTemperature_Success() {
        WeatherData weather = new WeatherData(testCityId, -10.5, "Snowy", 90, 25.0);
        weatherDao.insert(weather);

        WeatherData retrieved = weatherDao.getWeatherByCityId(testCityId);

        assertNotNull("Weather data should exist", retrieved);
        assertEquals("Should store negative temperature", -10.5, retrieved.getTemperature(), 0.01);
    }

    /**
     * Test that weather data stores zero values correctly.
     */
    @Test
    public void insertWeatherData_ZeroValues_Success() {
        WeatherData weather = new WeatherData(testCityId, 0.0, "Calm", 0, 0.0);
        weatherDao.insert(weather);

        WeatherData retrieved = weatherDao.getWeatherByCityId(testCityId);

        assertNotNull("Weather data should exist", retrieved);
        assertEquals("Temperature should be zero", 0.0, retrieved.getTemperature(), 0.01);
        assertEquals("Humidity should be zero", Integer.valueOf(0), retrieved.getHumidity());
        assertEquals("Wind speed should be zero", 0.0, retrieved.getWindSpeed(), 0.01);
    }

    /**
     * Test that multiple weather updates for same city keep only the latest.
     */
    @Test
    public void multipleWeatherUpdates_KeepsLatest() {
        // Insert 3 weather updates
        WeatherData weather1 = new WeatherData(testCityId, 70.0, "Sunny", 60, 5.0);
        WeatherData weather2 = new WeatherData(testCityId, 72.0, "Partly Cloudy", 65, 8.0);
        WeatherData weather3 = new WeatherData(testCityId, 68.0, "Cloudy", 70, 12.0);

        weatherDao.insert(weather1);
        weatherDao.insert(weather2);
        weatherDao.insert(weather3);

        WeatherData retrieved = weatherDao.getWeatherByCityId(testCityId);

        assertNotNull("Weather data should exist", retrieved);
        assertEquals("Should have latest temperature", 68.0, retrieved.getTemperature(), 0.01);
        assertEquals("Should have latest conditions", "Cloudy", retrieved.getConditions());
    }

    /**
     * Test that weather data precision is maintained for decimal values.
     */
    @Test
    public void insertWeatherData_DecimalPrecision() {
        WeatherData weather = new WeatherData(testCityId, 72.567, "Sunny", 64, 10.234);
        weatherDao.insert(weather);

        WeatherData retrieved = weatherDao.getWeatherByCityId(testCityId);

        assertNotNull("Weather data should exist", retrieved);
        assertEquals("Temperature precision should be maintained", 72.567, retrieved.getTemperature(), 0.001);
        assertEquals("Wind speed precision should be maintained", 10.234, retrieved.getWindSpeed(), 0.001);
    }
}
