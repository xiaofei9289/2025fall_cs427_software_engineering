package edu.uiuc.cs427app.ui.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.uiuc.cs427app.MainActivity;
import edu.uiuc.cs427app.R;
import edu.uiuc.cs427app.data.entity.City;
import edu.uiuc.cs427app.data.entity.Theme;
import edu.uiuc.cs427app.data.entity.WeatherData;
import edu.uiuc.cs427app.helper.CityImageGenerator;
import edu.uiuc.cs427app.repository.CityRepository;
import edu.uiuc.cs427app.repository.UserRepository;
import edu.uiuc.cs427app.viewmodel.ThemeViewModel;
import edu.uiuc.cs427app.viewmodel.WeatherViewModel;

/**
 * DetailsFragment - Shows detailed information about a selected city.
 *
 * This fragment displays city name, weather information, LLM-generated city image,
 * and provides buttons to show the city on a map and view weather insights.
 *
 * Requirements (Iteration 4):
 * - Display city name
 * - Show real-time weather information from API with caching
 * - Display LLM-generated city image matching current weather
 * - "Show Map" button to navigate to map
 * - "Weather Insights" button to navigate to LLM insights
 *
 * @author CS427 Team 407
 */
public class DetailsFragment extends Fragment {

    private static final String TAG = "DetailsFragment";

    // Weather display views
    private TextView tvWelcome;
    private TextView tvDateTime;
    private TextView tvTemperature;
    private TextView tvConditions;
    private TextView tvHumidity;
    private TextView tvWind;
    private ProgressBar progressBar;

    // City image views
    private ImageView cityImageView;
    private ProgressBar imageProgressBar;
    private TextView imageStatusText;

    // Action buttons
    private Button btnMap;
    private Button btnInsights;

    // ViewModels and helpers
    private ThemeViewModel themeViewModel;
    private WeatherViewModel weatherViewModel;
    private CityRepository cityRepository;
    private UserRepository userRepository;
    private CityImageGenerator imageGenerator;

    // Data fields
    private String cityName;
    private double latitude;
    private double longitude;
    private WeatherData currentWeatherData;
    private Long currentCityId;
    private WeatherData lastImageWeatherData;
    private boolean isGeneratingImage = false;

    /**
     * Factory method to create a new instance of DetailsFragment with city data.
     *
     * @param cityId    City ID (currently unused, for future use)
     * @param cityName  Name of the city
     * @param latitude  Latitude coordinate
     * @param longitude Longitude coordinate
     * @return New DetailsFragment instance
     */
    public static DetailsFragment newInstance(Long cityId, String cityName, double latitude, double longitude) {
        DetailsFragment fragment = new DetailsFragment();
        Bundle args = new Bundle();
        args.putString("cityName", cityName);
        args.putDouble("latitude", latitude);
        args.putDouble("longitude", longitude);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            cityName = getArguments().getString("cityName");
            latitude = getArguments().getDouble("latitude");
            longitude = getArguments().getDouble("longitude");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_details, container, false);

        // Initialize weather display views
        tvWelcome = view.findViewById(R.id.welcomeText);
        tvDateTime = view.findViewById(R.id.tvDateTime);
        tvTemperature = view.findViewById(R.id.tvTemperature);
        tvConditions = view.findViewById(R.id.tvConditions);
        tvHumidity = view.findViewById(R.id.tvHumidity);
        tvWind = view.findViewById(R.id.tvWind);
        progressBar = view.findViewById(R.id.progressBar);

        // Initialize city image views
        cityImageView = view.findViewById(R.id.cityImageView);
        imageProgressBar = view.findViewById(R.id.imageProgressBar);
        imageStatusText = view.findViewById(R.id.imageStatusText);

        // Initialize buttons
        btnMap = view.findViewById(R.id.mapButton);
        btnInsights = view.findViewById(R.id.insightsButton);

        // Initialize ViewModels, Repositories, and Helpers
        themeViewModel = new ViewModelProvider(requireActivity()).get(ThemeViewModel.class);
        weatherViewModel = new ViewModelProvider(requireActivity()).get(WeatherViewModel.class);
        cityRepository = new CityRepository(requireContext());
        userRepository = new UserRepository(requireContext());
        imageGenerator = new CityImageGenerator();

        // Display city information and fetch weather
        if (cityName != null && !cityName.isEmpty()) {
            String welcome = "Welcome to " + cityName;
            tvWelcome.setText(welcome);
            fetchWeatherForCity(cityName);
        } else {
            tvWelcome.setText("Welcome");
            Toast.makeText(getContext(), "No city information available", Toast.LENGTH_SHORT).show();
        }

        // Observe weather data
        weatherViewModel.getWeatherData().observe(getViewLifecycleOwner(), weatherData -> {
            if (weatherData != null) {
                currentWeatherData = weatherData;
                displayWeatherData(weatherData);
                // Enable insights button once we have weather data
                btnInsights.setEnabled(true);
                if (shouldGenerateImage(weatherData)) {
                    generateCityImage(weatherData);
                }
            }
        });

