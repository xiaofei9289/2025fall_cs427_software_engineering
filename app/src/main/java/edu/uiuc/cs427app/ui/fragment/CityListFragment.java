package edu.uiuc.cs427app.ui.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;

import java.util.List;

import edu.uiuc.cs427app.MainActivity;
import edu.uiuc.cs427app.R;
import edu.uiuc.cs427app.repository.UserRepository;
import edu.uiuc.cs427app.data.entity.User;
import edu.uiuc.cs427app.data.entity.City;
import edu.uiuc.cs427app.data.entity.Theme;
import edu.uiuc.cs427app.ui.adapter.CityAdapter;
import edu.uiuc.cs427app.viewmodel.CityListViewModel;
import edu.uiuc.cs427app.viewmodel.ThemeViewModel;

/**
 * CityListFragment - Main screen displaying user's list of cities (Iteration 3).
 *
 * This fragment displays the user's personalized list of cities and allows
 * adding/removing cities. The list is specific to each logged-in user.
 *
 * Requirements:
 * - Display "Team # - Username" at the top
 * - Show empty list on first login
 * - "ADD A LOCATION" button to add new cities
 * - Remove city functionality (long-press or swipe)
 * - Navigate to DetailsFragment when city is clicked
 * - Logout button to return to LoginFragment
 *
 * 
 * TODO: xincai3/twwang2 to implement:
 * 1. Setup RecyclerView with CityAdapter
 *    - Observe CityListViewModel.citiesLiveData
 *    - Update adapter when data changes
 *    - Handle empty state (show message when no cities)
 *
 * 2. Implement "ADD A LOCATION" button
 *    - Show dialog or navigate to new screen
 *    - Get city name from user input
 *    - Call CityListViewModel.addCity(cityName)
 *    - Validate city exists (via weather API)
 *
 * 3. Implement city removal
 *    - Long-press on city item
 *    - Show confirmation dialog
 *    - Call CityListViewModel.removeCity(cityId)
 *
 * 4. Implement city item click
 *    - Navigate to DetailsFragment with city ID
 *    - Pass city name as argument
 *
 * TODO: edmong2/xiaofei to implement:
 * 1. Apply user's theme from database
 *    - Get theme from User entity
 *    - Apply colors dynamically to UI elements
 *
 * @author CS427 Team 407
 */
public class CityListFragment extends Fragment {

