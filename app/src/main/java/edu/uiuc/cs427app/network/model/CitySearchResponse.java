package edu.uiuc.cs427app.network.model;

import com.google.gson.annotations.SerializedName;

/**
 * Response model for OpenWeatherMap Geocoding API.
 *
 * This class maps the JSON response to get city coordinates.
 *
 * Example JSON response:
 * [
 *   {
 *     "name": "Chicago",
 *     "lat": 41.8781,
 *     "lon": -87.6298,
 *     "country": "US",
 *     "state": "Illinois"
 *   }
 * ]
 *
 * Note: The API returns an array, but we typically only use the first result.
 *
 * TODO: Team member assigned to City Management:
 * - Use this to validate city names when user adds a city
 * - Extract latitude and longitude for storing in database
 * - Handle cases where city is not found (empty array)
 *
 * @author CS427 Team 407
 */
public class CitySearchResponse {

    @SerializedName("name")
    private String name;

    @SerializedName("lat")
    private double latitude;

    @SerializedName("lon")
    private double longitude;

    @SerializedName("country")
    private String country;

    @SerializedName("state")
    private String state;

    // Getters
    /**
     * Get the Name.
     *
     * @return Name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the Latitude.
     *
     * @return Latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Get the Longitude.
     *
     * @return Longitude
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Get the Country.
     *
     * @return Country
     */
    public String getCountry() {
        return country;
    }

    /**
     * Get the State.
     *
     * @return State
     */
    public String getState() {
        return state;
    }

    /**
     * Get formatted city name with state and country.
     *
     * Example: "Chicago, Illinois, US"
     */
    /**
     * Get the FullName.
     *
     * @return FullName
     */
    public String getFullName() {
        StringBuilder sb = new StringBuilder(name);
        if (state != null && !state.isEmpty()) {
            sb.append(", ").append(state);
        }
        if (country != null && !country.isEmpty()) {
            sb.append(", ").append(country);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "CitySearchResponse{" +
                "name='" + name + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", country='" + country + '\'' +
                ", state='" + state + '\'' +
                '}';
    }
}