        // Observe loading state
        weatherViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            if (isLoading) {
                // Hide weather data while loading
                tvDateTime.setVisibility(View.GONE);
                tvTemperature.setVisibility(View.GONE);
                tvConditions.setVisibility(View.GONE);
                tvHumidity.setVisibility(View.GONE);
                tvWind.setVisibility(View.GONE);
                // Disable insights button while loading
                btnInsights.setEnabled(false);
            }
        });

        // Observe errors
        weatherViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                weatherViewModel.clearError();
            }
        });

        // Observe theme changes
        themeViewModel.getCurrentTheme().observe(getViewLifecycleOwner(), theme -> {
            if (theme != null) {
                applyThemeColors(theme, view);
            }
        });

        // Setup map button click listener
        btnMap.setOnClickListener(v -> {
            if (cityName != null) {
                MapFragment mapFragment = MapFragment.newInstance(latitude, longitude, cityName);
                ((MainActivity) requireActivity()).navigateToFragment(mapFragment, true);
            }
        });

        // Setup insights button click listener
        btnInsights.setOnClickListener(v -> {
            if (currentWeatherData != null && cityName != null) {
                WeatherInsightFragment weatherInsightFragment =
                        WeatherInsightFragment.newInstance(cityName, currentWeatherData);
                ((MainActivity) requireActivity()).navigateToFragment(weatherInsightFragment, true);
            } else {
                Toast.makeText(getContext(), "Weather data not available", Toast.LENGTH_SHORT).show();
            }
        });

        // Initially disable insights button until weather data loads
        btnInsights.setEnabled(false);

        return view;
    }

    /**
     * Fetch weather data for the specified city.
     *
     * @param cityName Name of the city
     */
    private void fetchWeatherForCity(String cityName) {
        Log.d(TAG, "Fetching weather for city: " + cityName);

        edu.uiuc.cs427app.data.entity.User currentUser = userRepository.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        List<City> cities = cityRepository.getCitiesForUser(currentUser.getId());
        City selectedCity = findCityByName(cities, cityName);

        if (selectedCity != null) {
            currentCityId = selectedCity.getId();
            Log.d(TAG, "City found with ID: " + currentCityId);
            weatherViewModel.fetchWeather(currentCityId);
        } else {
            Log.e(TAG, "City not found in database: " + cityName);
            Toast.makeText(getContext(), "City not found", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Find a city by name from a list of cities.
     *
     * @param cities   List of cities
     * @param cityName Name to search for
     * @return City object if found, null otherwise
     */
    private City findCityByName(List<City> cities, String cityName) {
        if (cities == null || cityName == null) {
            return null;
        }

        for (City city : cities) {
            if (city.getName().equalsIgnoreCase(cityName.trim())) {
                return city;
            }
        }
        return null;
    }

    /**
     * Display weather data in the UI.
     *
     * @param weatherData Weather data to display
     */
    private void displayWeatherData(WeatherData weatherData) {
        Log.d(TAG, "Displaying weather data");

        // Show all weather views
        tvDateTime.setVisibility(View.VISIBLE);
        tvTemperature.setVisibility(View.VISIBLE);
        tvConditions.setVisibility(View.VISIBLE);
        tvHumidity.setVisibility(View.VISIBLE);
        tvWind.setVisibility(View.VISIBLE);

        // Format and display date/time
        String dateTime = formatDateTime(weatherData.getTimestamp());
        tvDateTime.setText(dateTime);

        // Display temperature
        String temperature = String.format(Locale.US, "%.1f°F", weatherData.getTemperature());
        tvTemperature.setText(temperature);

        // Display conditions
        tvConditions.setText(weatherData.getConditions());

        // Display humidity
        String humidity = "Humidity: " + weatherData.getHumidity() + "%";
        tvHumidity.setText(humidity);

        // Display wind
        String wind = String.format(Locale.US, "Wind: %.1f mph", weatherData.getWindSpeed());
        tvWind.setText(wind);
    }

    /**
     * Check if we should generate an image for the given weather data.
     *
     * @param weatherData Weather data to check
     * @return true if we should generate image, false otherwise
     */
    private boolean shouldGenerateImage(WeatherData weatherData) {
        if (isGeneratingImage) {
            Log.d(TAG, "Image generation already in progress, skipping");
            return false;
        }

        if (weatherData.isStale()) {
            Log.d(TAG, "Weather data is stale, waiting for fresh data");
            return false;
        }

        if (currentCityId == null) {
            Log.w(TAG, "currentCityId is null, cannot verify weather data matches city");
            return false;
        }

        if (weatherData.getCityId() == null) {
            Log.w(TAG, "Weather data cityId is null, cannot verify it matches current city");
            return false;
        }

        if (!currentCityId.equals(weatherData.getCityId())) {
            Log.d(TAG, "Weather data cityId (" + weatherData.getCityId() + 
                ") doesn't match current city (" + currentCityId + "), skipping");
            return false;
        }

        if (lastImageWeatherData != null && 
            lastImageWeatherData.getConditions() != null &&
            weatherData.getConditions() != null &&
            lastImageWeatherData.getConditions().equals(weatherData.getConditions()) &&
            Math.abs((lastImageWeatherData.getTemperature() != null ? lastImageWeatherData.getTemperature() : 0.0) - 
                    (weatherData.getTemperature() != null ? weatherData.getTemperature() : 0.0)) < 0.1) {
            Log.d(TAG, "Weather data unchanged, skipping image generation");
            return false;
        }

        return true;
    }

    /**
     * Generate city landscape image using actual weather data.
     *
     * This method automatically generates a city image that matches
     * the current weather conditions using Gemini 2.0 Flash Image Generation.
     *
     * @param weatherData Current weather data from API
     */
    private void generateCityImage(WeatherData weatherData) {
        Log.d(TAG, "Auto-generating city image for: " + cityName + 
            " with weather: " + weatherData.getConditions());
        
        isGeneratingImage = true;
        lastImageWeatherData = weatherData;

        imageProgressBar.setVisibility(View.VISIBLE);
        imageStatusText.setVisibility(View.VISIBLE);
        imageStatusText.setText("Generating city image...");
        cityImageView.setVisibility(View.GONE);

        String timeOfDay = getTimeOfDay(weatherData.getTimestamp());
        String weatherCondition = weatherData.getConditions();

        Log.d(TAG, "Image generation params: weather=" + weatherCondition + ", time=" + timeOfDay);

        imageGenerator.generateCityImage(
                cityName,
                weatherCondition,
                timeOfDay,
                new CityImageGenerator.ImageCallback() {
                    @Override
                    public void onSuccess(String base64Image) {
                        isGeneratingImage = false;
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                displayBase64Image(base64Image);
                                imageProgressBar.setVisibility(View.GONE);
                                imageStatusText.setVisibility(View.GONE);
                            });
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        isGeneratingImage = false;
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                imageProgressBar.setVisibility(View.GONE);
                                imageStatusText.setVisibility(View.VISIBLE);
                                imageStatusText.setText("Image generation unavailable");
                                Log.e(TAG, "Image generation failed: " + errorMessage);
                            });
                        }
                    }
                }
        );
    }

    /**
     * Determine time of day from timestamp.
     *
     * @param timestamp Weather data timestamp
     * @return Time of day string (morning, afternoon, evening, night)
     */
    private String getTimeOfDay(Date timestamp) {
        SimpleDateFormat hourFormat = new SimpleDateFormat("HH", Locale.US);
        int hour = Integer.parseInt(hourFormat.format(timestamp));

        if (hour >= 5 && hour < 12) {
            return "morning";
        } else if (hour >= 12 && hour < 17) {
            return "afternoon";
        } else if (hour >= 17 && hour < 21) {
            return "evening";
        } else {
            return "night";
        }
    }

    /**
     * Display Base64-encoded image in ImageView.
     *
     * @param base64Image Base64-encoded image data
     */
    private void displayBase64Image(String base64Image) {
        try {
            byte[] imageBytes = Base64.decode(base64Image, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

            if (bitmap != null) {
                cityImageView.setImageBitmap(bitmap);
                cityImageView.setVisibility(View.VISIBLE);
                Log.d(TAG, "Successfully displayed image: " +
                        bitmap.getWidth() + "x" + bitmap.getHeight());
            } else {
                Log.e(TAG, "Failed to decode bitmap from Base64");
                imageStatusText.setVisibility(View.VISIBLE);
                imageStatusText.setText("Failed to display image");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error displaying Base64 image: " + e.getMessage(), e);
            imageStatusText.setVisibility(View.VISIBLE);
            imageStatusText.setText("Failed to display image");
        }
    }

    /**
     * Format timestamp to readable date/time string.
     *
     * @param timestamp Timestamp to format
     * @return Formatted date/time string
     */
    private String formatDateTime(Date timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US);
        return sdf.format(timestamp);
    }

    /**
     * Apply theme colors to UI elements.
     *
     * @param theme    Theme object with colors
     * @param rootView Root view of the fragment
     */
    private void applyThemeColors(Theme theme, View rootView) {
        if (theme == null || rootView == null) {
            return;
        }

        try {
            int backgroundColor = android.graphics.Color.parseColor(theme.getPrimaryColor());
            rootView.setBackgroundColor(backgroundColor);

            int textColor = android.graphics.Color.parseColor(theme.getSecondaryColor());
            tvWelcome.setTextColor(textColor);
            tvDateTime.setTextColor(textColor);
            tvTemperature.setTextColor(textColor);
            tvConditions.setTextColor(textColor);
            tvHumidity.setTextColor(textColor);
            tvWind.setTextColor(textColor);
        } catch (Exception e) {
            // Ignore color parse errors
        }
    }
}
