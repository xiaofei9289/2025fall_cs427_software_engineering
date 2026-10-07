package edu.uiuc.cs427app.helper;

import edu.uiuc.cs427app.BuildConfig;
import edu.uiuc.cs427app.data.entity.WeatherData;
import edu.uiuc.cs427app.data.entity.Theme;
import edu.uiuc.cs427app.network.GeminiApiClient;
import edu.uiuc.cs427app.network.RetrofitClient;
import edu.uiuc.cs427app.network.model.GeminiRequest;
import edu.uiuc.cs427app.network.model.GeminiResponse;
import edu.uiuc.cs427app.network.model.WeatherInsightsResponse;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Helper class for LLM (Large Language Model) operations.
 *
 * Encapsulates Google Gemini API calls for generating weather insights.
 * Following Iteration 2 Class Diagram design.
 *
 * TODO: Team member assigned to LLM Integration:
 * - Implement generateInsights() to call Gemini API
 * - Implement constructPrompt() to create effective prompts
 * - Experiment with different prompt formats for best results
 * - Handle API errors (rate limiting, invalid API key, etc.)
 * - Parse and format LLM responses for display
 * - Add unit tests for prompt construction
 *
 * @author CS427 Team 407
 */
public class LLMHelper {

    private final GeminiApiClient geminiApiClient;
    private final String apiKey;
    private DefaultThemeGenerator defaultThemeGenerator;
    private final Gson gson;


    /**
     * Constructor.
     *
     * Initializes Gemini API client with API key from BuildConfig.
     */
    public LLMHelper() {
        this.geminiApiClient = RetrofitClient.getGeminiApiClient();
        this.apiKey = BuildConfig.GEMINI_API_KEY;
        this.gson = new Gson();
    }

    /**
     * Set the default theme generator for fallback support.
     *
     * @param generator DefaultThemeGenerator instance
     */
    public void setDefaultThemeGenerator(DefaultThemeGenerator generator) {
        this.defaultThemeGenerator = generator;
    }

