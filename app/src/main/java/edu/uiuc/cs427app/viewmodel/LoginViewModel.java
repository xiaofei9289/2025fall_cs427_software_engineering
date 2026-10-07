package edu.uiuc.cs427app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import edu.uiuc.cs427app.data.entity.User;
import edu.uiuc.cs427app.data.entity.Theme;
import edu.uiuc.cs427app.repository.UserRepository;
import edu.uiuc.cs427app.helper.LLMHelper;
import edu.uiuc.cs427app.helper.DefaultThemeGenerator;

/**
 * ViewModel for Login and Account Creation screens.
 *
 * Manages authentication logic and user session.
 * Following Iteration 2 Class Diagram design - MVVM pattern.
 *
 * TODO: Team member assigned to Authentication:
 * - Implement login() method with validation
 * - Implement createAccount() method with duplicate check
 * - Implement validateCredentials() for input validation
 * - Handle authentication errors appropriately
 * - Update LiveData observers when login succeeds/fails
 * - Add unit tests for this ViewModel
 *
 * @author CS427 Team 407
 */
public class LoginViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private LLMHelper llmHelper;
    private DefaultThemeGenerator defaultThemeGenerator;

    private final MutableLiveData<LoginResult> loginResult;

    private final MutableLiveData<CreateAccountResult> createAccountResult;

    private final MutableLiveData<Boolean> isLoading;

    /**
     * Constructor with Application context.
     *
     * @param application Application context for repository initialization
     */
    public LoginViewModel(@NonNull Application application) {
        super(application);
        this.userRepository = new UserRepository(application);
        // LLMHelper will be injected by MainActivity via setLLMHelper()
        this.llmHelper = new LLMHelper(); // Temporary instance until injection
        this.loginResult = new MutableLiveData<>();
        this.createAccountResult = new MutableLiveData<>();
        this.isLoading = new MutableLiveData<>(false);
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
     * Set the DefaultThemeGenerator instance (injected by MainActivity).
     * 
     * @param generator DefaultThemeGenerator instance
     */
    public void setDefaultThemeGenerator(DefaultThemeGenerator generator) {
        this.defaultThemeGenerator = generator;
    }

    /**
     * Get LiveData for login result.
     *
     * Fragment should observe this to handle login success/failure.
     *
     * @return LiveData containing login result
     */
    public LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    /**
     * Get LiveData for account creation result.
     *
     * Fragment should observe this to handle account creation success/failure.
     *
     * @return LiveData containing account creation result
     */
    public LiveData<CreateAccountResult> getCreateAccountResult() {
        return createAccountResult;
    }

    /**
     * Get LiveData for loading state.
     *
     * Fragment should observe this to show/hide loading indicator during LLM theme generation.
     *
     * @return LiveData containing loading state (true when loading, false otherwise)
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * Attempt to log in a user with username and password.
     *
     * Steps:
     * 1. Validate inputs using validateCredentials()
     * 2. Authenticate user
     * 3. Generate theme using LLM from themeDescription
     * 4. Update user's theme preference
     * 5. Update loginResult LiveData with success or failure
     *
     * @param username Username entered by user
     * @param password Password entered by user
     * @param themeDescription Natural language theme description (optional)
     */
    public void login(String username, String password, String themeDescription) {
        if (!validateCredentials(username, password)) {
            loginResult.setValue(new LoginResult(false, "Invalid credentials"));
            return;
        }

        User user = userRepository.authenticateUser(username, password);
        if (user != null) {
            // If theme description is provided, generate new theme using LLM
            if (themeDescription != null && !themeDescription.trim().isEmpty()) {
                // Generate new theme using LLM
                isLoading.setValue(true); // Show loading indicator
                llmHelper.generateTheme(themeDescription, new LLMHelper.ThemeCallback() {
                    @Override
                    public void onSuccess(Theme theme) {
                        isLoading.setValue(false); // Hide loading indicator
                        // Update user's theme preference with LLM-generated theme
                        String themeJson = "{\"backgroundColor\":\"" + theme.getPrimaryColor() +
                                          "\",\"textColor\":\"" + theme.getSecondaryColor() + "\"}";
                        user.setThemePreference(themeJson);
                        userRepository.updateUser(user);
                        userRepository.saveCurrentUser(user);
                        loginResult.setValue(new LoginResult(true, user));
                    }

                    @Override
                    public void onError(String errorMessage) {
                        isLoading.setValue(false); // Hide loading indicator
                        // If LLM fails, keep the user's existing theme and proceed with login
                        userRepository.saveCurrentUser(user);
                        loginResult.setValue(new LoginResult(true, user));
                    }
                });
            } else {
                // No theme description, keep existing theme and proceed with login
                userRepository.saveCurrentUser(user);
                loginResult.setValue(new LoginResult(true, user));
            }
        } else {
            loginResult.setValue(new LoginResult(false, "Invalid username or password"));
        }
    }

    /**
     * Create a new user account.
     *
     * Steps:
     * 1. Validate inputs
     * 2. Check if username is available
     * 3. Generate theme using LLM from themeDescription (or use default if empty)
     * 4. Create User object with hashed password and theme
     * 5. Call userRepository.createUser()
     * 6. Update createAccountResult LiveData
     *
     * @param username Username for new account
     * @param password Password for new account
     * @param themeDescription Natural language theme description (optional - if empty, uses default theme)
     */
    public void createAccount(String username, String password, String themeDescription) {
        if (!validateCredentials(username, password)) {
            createAccountResult.setValue(new CreateAccountResult(false, "Invalid credentials"));
            return;
        }

        if (!userRepository.isUsernameAvailable(username)) {
            createAccountResult.setValue(new CreateAccountResult(false, "Username already exists"));
            return;
        }

        // If theme description is empty, use default theme
        if (themeDescription == null || themeDescription.trim().isEmpty()) {
            android.util.Log.d("LoginViewModel", "No theme description provided, using default theme");
            Theme defaultTheme = getDefaultTheme();
            
            User newUser = new User(username, password, null);
            String themeJson = "{\"backgroundColor\":\"" + defaultTheme.getPrimaryColor() + 
                              "\",\"textColor\":\"" + defaultTheme.getSecondaryColor() + "\"}";
            newUser.setThemePreference(themeJson);
            
            long userId = userRepository.createUser(newUser);
            
            if (userId != -1) {
                newUser.setId(userId);
                userRepository.saveCurrentUser(newUser);
                createAccountResult.setValue(new CreateAccountResult(true, "Account created successfully with default theme"));
            } else {
                createAccountResult.setValue(new CreateAccountResult(false, "Failed to create account"));
            }
            return;
        }

        // Generate theme using LLM if description is provided
        isLoading.setValue(true); // Show loading indicator
        llmHelper.generateTheme(themeDescription, new LLMHelper.ThemeCallback() {
            @Override
            public void onSuccess(Theme theme) {
                isLoading.setValue(false); // Hide loading indicator
                // Create user with LLM-generated theme JSON
                User newUser = new User(username, password, null);
                // Store the LLM-generated theme as JSON in themePreference
                String themeJson = "{\"backgroundColor\":\"" + theme.getPrimaryColor() +
                                  "\",\"textColor\":\"" + theme.getSecondaryColor() + "\"}";
                newUser.setThemePreference(themeJson);

                long userId = userRepository.createUser(newUser);

                if (userId != -1) {
                    newUser.setId(userId);
                    userRepository.saveCurrentUser(newUser);
                    createAccountResult.setValue(new CreateAccountResult(true, "Account created successfully with custom theme"));
                } else {
                    createAccountResult.setValue(new CreateAccountResult(false, "Failed to create account"));
                }
            }

            @Override
            public void onError(String errorMessage) {
                isLoading.setValue(false); // Hide loading indicator
                // Try to use cached LLM-generated default theme as fallback
                android.util.Log.w("LoginViewModel", "LLM failed: " + errorMessage + ". Attempting to use cached default theme.");
                
                // First, try to find a cached theme that matches the description
                Theme fallbackTheme = null;
                if (defaultThemeGenerator != null && themeDescription != null && !themeDescription.trim().isEmpty()) {
                    String lowerDesc = themeDescription.toLowerCase();
                    
                    // Try to match description to cached themes
                    if (lowerDesc.contains("dark") || lowerDesc.contains("night") || lowerDesc.contains("black")) {
                        fallbackTheme = defaultThemeGenerator.getDefaultTheme("dark");
                    } else if (lowerDesc.contains("blue") || lowerDesc.contains("ocean") || lowerDesc.contains("sky")) {
                        fallbackTheme = defaultThemeGenerator.getDefaultTheme("blue");
                    }
                    
                    // If no specific match, try light theme
                    if (fallbackTheme == null) {
                        fallbackTheme = defaultThemeGenerator.getDefaultTheme("light");
                    }
                }
                
                // If still no fallback, create a hardcoded default theme as last resort
                if (fallbackTheme == null) {
                    android.util.Log.w("LoginViewModel", "No cached themes available, using hardcoded light theme as last resort");
                    fallbackTheme = createHardcodedLightTheme();
                }
                
                // If we found a fallback theme, use it
                if (fallbackTheme != null) {
                    android.util.Log.d("LoginViewModel", "Using fallback theme");
                    User newUser = new User(username, password, null);
                    String themeJson = "{\"backgroundColor\":\"" + fallbackTheme.getPrimaryColor() + 
                                      "\",\"textColor\":\"" + fallbackTheme.getSecondaryColor() + "\"}";
                    newUser.setThemePreference(themeJson);
                    
                    long userId = userRepository.createUser(newUser);
                    
                    if (userId != -1) {
                        newUser.setId(userId);
                        userRepository.saveCurrentUser(newUser);
                        createAccountResult.setValue(new CreateAccountResult(true, 
                            "Account created with default theme"));
                    } else {
                        createAccountResult.setValue(new CreateAccountResult(false, "Failed to create account"));
                    }
                } else {
                    // No fallback available - fail the account creation
                    android.util.Log.e("LoginViewModel", "No fallback theme available");
                    createAccountResult.setValue(new CreateAccountResult(false, 
                        "Failed to generate theme: " + errorMessage + ". Please try again."));
                }
            }
        });
    }

    /**
     * Validate username and password inputs.
     *
     * Rules:
     * - Username: 3-20 characters, alphanumeric
     * - Password: minimum 6 characters
     *
     * @param username Username to validate
     * @param password Password to validate
     * @return true if valid, false otherwise
     */
    public static boolean validateCredentials(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        
        String normalizedUsername = normalizeUsername(username);
        String trimmedPassword = password.trim();
        
        if (normalizedUsername.length() < 3 || normalizedUsername.length() > 20) {
            return false;
        }
        
        if (!normalizedUsername.matches("^[a-zA-Z0-9]+$")) {
            return false;
        }
        
        if (trimmedPassword.length() < 6) {
            return false;
        }
        
        return true;
    }

    /**
     * Normalize username to lowercase for case-insensitive comparison.
     *
     * @param username The username to normalize
     * @return Lowercase username, or null if input is null
     */
    private static String normalizeUsername(String username) {
        return username != null ? username.toLowerCase().trim() : null;
    }
    
    /**
     * Get the default theme for new users when no theme description is provided.
     * 
     * Uses the default theme generator if available, otherwise falls back to hardcoded light theme.
     * 
     * @return A default theme (light theme by default)
     */
    private Theme getDefaultTheme() {
        // Try to use default theme generator first
        if (defaultThemeGenerator != null) {
            Theme theme = defaultThemeGenerator.getDefaultTheme("light");
            if (theme != null) {
                android.util.Log.d("LoginViewModel", "Using DefaultThemeGenerator light theme");
                return theme;
            }
        }
        
        // Fallback to hardcoded light theme
        android.util.Log.d("LoginViewModel", "Using hardcoded light theme");
        return createHardcodedLightTheme();
    }
    
    /**
     * Create a hardcoded light theme as last resort when all else fails.
     * 
     * This ensures users can always create an account even if the API is down.
     * This is a necessary fallback to allow users to use the app.
     * 
     * @return A light theme with white background and dark text
     */
    private Theme createHardcodedLightTheme() {
        Theme theme = new Theme("light_fallback", "#FFFFFF", "#000000", false);
        return theme;
    }

    /**
     * Result class for login operation.
     */
    public static class LoginResult {
        private final boolean success;
        private final User user;
        private final String errorMessage;

        /**
         * Constructor for successful login.
         *
         * @param success true for successful login
         * @param user The authenticated user
         */
        public LoginResult(boolean success, User user) {
            this.success = success;
            this.user = user;
            this.errorMessage = null;
        }

        /**
         * Constructor for failed login.
         *
         * @param success false for failed login
         * @param errorMessage Error message describing the failure
         */
        public LoginResult(boolean success, String errorMessage) {
            this.success = success;
            this.user = null;
            this.errorMessage = errorMessage;
        }

        /**
         * Check if login was successful.
         *
         * @return true if login succeeded, false otherwise
         */
        public boolean isSuccess() {
            return success;
        }

        /**
         * Get the authenticated user.
         *
         * @return User object if login succeeded, null otherwise
         */
        public User getUser() {
            return user;
        }

        /**
         * Get the error message.
         *
         * @return Error message if login failed, null otherwise
         */
        public String getErrorMessage() {
            return errorMessage;
        }
    }

    /**
     * Result class for account creation operation.
     */
    public static class CreateAccountResult {
        private final boolean success;
        private final String message;

        /**
         * Constructor for account creation result.
         *
         * @param success true if account was created successfully, false otherwise
         * @param message Success or error message
         */
        public CreateAccountResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        /**
         * Check if account creation was successful.
         *
         * @return true if account was created, false otherwise
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

    /**
     * Clear the login result LiveData.
     * This should be called on logout to prevent stale results from being observed.
     */
    public void clearLoginResult() {
        loginResult.setValue(null);
    }

    /**
     * Clear the create account result LiveData.
     * This should be called on logout to prevent stale results from being observed.
     */
    public void clearCreateAccountResult() {
        createAccountResult.setValue(null);
    }
}
