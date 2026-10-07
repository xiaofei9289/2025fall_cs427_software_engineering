package edu.uiuc.cs427app.network;

import edu.uiuc.cs427app.network.model.CitySearchResponse;
import edu.uiuc.cs427app.network.model.WeatherResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.List;

/**
 * Retrofit API client interface for Weather API.
 *
 * This interface defines the endpoints for fetching weather data.
 *
 * TODO: Team member assigned to Weather Integration:
 * - Sign up for OpenWeatherMap API at https://openweathermap.org/api
 * - Add your API key to local.properties: WEATHER_API_KEY=your_key_here
 * - Implement the actual API calls in WeatherHelper
 * - Handle API rate limiting (60 calls/minute for free tier)
 * - Handle network errors gracefully
 *
 * Base URL: https://api.openweathermap.org/data/2.5/
 *
 * Following Iteration 2 Class Diagram design.
 *
 * @author CS427 Team 407
 */
public interface WeatherApiClient {

    /**
     * Get current weather data for a specific location.
     *
     * Endpoint: GET /weather
     * Docs: https://openweathermap.org/current
     *
     * Example call:
     * getCurrentWeather(40.1164, -88.2434, "YOUR_API_KEY", "imperial")
     *
     * @param latitude  Latitude of the location
     * @param longitude Longitude of the location
     * @param apiKey    OpenWeatherMap API key
     * @param units     Units of measurement ("metric", "imperial", or "standard")
     * @return Call<WeatherResponse> containing weather data
     */
    @GET("weather")
    Call<WeatherResponse> getCurrentWeather(
            @Query("lat") double latitude,
            @Query("lon") double longitude,
            @Query("appid") String apiKey,
            @Query("units") String units  // "metric" for Celsius, "imperial" for Fahrenheit
    );

    /**
     * Search for a city by name to get its coordinates.
     *
     * Endpoint: GET /direct
     * Base URL for this endpoint: https://api.openweathermap.org/geo/1.0/
     * Full URL: https://api.openweathermap.org/geo/1.0/direct
     * Docs: https://openweathermap.org/api/geocoding-api
     *
     * Example call:
     * searchCity("Chicago", 1, "YOUR_API_KEY")
     *
     * @param cityName City name to search for (e.g., "Chicago" or "Chicago,IL,US")
     * @param limit    Number of results to return (typically 1)
     * @param apiKey   OpenWeatherMap API key
     * @return Call<List<CitySearchResponse>> containing city information with coordinates
     */
    @GET("direct")
    Call<List<CitySearchResponse>> searchCity(
            @Query("q") String cityName,
            @Query("limit") int limit,
            @Query("appid") String apiKey
    );
}