    /**
     * Generate AI insights about weather data.
     *
     * TODO: Implement this method
     * Steps:
     * 1. Construct prompt using constructPrompt()
     * 2. Create GeminiRequest with the prompt
     * 3. Call geminiApiClient.generateContent()
     * 4. Parse response and extract generated text
     * 5. Call callback with result
     *
     * Example usage:
     * generateInsights(weatherData, new InsightsCallback() {
     *     public void onSuccess(String insights) {
     *         // Display insights in UI
     *     }
     *     public void onError(String error) {
     *         // Show error message
     *     }
     * });
     *
     * @param weatherData WeatherData to generate insights about
     * @param callback    Callback to handle result
     */
    /**
     * Generate AI insights about weather data (overload with city name).
     *
     * Generates a list of questions that users might ask about the current weather.
     * Returns JSON with a questions array.
     *
     * @param weatherData WeatherData to generate insights about
     * @param cityName City name for location context (optional, can be null)
     * @param callback Callback to handle result
     */
    public void generateInsights(WeatherData weatherData, String cityName, InsightsCallback callback) {
        //WeatherData weatherData = new WeatherData();
        //weatherData.setTemperature(weatherResponse.getTemperature());
        //weatherData.setConditions(weatherResponse.getConditions());
        //weatherData.setHumidity(weatherResponse.getHumidity());
        //weatherData.setWindSpeed(weatherResponse.getWindSpeed());
        if (apiKey == null || apiKey.isEmpty()) {
            android.util.Log.e("LLMHelper", "Gemini API key not configured");
            callback.onError("Gemini API key not configured");
            return;
        }

        if (weatherData == null) {
            android.util.Log.e("LLMHelper", "WeatherData cannot be null");
            callback.onError("Weather data is required");
            return;
        }

        android.util.Log.d("LLMHelper", "Generating insights for weather data");
        String prompt = constructPrompt(weatherData, cityName);
        GeminiRequest request = new GeminiRequest(prompt);

        geminiApiClient.generateContent(request, apiKey)
            .enqueue(new Callback<GeminiResponse>() {
                @Override
                public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String generatedText = response.body().getGeneratedText();
                        android.util.Log.d("LLMHelper", "LLM Response: " + generatedText);
                        try {
                            String formattedInsights = parseResponse(generatedText);
                            callback.onSuccess(formattedInsights);
                        } catch (Exception e) {
                            android.util.Log.e("LLMHelper", "Failed to parse LLM response: " + e.getMessage());
                            android.util.Log.e("LLMHelper", "Raw response: " + generatedText);
                            callback.onError("Failed to parse response: " + e.getMessage());
                        }
                    } else {
                        String errorMsg = "Failed to generate insights: HTTP " + response.code();
                        if (response.errorBody() != null) {
                            try {
                                String errorBody = response.errorBody().string();
                                errorMsg += ": " + errorBody;
                                android.util.Log.e("LLMHelper", "API error: " + errorMsg);
                            } catch (Exception e) {
                                android.util.Log.e("LLMHelper", "Error reading error body: " + e.getMessage());
                            }
                        }
                        callback.onError(errorMsg);
                    }
                }

                @Override
                public void onFailure(Call<GeminiResponse> call, Throwable t) {
                    android.util.Log.e("LLMHelper", "Network error: " + t.getMessage(), t);
                    callback.onError("Network error: " + t.getMessage());
                }
            });
    }

    /**
     * Generate AI insights about weather data (overload without city name).
     *
     * @param weatherData WeatherData to generate insights about
     * @param callback Callback to handle result
     */
    public void generateInsights(WeatherData weatherData, InsightsCallback callback) {
        generateInsights(weatherData, null, callback);
    }

    /**
     * Construct a prompt for the LLM based on weather data.
     *
     * Creates a prompt that requests JSON output with a questions array.
     * Based on the weather insights plan sample prompt.
     *
     * @param weatherData WeatherData to create prompt from
     * @param cityName City name for location context (optional)
     * @return Formatted prompt string
     */
    public String constructPrompt(WeatherData weatherData, String cityName) {
        StringBuilder prompt = new StringBuilder();
        
        // Enhanced role and context
        prompt.append("You are an expert weather assistant with deep knowledge of how weather conditions ");
        prompt.append("affect daily life, health, and activities. Your goal is to help people make informed ");
        prompt.append("decisions about their day based on current weather conditions.\n\n");
        
        // Detailed instructions
        prompt.append("TASK: Generate exactly two practical, actionable questions that people would naturally ");
        prompt.append("ask when planning their day based on the weather conditions provided below.\n\n");
        
        prompt.append("QUESTION GUIDELINES:\n");
        prompt.append("- Make questions specific to the actual weather conditions (temperature, conditions, humidity, wind)\n");
        prompt.append("- Focus on practical daily decisions: outdoor activities, clothing choices, health considerations, travel plans\n");
        prompt.append("- Questions should be conversational and natural, as if a friend is asking\n");
        prompt.append("- Avoid generic questions - tailor them to the specific weather data provided\n");
        prompt.append("- Consider different aspects: comfort level, safety, activity suitability, health impacts\n");
        prompt.append("- Keep questions concise (one sentence, ending with a question mark)\n");
        prompt.append("- Make questions actionable - they should lead to useful insights when answered\n\n");
        
        prompt.append("EXAMPLES OF GOOD QUESTIONS:\n");
        prompt.append("- \"Should I wear a jacket or light layers today?\" (for moderate temps)\n");
        prompt.append("- \"Is it safe to go for a long walk or run outside?\" (for extreme conditions)\n");
        prompt.append("- \"Will I need an umbrella or rain gear?\" (for rainy conditions)\n");
        prompt.append("- \"Is it too hot/cold for outdoor sports or exercise?\" (for extreme temps)\n");
        prompt.append("- \"Should I plan indoor activities instead?\" (for severe weather)\n");
        prompt.append("- \"Will the wind make it feel much colder than the temperature?\" (for windy conditions)\n\n");
        
        prompt.append("EXAMPLES OF BAD QUESTIONS (avoid these):\n");
        prompt.append("- \"What's the weather like?\" (too generic, doesn't use the data)\n");
        prompt.append("- \"Is it sunny?\" (obvious from conditions, not actionable)\n");
        prompt.append("- \"What should I do?\" (too vague, not specific to weather)\n\n");

        // Weather data section with context
        prompt.append("CURRENT WEATHER CONDITIONS:\n");
        prompt.append("==========================\n");
        
        if (cityName != null && !cityName.trim().isEmpty()) {
            prompt.append("Location: ").append(cityName).append("\n");
        }

        // Format temperature with context
        String tempStr = weatherData.getFormattedTemperature();
        if (tempStr == null || tempStr.isEmpty()) {
            tempStr = String.format("%.1f°F", weatherData.getTemperature() != null ? weatherData.getTemperature() : 0.0);
        }
        prompt.append("Temperature: ").append(tempStr);
        
        // Add temperature context
        if (weatherData.getTemperature() != null) {
            double temp = weatherData.getTemperature();
            if (temp >= 80) {
                prompt.append(" (Hot - consider heat safety, hydration, and staying cool)");
            } else if (temp >= 70) {
                prompt.append(" (Warm - comfortable for most outdoor activities)");
            } else if (temp >= 60) {
                prompt.append(" (Mild - pleasant for outdoor activities)");
            } else if (temp >= 50) {
                prompt.append(" (Cool - may need light layers)");
            } else if (temp >= 32) {
                prompt.append(" (Cold - bundle up, limit outdoor exposure)");
            } else {
                prompt.append(" (Very Cold - take precautions against frostbite)");
            }
        }

        prompt.append("\nConditions: ");
        String conditions = weatherData.getConditions() != null ? weatherData.getConditions() : "Unknown";
        prompt.append(conditions);
        // Add conditions context
        if (conditions != null && !conditions.equalsIgnoreCase("Unknown")) {
            String lowerConditions = conditions.toLowerCase();
            if (lowerConditions.contains("rain") || lowerConditions.contains("drizzle") || lowerConditions.contains("shower")) {
                prompt.append(" (Wet conditions - consider rain gear and indoor alternatives)");
            } else if (lowerConditions.contains("snow") || lowerConditions.contains("sleet")) {
                prompt.append(" (Precipitation - be cautious with travel and outdoor activities)");
            } else if (lowerConditions.contains("fog") || lowerConditions.contains("mist")) {
                prompt.append(" (Reduced visibility - drive carefully)");
            } else if (lowerConditions.contains("cloud") || lowerConditions.contains("overcast")) {
                prompt.append(" (Cloudy - UV protection still recommended)");
            } else if (lowerConditions.contains("clear") || lowerConditions.contains("sunny")) {
                prompt.append(" (Clear skies - great for outdoor activities, use sun protection)");
            }
        }

        prompt.append("\nHumidity: ");
        Integer humidity = weatherData.getHumidity();
        prompt.append(humidity != null ? humidity : 0);
        prompt.append("%");
        // Add humidity context
        if (humidity != null) {
            if (humidity >= 70) {
                prompt.append(" (High humidity - can make temperatures feel warmer, may be uncomfortable)");
            } else if (humidity >= 50) {
                prompt.append(" (Moderate humidity - comfortable range)");
            } else if (humidity >= 30) {
                prompt.append(" (Low humidity - may cause dry skin, stay hydrated)");
            } else {
                prompt.append(" (Very low humidity - very dry conditions)");
            }
        }

        prompt.append("\nWind Speed: ");
        if (weatherData.getWindSpeed() != null) {
            double windSpeed = weatherData.getWindSpeed();
            prompt.append(String.format("%.1f", windSpeed));
            prompt.append(" mph");
            // Add wind context
            if (windSpeed >= 25) {
                prompt.append(" (Strong winds - be cautious outdoors, may affect activities)");
            } else if (windSpeed >= 15) {
                prompt.append(" (Moderate winds - may make it feel cooler, secure loose items)");
            } else if (windSpeed >= 7) {
                prompt.append(" (Light breeze - pleasant for most activities)");
            } else {
                prompt.append(" (Calm conditions - minimal wind impact)");
            }
        } else {
            prompt.append("0 mph (Calm conditions)");
        }

        // Note about missing data
        prompt.append("\n\nNote: Precipitation chance and weather alerts are not available in this data.\n");

        // Enhanced output format instructions
        prompt.append("\nOUTPUT REQUIREMENTS:\n");
        prompt.append("====================\n");
        prompt.append("Return your response as a valid JSON object with exactly this structure:\n");
        prompt.append("{\n");
        prompt.append("  \"questions\": [\n");
        prompt.append("    \"Your first practical question here?\",\n");
        prompt.append("    \"Your second practical question here?\"\n");
        prompt.append("  ]\n");
        prompt.append("}\n\n");
        
        prompt.append("IMPORTANT:\n");
        prompt.append("- Return ONLY the JSON object, no additional text, explanations, or markdown formatting\n");
        prompt.append("- Do not wrap the JSON in code blocks or markdown\n");
        prompt.append("- Ensure the JSON is valid and properly formatted\n");
        prompt.append("- Both questions must be different and address different aspects of the weather\n");
        prompt.append("- Questions should be based on the actual weather data provided above\n");

        return prompt.toString();
    }

    /**
     * Construct a prompt for the LLM based on weather data (overload without city name).
     *
     * @param weatherData WeatherData to create prompt from
     * @return Formatted prompt string
     */
    public String constructPrompt(WeatherData weatherData) {
        return constructPrompt(weatherData, null);
    }

    /**
     * Parse and extract JSON from the LLM response.
     *
     * Extracts JSON from the response and returns the clean JSON string.
     * Handles cases where the LLM wraps JSON in markdown code blocks or adds extra text.
     *
     * @param rawResponse Raw response from LLM
     * @return Clean JSON string ready for parsing
     * @throws IllegalArgumentException if response is empty or invalid
     */
    public String parseResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.trim().isEmpty()) {
            throw new IllegalArgumentException("Empty response from LLM");
        }

        // Remove markdown code blocks if present
        String cleaned = rawResponse.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        cleaned = cleaned.trim();

        // Extract JSON object from response
        int jsonStart = cleaned.indexOf("{");
        int jsonEnd = cleaned.lastIndexOf("}");

        if (jsonStart == -1 || jsonEnd == -1 || jsonEnd <= jsonStart) {
            android.util.Log.w("LLMHelper", "No JSON found in response, returning raw text");
            // Fallback: return the raw response if JSON parsing fails
            return rawResponse;
        }

        String jsonString = cleaned.substring(jsonStart, jsonEnd + 1);

        // Validate JSON using Gson
        try {
            WeatherInsightsResponse insightsResponse = gson.fromJson(jsonString, WeatherInsightsResponse.class);

            if (insightsResponse == null) {
                android.util.Log.w("LLMHelper", "Parsed response is null, returning raw JSON");
                return jsonString;
            }

            if (!insightsResponse.isValid()) {
                android.util.Log.w("LLMHelper", "Parsed response is invalid (no questions), returning raw JSON");
                return jsonString;
            }

            android.util.Log.d("LLMHelper", "Successfully validated " + insightsResponse.getQuestionCount() + " questions");
            return jsonString;

        } catch (JsonSyntaxException e) {
            android.util.Log.e("LLMHelper", "Failed to parse JSON: " + e.getMessage());
            android.util.Log.e("LLMHelper", "JSON string: " + jsonString);
            // Fallback: return the raw JSON string if parsing fails
            return jsonString;
        } catch (Exception e) {
            android.util.Log.e("LLMHelper", "Unexpected error parsing response: " + e.getMessage(), e);
            // Fallback: return the raw JSON string
            return jsonString;
        }
    }

    /**
     * Generate an answer to a follow-up question about weather data.
     *
     * Takes a user's question and generates a helpful answer based on the current weather conditions.
     *
     * @param weatherData WeatherData to base the answer on
     * @param question User's question about the weather
     * @param cityName City name for location context (optional, can be null)
     * @param callback Callback to handle result
     */
    public void generateAnswer(WeatherData weatherData, String question, String cityName, InsightsCallback callback) {
        if (apiKey == null || apiKey.isEmpty()) {
            android.util.Log.e("LLMHelper", "Gemini API key not configured");
            callback.onError("Gemini API key not configured");
            return;
        }

        if (weatherData == null) {
            android.util.Log.e("LLMHelper", "WeatherData cannot be null");
            callback.onError("Weather data is required");
            return;
        }

        if (question == null || question.trim().isEmpty()) {
            android.util.Log.e("LLMHelper", "Question cannot be empty");
            callback.onError("Question is required");
            return;
        }

        android.util.Log.d("LLMHelper", "Generating answer for question: " + question);
        String prompt = constructAnswerPrompt(weatherData, question, cityName);
        GeminiRequest request = new GeminiRequest(prompt);

        geminiApiClient.generateContent(request, apiKey)
            .enqueue(new Callback<GeminiResponse>() {
                @Override
                public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String generatedText = response.body().getGeneratedText();
                        android.util.Log.d("LLMHelper", "LLM Answer Response: " + generatedText);
                        // For answers, return the text directly (not JSON)
                        callback.onSuccess(generatedText.trim());
                    } else {
                        String errorMsg = "Failed to generate answer: HTTP " + response.code();
                        if (response.errorBody() != null) {
                            try {
                                String errorBody = response.errorBody().string();
                                errorMsg += ": " + errorBody;
                                android.util.Log.e("LLMHelper", "API error: " + errorMsg);
                            } catch (Exception e) {
                                android.util.Log.e("LLMHelper", "Error reading error body: " + e.getMessage());
                            }
                        }
                        callback.onError(errorMsg);
                    }
                }

                @Override
                public void onFailure(Call<GeminiResponse> call, Throwable t) {
                    android.util.Log.e("LLMHelper", "Network error: " + t.getMessage(), t);
                    callback.onError("Network error: " + t.getMessage());
                }
            });
    }

    /**
     * Generate an answer to a follow-up question about weather data (overload without city name).
     *
     * @param weatherData WeatherData to base the answer on
     * @param question User's question about the weather
     * @param callback Callback to handle result
     */
    public void generateAnswer(WeatherData weatherData, String question, InsightsCallback callback) {
        generateAnswer(weatherData, question, null, callback);
    }

    /**
     * Construct a prompt for answering a user's question about weather.
     *
     * @param weatherData WeatherData to base the answer on
     * @param question User's question
     * @param cityName City name for location context (optional)
     * @return Formatted prompt string
     */
    private String constructAnswerPrompt(WeatherData weatherData, String question, String cityName) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a helpful weather assistant. Answer the following question ");
        prompt.append("based on the current weather conditions provided below.\n\n");

        if (cityName != null && !cityName.trim().isEmpty()) {
            prompt.append("Location: ").append(cityName).append("\n");
        }

        // Format temperature
        String tempStr = weatherData.getFormattedTemperature();
        if (tempStr == null || tempStr.isEmpty()) {
            tempStr = String.format("%.1f°F", weatherData.getTemperature() != null ? weatherData.getTemperature() : 0.0);
        }
        prompt.append("Temperature: ").append(tempStr);

        prompt.append("\nConditions: ");
        prompt.append(weatherData.getConditions() != null ? weatherData.getConditions() : "Unknown");

        prompt.append("\nHumidity: ");
        prompt.append(weatherData.getHumidity() != null ? weatherData.getHumidity() : 0);
        prompt.append("%");

        prompt.append("\nWind: ");
        if (weatherData.getWindSpeed() != null) {
            prompt.append(String.format("%.1f", weatherData.getWindSpeed()));
        } else {
            prompt.append("0");
        }
        prompt.append(" mph");

        prompt.append("\n\nQuestion: ").append(question);
        prompt.append("\n\nProvide a clear, concise, and helpful answer. Be specific and practical.");

        return prompt.toString();
    }

    /**
     * Generate a theme from natural language description using LLM.
     *
     * This method is used during user registration to create custom themes
     * based on natural language descriptions like "summer beach" or "cyberpunk nightscape".
     *
     * Steps:
     * 1. Construct prompt asking for JSON with backgroundColor and textColor
     * 2. Call Gemini API with the prompt
     * 3. Parse JSON response to extract colors
     * 4. Return Theme object with generated colors
     * 5. Handle API errors gracefully with default theme fallback
     *
     * @param themeDescription Natural language description of desired theme
     * @param callback Callback to handle result
     */
    public void generateTheme(String themeDescription, ThemeCallback callback) {
        if (apiKey == null || apiKey.isEmpty()) {
            android.util.Log.e("LLMHelper", "Gemini API key not configured");
            callback.onError("Gemini API key not configured");
            return;
        }

        if (themeDescription == null || themeDescription.trim().isEmpty()) {
            android.util.Log.e("LLMHelper", "Theme description cannot be empty");
            callback.onError("Theme description cannot be empty");
            return;
        }

        android.util.Log.d("LLMHelper", "Generating theme for: " + themeDescription);
        String prompt = constructThemePrompt(themeDescription);
        GeminiRequest request = new GeminiRequest(prompt);

        geminiApiClient.generateContent(request, apiKey)
            .enqueue(new Callback<GeminiResponse>() {
                @Override
                public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String generatedText = response.body().getGeneratedText();
                        android.util.Log.d("LLMHelper", "LLM Response: " + generatedText);
                        try {
                            Theme theme = parseThemeResponse(generatedText, themeDescription);
                            android.util.Log.d("LLMHelper", "Successfully parsed theme: bg=" + theme.getPrimaryColor() + ", text=" + theme.getSecondaryColor());
                            callback.onSuccess(theme);
                        } catch (Exception e) {
                            android.util.Log.e("LLMHelper", "Failed to parse LLM response: " + e.getMessage());
                            android.util.Log.e("LLMHelper", "Raw response: " + generatedText);
                            // Try to use cached LLM-generated default theme as fallback
                            useDefaultThemeFallback(themeDescription, callback, "Failed to parse LLM response: " + e.getMessage());
                        }
                    } else {
                        // API error, try to use cached default theme
                        String errorMsg = "API error: ";
                        if (response.errorBody() != null) {
                            try {
                                String errorBody = response.errorBody().string();
                                errorMsg += "HTTP " + response.code() + ": " + errorBody;
                                android.util.Log.e("LLMHelper", "API error details: " + errorMsg);
                            } catch (Exception e) {
                                errorMsg += "HTTP " + response.code() + ": " + response.errorBody().toString();
                            }
                        } else {
                            errorMsg += "HTTP " + response.code() + ": Unknown error";
                        }
                        android.util.Log.e("LLMHelper", errorMsg);
                        useDefaultThemeFallback(themeDescription, callback, errorMsg);
                    }
                }

                @Override
                public void onFailure(Call<GeminiResponse> call, Throwable t) {
                    // Network error, try to use cached default theme
                    android.util.Log.e("LLMHelper", "Network error: " + t.getMessage(), t);
                    useDefaultThemeFallback(themeDescription, callback, "Network error: " + t.getMessage());
                }
            });
    }

    /**
     * Construct a prompt for theme generation.
     *
     * @param themeDescription Natural language description
     * @return Formatted prompt string
     */
    private String constructThemePrompt(String themeDescription) {
        return String.format(
            "Generate a UI theme for '%s'. Return ONLY a JSON object with this exact format:\n" +
            "{\n" +
            "  \"backgroundColor\": \"#hexcolor\",\n" +
            "  \"textColor\": \"#hexcolor\"\n" +
            "}\n" +
            "Use appropriate colors that match the theme description. " +
            "Make sure the text color contrasts well with the background color.",
            themeDescription
        );
    }

    /**
     * Parse LLM response to extract theme colors.
     *
     * @param response Raw LLM response
     * @param themeDescription Original theme description
     * @return Theme object with parsed colors
     */
    private Theme parseThemeResponse(String response, String themeDescription) {
        // Extract JSON from response (remove any extra text)
        int jsonStart = response.indexOf("{");
        int jsonEnd = response.lastIndexOf("}");
        
        if (jsonStart == -1 || jsonEnd == -1) {
            throw new IllegalArgumentException("No JSON found in response");
        }
        
        String jsonString = response.substring(jsonStart, jsonEnd + 1);
        
        // Simple JSON parsing (for production, use Gson or similar)
        String backgroundColor = extractColorFromJson(jsonString, "backgroundColor");
        String textColor = extractColorFromJson(jsonString, "textColor");
        
        if (backgroundColor == null || textColor == null) {
            throw new IllegalArgumentException("Could not parse colors from JSON");
        }
        
        return new Theme(themeDescription, backgroundColor, textColor, false);
    }

    /**
     * Extract color value from JSON string.
     *
     * @param json JSON string
     * @param key Key to extract
     * @return Color value or null if not found
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
     * Use cached LLM-generated default theme as fallback.
     * 
     * This provides a compliant fallback mechanism:
     * - Uses LLM-generated themes (compliant)
     * - Cached for instant access
     * - No hardcoded colors
     *
     * @param themeDescription Original theme description
     * @param callback Callback to return theme or error
     * @param errorMessage Original error message
     */
    private void useDefaultThemeFallback(String themeDescription, ThemeCallback callback, String errorMessage) {
        android.util.Log.d("LLMHelper", "Attempting to use cached LLM-generated default theme for: " + themeDescription);
        
        if (defaultThemeGenerator == null) {
            android.util.Log.e("LLMHelper", "No default theme generator available, returning error");
            callback.onError(errorMessage);
            return;
        }
        
        Theme defaultTheme = defaultThemeGenerator.getDefaultTheme(themeDescription);
        if (defaultTheme != null) {
            android.util.Log.d("LLMHelper", "Using cached LLM-generated default theme (compliant): " + themeDescription);
            callback.onSuccess(defaultTheme);
        } else {
            android.util.Log.w("LLMHelper", "No cached default theme available, returning error");
            callback.onError(errorMessage + ". Note: Default themes need to be initialized first.");
        }
    }

    /**
     * Callback interface for AI insights generation.
     *
     * This interface is used to notify when AI insights are successfully
     * generated from weather data or when an error occurs during generation.
     */
    public interface InsightsCallback {
        /**
         * Called when AI insights are successfully generated.
         *
         * @param insights The generated insights text from the LLM
         */
        void onSuccess(String insights);

        /**
         * Called when insights generation fails.
         *
         * @param errorMessage Error message describing the failure
         */
        void onError(String errorMessage);
    }

    /**
     * Callback interface for AI theme generation.
     *
     * This interface is used to notify when a custom theme is successfully
     * generated from a natural language description or when an error occurs.
     */
    public interface ThemeCallback {
        /**
         * Called when a custom theme is successfully generated.
         *
         * @param theme The generated Theme object with colors
         */
        void onSuccess(Theme theme);

        /**
         * Called when theme generation fails.
         *
         * @param errorMessage Error message describing the failure
         */
        void onError(String errorMessage);
    }
}
