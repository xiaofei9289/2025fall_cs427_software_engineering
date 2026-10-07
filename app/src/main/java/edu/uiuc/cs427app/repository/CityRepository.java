package edu.uiuc.cs427app.repository;

import android.content.Context;

import edu.uiuc.cs427app.BuildConfig;
import edu.uiuc.cs427app.data.AppDatabase;
import edu.uiuc.cs427app.data.dao.CityDao;
import edu.uiuc.cs427app.data.entity.City;
import edu.uiuc.cs427app.network.RetrofitClient;
import edu.uiuc.cs427app.network.WeatherApiClient;
import edu.uiuc.cs427app.network.model.CitySearchResponse;

import android.util.Pair;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository for City-related data operations.
 *
 * Mediates between ViewModels and data sources (CityDao, WeatherApiClient).
 * Following Iteration 2 Class Diagram design.
 *
 * TODO: Team member assigned to City Management:
 * - Implement addCity() with city validation via API
 * - Implement removeCity() with confirmation
 * - Handle duplicate city prevention
 * - Handle network errors gracefully
 * - Update UI with appropriate error messages
 *
 * @author CS427 Team 407
 */
public class CityRepository {

    private final CityDao cityDao;
    private final WeatherApiClient geocodingApiClient;

    /**
     * Constructor with dependency injection.
     *
     * @param context Application context for database access
     */
    public CityRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.cityDao = database.cityDao();
        // Use geocoding API client for city search
        this.geocodingApiClient = RetrofitClient.getGeocodingApiClient();
    }

    /**
     * Get all cities for a specific user.
     *
     * @param userId User ID
     * @return List of cities belonging to the user
     */
    public List<City> getCitiesForUser(Long userId) {
        return cityDao.getCitiesByUserId(userId);
    }

    /**
     * Add a new city to the user's list.
     *
     * TODO: Implement this method
     * Steps:
     * 1. Validate city name using validateCity()
     * 2. Fetch coordinates using fetchCityCoordinates()
     * 3. Check if user already has this city
     * 4. Insert city into database
     * 5. Return city ID
     *
     * @param city City object to add
     * @return City ID if successful, -1 if failed
     */
    public long addCity(City city) {
        // Check if city already exists for this user
        City existing = cityDao.getCityByUserIdAndName(city.getUserId(), city.getName());
        if (existing != null) {
            return -1; // City already added
        }
        return cityDao.insert(city);
    }

    /**
     * Remove a city from the user's list.
     *
     * This will cascade delete associated weather data.
     *
     * @param cityId City ID to remove
     */
    public void removeCity(Long cityId) {
        City city = cityDao.getCityById(cityId);
        if (city != null) {
            cityDao.delete(city);
        }
    }

    /**
     * Validate a city name by checking if it exists via the Weather API.
     *
     * TODO: Implement this method
     * Call the geocoding API to verify the city exists and get coordinates.
     *
     * @param cityName City name to validate
     * @return true if city exists, false otherwise
     */
    public boolean validateCity(String cityName) {
        // TODO: Implement city validation
        // This should be done asynchronously with a callback
        // Use weatherApiClient.searchCity(cityName, 1, BuildConfig.WEATHER_API_KEY)
        return false;
    }

    /**
     * Fetch city coordinates from the Weather API.
     *
     * Steps:
     * 1. Call weatherApiClient.searchCity()
     * 2. Parse response to get latitude and longitude
     * 3. Return as Pair
     * 4. Handle errors (city not found, network error)
     *
     * @param cityName City name to search
     * @param callback Callback to handle result
     */
    public void fetchCityCoordinates(String cityName, CoordinatesCallback callback) {
        android.util.Log.d("CityRepository", "Searching for city: " + cityName + " with API key: " + BuildConfig.WEATHER_API_KEY);
        geocodingApiClient.searchCity(cityName, 1, BuildConfig.WEATHER_API_KEY)
            .enqueue(new Callback<List<CitySearchResponse>>() {
                @Override
                public void onResponse(Call<List<CitySearchResponse>> call, Response<List<CitySearchResponse>> response) {
                    android.util.Log.d("CityRepository", "Response received. Success: " + response.isSuccessful() + ", Body: " + response.body());
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        CitySearchResponse result = response.body().get(0);
                        android.util.Log.d("CityRepository", "City found: " + result.getName() + " at (" + result.getLatitude() + ", " + result.getLongitude() + ")");
                        callback.onSuccess(result.getLatitude(), result.getLongitude());
                    } else {
                        android.util.Log.w("CityRepository", "City not found or empty response");
                        callback.onError("City not found: " + cityName);
                    }
                }

                @Override
                public void onFailure(Call<List<CitySearchResponse>> call, Throwable t) {
                    android.util.Log.e("CityRepository", "Network error: " + t.getMessage(), t);
                    callback.onError("Network error: " + t.getMessage());
                }
            });
    }

    /**
     * Callback interface for coordinate fetching.
     */
    public interface CoordinatesCallback {
        /**
         * Called when city coordinates are successfully fetched.
         *
         * @param latitude City latitude
         * @param longitude City longitude
         */
        void onSuccess(double latitude, double longitude);

        /**
         * Called when coordinate fetching fails.
         *
         * @param errorMessage Error message describing the failure
         */
        void onError(String errorMessage);
    }
}
