package edu.uiuc.cs427app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.graphics.Color;

import edu.uiuc.cs427app.databinding.ActivityMainBinding;
import edu.uiuc.cs427app.ui.fragment.LoginFragment;
import edu.uiuc.cs427app.ui.fragment.CityListFragment;
import edu.uiuc.cs427app.viewmodel.CityListViewModel;
import edu.uiuc.cs427app.viewmodel.ThemeViewModel;
import edu.uiuc.cs427app.viewmodel.LoginViewModel;
import edu.uiuc.cs427app.viewmodel.WeatherViewModel;
import edu.uiuc.cs427app.repository.UserRepository;
import edu.uiuc.cs427app.data.entity.User;
import edu.uiuc.cs427app.data.entity.Theme;
import edu.uiuc.cs427app.helper.DefaultThemeGenerator;
import edu.uiuc.cs427app.helper.LLMHelper;

/**
 * Main Activity - Single Activity + Fragments architecture.
 *
 * This is the ONLY Activity in the app. It hosts all Fragments.
 * Following Iteration 2 Class Diagram design.
 *
 * Fragment Flow:
 * LoginFragment -> CityListFragment -> DetailsFragment -> MapFragment
 *                                   -> ThemeSettingsFragment
 *
 * TODO: Team members working on Fragments:
 * - Fragments should call navigateToFragment() to navigate
 * - Use onBackPressed() for back navigation (automatically handled)
 * - Access shared CityListViewModel using getViewModel(CityListViewModel.class)
 *
 * @author CS427 Team 407
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FragmentManager fragmentManager;
    private CityListViewModel cityListViewModel;
    private UserRepository userRepository;
    private DefaultThemeGenerator defaultThemeGenerator;
    private LLMHelper llmHelper;

    /**
     * Called when the activity is first created.
     *
     * Initializes the fragment manager, ViewModels, default themes, and loads
     * the appropriate initial fragment (LoginFragment or CityListFragment) based
     * on whether a user is already logged in.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in onSaveInstanceState(Bundle). Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fragmentManager = getSupportFragmentManager();

        cityListViewModel = new ViewModelProvider(this).get(CityListViewModel.class);

        userRepository = new UserRepository(this);

        // Initialize default theme generator and LLM helper
        initializeDefaultThemes();
        
        // Connect ViewModels to default theme generator
        connectViewModelsToThemeGenerator();

        if (savedInstanceState == null) {
            User currentUser = userRepository.getCurrentUser();
            if (currentUser != null) {
                ThemeViewModel themeViewModel = new ViewModelProvider(this).get(ThemeViewModel.class);
                themeViewModel.setUserId(currentUser.getId());
                applyUserTheme();
                loadCityListFragment();
            } else {
                loadLoginFragment();
            }
        } else {
            // Apply theme even if restoring state
            User currentUser = userRepository.getCurrentUser();
            if (currentUser != null) {
                applyUserTheme();
            }
        }
    }

    /**
     * Load the Login Fragment.
     *
     * This is the starting point of the app when user is not authenticated.
     */
    private void loadLoginFragment() {
        LoginFragment loginFragment = new LoginFragment();
        navigateToFragment(loginFragment, false);
    }

    /**
     * Load the CityList Fragment.
     *
     * This is shown when user is already authenticated.
     */
    private void loadCityListFragment() {
        CityListFragment cityListFragment = new CityListFragment();
        navigateToFragment(cityListFragment, false);
    }

    /**
     * Navigate to a new Fragment.
     *
     * This method should be called by Fragments to navigate to other screens.
     *
     * @param fragment Fragment to navigate to
     * @param addToBackStack Whether to add this transaction to back stack
     */
    public void navigateToFragment(Fragment fragment, boolean addToBackStack) {
        android.util.Log.d("MainActivity", "navigateToFragment: " + fragment.getClass().getSimpleName() + ", addToBackStack: " + addToBackStack);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);

        if (addToBackStack) {
            transaction.addToBackStack(null);
        }

        transaction.commit();
        android.util.Log.d("MainActivity", "Transaction committed");
    }

    /**
     * Handle back button press.
     *
     * Pops the back stack if there are fragments, otherwise exits the app.
     */
    @Override
    public void onBackPressed() {
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Apply user's theme preference.
     *
     * Reads the user's theme preference and applies it to the activity.
     * Supports both predefined themes ("light", "dark", "blue") and LLM-generated themes.
     */
    public void applyUserTheme() {
        User currentUser = userRepository.getCurrentUser();
        if (currentUser == null || currentUser.getThemePreference() == null) {
            return;
        }
        
        String themePreference = currentUser.getThemePreference();
        Theme theme = parseThemeFromPreference(themePreference);
        
        if (theme != null) {
            applyThemeToActivity(theme);
        }
    }
    
    /**
     * Parse theme from user's preference.
     * 
     * Uses LLM-generated themes (either cached or fresh).
     * Falls back to cached default themes if parsing fails.
     * 
     * @param themePreference Theme preference string
     * @return Theme object or cached default theme
     */
    private Theme parseThemeFromPreference(String themePreference) {

        // use predefined themes directly, without relying on LLM generation
        // if LLM doesn't response
        if ("light".equals(themePreference)) {
            Theme lightTheme = new Theme();
            //white background
            lightTheme.setPrimaryColor("#FFFFFF");
            //black text
            lightTheme.setSecondaryColor("#000000");
            lightTheme.setIsDarkMode(false);
            lightTheme.setName("light");
            return lightTheme;
        }

        if ("dark".equals(themePreference)) {
            Theme darkTheme = new Theme();
            //dark background
            darkTheme.setPrimaryColor("#121212");
            //white text
            darkTheme.setSecondaryColor("#FFFFFF");
            darkTheme.setIsDarkMode(true);
            darkTheme.setName("dark");
            return darkTheme;
        }

        if ("blue".equals(themePreference)) {
            Theme blueTheme = new Theme();
            //blue background
            blueTheme.setPrimaryColor("#1976D2");
            //white text
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
            android.util.Log.e("MainActivity", "Failed to parse theme: " + e.getMessage());
        }
        
        // Try to use cached LLM-generated default theme as fallback
        if (defaultThemeGenerator != null) {
            Theme defaultTheme = defaultThemeGenerator.getDefaultTheme(themePreference);
            if (defaultTheme != null) {
                android.util.Log.d("MainActivity", "Using cached LLM-generated default theme: " + themePreference);
                return defaultTheme;
            }
        }
        
        // If no cached theme available, log warning but still return a theme to avoid crash
        android.util.Log.w("MainActivity", 
            "No theme available for: " + themePreference + ". Using light theme as fallback.");
        return getFallbackLightTheme(themePreference);
    }
    
    /**
     * Get a fallback theme when no theme is available.
     * 
     * This attempts to provide appropriate themes for "light", "dark", and "blue"
     * using LLM-generated default themes, or falls back to a neutral gray theme.
     * 
     * @param themeName Theme name or description
     * @return Fallback theme
     */
    private Theme getFallbackLightTheme(String themeName) {
        // Try to match common theme names with cached themes
        if (defaultThemeGenerator != null) {
            // Try exact match first
            Theme cachedTheme = defaultThemeGenerator.getDefaultTheme(themeName);
            if (cachedTheme != null) {
                android.util.Log.d("MainActivity", "Found cached fallback theme for: " + themeName);
                return cachedTheme;
            }
            
            // Try common variations
            if (themeName.equalsIgnoreCase("light")) {
                Theme lightTheme = defaultThemeGenerator.getDefaultTheme("light");
                if (lightTheme != null) return lightTheme;
            }
            if (themeName.equalsIgnoreCase("dark")) {
                Theme darkTheme = defaultThemeGenerator.getDefaultTheme("dark");
                if (darkTheme != null) return darkTheme;
            }
            if (themeName.toLowerCase().contains("blue")) {
                Theme blueTheme = defaultThemeGenerator.getDefaultTheme("blue");
                if (blueTheme != null) return blueTheme;
            }
        }
        
        // Last resort: Use a neutral gray theme as a placeholder
        android.util.Log.w("MainActivity", "No appropriate fallback theme available, using neutral gray");
        Theme placeholder = new Theme();
        placeholder.setPrimaryColor("#F5F5F5");  // Light gray background
        placeholder.setSecondaryColor("#333333"); // Dark gray text
        placeholder.setIsDarkMode(false);
        return placeholder;
    }
    
    /**
     * Extract color value from JSON string.
     *
     * @param json JSON string to parse
     * @param key Key to extract (e.g., "backgroundColor", "textColor")
     * @return Color hex value (e.g., "#FFFFFF") or null if not found
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
     *
     * Uses luminance calculation to determine if a color is dark or light.
     *
     * @param hexColor Hex color string (e.g., "#FFFFFF")
     * @return true if color is dark (luminance < 128), false otherwise
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
     * Apply theme to the activity.
     * 
     * @param theme Theme to apply
     */
    private void applyThemeToActivity(Theme theme) {
        if (theme == null) {
            return;
        }
        
        try {
            // Apply window background color
            int backgroundColor = Color.parseColor(theme.getPrimaryColor());
            getWindow().getDecorView().setBackgroundColor(backgroundColor);
            
            // Note: For more advanced theming, you could use AppCompatDelegate
            // to switch between dark/light modes, but for this iteration we're
            // just setting colors programmatically
            
        } catch (Exception e) {
            // Ignore color parse errors
        }
    }

    /**
     * Get the shared CityListViewModel.
     *
     * Fragments can call this to access the shared ViewModel.
     *
     * @return CityListViewModel scoped to MainActivity
     */
    public CityListViewModel getCityListViewModel() {
        return cityListViewModel;
    }

    /**
     * Log out the current user and return to login screen.
     *
     * This method can be called by fragments to log out the user.
     */
    public void logout() {
        userRepository.logout();

        // Clear the login result in the view model to prevent LoginFragment
        // from observing a stale "success" state.
        LoginViewModel loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        loginViewModel.clearLoginResult();
        loginViewModel.clearCreateAccountResult();
        
        // Reset the theme in the ViewModel
        ThemeViewModel themeViewModel = new ViewModelProvider(this).get(ThemeViewModel.class);
        themeViewModel.resetTheme();

        // Explicitly apply a default light theme for the login screen
        Theme defaultTheme = new Theme();
        defaultTheme.setPrimaryColor("#FFFFFF"); // White background
        defaultTheme.setSecondaryColor("#000000"); // Black text
        applyThemeToActivity(defaultTheme);
        
        loadLoginFragment();
    }

    /**
     * Initialize default theme generator and LLM helper.
     * 
     * This creates fallback themes that are LLM-generated and cached locally.
     * All themes are generated by LLM (compliant with requirements).
     */
    private void initializeDefaultThemes() {
        android.util.Log.d("MainActivity", "Initializing default theme generator...");
        
        // Create default theme generator (it will create its own LLMHelper internally)
        defaultThemeGenerator = new DefaultThemeGenerator(this);
        
        // Get the LLM helper from the generator for MainActivity's use
        llmHelper = defaultThemeGenerator.getLLMHelper();
        
        // Initialize default themes (generate and cache)
        defaultThemeGenerator.initialize(new DefaultThemeGenerator.InitializationCallback() {
            @Override
            public void onInitializationComplete(boolean success) {
                if (success) {
                    android.util.Log.d("MainActivity", "Default themes initialized successfully");
                } else {
                    android.util.Log.w("MainActivity", "Some default themes may not have been generated");
                }
            }
        });
    }
    
    /**
     * Connect ViewModels to the default theme generator and LLM helper.
     * 
     * This allows ViewModels to use cached LLM-generated themes as fallback
     * and share the same LLMHelper instance.
     */
    private void connectViewModelsToThemeGenerator() {
        ViewModelProvider viewModelProvider = new ViewModelProvider(this);
        
        try {
            // Connect ThemeViewModel to default theme generator
            ThemeViewModel themeViewModel = viewModelProvider.get(ThemeViewModel.class);
            themeViewModel.setDefaultThemeGenerator(defaultThemeGenerator);
            android.util.Log.d("MainActivity", "Connected ThemeViewModel to default theme generator");
        } catch (Exception e) {
            android.util.Log.d("MainActivity", "ThemeViewModel not yet initialized");
        }
        
        try {
            // Connect LoginViewModel to LLMHelper and DefaultThemeGenerator
            LoginViewModel loginViewModel = viewModelProvider.get(LoginViewModel.class);
            loginViewModel.setLLMHelper(llmHelper);
            loginViewModel.setDefaultThemeGenerator(defaultThemeGenerator);
            android.util.Log.d("MainActivity", "Connected LoginViewModel to LLMHelper and DefaultThemeGenerator");
        } catch (Exception e) {
            android.util.Log.d("MainActivity", "LoginViewModel not yet initialized");
        }
        
        try {
            // Connect WeatherViewModel to LLMHelper
            WeatherViewModel weatherViewModel = viewModelProvider.get(WeatherViewModel.class);
            weatherViewModel.setLLMHelper(llmHelper);
            android.util.Log.d("MainActivity", "Connected WeatherViewModel to LLMHelper");
        } catch (Exception e) {
            android.util.Log.d("MainActivity", "WeatherViewModel not yet initialized");
        }
    }
    
    /**
     * Get the default theme generator (for use by Fragments).
     * 
     * @return DefaultThemeGenerator instance
     */
    public DefaultThemeGenerator getDefaultThemeGenerator() {
        return defaultThemeGenerator;
    }
}

