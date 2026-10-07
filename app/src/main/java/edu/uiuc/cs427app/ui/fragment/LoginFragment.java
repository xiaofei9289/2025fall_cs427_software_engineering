package edu.uiuc.cs427app.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.Observer;

import edu.uiuc.cs427app.R;
import edu.uiuc.cs427app.viewmodel.LoginViewModel;
import edu.uiuc.cs427app.data.entity.User;
import edu.uiuc.cs427app.ui.fragment.CityListFragment;

/**
 * LoginFragment - User authentication screen (Iteration 3).
 *
 * This fragment implements the authentication page as the first screen users see.
 * Requirements:
 * - Username and password fields for sign-in
 * - Sign-up functionality for new users
 * - Theme description field for LLM-driven UI customization
 * - Navigate to CityListFragment on successful authentication
 *
 * TODO: Team member xfeng18 to implement:
 * 1. Implement sign-in button click handler
 *    - Validate username and password
 *    - Call LoginViewModel.login(username, password)
 *    - Observe login result LiveData
 *    - Navigate to CityListFragment on success
 *    - Show error message on failure
 *
 * 2. Implement sign-up button click handler
 *    - Validate username, password, and theme description
 *    - Call LoginViewModel.createAccount(username, password, themeDescription)
 *    - Send theme description to LLM via LLMHelper
 *    - Parse LLM response and store theme in User entity
 *    - Navigate to CityListFragment on success
 *    - Show error message on failure
 *
 * 3. Handle LLM API errors
 *    - Apply default/backup theme if LLM fails
 *    - Show user-friendly error messages
 *
 * @author CS427 Team 407
 */
public class LoginFragment extends Fragment {

    private LoginViewModel loginViewModel;
    private EditText etUsername;
    private EditText etPassword;

    /**
     * etThemeDescription - Natural language theme description input field.
     *
     * DESIGN NOTE: This is intentionally free-form text input (not RadioButton/Spinner)
     * per Iteration 3 PDF requirements (FA25-Iteration3.pdf, page 4).
     *
     * The LLM (Gemini API) generates dynamic themes from natural language descriptions
     * like "summer beach" or "cyberpunk nightscape". The system parses the LLM's JSON
     * response to extract backgroundColor and textColor hex values.
     *
     * This approach differs from predefined themes (light/dark/blue) mentioned in earlier
     * documentation. The current requirement explicitly mandates LLM-driven customization
     * with natural language input to demonstrate AI-powered UI personalization.
     *
     * @see edu.uiuc.cs427app.helper.LLMHelper#generateTheme(String)
     */
    private EditText etThemeDescription;
    private Button btnLogin;
    private Button btnSignUp;
    private Button btnSignUpConfirm;
    private Button btnBack;
    private LinearLayout layoutLoginMode;
    private LinearLayout layoutSignupMode;
    private ProgressBar progressBar;
    private TextView tvLoadingMessage;

