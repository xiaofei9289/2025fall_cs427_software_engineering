package edu.uiuc.cs427app.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.Observer;

import edu.uiuc.cs427app.MainActivity;
import edu.uiuc.cs427app.R;
import edu.uiuc.cs427app.data.entity.Theme;
import edu.uiuc.cs427app.data.entity.User;
import edu.uiuc.cs427app.repository.UserRepository;
import edu.uiuc.cs427app.viewmodel.ThemeViewModel;

import java.util.List;

/**
 * ThemeSettingsFragment - Theme selection and customization screen.
 *
 * This fragment allows users to:
 * - Select from predefined themes (Light, Dark, Blue)
 * - Apply custom LLM-generated themes
 * - See a preview of the theme
 * - Switch between themes dynamically
 *
 * @author CS427 Team 407
 */
public class ThemeSettingsFragment extends Fragment {

    private ThemeViewModel themeViewModel;
    private UserRepository userRepository;
    
    private TextView tvCurrentTheme;
    private Button btnLightTheme;
    private Button btnDarkTheme;
    private Button btnBlueTheme;
    private Button btnCustomTheme;
    private Button btnBack;
    
    private Theme currentTheme;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_theme_settings, container, false);

        // Initialize ViewModels and Repository
        themeViewModel = new ViewModelProvider(requireActivity()).get(ThemeViewModel.class);
        userRepository = new UserRepository(requireContext());

        // Initialize views
        tvCurrentTheme = view.findViewById(R.id.tvCurrentTheme);
        btnLightTheme = view.findViewById(R.id.btnLightTheme);
        btnDarkTheme = view.findViewById(R.id.btnDarkTheme);
        btnBlueTheme = view.findViewById(R.id.btnBlueTheme);
        btnCustomTheme = view.findViewById(R.id.btnCustomTheme);
        btnBack = view.findViewById(R.id.btnBack);

        // Setup click listeners
        setupClickListeners();

        // Load current theme
        loadCurrentUserTheme();

        // Observe theme changes
        themeViewModel.getCurrentTheme().observe(getViewLifecycleOwner(), theme -> {
            if (theme != null) {
                currentTheme = theme;
                updateUI();
                applyThemeColors(theme, view);
            }
        });

        return view;
    }

    /**
     * Setup click listeners for all buttons.
     */
    private void setupClickListeners() {
        btnLightTheme.setOnClickListener(v -> applyTheme("light", 1L));
        
        btnDarkTheme.setOnClickListener(v -> applyTheme("dark", 2L));
        
        btnBlueTheme.setOnClickListener(v -> applyTheme("blue", 3L));
        
        btnCustomTheme.setOnClickListener(v -> applyCustomTheme());
        
        btnBack.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.onBackPressed();
            }
        });
    }

    /**
     * Load current user's theme preference.
     */
    private void loadCurrentUserTheme() {
        User currentUser = userRepository.getCurrentUser();
        if (currentUser == null || currentUser.getId() == null) {
            return;
        }
        
        themeViewModel.setUserId(currentUser.getId());
    }

    /**
     * Apply a theme and save to database.
     *
     * @param themeName Theme name ("light", "dark", "blue")
     * @param themeId Theme ID (1=Light, 2=Dark, 3=Blue)
     */
    private void applyTheme(String themeName, Long themeId) {
        User currentUser = userRepository.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Update theme in ViewModel
        themeViewModel.selectTheme(themeId);
        
        // Update user's theme preference
        currentUser.setThemePreference(themeName);
        userRepository.updateUser(currentUser);
        
        // Apply theme immediately to MainActivity
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.applyUserTheme();
        }
        
        Toast.makeText(getContext(), "Theme applied successfully!", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Apply custom LLM-generated theme.
     * This method loads and applies the user's custom theme from their themePreference.
     */
    private void applyCustomTheme() {
        User currentUser = userRepository.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check if user has a custom theme
        if (currentUser.getThemePreference() == null) {
            Toast.makeText(getContext(), "You don't have a custom theme yet. Please login with a theme description first.", Toast.LENGTH_LONG).show();
            return;
        }
        
        // Try to apply custom theme using ViewModel
        boolean success = themeViewModel.applyCustomTheme();
        
        if (success) {
            // Apply theme immediately to MainActivity
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.applyUserTheme();
            }
            
            Toast.makeText(getContext(), "Custom theme applied successfully!", Toast.LENGTH_SHORT).show();
        } else {
            // Check if user has a predefined theme
            String themePreference = currentUser.getThemePreference();
            if ("light".equals(themePreference) || "dark".equals(themePreference) || "blue".equals(themePreference)) {
                Toast.makeText(getContext(), "You are currently using a predefined theme. Please login with a theme description to create a custom theme.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), "Failed to load custom theme. Please try logging in again with a theme description.", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Update UI to reflect current theme.
     */
    private void updateUI() {
        if (currentTheme == null) {
            tvCurrentTheme.setText("Current Theme: Default");
            return;
        }
        
        String themeName = currentTheme.getName();
        if (themeName == null || themeName.isEmpty()) {
            themeName = "Custom Theme";
        }
        
        tvCurrentTheme.setText("Current Theme: " + themeName);
        applyThemeColors(currentTheme, getView());
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
            int backgroundColor = android.graphics.Color.parseColor(theme.getPrimaryColor());
            rootView.setBackgroundColor(backgroundColor);
            
            // Apply text color
            int textColor = android.graphics.Color.parseColor(theme.getSecondaryColor());
            ((TextView) rootView.findViewById(R.id.tvCurrentTheme)).setTextColor(textColor);
            ((TextView) rootView.findViewById(R.id.header)).setTextColor(textColor);

        } catch (Exception e) {
            // Ignore color parse errors
        }
    }
}
