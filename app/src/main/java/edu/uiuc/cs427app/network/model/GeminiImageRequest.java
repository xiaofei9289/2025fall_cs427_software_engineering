package edu.uiuc.cs427app.network.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Request model for Google Gemini Image Generation API.
 *
 * This class structures the JSON request body for generating images
 * using the Gemini 2.0 Flash model with image output modality.
 *
 * Key differences from text generation:
 * - Uses "response_modalities": ["IMAGE"] as a TOP-LEVEL field (not nested in generationConfig)
 * - Uses gemini-2.0-flash-exp model (supports image generation)
 * - Returns Base64-encoded image data in response
 *
 * Example JSON request:
 * {
 *   "contents": [
 *     {
 *       "parts": [
 *         {
 *           "text": "A photorealistic landscape image of Chicago skyline..."
 *         }
 *       ]
 *     }
 *   ],
 *   "response_modalities": ["IMAGE"]
 * }
 *
 * CRITICAL: 
 * - "response_modalities" is a TOP-LEVEL field, NOT inside "generationConfig"
 * - Field name must use snake_case ("response_modalities") to satisfy the REST API
 * - Value must be "IMAGE" (uppercase) NOT "image" (lowercase)
 *
 * API Documentation:
 * https://ai.google.dev/gemini-api/docs/image-generation
 *
 * @author CS427 Team 407 - LLM Image Generation Member
 */
public class GeminiImageRequest {

    @SerializedName("contents")
    private List<Content> contents;

    @SerializedName("generationConfig")
    private GenerationConfig generationConfig;

    /**
     * Create a Gemini API request for image generation.
     *
     * This constructor sets up the request with:
     * - Text prompt describing the desired image
     * - generationConfig with temperature, topP, topK settings
     * - responseModalities set to ["IMAGE"] for Gemini 2.5 Flash Image model
     *
     * Note: Different Gemini models have different requirements:
     * - gemini-2.0-flash-preview-image-generation: Native image model, may not need responseModalities
     * - gemini-2.5-flash-image: Requires responseModalities: ["IMAGE"] in generationConfig
     * - gemini-2.0-flash-exp: Experimental model with flexible configuration
     *
     * @param promptText The text prompt describing the image to generate
     */
    public GeminiImageRequest(String promptText) {
        // Initialize contents with the prompt
        this.contents = new ArrayList<>();
        Content content = new Content();
        content.parts = new ArrayList<>();
        content.parts.add(new Part(promptText));
        this.contents.add(content);

        // Configure generation settings
        this.generationConfig = new GenerationConfig();
    }
    // Nested classes for JSON structure

    /**
     * Content wrapper containing parts of the request.
     */
    public static class Content {
        @SerializedName("parts")
        private List<Part> parts;

        /**
         * Get the Parts.
         *
         * @return Parts
         */
        public List<Part> getParts() {
            return parts;
        }
    }

    /**
     * Part containing the text prompt.
     */
    public static class Part {
        @SerializedName("text")
        private String text;

        /**
         * Constructor.
         *
         * @param text The prompt text
         */
        public Part(String text) {
            this.text = text;
        }

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

    /**
     * Get the Contents.
     *
     * @return Contents
     */
    public List<Content> getContents() {
        return contents;
    }

    /**
     * Get the GenerationConfig.
     *
     * @return GenerationConfig
     */
    public GenerationConfig getGenerationConfig() {
        return generationConfig;
    }

    /**
     * GenerationConfig for controlling image generation parameters.
     */
    public static class GenerationConfig {
        @SerializedName("temperature")
        private double temperature = 0.9;

        @SerializedName("topP")
        private double topP = 0.95;

        @SerializedName("topK")
        private int topK = 40;

        @SerializedName("responseModalities")
        private List<String> responseModalities;

        /**
         * Constructor initializes with IMAGE response modality for Gemini 2.5 Flash Image.
         */
        public GenerationConfig() {
            this.responseModalities = new ArrayList<>();
            this.responseModalities.add("IMAGE");
        }

        /**
         * Get the Temperature.
         *
         * @return Temperature
         */
        public double getTemperature() {
            return temperature;
        }

        /**
         * Set the Temperature.
         *
         * @param temperature Temperature value (0.0 to 2.0)
         */
        public void setTemperature(double temperature) {
            this.temperature = temperature;
        }

        /**
         * Get the TopP.
         *
         * @return TopP
         */
        public double getTopP() {
            return topP;
        }

        /**
         * Get the TopK.
         *
         * @return TopK
         */
        public int getTopK() {
            return topK;
        }

        /**
         * Get the ResponseModalities.
         *
         * @return ResponseModalities
         */
        public List<String> getResponseModalities() {
            return responseModalities;
        }
    }
}