    private boolean isSignupMode = false;

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * Initializes all UI components, sets up the LoginViewModel, and establishes
     * click listeners and LiveData observers.
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
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Get LoginViewModel from MainActivity (which has DefaultThemeGenerator injected)
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);

        etUsername = view.findViewById(R.id.etUsername);
        etPassword = view.findViewById(R.id.etPassword);
        etThemeDescription = view.findViewById(R.id.etThemeDescription);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnSignUp = view.findViewById(R.id.btnSignUp);
        btnSignUpConfirm = view.findViewById(R.id.btnSignUpConfirm);
        btnBack = view.findViewById(R.id.btnBack);
        layoutLoginMode = view.findViewById(R.id.layoutLoginMode);
        layoutSignupMode = view.findViewById(R.id.layoutSignupMode);
        progressBar = view.findViewById(R.id.progressBar);
        tvLoadingMessage = view.findViewById(R.id.tvLoadingMessage);

        setupClickListeners();

        setupObservers();


        return view;
    }

    /**
     * Set up click listeners for all buttons.
     */
    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String themeDescription = etThemeDescription.getText().toString().trim();
            
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Please enter username and password", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Call login with optional theme description
            loginViewModel.login(username, password, themeDescription);
        });

        btnSignUp.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Switching to signup mode", Toast.LENGTH_SHORT).show();
            switchToSignupMode();
        });

        btnSignUpConfirm.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String themeDescription = etThemeDescription.getText().toString().trim();
                        
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Please enter username and password", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (username.length() < 3) {
                Toast.makeText(getContext(), "Username must be at least 3 characters", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (username.length() > 20) {
                Toast.makeText(getContext(), "Username must be 20 characters or less", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (!username.matches("^[a-zA-Z0-9]+$")) {
                Toast.makeText(getContext(), "Username can only contain letters and numbers", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (password.length() < 6) {
                Toast.makeText(getContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Theme description is optional - if empty, will use default theme
            loginViewModel.createAccount(username, password, themeDescription);
        });

        btnBack.setOnClickListener(v -> {
            switchToLoginMode();
        });
    }

    /**
     * Set up LiveData observers for login and signup results.
     */
    private void setupObservers() {
        loginViewModel.getLoginResult().observe(getViewLifecycleOwner(), new Observer<LoginViewModel.LoginResult>() {
            @Override
            public void onChanged(LoginViewModel.LoginResult result) {
                if (result == null) {
                    return; // This is the state after a logout, do nothing.
                }
                if (result.isSuccess()) {
                    Toast.makeText(getContext(), "Login successful!", Toast.LENGTH_SHORT).show();
                    navigateToCityList();
                } else {
                    Toast.makeText(getContext(), result.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        loginViewModel.getCreateAccountResult().observe(getViewLifecycleOwner(), new Observer<LoginViewModel.CreateAccountResult>() {
            @Override
            public void onChanged(LoginViewModel.CreateAccountResult result) {
                if (result == null) {
                    return; // This is the state after a logout, do nothing.
                }
                if (result.isSuccess()) {
                    Toast.makeText(getContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
                    navigateToCityList();
                } else {
                    Toast.makeText(getContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Observe loading state to show/hide loading indicator
        loginViewModel.getIsLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading) {
                if (isLoading != null && isLoading) {
                    // Show loading indicator
                    progressBar.setVisibility(View.VISIBLE);
                    tvLoadingMessage.setVisibility(View.VISIBLE);
                    // Disable buttons to prevent double-click
                    btnLogin.setEnabled(false);
                    btnSignUpConfirm.setEnabled(false);
                } else {
                    // Hide loading indicator
                    progressBar.setVisibility(View.GONE);
                    tvLoadingMessage.setVisibility(View.GONE);
                    // Re-enable buttons
                    btnLogin.setEnabled(true);
                    btnSignUpConfirm.setEnabled(true);
                }
            }
        });
    }

    /**
     * Switch to signup mode - show theme field and signup buttons.
     */
    private void switchToSignupMode() {
        isSignupMode = true;
        // Theme field is now always visible
        layoutLoginMode.setVisibility(View.GONE);
        layoutSignupMode.setVisibility(View.VISIBLE);
        Toast.makeText(getContext(), "Switched to signup mode", Toast.LENGTH_SHORT).show();
    }

    /**
     * Switch to login mode - hide theme field and show login buttons.
     */
    private void switchToLoginMode() {
        isSignupMode = false;
        // Theme field is now always visible, just clear it
        layoutLoginMode.setVisibility(View.VISIBLE);
        layoutSignupMode.setVisibility(View.GONE);
        etThemeDescription.setText("");
    }

    /**
     * Navigate to CityListFragment after successful authentication.
     */
    private void navigateToCityList() {
        if (getActivity() instanceof edu.uiuc.cs427app.MainActivity) {
            edu.uiuc.cs427app.MainActivity mainActivity = (edu.uiuc.cs427app.MainActivity) getActivity();
            CityListFragment cityListFragment = new CityListFragment();
            mainActivity.navigateToFragment(cityListFragment, true);
        }
    }
}
