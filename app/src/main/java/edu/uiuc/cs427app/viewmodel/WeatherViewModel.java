package edu.uiuc.cs427app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import edu.uiuc.cs427app.data.entity.WeatherData;
import edu.uiuc.cs427app.helper.LLMHelper;
import edu.uiuc.cs427app.repository.WeatherRepository;

/**
 * ViewModel for Weather Details screen.
 *
 * Manages weather data fetching and LLM insights generation.
 * Following Iteration 2 Class Diagram design - MVVM pattern.
 *
 * TODO: Team member assigned to Weather Integration:
 * - Implement fetchWeather() with caching logic
 * - Implement fetchLLMInsights() to get AI insights
 * - Handle loading states (show loading indicator)
 * - Handle errors (network errors, API errors)
 * - Update LiveData when data is fetched
 * - Add unit tests for this ViewModel
 *
 * @author CS427 Team 407
 */
public class WeatherViewModel extends AndroidViewModel {

    private final WeatherRepository weatherRepository;
    private LLMHelper llmHelper;

    // LiveData for observing weather data
    private final MutableLiveData<WeatherData> weatherData;

    // LiveData for observing LLM insights
    private final MutableLiveData<String> llmInsights;

    // LiveData for observing loading state
    private final MutableLiveData<Boolean> isLoading;

    // LiveData for observing errors
    private final MutableLiveData<String> errorMessage;

    /**
     * Constructor with Application context.
     *
     * @param application Application context for repository initialization
     */
    public WeatherViewModel(@NonNull Application application) {
        super(application);
        this.weatherRepository = new WeatherRepository(application);
        // LLMHelper will be injected by MainActivity via setLLMHelper()
        this.llmHelper = new LLMHelper(); // Temporary instance until injection
        this.weatherData = new MutableLiveData<>();
        this.llmInsights = new MutableLiveData<>();
        this.isLoading = new MutableLiveData<>(false);
        this.errorMessage = new MutableLiveData<>();
    }
    
    /**
     * Set the LLMHelper instance (injected by MainActivity).
     * 
     * @param llmHelper LLMHelper instance
     */
    public void setLLMHelper(LLMHelper llmHelper) {
        this.llmHelper = llmHelper;
    }

    /**
     * Get LiveData for weather data.
     *
     * Fragment should observe this to update the UI.
     *
     * @return LiveData containing weather data
     */
    public LiveData<WeatherData> getWeatherData() {
        return weatherData;
    }

    /**
     * Get LiveData for LLM insights.
     *
     * Fragment should observe this to display AI-generated insights.
     *
     * @return LiveData containing LLM insights
     */
    public LiveData<String> getLlmInsights() {
        return llmInsights;
    }

    /**
     * Get LiveData for loading state.
     *
     * Fragment should observe this to show/hide loading indicator.
     *
     * @return LiveData containing loading state
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * Get LiveData for error messages.
     *
     * Fragment should observe this to display errors.
     *
     * @return LiveData containing error message
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Fetch weather data for a city.
     *
     * Uses smart caching: returns cached data if fresh, fetches from API if stale.
     *
     * TODO: Implement this method
     * Steps:
     * 1. Set isLoading to true
     * 2. Call weatherRepository.getWeather(cityId)
     * 3. Update weatherData LiveData with result
     * 4. Set isLoading to false
     * 5. Handle errors by updating errorMessage LiveData
     *
     * Note: This should run on a background thread if using synchronous repository methods.
     *
     * @param cityId City ID to fetch weather for
     */
    public void fetchWeather(Long cityId) {
        android.util.Log.d("WeatherViewModel", "Fetching weather for cityId: " + cityId);
        
        isLoading.postValue(true);
        errorMessage.postValue(null);
        
        // Run on background thread to avoid blocking UI
        new Thread(() -> {
            try {
                WeatherData data = weatherRepository.getWeather(cityId);
                
                if (data != null) {
                    android.util.Log.d("WeatherViewModel", "Weather data retrieved successfully");
                    weatherData.postValue(data);
                } else {
                    android.util.Log.e("WeatherViewModel", "Failed to fetch weather data");
                    errorMessage.postValue("Failed to fetch weather data. Please try again.");
                }
            } catch (Exception e) {
                android.util.Log.e("WeatherViewModel", "Error fetching weather", e);
                errorMessage.postValue("Error: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        }).start();
    }

    /**
     * Fetch AI-generated insights about the current weather.
     *
     * TODO: Implement this method
     * Steps:
     * 1. Check if weatherData is available
     * 2. Set isLoading to true
     * 3. Call llmHelper.generateInsights(weatherData, callback)
     * 4. Update llmInsights LiveData with result
     * 5. Set isLoading to false
     * 6. Handle errors appropriately
     *
     * Example implementation:
     * WeatherData currentWeather = weatherData.getValue();
     * if (currentWeather == null) {
     *     errorMessage.setValue("No weather data available");
     *     return;
     * }
     *
     * isLoading.setValue(true);
     * llmHelper.generateInsights(currentWeather, new LLMHelper.InsightsCallback() {
     *     public void onSuccess(String insights) {
     *         llmInsights.setValue(insights);
     *         isLoading.setValue(false);
     *     }
     *     public void onError(String error) {
     *         errorMessage.setValue(error);
     *         isLoading.setValue(false);
     *     }
     * });
     *
     * @param weatherData WeatherData to generate insights for
     */
    public void fetchLLMInsights(WeatherData weatherData) {
        // TODO: Implement LLM insights fetching
        // Check if weather data is available
        // Call LLM helper
        // Update llmInsights LiveData
    }

    /**
     * Get cached weather data without API call.
     *
     * Useful for displaying last known weather immediately.
     *
     * @param cityId City ID
     * @return Cached WeatherData, or null if not in cache
     */
    public WeatherData getCachedWeather(Long cityId) {
        return weatherRepository.getCachedWeather(cityId);
    }

    /**
     * Clear error message.
     *
     * Call this after displaying the error to the user.
     */
    public void clearError() {
        errorMessage.setValue(null);
    }
}
