package edu.uiuc.cs427app.network.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Response model for Google Gemini Image Generation API.
 *
 * This class maps the JSON response from the Gemini API when
 * generating images with response_modalities: ["image"].
 *
 * The response structure is similar to text generation but contains
 * Base64-encoded image data instead of text in the parts.
 *
 * Example JSON response:
 * {
 *   "candidates": [
 *     {
 *       "content": {
 *         "parts": [
 *           {
 *             "inlineData": {
 *               "mimeType": "image/png",
 *               "data": "iVBORw0KGgoAAAANSUhEUgAA..." (Base64 encoded image)
 *             }
 *           }
 *         ]
 *       }
 *     }
 *   ]
 * }
 *
 * The image data can be decoded and displayed directly in an ImageView.
 *
 * API Documentation:
 * https://ai.google.dev/gemini-api/docs/image-generation
 *
 * @author CS427 Team 407 - LLM Image Generation Member
 */
public class GeminiImageResponse {

    @SerializedName("candidates")
    private List<Candidate> candidates;

    // Nested classes for JSON structure

    /**
     * Candidate containing the generated content.
     */
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

    /**
     * Content containing parts with image data.
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
     * Part containing inline image data.
     *
     * Unlike text responses which have a "text" field,
     * image responses have an "inlineData" field containing
     * the Base64-encoded image.
     *
     * Note: Some API versions might also include a "text" field with
     * a description or error message. We parse both for robustness.
     */
    public static class Part {
        @SerializedName("inlineData")
        private InlineData inlineData;

        @SerializedName("text")
        private String text;

        /**
         * Get the InlineData.
         *
         * @return InlineData
         */
        public InlineData getInlineData() {
            return inlineData;
        }

        /**
         * Get the text (if present - usually for errors or mixed responses).
         *
         * @return Text content, or null if not present
         */
        public String getText() {
            return text;
        }
    }

    /**
     * Inline data containing Base64-encoded image and MIME type.
     */
    public static class InlineData {
        @SerializedName("mimeType")
        private String mimeType; // e.g., "image/png"

        @SerializedName("data")
        private String data; // Base64-encoded image data

        /**
         * Get the MimeType.
         *
         * @return MimeType (e.g., "image/png", "image/jpeg")
         */
        public String getMimeType() {
            return mimeType;
        }

        /**
         * Get the Data.
         *
         * @return Base64-encoded image data
         */
        public String getData() {
            return data;
        }
    }

    // Getters

    /**
     * Get the Candidates.
     *
     * @return Candidates list
     */
    public List<Candidate> getCandidates() {
        return candidates;
    }

    /**
     * Extract the Base64-encoded image data from the response.
     *
     * This is a convenience method to directly get the image data
     * without navigating through the nested structure.
     *
     * Usage example:
     * String base64Image = response.getImageData();
     * if (base64Image != null) {
     *     displayBase64Image(imageView, base64Image);
     * }
     *
     * @return Base64-encoded image data, or null if not available
     */
    public String getImageData() {
        if (candidates != null && !candidates.isEmpty()) {
            Candidate firstCandidate = candidates.get(0);
            if (firstCandidate.getContent() != null &&
                    firstCandidate.getContent().getParts() != null &&
                    !firstCandidate.getContent().getParts().isEmpty()) {

                Part firstPart = firstCandidate.getContent().getParts().get(0);
                if (firstPart.getInlineData() != null) {
                    return firstPart.getInlineData().getData();
                }
            }
        }
        return null;
    }

    /**
     * Get the MIME type of the generated image.
     *
     * Typically returns "image/png" or "image/jpeg".
     *
     * @return MIME type string, or null if not available
     */
    public String getImageMimeType() {
        if (candidates != null && !candidates.isEmpty()) {
            Candidate firstCandidate = candidates.get(0);
            if (firstCandidate.getContent() != null &&
                    firstCandidate.getContent().getParts() != null &&
                    !firstCandidate.getContent().getParts().isEmpty()) {

                Part firstPart = firstCandidate.getContent().getParts().get(0);
                if (firstPart.getInlineData() != null) {
                    return firstPart.getInlineData().getMimeType();
                }
            }
        }
        return null;
    }
}