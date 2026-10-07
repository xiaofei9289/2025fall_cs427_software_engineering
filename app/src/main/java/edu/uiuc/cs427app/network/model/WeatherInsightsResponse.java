package edu.uiuc.cs427app.network.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Response model for weather insights JSON from LLM.
 *
 * This class maps the JSON response containing weather-related questions.
 *
 * Example JSON response:
 * {
 *   "questions": [
 *     "Should I wear a jacket or light layers today?",
 *     "Is it safe to go for a long walk or run outside?"
 *   ]
 * }
 *
 * @author CS427 Team 407
 */
public class WeatherInsightsResponse {

    @SerializedName("questions")
    private List<String> questions;

    /**
     * Default constructor.
     */
    public WeatherInsightsResponse() {
    }

    /**
     * Constructor with questions.
     *
     * @param questions List of weather-related questions
     */
    public WeatherInsightsResponse(List<String> questions) {
        this.questions = questions;
    }

    /**
     * Get the list of questions.
     *
     * @return List of questions, or null if not set
     */
    public List<String> getQuestions() {
        return questions;
    }

    /**
     * Set the list of questions.
     *
     * @param questions List of questions
     */
    public void setQuestions(List<String> questions) {
        this.questions = questions;
    }

    /**
     * Check if the response is valid.
     *
     * @return true if questions list is not null and not empty
     */
    public boolean isValid() {
        return questions != null && !questions.isEmpty();
    }

    /**
     * Get the number of questions.
     *
     * @return Number of questions, or 0 if questions is null
     */
    public int getQuestionCount() {
        return questions != null ? questions.size() : 0;
    }

    /**
     * Format the questions as a user-friendly string.
     *
     * @return Formatted string with questions, or empty string if no questions
     */
    public String formatForDisplay() {
        if (questions == null || questions.isEmpty()) {
            return "";
        }

        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < questions.size(); i++) {
            formatted.append("• ").append(questions.get(i));
            if (i < questions.size() - 1) {
                formatted.append("\n\n");
            }
        }
        return formatted.toString();
    }

    @Override
    public String toString() {
        return "WeatherInsightsResponse{" +
                "questions=" + questions +
                ", questionCount=" + getQuestionCount() +
                '}';
    }
}

