package edu.uiuc.cs427app.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * WeatherData entity for Room database.
 * Represents cached weather information for a city.
 *
 * Following Iteration 2 Class Diagram design.
 * Relationship: City (1) -- (1) WeatherData
 *
 * @author CS427 Team 407
 */
@Entity(tableName = "weather_data",
        foreignKeys = @ForeignKey(
                entity = City.class,
                parentColumns = "id",
                childColumns = "city_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index(value = "city_id", unique = true)})
public class WeatherData {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private Long id;

    @ColumnInfo(name = "city_id")
    private Long cityId; // Foreign key to City

    @ColumnInfo(name = "temperature")
    private Double temperature; // In Celsius or Fahrenheit (based on API)

    @ColumnInfo(name = "conditions")
    private String conditions; // e.g., "Sunny", "Cloudy", "Rainy"

    @ColumnInfo(name = "humidity")
    private Integer humidity; // Percentage (0-100)

    @ColumnInfo(name = "wind_speed")
    private Double windSpeed; // In m/s or mph (based on API)

    @ColumnInfo(name = "timestamp")
    private Date timestamp; // When the data was fetched

    // Cache expiry time: 30 minutes
    private static final long CACHE_EXPIRY_MILLIS = TimeUnit.MINUTES.toMillis(30);

    // Constructors
    /**
     * Constructor.
     */
    public WeatherData() {
        this.timestamp = new Date();
    }

    /**
     * Constructor.
     */
    @Ignore
    public WeatherData(Long cityId, Double temperature, String conditions, Integer humidity, Double windSpeed) {
        this();
        this.cityId = cityId;
        this.temperature = temperature;
        this.conditions = conditions;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
    }

    // Getters and Setters
    /**
     * Get the Id.
     *
     * @return Id
     */
    public Long getId() {
        return id;
    }

    /**
     * Set the Id.
     *
     * @param id Id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get the CityId.
     *
     * @return CityId
     */
    public Long getCityId() {
        return cityId;
    }

    /**
     * Set the CityId.
     *
     * @param cityId CityId
     */
    public void setCityId(Long cityId) {
        this.cityId = cityId;
    }

    /**
     * Get the Temperature.
     *
     * @return Temperature
     */
    public Double getTemperature() {
        return temperature;
    }

    /**
     * Set the Temperature.
     *
     * @param temperature Temperature
     */
    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    /**
     * Get the Conditions.
     *
     * @return Conditions
     */
    public String getConditions() {
        return conditions;
    }

    /**
     * Set the Conditions.
     *
     * @param conditions Conditions
     */
    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    /**
     * Get the Humidity.
     *
     * @return Humidity
     */
    public Integer getHumidity() {
        return humidity;
    }

    /**
     * Set the Humidity.
     *
     * @param humidity Humidity
     */
    public void setHumidity(Integer humidity) {
        this.humidity = humidity;
    }

    /**
     * Get the WindSpeed.
     *
     * @return WindSpeed
     */
    public Double getWindSpeed() {
        return windSpeed;
    }

    /**
     * Set the WindSpeed.
     *
     * @param windSpeed WindSpeed
     */
    public void setWindSpeed(Double windSpeed) {
        this.windSpeed = windSpeed;
    }

    /**
     * Get the Timestamp.
     *
     * @return Timestamp
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Set the Timestamp.
     *
     * @param timestamp Timestamp
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    // Business Logic Methods

    /**
     * Check if the cached weather data is stale (older than 30 minutes).
     *
     * @return true if data is stale and should be refreshed
     */
    public boolean isStale() {
        long currentTime = System.currentTimeMillis();
        long dataTime = timestamp.getTime();
        return (currentTime - dataTime) > CACHE_EXPIRY_MILLIS;
    }

    /**
     * Get formatted temperature string with degree symbol.
     *
     * @return Formatted temperature (e.g., "72.5°F")
     */
    /**
     * Get the FormattedTemperature.
     *
     * @return FormattedTemperature
     */
    public String getFormattedTemperature() {
        DecimalFormat df = new DecimalFormat("#.#");
        return df.format(temperature) + "°";
    }

    @Override
    public String toString() {
        return "WeatherData{" +
                "id=" + id +
                ", cityId=" + cityId +
                ", temperature=" + temperature +
                ", conditions='" + conditions + '\'' +
                ", humidity=" + humidity +
                ", windSpeed=" + windSpeed +
                ", timestamp=" + timestamp +
                ", isStale=" + isStale() +
                '}';
    }
}
