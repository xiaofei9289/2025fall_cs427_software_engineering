package edu.uiuc.cs427app.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import edu.uiuc.cs427app.data.entity.City;

/**
 * Data Access Object (DAO) for City entity.
 * Defines database operations for City table.
 *
 * Following Iteration 2 Class Diagram design.
 *
 * @author CS427 Team 407
 */
@Dao
public interface CityDao {

    /**
     * Insert a new city into the database.
     *
     * @param city City to insert
     * @return Row ID of the inserted city
     */
    @Insert
    long insert(City city);

    /**
     * Delete a city from the database.
     *
     * This will cascade delete associated WeatherData.
     *
     * @param city City to delete
     */
    @Delete
    void delete(City city);

    /**
     * Get all cities for a specific user.
     *
     * Returns cities ordered by when they were added (newest first).
     *
     * @param userId User ID to filter by
     * @return List of cities belonging to the user
     */
    @Query("SELECT * FROM cities WHERE user_id = :userId ORDER BY added_at DESC")
    List<City> getCitiesByUserId(Long userId);

    /**
     * Get a specific city by ID.
     *
     * @param cityId City ID to search for
     * @return City object if found, null otherwise
     */
    @Query("SELECT * FROM cities WHERE id = :cityId LIMIT 1")
    City getCityById(Long cityId);

    /**
     * Get a city by user ID and city name.
     *
     * Used to check if a user has already added a specific city.
     *
     * @param userId User ID
     * @param cityName City name to search for
     * @return City object if found, null otherwise
     */
    @Query("SELECT * FROM cities WHERE user_id = :userId AND name = :cityName LIMIT 1")
    City getCityByUserIdAndName(Long userId, String cityName);

    /**
     * Delete all cities for a specific user.
     *
     * Used for testing or account deletion.
     *
     * @param userId User ID
     */
    @Query("DELETE FROM cities WHERE user_id = :userId")
    void deleteAllByUserId(Long userId);

    /**
     * Delete all cities (for testing purposes only).
     */
    @Query("DELETE FROM cities")
    void deleteAll();
}
