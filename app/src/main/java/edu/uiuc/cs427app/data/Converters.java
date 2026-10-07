package edu.uiuc.cs427app.data;

import androidx.room.TypeConverter;

import java.util.Date;

/**
 * Type converters for Room database.
 *
 * Room doesn't know how to store Date objects in SQLite,
 * so we convert them to/from Long (milliseconds since epoch).
 *
 * @author CS427 Team 407
 */
public class Converters {

    /**
     * Convert Date to Long (for storing in database).
     *
     * @param date Date to convert
     * @return Milliseconds since epoch, or null if date is null
     */
    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    /**
     * Convert Long to Date (for reading from database).
     *
     * @param timestamp Milliseconds since epoch
     * @return Date object, or null if timestamp is null
     */
    @TypeConverter
    public static Date timestampToDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }
}
