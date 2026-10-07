package edu.uiuc.cs427app.network.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Response model for Google Gemini API.
 *
 * This class maps the JSON response from the Gemini API.
 *
 * Example JSON response:
 * {
 *   "candidates": [
 *     {
 *       "content": {
 *         "parts": [
 *           {
 *             "text": "The weather is pleasant with sunny conditions..."
 *           }
 *         ]
 *       }
 *     }
 *   ]
 * }
 *
 * TODO: Team member assigned to LLM Integration:
 * - Parse the generated text from the response
 * - Handle cases where generation fails
 * - Display insights in DetailsFragment
 *
 * @author CS427 Team 407
 */
public class GeminiResponse {

    @SerializedName("candidates")
    private List<Candidate> candidates;

    // Nested classes for JSON structure

    public static class Candidate {
        @SerializedName("content")
        private Content content;

        /**
         * Get the Content.
         *
         * @return Content
         */
        public Content getContent() {
            return content;
        }
    }

    public static class Content {
        @SerializedName("parts")
        private List<Part> parts;

        public List<Part> getParts() {
            return parts;
        }
    }

    public static class Part {
        @SerializedName("text")
        private String text;

        /**
         * Get the Text.
         *
         * @return Text
         */
        public String getText() {
            return text;
        }
    }

    // Getters
    public List<Candidate> getCandidates() {
        return candidates;
    }

    /**
     * Extract the generated text from the response.
     *
     * @return Generated text, or empty string if not available
     */
    /**
     * Get the GeneratedText.
     *
     * @return GeneratedText
     */
    public String getGeneratedText() {
        if (candidates != null && !candidates.isEmpty()) {
            Candidate firstCandidate = candidates.get(0);
            if (firstCandidate.getContent() != null &&
                firstCandidate.getContent().getParts() != null &&
                !firstCandidate.getContent().getParts().isEmpty()) {
                return firstCandidate.getContent().getParts().get(0).getText();
            }
        }
        return "";
    }
}
