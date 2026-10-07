package edu.uiuc.cs427app.network.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Request model for Google Gemini API.
 *
 * This class structures the JSON request body for the Gemini API.
 *
 * Example JSON request:
 * {
 *   "contents": [
 *     {
 *       "parts": [
 *         {
 *           "text": "Provide weather insights for 72°F, Sunny conditions"
 *         }
 *       ]
 *     }
 *   ]
 * }
 *
 * TODO: Team member assigned to LLM Integration:
 * - Construct prompts with weather data
 * - Experiment with different prompt formats for best insights
 * - Handle prompt length limits
 *
 * @author CS427 Team 407
 */
public class GeminiRequest {

    @SerializedName("contents")
    private List<Content> contents;

    /**
     * Create a Gemini API request with a text prompt.
     *
     * @param promptText The text prompt to send to the LLM
     */
    /**
     * Constructor.
     */
    public GeminiRequest(String promptText) {
        this.contents = new ArrayList<>();
        Content content = new Content();
        content.parts = new ArrayList<>();
        content.parts.add(new Part(promptText));
        this.contents.add(content);
    }

    // Nested classes for JSON structure

    public static class Content {
        @SerializedName("parts")
        private List<Part> parts;
    }

    public static class Part {
        @SerializedName("text")
        private String text;

        /**
         * Constructor.
         */
        public Part(String text) {
            this.text = text;
        }
    }

    // Getters
    public List<Content> getContents() {
        return contents;
    }
}
