package edu.uiuc.cs427app.helper;

import android.content.Context;

import edu.uiuc.cs427app.BuildConfig;
import edu.uiuc.cs427app.data.AppDatabase;
import edu.uiuc.cs427app.data.entity.WeatherData;
import edu.uiuc.cs427app.network.RetrofitClient;
import edu.uiuc.cs427app.network.WeatherApiClient;
import edu.uiuc.cs427app.network.model.WeatherResponse;

import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Helper class for weather-related operations.
 *
 * Encapsulates weather API calls and data caching logic.
 * Following Iteration 2 Class Diagram design.
 *
 * TODO: Team member assigned to Weather Integration:
 * - Implement fetchWeatherData() to call OpenWeatherMap API
 * - Implement cacheWeatherData() to store in database
 * - Handle API errors (invalid API key, rate limiting, network errors)
 * - Parse WeatherResponse and convert to WeatherData entity
 * - Add unit tests for this class
 *
 * @author CS427 Team 407
 */
public class WeatherHelper {

    private final WeatherApiClient weatherApiClient;
    private final AppDatabase database;

    /**
     * Constructor with dependency injection.
     *
     * @param context Application context
     */
    public WeatherHelper(Context context) {
        this.weatherApiClient = RetrofitClient.getWeatherApiClient();
        this.database = AppDatabase.getInstance(context);
    }

    /**
     * Fetch weather data from the OpenWeatherMap API.
     *
     * TODO: Implement this method
     * Steps:
     * 1. Get API key from BuildConfig.WEATHER_API_KEY
     * 2. Call weatherApiClient.getCurrentWeather(lat, lon, apiKey, "imperial")
     * 3. Parse WeatherResponse into WeatherData entity
     * 4. Call callback with result
     * 5. Handle errors appropriately
     *
     * Example usage:
     * fetchWeatherData(40.1164, -88.2434, new WeatherCallback() {
     *     public void onSuccess(WeatherData data) {
     *         // Display weather
     *     }
     *     public void onError(String error) {
     *         // Show error message
     *     }
     * });
     *
     * @param latitude  City latitude
     * @param longitude City longitude
     * @param callback  Callback to handle result
     */
    public void fetchWeatherData(double latitude, double longitude, WeatherCallback callback) {
        android.util.Log.d("WeatherHelper", "Fetching weather for coordinates: " + latitude + ", " + longitude);
        
        String apiKey = BuildConfig.WEATHER_API_KEY;
        
        if (apiKey == null || apiKey.isEmpty()) {
            android.util.Log.e("WeatherHelper", "Weather API key not configured");
            callback.onError("Weather API key not configured");
            return;
        }
        
        weatherApiClient.getCurrentWeather(latitude, longitude, apiKey, "imperial")
            .enqueue(new Callback<WeatherResponse>() {
                @Override
                public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                    android.util.Log.d("WeatherHelper", "API Response received. Success: " + response.isSuccessful());
                    
                    if (response.isSuccessful() && response.body() != null) {
                        WeatherResponse weatherResponse = response.body();
                        
                        // Convert WeatherResponse to WeatherData entity
                        WeatherData weatherData = new WeatherData();
                        weatherData.setTemperature(weatherResponse.getTemperature());
                        weatherData.setConditions(weatherResponse.getConditions());
                        weatherData.setHumidity(weatherResponse.getHumidity());
                        weatherData.setWindSpeed(weatherResponse.getWindSpeed());
                        weatherData.setTimestamp(new Date());
                        
                        android.util.Log.d("WeatherHelper", "Weather data parsed successfully: " + 
                            weatherData.getTemperature() + "°F, " + weatherData.getConditions());
                        
                        callback.onSuccess(weatherData);
                    } else {
                        String errorMsg = "Failed to fetch weather: HTTP " + response.code();
                        android.util.Log.e("WeatherHelper", errorMsg);
                        callback.onError(errorMsg);
                    }
                }
                
                @Override
                public void onFailure(Call<WeatherResponse> call, Throwable t) {
                    String errorMsg = "Network error: " + t.getMessage();
                    android.util.Log.e("WeatherHelper", errorMsg, t);
                    callback.onError(errorMsg);
                }
            });
    }

    /**
     * Cache weather data in the database.
     *
     * @param weatherData WeatherData to cache
     */
    public void cacheWeatherData(WeatherData weatherData) {
        if (database != null && weatherData != null) {
            database.weatherDao().insert(weatherData);
        }
    }

    /**
     * Get cached weather data from the database.
     *
     * @param cityId City ID
     * @return Cached WeatherData, or null if not found
     */
    public WeatherData getCachedData(Long cityId) {
        if (database != null) {
            return database.weatherDao().getWeatherByCityId(cityId);
        }
        return null;
    }

    /**
     * Check if cached weather data is stale.
     *
     * @param weatherData WeatherData to check
     * @return true if stale (> 30 minutes old), false otherwise
     */
    public boolean isDataStale(WeatherData weatherData) {
        return weatherData != null && weatherData.isStale();
    }

    /**
     * Callback interface for weather data fetching.
     *
     * This interface is used to notify when weather data is successfully
     * fetched or when an error occurs during fetching.
     */
    public interface WeatherCallback {
        /**
         * Called when weather data is successfully fetched.
         *
         * @param weatherData The fetched weather data for the city
         */
        void onSuccess(WeatherData weatherData);

        /**
         * Called when weather data fetching fails.
         *
         * @param errorMessage Error message describing the failure
         */
        void onError(String errorMessage);
    }
}
