package edu.uiuc.cs427app.network.model;

import com.google.gson.annotations.SerializedName;

/**
 * Response model for OpenWeatherMap API current weather endpoint.
 *
 * This class maps the JSON response from the API to Java objects.
 * Gson will automatically parse the JSON into this structure.
 *
 * Example JSON response:
 * {
 *   "main": {
 *     "temp": 72.5,
 *     "humidity": 65
 *   },
 *   "weather": [
 *     {
 *       "main": "Clear",
 *       "description": "clear sky"
 *     }
 *   ],
 *   "wind": {
 *     "speed": 5.1
 *   }
 * }
 *
 * TODO: Team member assigned to Weather Integration:
 * - Verify JSON structure matches OpenWeatherMap API response
 * - Add any additional fields needed (pressure, visibility, etc.)
 * - Implement helper methods to convert to WeatherData entity
 *
 * @author CS427 Team 407
 */
public class WeatherResponse {

    @SerializedName("main")
    private Main main;

    @SerializedName("weather")
    private Weather[] weather;

    @SerializedName("wind")
    private Wind wind;

    @SerializedName("dt")
    private long dt; // Unix timestamp (seconds since epoch)

    @SerializedName("timezone")
    private int timezone; // Timezone offset in seconds from UTC

    // Nested class for main weather data
    public static class Main {
        @SerializedName("temp")
        private double temperature;

        @SerializedName("humidity")
        private int humidity;

        /**
         * Get the Temperature.
         *
         * @return Temperature
         */
        public double getTemperature() {
            return temperature;
        }

        /**
         * Get the Humidity.
         *
         * @return Humidity
         */
        public int getHumidity() {
            return humidity;
        }
    }

    // Nested class for weather condition
    public static class Weather {
        @SerializedName("main")
        private String main;

        @SerializedName("description")
        private String description;

        /**
         * Get the Main.
         *
         * @return Main
         */
        public String getMain() {
            return main;
        }

        /**
         * Get the Description.
         *
         * @return Description
         */
        public String getDescription() {
            return description;
        }
    }

    // Nested class for wind data
    public static class Wind {
        @SerializedName("speed")
        private double speed;

        /**
         * Get the Speed.
         *
         * @return Speed
         */
        public double getSpeed() {
            return speed;
        }
    }

    // Getters
    /**
     * Get the Main.
     *
     * @return Main
     */
    public Main getMain() {
        return main;
    }

    public Weather[] getWeather() {
        return weather;
    }

    /**
     * Get the Wind.
     *
     * @return Wind
     */
    public Wind getWind() {
        return wind;
    }

    // Helper methods

    /**
     * Get temperature value.
     */
    /**
     * Get the Temperature.
     *
     * @return Temperature
     */
    public double getTemperature() {
        return main != null ? main.getTemperature() : 0.0;
    }

    /**
     * Get weather condition (e.g., "Clear", "Clouds").
     */
    /**
     * Get the Conditions.
     *
     * @return Conditions
     */
    public String getConditions() {
        return (weather != null && weather.length > 0) ? weather[0].getMain() : "Unknown";
    }

    /**
     * Get humidity percentage.
     */
    /**
     * Get the Humidity.
     *
     * @return Humidity
     */
    public int getHumidity() {
        return main != null ? main.getHumidity() : 0;
    }

    /**
     * Get wind speed.
     */
    /**
     * Get the WindSpeed.
     *
     * @return WindSpeed
     */
    public double getWindSpeed() {
        return wind != null ? wind.getSpeed() : 0.0;
    }

    /**
     * Get the Unix timestamp (seconds since epoch).
     *
     * @return Unix timestamp
     */
    public long getDt() {
        return dt;
    }

    /**
     * Get the timezone offset in seconds from UTC.
     *
     * @return Timezone offset in seconds
     */
    public int getTimezone() {
        return timezone;
    }
}