    private CityListViewModel cityListViewModel;
    private ThemeViewModel themeViewModel;
    private UserRepository userRepository;
    private TextView tvWelcome;
    private TextView tvEmptyState;
    private TextView tvSubtitle;
    private TextView tvLocationsHeader;
    private RecyclerView rvCities;
    private CityAdapter cityAdapter;
    private Button btnAddLocation;
    private Button btnSettings;
    private Button btnLogout;

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * Initializes the RecyclerView, adapters, ViewModels, and sets up observers
     * for city list changes. Also displays the current user's username and applies
     * the user's theme preference.
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     *     any views in the fragment
     * @param container If non-null, this is the parent view that the fragment's
     *     UI should be attached to. The fragment should not add the view itself,
     *     but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *     from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_city_list, container, false);

        // Get shared ViewModel from MainActivity
        cityListViewModel = ((MainActivity) requireActivity()).getCityListViewModel();

        // Initialize ThemeViewModel
        themeViewModel = new ViewModelProvider(requireActivity()).get(ThemeViewModel.class);

        // Initialize UserRepository
        userRepository = new UserRepository(requireContext());

        tvSubtitle = view.findViewById(R.id.tvSubtitle);
        tvLocationsHeader = view.findViewById(R.id.tvLocationsHeader);

        // Initialize views
        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        rvCities = view.findViewById(R.id.rvCities);
        btnAddLocation = view.findViewById(R.id.btnAddLocation);
        btnSettings = view.findViewById(R.id.btnSettings);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Setup RecyclerView
        rvCities.setLayoutManager(new LinearLayoutManager(requireContext()));
        cityAdapter = new CityAdapter();
        rvCities.setAdapter(cityAdapter);

        // Display current user's username
        displayCurrentUser();
        
        // Set userId for ViewModel to load cities
        User currentUser = userRepository.getCurrentUser();
        if (currentUser != null && currentUser.getId() != null) {
            cityListViewModel.setUserId(currentUser.getId());
        }
        
        // Apply user's theme
        applyUserTheme();

        // Observe cities LiveData and update RecyclerView
        cityListViewModel.getCitiesLiveData().observe(getViewLifecycleOwner(), new Observer<List<City>>() {
            @Override
            public void onChanged(List<City> cities) {
                cityAdapter.updateCities(cities);
                // Handle empty state
                if (cities == null || cities.isEmpty()) {
                    tvEmptyState.setVisibility(View.VISIBLE);
                    rvCities.setVisibility(View.GONE);
                } else {
                    tvEmptyState.setVisibility(View.GONE);
                    rvCities.setVisibility(View.VISIBLE);
                }
            }
        });

        // Implement add location button click listener
        btnAddLocation.setOnClickListener(v -> {
            showAddCityDialog();
        });
        
        // Setup adapter click listeners
        setupAdapterListeners();

        btnSettings.setOnClickListener(v -> {
            ThemeSettingsFragment themeSettingsFragment = new ThemeSettingsFragment();
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.navigateToFragment(themeSettingsFragment, true);
            }
        });

        btnLogout.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Logging out...", Toast.LENGTH_SHORT).show();
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.logout();
            }
        });

        // Observe theme changes
        themeViewModel.getCurrentTheme().observe(getViewLifecycleOwner(), theme -> {
            if (theme != null) {
                applyThemeColors(theme, view);
                cityAdapter.setTheme(theme);
            }
        });

        return view;
    }

    /**
     * Called when the fragment is visible to the user and actively running.
     *
     * Reapplies the user's theme when the fragment becomes visible again after
     * being paused (e.g., after returning from theme settings).
     */
    @Override
    public void onResume() {
        super.onResume();
        // Reapply theme when fragment becomes visible again
        applyUserTheme();
    }

    /**
     * Display the current logged-in user's username in the welcome text.
     */
    private void displayCurrentUser() {
        User currentUser = userRepository.getCurrentUser();
        if (currentUser == null) {
            // If the user is null, it means they have been logged out.
            // The MainActivity is responsible for navigating to the LoginFragment.
            // Calling logout() here would cause an infinite loop if this fragment
            // is recreated for any reason after the user has been logged out.
            // By returning here, we prevent the loop and avoid a NullPointerException.
            return;
        }
        
        String username = currentUser.getUsername();
        tvWelcome.setText("Team 407 - " + username);
        
        // Set themeViewModel's userId if not set
        if (themeViewModel != null && currentUser.getId() != null) {
            themeViewModel.setUserId(currentUser.getId());
        }
    }
    
    /**
     * Apply user's theme from database.
     * 
     * Gets the user's theme preference and applies it to the UI.
     */
    private void applyUserTheme() {
        User currentUser = userRepository.getCurrentUser();
        if (currentUser == null || currentUser.getThemePreference() == null) {
            return;
        }
        
        String themePreference = currentUser.getThemePreference();
        Theme theme = parseThemeFromPreference(themePreference);

        if (theme != null) {
            applyThemeColors(theme, getView());
        }
    }
    
    /**
     * Parse theme from user's theme preference.
     * 
     * @param themePreference Theme preference string (can be "light"/"dark"/"blue" or LLM JSON)
     * @return Theme object
     */
    private Theme parseThemeFromPreference(String themePreference) {

        // Handle predefined themes first
        if ("light".equals(themePreference)) {
            Theme lightTheme = new Theme();
            lightTheme.setPrimaryColor("#FFFFFF");
            lightTheme.setSecondaryColor("#000000");
            lightTheme.setIsDarkMode(false);
            lightTheme.setName("light");
            return lightTheme;
        }

        if ("dark".equals(themePreference)) {
            Theme darkTheme = new Theme();
            darkTheme.setPrimaryColor("#121212");
            darkTheme.setSecondaryColor("#FFFFFF");
            darkTheme.setIsDarkMode(true);
            darkTheme.setName("dark");
            return darkTheme;
        }

        if ("blue".equals(themePreference)) {
            Theme blueTheme = new Theme();
            blueTheme.setPrimaryColor("#1976D2");
            blueTheme.setSecondaryColor("#FFFFFF");
            blueTheme.setIsDarkMode(true);
            blueTheme.setName("blue");
            return blueTheme;
        }

        // Try to parse LLM-generated theme (JSON)
        try {
            int jsonStart = themePreference.indexOf("{");
            int jsonEnd = themePreference.lastIndexOf("}");
            
            if (jsonStart != -1 && jsonEnd != -1) {
                String jsonString = themePreference.substring(jsonStart, jsonEnd + 1);
                String backgroundColor = extractColorFromJson(jsonString, "backgroundColor");
                String textColor = extractColorFromJson(jsonString, "textColor");
                
                if (backgroundColor != null && textColor != null) {
                    Theme theme = new Theme();
                    theme.setPrimaryColor(backgroundColor);
                    theme.setSecondaryColor(textColor);
                    theme.setIsDarkMode(isDarkColor(backgroundColor));
                    return theme;
                }
            }
        } catch (Exception e) {
            android.util.Log.e("CityListFragment", "Failed to parse theme: " + e.getMessage());
        }
        
        // WARNING: Temporary placeholder theme (to avoid crashes)
        // Themes should be dynamically generated by LLM, hardcoded themes cannot be used
        android.util.Log.w("CityListFragment", 
            "Using temporary placeholder theme. Themes should be dynamically generated by LLM.");
        return createPlaceholderTheme();
    }
    
    /**
     * Create a temporary placeholder theme to avoid crashes.
     * WARNING: This is only a placeholder theme to avoid crashes. Real themes should be dynamically generated by LLM.
     */
    private Theme createPlaceholderTheme() {
        Theme placeholder = new Theme();
        placeholder.setPrimaryColor("#F5F5F5");
        placeholder.setSecondaryColor("#333333");
        placeholder.setIsDarkMode(false);
        return placeholder;
    }
    
    /**
     * Extract color value from JSON string.
     */
    private String extractColorFromJson(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]+)\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }
    
    /**
     * Check if a color is dark.
     */
    private boolean isDarkColor(String hexColor) {
        if (hexColor == null || !hexColor.startsWith("#")) {
            return false;
        }
        
        try {
            int color = Color.parseColor(hexColor);
            int r = Color.red(color);
            int g = Color.green(color);
            int b = Color.blue(color);
            // Calculate luminance
            double luminance = 0.299 * r + 0.587 * g + 0.114 * b;
            return luminance < 128;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Apply theme colors to UI elements.
     * 
     * @param theme Theme object with colors
     * @param rootView Root view of the fragment
     */
    private void applyThemeColors(Theme theme, View rootView) {
        if (theme == null || rootView == null) {
            return;
        }
        
        try {
            // Apply background color
            int backgroundColor = Color.parseColor(theme.getPrimaryColor());
            rootView.setBackgroundColor(backgroundColor);
            
            // Apply text color
            int textColor = Color.parseColor(theme.getSecondaryColor());
            tvWelcome.setTextColor(textColor);
            tvSubtitle.setTextColor(textColor);
            tvLocationsHeader.setTextColor(textColor);

            // Apply button colors if needed
            // btnAddLocation.setBackgroundColor(backgroundColor);
            // btnLogout.setBackgroundColor(backgroundColor);
            
        } catch (Exception e) {
            // Ignore color parse errors
        }
    }
    
    /**
     * Show dialog to add a new city.
     */
    private void showAddCityDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add A Location");

        // Set up the input
        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Enter city name");
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Add", (dialog, which) -> {
            String cityName = input.getText().toString().trim();
            if (cityName.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a city name", Toast.LENGTH_SHORT).show();
            } else {
                cityListViewModel.addCity(cityName);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
    
    /**
     * Setup adapter click listeners for city items.
     */
    private void setupAdapterListeners() {
        android.util.Log.d("CityListFragment", "Setting up adapter listeners");
        cityAdapter.setOnCityClickListener(new CityAdapter.OnCityClickListener() {
            @Override
            public void onShowDetailsClick(City city) {
                android.util.Log.d("CityListFragment", "onShowDetailsClick called for city: " + city.getName());
                // Navigate to DetailsFragment with city data
                DetailsFragment detailsFragment = DetailsFragment.newInstance(city.getId(), city.getName(), city.getLatitude(), city.getLongitude());

                android.util.Log.d("CityListFragment", "Navigating to DetailsFragment");
                if (getActivity() instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) getActivity();
                    mainActivity.navigateToFragment(detailsFragment, true);
                } else {
                    android.util.Log.e("CityListFragment", "Activity is not MainActivity!");
                }
            }

            @Override
            public void onCityLongPress(City city) {
                showDeleteConfirmationDialog(city);
            }
        });
        
        // Observe operation results to show messages
        cityListViewModel.getOperationResult().observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                Toast.makeText(getContext(), result.getMessage(), 
                    result.isSuccess() ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG).show();
            }
        });
    }
    
    /**
     * Show confirmation dialog before deleting a city.
     */
    private void showDeleteConfirmationDialog(City city) {
        new AlertDialog.Builder(requireContext())
            .setTitle("Delete City")
            .setMessage("Are you sure you want to remove " + city.getName() + "?")
            .setPositiveButton("Delete", (dialog, which) -> {
                cityListViewModel.removeCity(city.getId());
            })
            .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
            .show();
    }
}
