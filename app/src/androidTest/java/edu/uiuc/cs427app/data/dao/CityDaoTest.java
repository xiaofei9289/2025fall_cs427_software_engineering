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
import java.util.List;

import edu.uiuc.cs427app.data.AppDatabase;
import edu.uiuc.cs427app.data.entity.City;
import edu.uiuc.cs427app.data.entity.User;

import static org.junit.Assert.*;

/**
 * Unit tests for CityDao.
 *
 * Tests CRUD operations on City entities using an in-memory Room database.
 * These tests verify that user-specific city lists are correctly managed.
 *
 * @author CS427 Team 407
 */
@RunWith(AndroidJUnit4.class)
public class CityDaoTest {

    private AppDatabase database;
    private CityDao cityDao;
    private UserDao userDao;
    private long testUserId;

    /**
     * Setup in-memory database and create a test user before each test.
     */
    @Before
    public void setup() {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        cityDao = database.cityDao();
        userDao = database.userDao();

        // Create a test user
        User testUser = new User("testuser", "password123", "");
        testUserId = userDao.insert(testUser);
    }

    /**
     * Close database after each test.
     */
    @After
    public void tearDown() {
        database.close();
    }

    /**
     * Test that inserting a city returns a valid ID.
     */
    @Test
    public void insertCity_ReturnsValidId() {
        City city = new City(testUserId, "Chicago", 41.8781, -87.6298);

        long cityId = cityDao.insert(city);

        assertTrue("City ID should be positive", cityId > 0);
    }

    /**
     * Test that inserting and retrieving a city by ID works correctly.
     */
    @Test
    public void insertAndGetCityById_Success() {
        City city = new City(testUserId, "New York", 40.7128, -74.0060);
        long cityId = cityDao.insert(city);

        City retrieved = cityDao.getCityById(cityId);

        assertNotNull("Retrieved city should not be null", retrieved);
        assertEquals("City IDs should match", cityId, (long) retrieved.getId());
        assertEquals("City names should match", "New York", retrieved.getName());
        assertEquals("Latitudes should match", 40.7128, retrieved.getLatitude(), 0.0001);
        assertEquals("Longitudes should match", -74.0060, retrieved.getLongitude(), 0.0001);
    }

    /**
     * Test that retrieving cities by user ID works correctly.
     */
    @Test
    public void getCitiesByUserId_Success() {
        City city1 = new City(testUserId, "Chicago", 41.8781, -87.6298);
        City city2 = new City(testUserId, "Seattle", 47.6062, -122.3321);
        cityDao.insert(city1);
        cityDao.insert(city2);

        List<City> cities = cityDao.getCitiesByUserId(testUserId);

        assertNotNull("City list should not be null", cities);
        assertEquals("Should have 2 cities", 2, cities.size());
    }

    /**
     * Test that deleting a city removes it from the database.
     */
    @Test
    public void deleteCity_Success() {
        City city = new City(testUserId, "Boston", 42.3601, -71.0589);
        long cityId = cityDao.insert(city);

        City retrieved = cityDao.getCityById(cityId);
        assertNotNull("City should exist before deletion", retrieved);

        cityDao.delete(retrieved);

        City afterDelete = cityDao.getCityById(cityId);
        assertNull("City should be null after deletion", afterDelete);
    }

    /**
     * Test that cities are user-specific.
     */
    @Test
    public void getCitiesByUserId_UserSpecific() {
        // Create another user
        User user2 = new User("user2", "password456", null);
        long user2Id = userDao.insert(user2);

        // Add cities for both users
        City city1 = new City(testUserId, "Chicago", 41.8781, -87.6298);
        City city2 = new City(user2Id, "Seattle", 47.6062, -122.3321);
        cityDao.insert(city1);
        cityDao.insert(city2);

        List<City> user1Cities = cityDao.getCitiesByUserId(testUserId);
        List<City> user2Cities = cityDao.getCitiesByUserId(user2Id);

        assertEquals("User 1 should have 1 city", 1, user1Cities.size());
        assertEquals("User 2 should have 1 city", 1, user2Cities.size());
        assertEquals("User 1's city should be Chicago", "Chicago", user1Cities.get(0).getName());
        assertEquals("User 2's city should be Seattle", "Seattle", user2Cities.get(0).getName());
    }

    /**
     * Test that getting cities for non-existent user returns empty list.
     */
    @Test
    public void getCitiesByUserId_NonExistentUser_ReturnsEmptyList() {
        List<City> cities = cityDao.getCitiesByUserId(999L);

        assertNotNull("City list should not be null", cities);
        assertTrue("City list should be empty", cities.isEmpty());
    }

    /**
     * Test that retrieving a non-existent city by ID returns null.
     */
    @Test
    public void getCityById_NonExistent_ReturnsNull() {
        City retrieved = cityDao.getCityById(999L);

        assertNull("Non-existent city should return null", retrieved);
    }

    /**
     * Test that multiple cities can be added for the same user.
     */
    @Test
    public void insertMultipleCitiesForSameUser_Success() {
        City city1 = new City(testUserId, "Chicago", 41.8781, -87.6298);
        City city2 = new City(testUserId, "New York", 40.7128, -74.0060);
        City city3 = new City(testUserId, "Los Angeles", 34.0522, -118.2437);

        cityDao.insert(city1);
        cityDao.insert(city2);
        cityDao.insert(city3);

        List<City> cities = cityDao.getCitiesByUserId(testUserId);

        assertEquals("User should have 3 cities", 3, cities.size());
    }

    /**
     * Test that deleting one city doesn't affect others.
     */
    @Test
    public void deleteOneCity_OthersRemain() {
        City city1 = new City(testUserId, "Chicago", 41.8781, -87.6298);
        City city2 = new City(testUserId, "Seattle", 47.6062, -122.3321);
        cityDao.insert(city1);
        long city2Id = cityDao.insert(city2);

        // Delete city2
        City toDelete = cityDao.getCityById(city2Id);
        cityDao.delete(toDelete);

        List<City> remainingCities = cityDao.getCitiesByUserId(testUserId);

        assertEquals("Should have 1 city remaining", 1, remainingCities.size());
        assertEquals("Remaining city should be Chicago", "Chicago", remainingCities.get(0).getName());
    }

    /**
     * Test that city coordinates are stored with correct precision.
     */
    @Test
    public void insertCity_CoordinatePrecision() {
        City city = new City(testUserId, "Test City", 40.123456, -74.987654);
        long cityId = cityDao.insert(city);

        City retrieved = cityDao.getCityById(cityId);

        assertNotNull("City should be retrieved", retrieved);
        assertEquals("Latitude should match", 40.123456, retrieved.getLatitude(), 0.000001);
        assertEquals("Longitude should match", -74.987654, retrieved.getLongitude(), 0.000001);
    }
}
