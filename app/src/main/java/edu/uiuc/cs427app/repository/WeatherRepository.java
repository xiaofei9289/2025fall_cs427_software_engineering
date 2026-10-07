package edu.uiuc.cs427app.repository;

import android.content.Context;

import edu.uiuc.cs427app.data.AppDatabase;
import edu.uiuc.cs427app.data.dao.WeatherDao;
import edu.uiuc.cs427app.data.entity.WeatherData;
import edu.uiuc.cs427app.helper.WeatherHelper;

/**
 * Repository for Weather-related data operations.
 *
 * Mediates between ViewModels and data sources (WeatherDao, WeatherHelper).
 * Implements caching strategy: check cache first, fetch from API if stale.
 *
 * Following Iteration 2 Class Diagram design.
 *
 * TODO: Team member assigned to Weather Integration:
 * - Implement getWeather() with cache-first strategy
 * - Implement refreshWeather() to force API fetch
 * - Handle cache expiry (30 minutes)
 * - Handle network errors gracefully
 * - Update UI with appropriate error messages
 *
 * @author CS427 Team 407
 */
public class WeatherRepository {

    private final WeatherHelper weatherHelper;
    private final WeatherDao weatherDao;
    private final edu.uiuc.cs427app.data.dao.CityDao cityDao;

    /**
     * Constructor with dependency injection.
     *
     * @param context Application context for database and API access
     */
    public WeatherRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.weatherDao = database.weatherDao();
        this.cityDao = database.cityDao();
        this.weatherHelper = new WeatherHelper(context);
    }

    /**
     * Get weather data for a city.
     *
     * Smart fetch strategy:
     * 1. Check if cached data exists
     * 2. If cache is fresh (< 30 min old), return cached data
     * 3. If cache is stale or missing, fetch from API
     * 4. Cache the new data
     * 5. Return weather data
     *
     * TODO: Implement this method
     * Use weatherDao.getWeatherByCityId() to check cache.
     * Use weatherHelper.fetchWeatherData() to get fresh data.
     * Use weatherData.isStale() to check if cache is expired.
     *
     * @param cityId City ID
     * @return WeatherData object, or null if failed
     */
    public WeatherData getWeather(Long cityId) {
        android.util.Log.d("WeatherRepository", "Getting weather for cityId: " + cityId);
        
        // Check cache first
        WeatherData cachedData = weatherDao.getWeatherByCityId(cityId);
        
        if (cachedData != null && !cachedData.isStale()) {
            cachedData.setCityId(cityId);
            android.util.Log.d("WeatherRepository", "Returning cached weather data (fresh)");
            return cachedData;
        }
        
        android.util.Log.d("WeatherRepository", "Cache stale or missing, fetching from API");
        // Cache is stale or missing, fetch from API
        return refreshWeather(cityId);
    }

    /**
     * Force refresh weather data from API (bypass cache).
     *
     * TODO: Implement this method
     * Steps:
     * 1. Get city coordinates from CityRepository
     * 2. Fetch weather from API using WeatherHelper
     * 3. Cache the new data
     * 4. Return weather data
     *
     * @param cityId City ID
     * @return Fresh WeatherData from API, or null if failed
     */
    public WeatherData refreshWeather(Long cityId) {
        android.util.Log.d("WeatherRepository", "Refreshing weather from API for cityId: " + cityId);
        
        // Get city coordinates
        edu.uiuc.cs427app.data.entity.City city = cityDao.getCityById(cityId);
        if (city == null) {
            android.util.Log.e("WeatherRepository", "City not found for cityId: " + cityId);
            return null;
        }
        
        android.util.Log.d("WeatherRepository", "City found: " + city.getName() + 
            " at (" + city.getLatitude() + ", " + city.getLongitude() + ")");
        
        // Fetch from API (synchronous for simplicity - using CountDownLatch)
        final WeatherData[] result = new WeatherData[1];
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        
        weatherHelper.fetchWeatherData(city.getLatitude(), city.getLongitude(), 
            new WeatherHelper.WeatherCallback() {
                @Override
                public void onSuccess(WeatherData data) {
                    android.util.Log.d("WeatherRepository", "Weather data fetched successfully");
                    data.setCityId(cityId);
                    weatherDao.insert(data);
                    result[0] = data;
                    latch.countDown();
                }
                
                @Override
                public void onError(String error) {
                    android.util.Log.e("WeatherRepository", "Failed to fetch weather: " + error);
                    latch.countDown();
                }
            });
        
        try {
            // Wait for API call to complete (max 10 seconds)
            latch.await(10, java.util.concurrent.TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            android.util.Log.e("WeatherRepository", "Interrupted while waiting for weather data", e);
            return null;
        }
        
        return result[0];
    }

    /**
     * Get cached weather data without API call.
     *
     * @param cityId City ID
     * @return Cached WeatherData, or null if not in cache
     */
    public WeatherData getCachedWeather(Long cityId) {
        return weatherDao.getWeatherByCityId(cityId);
    }

    /**
     * Cleanup old weather data (older than 24 hours).
     *
     * TODO: Implement periodic cleanup
     * Call this method periodically to prevent database bloat.
     */
    public void cleanupOldWeather() {
        // TODO: Implement cleanup
        // long oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
        // weatherDao.deleteOldWeather(new Date(oneDayAgo));
    }
}
