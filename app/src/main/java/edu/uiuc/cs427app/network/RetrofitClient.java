package edu.uiuc.cs427app.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

/**
 * Retrofit client configuration.
 *
 * This class provides singleton instances of Retrofit configured
 * for different APIs (Weather API and Gemini API).
 *
 * Following Iteration 2 Class Diagram design.
 *
 * @author CS427 Team 407
 */
public class RetrofitClient {

    // Base URLs
    private static final String WEATHER_BASE_URL = "https://api.openweathermap.org/data/2.5/";
    private static final String GEOCODING_BASE_URL = "https://api.openweathermap.org/geo/1.0/";
    private static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/";

    // Singleton instances
    private static Retrofit weatherRetrofit;
    private static Retrofit geocodingRetrofit;
    private static Retrofit geminiRetrofit;

    /**
     * Get Retrofit instance for Weather API.
     *
     * @return Retrofit instance configured for OpenWeatherMap API
     */
    public static Retrofit getWeatherRetrofit() {
        if (weatherRetrofit == null) {
            weatherRetrofit = new Retrofit.Builder()
                    .baseUrl(WEATHER_BASE_URL)
                    .client(getOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return weatherRetrofit;
    }

    /**
     * Get Retrofit instance for Geocoding API (city search).
     *
     * @return Retrofit instance configured for OpenWeatherMap Geocoding API
     */
    public static Retrofit getGeocodingRetrofit() {
        if (geocodingRetrofit == null) {
            geocodingRetrofit = new Retrofit.Builder()
                    .baseUrl(GEOCODING_BASE_URL)
                    .client(getOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return geocodingRetrofit;
    }

    /**
     * Get Retrofit instance for Gemini API.
     *
     * @return Retrofit instance configured for Google Gemini API
     */
    public static Retrofit getGeminiRetrofit() {
        if (geminiRetrofit == null) {
            geminiRetrofit = new Retrofit.Builder()
                    .baseUrl(GEMINI_BASE_URL)
                    .client(getOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return geminiRetrofit;
    }

    /**
     * Create OkHttpClient with logging interceptor.
     *
     * Logs HTTP requests and responses for debugging.
     * TODO: Disable logging in production builds.
     *
     * @return Configured OkHttpClient
     */
    private static OkHttpClient getOkHttpClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Get WeatherApiClient instance for weather data.
     *
     * @return WeatherApiClient for making weather API calls
     */
    public static WeatherApiClient getWeatherApiClient() {
        return getWeatherRetrofit().create(WeatherApiClient.class);
    }

    /**
     * Get WeatherApiClient instance for geocoding (city search).
     * Uses the correct base URL for geocoding API.
     *
     * @return WeatherApiClient for making geocoding API calls
     */
    public static WeatherApiClient getGeocodingApiClient() {
        return getGeocodingRetrofit().create(WeatherApiClient.class);
    }

    /**
     * Get GeminiApiClient instance.
     *
     * @return GeminiApiClient for making LLM API calls
     */
    public static GeminiApiClient getGeminiApiClient() {
        return getGeminiRetrofit().create(GeminiApiClient.class);
    }
}
