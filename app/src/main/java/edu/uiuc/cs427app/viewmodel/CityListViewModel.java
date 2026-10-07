package edu.uiuc.cs427app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import edu.uiuc.cs427app.data.entity.City;
import edu.uiuc.cs427app.repository.CityRepository;

import java.util.List;

/**
 * ViewModel for City List screen.
 *
 * Manages the user's list of saved cities.
 * Following Iteration 2 Class Diagram design - MVVM pattern.
 *
 * IMPORTANT: This ViewModel is SHARED across multiple fragments
 * (CityListFragment and DetailsFragment) by scoping it to MainActivity.
 *
 * TODO: Team member assigned to City Management:
 * - Implement loadCities() to fetch user's cities from repository
 * - Implement addCity() with validation and coordinate fetching
 * - Implement removeCity() with confirmation
 * - Handle duplicate city prevention
 * - Update LiveData when cities change
 * - Add unit tests for this ViewModel
 *
 * @author CS427 Team 407
 */
public class CityListViewModel extends AndroidViewModel {

    private final CityRepository cityRepository;

    // LiveData for observing the list of cities
    private final MutableLiveData<List<City>> citiesLiveData;

    // LiveData for observing add/remove operations
    private final MutableLiveData<OperationResult> operationResult;

    // Current user ID
    private Long userId;

    /**
     * Constructor with Application context.
     *
     * @param application Application context for repository initialization
     */
    public CityListViewModel(@NonNull Application application) {
        super(application);
        this.cityRepository = new CityRepository(application);
        this.citiesLiveData = new MutableLiveData<>();
        this.operationResult = new MutableLiveData<>();
    }

    /**
     * Get LiveData for cities list.
     *
     * Fragment should observe this to update the RecyclerView.
     *
     * @return LiveData containing list of cities
     */
    public LiveData<List<City>> getCitiesLiveData() {
        return citiesLiveData;
    }

    /**
     * Get LiveData for operation results (add/remove).
     *
     * Fragment should observe this to show success/error messages.
     *
     * @return LiveData containing operation result
     */
    public LiveData<OperationResult> getOperationResult() {
        return operationResult;
    }

    /**
     * Set the current user ID.
     *
     * Should be called after successful login.
     *
     * @param userId User ID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
        loadCities(); // Auto-load cities when user is set
    }

    /**
     * Load all cities for the current user.
     *
     * Steps:
     * 1. Check if userId is set
     * 2. Call cityRepository.getCitiesForUser(userId)
     * 3. Update citiesLiveData with the result
     *
     * This should be called after login and after adding/removing cities.
     */
    public void loadCities() {
        if (userId != null) {
            List<City> cities = cityRepository.getCitiesForUser(userId);
            citiesLiveData.postValue(cities);
        }
    }

    /**
     * Add a new city to the user's list.
     *
     * Steps:
     * 1. Validate city name (not empty)
     * 2. Fetch city coordinates using CityRepository.fetchCityCoordinates()
     * 3. Create City object
     * 4. Call cityRepository.addCity()
     * 5. Reload cities if successful
     * 6. Update operationResult LiveData
     *
     * @param cityName Name of the city to add
     */
    public void addCity(String cityName) {
        // Validate input
        if (cityName == null || cityName.trim().isEmpty()) {
            operationResult.postValue(new OperationResult(false, "City name cannot be empty"));
            return;
        }
        
        if (userId == null) {
            operationResult.postValue(new OperationResult(false, "User not logged in"));
            return;
        }
        
        // Fetch coordinates from API
        cityRepository.fetchCityCoordinates(cityName.trim(), new CityRepository.CoordinatesCallback() {
            @Override
            public void onSuccess(double lat, double lon) {
                City city = new City(userId, cityName.trim(), lat, lon);
                long result = cityRepository.addCity(city);
                if (result > 0) {
                    loadCities();
                    operationResult.postValue(new OperationResult(true, "City added successfully"));
                } else {
                    operationResult.postValue(new OperationResult(false, "City already exists"));
                }
            }
            
            @Override
            public void onError(String error) {
                operationResult.postValue(new OperationResult(false, error));
            }
        });
    }

    /**
     * Remove a city from the user's list.
     *
     * Steps:
     * 1. Call cityRepository.removeCity()
     * 2. Reload cities
     * 3. Update operationResult LiveData
     *
     * @param cityId ID of the city to remove
     */
    public void removeCity(Long cityId) {
        if (cityId == null) {
            operationResult.postValue(new OperationResult(false, "Invalid city ID"));
            return;
        }
        
        cityRepository.removeCity(cityId);
        loadCities();
        operationResult.postValue(new OperationResult(true, "City removed"));
    }

    /**
     * Get cities for the current user (for direct access).
     *
     * @return LiveData containing list of cities
     */
    public LiveData<List<City>> getCitiesForUser() {
        return citiesLiveData;
    }

    /**
     * Result class for add/remove operations.
     */
    public static class OperationResult {
        private final boolean success;
        private final String message;

        /**
         * Constructor for operation result.
         *
         * @param success true if operation succeeded, false otherwise
         * @param message Success or error message
         */
        public OperationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        /**
         * Check if operation was successful.
         *
         * @return true if operation succeeded, false otherwise
         */
        public boolean isSuccess() {
            return success;
        }

        /**
         * Get the result message.
         *
         * @return Success or error message
         */
        public String getMessage() {
            return message;
        }
    }
}
