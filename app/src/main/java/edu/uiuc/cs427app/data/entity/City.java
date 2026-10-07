package edu.uiuc.cs427app.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import android.util.Pair;

import java.util.Date;

/**
 * City entity for Room database.
 * Represents a city saved by a user.
 *
 * Following Iteration 2 Class Diagram design.
 * Relationship: User (1) -- (*) City
 *
 * @author CS427 Team 407
 */
@Entity(tableName = "cities",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "user_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("user_id")})
public class City {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private Long id;

    @ColumnInfo(name = "user_id")
    private Long userId; // Foreign key to User

    @ColumnInfo(name = "name")
    private String name; // City name (e.g., "Chicago")

    @ColumnInfo(name = "latitude")
    private Double latitude;

    @ColumnInfo(name = "longitude")
    private Double longitude;

    @ColumnInfo(name = "added_at")
    private Date addedAt;

    // Constructors

    /**
     * Default constructor. Sets addedAt to current date.
     */
    public City() {
        this.addedAt = new Date();
    }

    /**
     * Constructor with required fields.
     *
     * @param userId ID of the user who owns this city
     * @param name City name
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     */
    @Ignore
    public City(Long userId, String name, Double latitude, Double longitude) {
        this();
        this.userId = userId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and Setters

    /**
     * Get the city ID.
     *
     * @return City ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Set the city ID.
     *
     * @param id City ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get the user ID who owns this city.
     *
     * @return User ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Set the user ID who owns this city.
     *
     * @param userId User ID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Get the city name.
     *
     * @return City name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the city name.
     *
     * @param name City name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the latitude coordinate.
     *
     * @return Latitude
     */
    public Double getLatitude() {
        return latitude;
    }

    /**
     * Set the latitude coordinate.
     *
     * @param latitude Latitude
     */
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    /**
     * Get the longitude coordinate.
     *
     * @return Longitude
     */
    public Double getLongitude() {
        return longitude;
    }

    /**
     * Set the longitude coordinate.
     *
     * @param longitude Longitude
     */
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    /**
     * Get the date when city was added.
     *
     * @return Date added
     */
    public Date getAddedAt() {
        return addedAt;
    }

    /**
     * Set the date when city was added.
     *
     * @param addedAt Date added
     */
    public void setAddedAt(Date addedAt) {
        this.addedAt = addedAt;
    }

    // Business Logic Methods

    /**
     * Get the coordinates as a Pair for convenience.
     *
     * @return Pair of (latitude, longitude)
     */
    public Pair<Double, Double> getCoordinates() {
        return new Pair<>(latitude, longitude);
    }

    @Override
    public String toString() {
        return "City{" +
                "id=" + id +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", addedAt=" + addedAt +
                '}';
    }
}
