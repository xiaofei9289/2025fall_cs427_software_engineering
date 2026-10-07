package edu.uiuc.cs427app.helper;

import android.util.Log;

import androidx.annotation.NonNull;

import edu.uiuc.cs427app.BuildConfig;
import edu.uiuc.cs427app.network.GeminiApiClient;
import edu.uiuc.cs427app.network.model.GeminiImageRequest;
import edu.uiuc.cs427app.network.model.GeminiImageResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * CityImageGenerator - Helper class for generating AI-powered city landscape images.
 *
 * This class uses Google Gemini 2.0 Flash model to generate photorealistic
 * city landscape images based on city name, weather conditions, and time of day.
 *
 * Features:
 * - Constructs detailed prompts for image generation
 * - Handles API communication with Gemini
 * - Returns Base64-encoded images via callback
 * - Provides comprehensive error handling
 *
 * Usage:
 * <pre>
 * CityImageGenerator generator = new CityImageGenerator();
 * generator.generateCityImage("Chicago", "Sunny", "afternoon",
 *     new CityImageGenerator.ImageCallback() {
 *         public void onSuccess(String base64Image) {
 *             // Display image
 *         }
 *         public void onError(String errorMessage) {
 *             // Handle error
 *         }
 *     }
 * );
 * </pre>
 *
 * Requirements:
 * - GEMINI_API_KEY must be configured in local.properties
 * - Internet connection required
 * - Callback methods will be called on background thread
 *
 * API Documentation:
 * https://ai.google.dev/gemini-api/docs/image-generation
 *
 * @author CS427 Team 407 - LLM Image Generation Member
 */
public class CityImageGenerator {

    private static final String TAG = "CityImageGenerator";
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/";

    private final GeminiApiClient apiClient;
    private final String apiKey;

    /**
     * Callback interface for image generation results.
     */
    public interface ImageCallback {
        /**
         * Called when image generation succeeds.
         *
         * @param base64Image Base64-encoded image data (PNG format)
         */
        void onSuccess(String base64Image);

        /**
         * Called when image generation fails.
         *
         * @param errorMessage Human-readable error message
         */
        void onError(String errorMessage);
    }

    /**
     * Constructor - Initializes Retrofit and API client.
     */
    public CityImageGenerator() {
        // Get API key from BuildConfig
        this.apiKey = BuildConfig.GEMINI_API_KEY;

        // Initialize Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Create API client
        this.apiClient = retrofit.create(GeminiApiClient.class);

        Log.d(TAG, "CityImageGenerator initialized");
    }

