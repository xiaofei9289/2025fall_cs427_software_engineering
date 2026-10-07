package edu.uiuc.cs427app.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.Date;

import edu.uiuc.cs427app.data.entity.WeatherData;

/**
 * Data Access Object (DAO) for WeatherData entity.
 * Defines database operations for WeatherData table.
 *
 * Following Iteration 2 Class Diagram design.
 *
 * @author CS427 Team 407
 */
@Dao
public interface WeatherDao {

    /**
     * Insert new weather data into the database.
     *
     * Uses REPLACE strategy to update existing weather data for a city.
     * This ensures we only keep the latest weather snapshot per city.
     *
     * @param weatherData WeatherData to insert
     * @return Row ID of the inserted weather data
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(WeatherData weatherData);

    /**
     * Get weather data for a specific city.
     *
     * Returns the most recent weather data for the city.
     *
     * @param cityId City ID to search for
     * @return WeatherData object if found, null otherwise
     */
    @Query("SELECT * FROM weather_data WHERE city_id = :cityId ORDER BY timestamp DESC LIMIT 1")
    WeatherData getWeatherByCityId(Long cityId);

    /**
     * Delete old weather data that is older than a specific date.
     *
     * Used for cleanup to prevent database bloat.
     * Typically called to remove data older than 24 hours.
     *
     * @param cutoffDate Date before which all data should be deleted
     */
    @Query("DELETE FROM weather_data WHERE timestamp < :cutoffDate")
    void deleteOldWeather(Date cutoffDate);

    /**
     * Delete weather data for a specific city.
     *
     * @param cityId City ID
     */
    @Query("DELETE FROM weather_data WHERE city_id = :cityId")
    void deleteWeatherByCityId(Long cityId);

    /**
     * Delete all weather data (for testing purposes only).
     */
    @Query("DELETE FROM weather_data")
    void deleteAll();

    /**
     * Count the total number of cached weather entries.
     *
     * Useful for monitoring cache size.
     *
     * @return Number of weather data entries in database
     */
    @Query("SELECT COUNT(*) FROM weather_data")
    int countAll();
}
