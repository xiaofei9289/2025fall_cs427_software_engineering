package edu.uiuc.cs427app.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import edu.uiuc.cs427app.data.dao.CityDao;
import edu.uiuc.cs427app.data.dao.UserDao;
import edu.uiuc.cs427app.data.dao.WeatherDao;
import edu.uiuc.cs427app.data.entity.City;
import edu.uiuc.cs427app.data.entity.Theme;
import edu.uiuc.cs427app.data.entity.User;
import edu.uiuc.cs427app.data.entity.WeatherData;

/**
 * Room Database configuration for the Weather App.
 *
 * This is a singleton class that provides access to DAOs.
 * Following Iteration 2 Class Diagram design.
 *
 * Entities: User, City, WeatherData, Theme
 * DAOs: UserDao, CityDao, WeatherDao
 *
 * @author CS427 Team 407
 */
@Database(
        entities = {User.class, City.class, WeatherData.class, Theme.class},
        version = 2,
        exportSchema = false
)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    // Database name
    private static final String DATABASE_NAME = "weather_app_database";

    // Singleton instance
    private static volatile AppDatabase INSTANCE;

    // Abstract DAO getters (Room will generate implementations)
    public abstract UserDao userDao();
    public abstract CityDao cityDao();
    public abstract WeatherDao weatherDao();

    /**
     * Get the singleton instance of the database.
     *
     * Uses double-check locking for thread safety.
     *
     * @param context Application context
     * @return AppDatabase instance
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME
                    )
                    // TODO: Remove this in production - allows main thread queries for testing
                    .allowMainThreadQueries()
                    // Destructive migration for schema changes during development
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Clear all data from all tables.
     *
     * WARNING: This deletes all user data. Use only for testing or logout.
     */
    public void clearAllTables() {
        if (INSTANCE != null) {
            INSTANCE.clearAllTables();
        }
    }

    /**
     * Close the database.
     *
     * Useful for testing or app shutdown.
     */
    public static void destroyInstance() {
        if (INSTANCE != null) {
            INSTANCE.close();
            INSTANCE = null;
        }
    }
}