    /**
     * Generate a city landscape image based on city name, weather, and time of day.
     *
     * This method:
     * 1. Validates input parameters
     * 2. Constructs a detailed image generation prompt
     * 3. Calls Gemini API for image generation
     * 4. Returns Base64-encoded image via callback
     *
     * The callback methods will be invoked on a background thread.
     * If you need to update UI, use runOnUiThread() or post to main handler.
     *
     * @param cityName   Name of the city (e.g., "Chicago", "New York")
     * @param weather    Weather condition (e.g., "Sunny", "Rainy", "Cloudy")
     * @param timeOfDay  Time of day (e.g., "morning", "afternoon", "evening", "night")
     * @param callback   Callback to receive result or error
     */
    public void generateCityImage(
            @NonNull String cityName,
            @NonNull String weather,
            @NonNull String timeOfDay,
            @NonNull ImageCallback callback) {

        Log.d(TAG, "Starting image generation for: " + cityName +
                " (" + weather + ", " + timeOfDay + ")");

        // Validate inputs
        if (cityName == null || cityName.trim().isEmpty()) {
            Log.e(TAG, "City name is empty");
            callback.onError("City name cannot be empty");
            return;
        }

        if (apiKey == null || apiKey.trim().isEmpty()) {
            Log.e(TAG, "API key not configured");
            callback.onError("API key not configured. Please add GEMINI_API_KEY to local.properties");
            return;
        }

        String prompt = buildPrompt(cityName, weather, timeOfDay);
        Log.d(TAG, "Generated prompt: " + prompt);

        GeminiImageRequest request = new GeminiImageRequest(prompt);

        apiClient.generateImage(request, apiKey).enqueue(new Callback<GeminiImageResponse>() {
            @Override
            public void onResponse(@NonNull Call<GeminiImageResponse> call,
                                   @NonNull Response<GeminiImageResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleSuccessResponse(response.body(), callback);
                } else {
                    handleErrorResponse(response, callback);
                }
            }

            @Override
            public void onFailure(@NonNull Call<GeminiImageResponse> call,
                                  @NonNull Throwable t) {
                handleFailure(t, callback);
            }
        });
    }

    /**
     * Build a detailed prompt for image generation.
     *
     * The prompt includes:
     * - City name and iconic features
     * - Weather conditions
     * - Time of day and lighting
     * - Art style (photorealistic)
     * - Image quality specifications
     *
     * @param cityName  Name of the city
     * @param weather   Weather condition
     * @param timeOfDay Time of day
     * @return Detailed image generation prompt
     */
    private String buildPrompt(String cityName, String weather, String timeOfDay) {
        return String.format(
                "A photorealistic landscape image of %s city skyline during %s, %s weather. " +
                        "Show iconic landmarks and buildings. High quality, detailed, " +
                        "professional photography style. 16:9 aspect ratio.",
                cityName,
                timeOfDay,
                weather.toLowerCase()
        );
    }

    /**
     * Handle successful API response.
     *
     * @param response Response from Gemini API
     * @param callback Callback to invoke
     */
    private void handleSuccessResponse(GeminiImageResponse response, ImageCallback callback) {
        try {
            // Extract image data
            String base64Image = response.getImageData();

            if (base64Image != null && !base64Image.isEmpty()) {
                Log.d(TAG, "Successfully generated image, size: " +
                        base64Image.length() + " characters");

                // Invoke success callback
                callback.onSuccess(base64Image);
            } else {
                Log.e(TAG, "Response contains no image data");
                callback.onError("API returned empty image data");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing response: " + e.getMessage(), e);
            callback.onError("Failed to parse image data: " + e.getMessage());
        }
    }

    /**
     * Handle HTTP error response.
     *
     * @param response Response with error
     * @param callback Callback to invoke
     */
    private void handleErrorResponse(Response<GeminiImageResponse> response,
                                      ImageCallback callback) {
        String errorMessage;
        String detailedError = "";
        int code = response.code();

        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                Log.e(TAG, "========== FULL API ERROR RESPONSE ==========");
                Log.e(TAG, "HTTP Status Code: " + code);
                Log.e(TAG, "Error Body: " + errorBody);
                Log.e(TAG, "=============================================");
                detailedError = errorBody;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to read error body", e);
        }

        switch (code) {
            case 400:
                errorMessage = "Invalid request: " + detailedError;
                break;
            case 401:
                errorMessage = "Authentication failed. Please check your API key.";
                break;
            case 403:
                errorMessage = "Access forbidden. API key may not have proper permissions.";
                break;
            case 404:
                errorMessage = "API endpoint not found. Model may not be available: " + detailedError;
                break;
            case 429:
                errorMessage = "Rate limit exceeded. Please try again later.";
                break;
            case 500:
            case 503:
                errorMessage = "Server error: " + detailedError;
                break;
            default:
                errorMessage = "Request failed with code " + code + ": " + detailedError;
        }

        Log.e(TAG, "API error: " + errorMessage + " (HTTP " + code + ")");

        callback.onError(errorMessage);
    }

    /**
     * Handle network failure.
     *
     * @param t        Throwable causing the failure
     * @param callback Callback to invoke
     */
    private void handleFailure(Throwable t, ImageCallback callback) {
        String errorMessage;

        if (t instanceof java.net.UnknownHostException) {
            errorMessage = "No internet connection. Please check your network.";
        } else if (t instanceof java.net.SocketTimeoutException) {
            errorMessage = "Request timed out. Please try again.";
        } else if (t instanceof javax.net.ssl.SSLException) {
            errorMessage = "SSL connection error. Please check your connection.";
        } else {
            errorMessage = "Network error: " + t.getMessage();
        }

        Log.e(TAG, "Network failure: " + errorMessage, t);
        callback.onError(errorMessage);
    }
}
