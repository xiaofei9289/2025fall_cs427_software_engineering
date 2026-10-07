package edu.uiuc.cs427app.network;

import edu.uiuc.cs427app.network.model.GeminiRequest;
import edu.uiuc.cs427app.network.model.GeminiResponse;
import edu.uiuc.cs427app.network.model.GeminiImageRequest;
import edu.uiuc.cs427app.network.model.GeminiImageResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Retrofit API client interface for Google Gemini LLM API.
 *
 * This interface defines the endpoints for generating AI insights.
 *
 * TODO: Team member assigned to LLM Integration:
 * - Sign up for Google AI Studio at https://makersuite.google.com/app/apikey
 * - Add your API key to local.properties: GEMINI_API_KEY=your_key_here
 * - Implement prompt construction in LLMHelper
 * - Handle API rate limiting and errors
 * - Parse and format LLM responses for display
 *
 * Base URL: https://generativelanguage.googleapis.com/
 *
 * Following Iteration 2 Class Diagram design.
 *
 * @author CS427 Team 407
 */
public interface GeminiApiClient {

    /**
     * Generate AI content based on a prompt.
     *
     * Endpoint: POST /v1/models/gemini-2.5-pro:generateContent
     * Docs: https://ai.google.dev/api/rest/v1/models/generateContent
     *
     * Example usage:
     * GeminiRequest request = new GeminiRequest("Provide insights about this weather: 72°F, Sunny");
     * generateContent(request, "YOUR_API_KEY")
     *
     * @param request GeminiRequest object containing the prompt
     * @param apiKey  Google Gemini API key
     * @return Call<GeminiResponse> containing AI-generated text
     */
    @POST("v1/models/gemini-2.5-pro:generateContent")
    Call<GeminiResponse> generateContent(
            @Body GeminiRequest request,
            @Query("key") String apiKey
    );

    /**
     * Generate an image based on a text prompt using Gemini 2.5 Flash Image Generation.
     *
     * Endpoint: POST /v1beta/models/gemini-2.5-flash-image:generateContent
     * Docs: https://ai.google.dev/gemini-api/docs/image-generation
     *
     * This endpoint uses the Gemini 2.5 Flash Image model
     * which natively supports image generation.
     *
     * Note: Using v1beta endpoint as image generation is still in preview.
     * Model: gemini-2.5-flash-image (verified working model)
     *
     * Example usage:
     * GeminiImageRequest request = new GeminiImageRequest("A photorealistic landscape of Chicago");
     * generateImage(request, "YOUR_API_KEY")
     *
     * @param request GeminiImageRequest object containing the image prompt
     * @param apiKey  Google Gemini API key
     * @return Call<GeminiImageResponse> containing Base64-encoded image data
     */
    @POST("v1beta/models/gemini-2.5-flash-image:generateContent")
    Call<GeminiImageResponse> generateImage(
            @Body GeminiImageRequest request,
            @Query("key") String apiKey
    );
}
