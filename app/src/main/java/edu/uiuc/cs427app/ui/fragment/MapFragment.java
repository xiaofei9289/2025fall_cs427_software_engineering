package edu.uiuc.cs427app.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import edu.uiuc.cs427app.R;

/**
 * MapFragment - Displays an interactive Google Map for a city location.
 *
 * This fragment shows the city's location on an interactive map with a marker,
 * along with the city name and coordinates. Users can zoom, pan, and interact
 * with the map.
 *
 * Requirements implemented:
 * - Display city name (Iteration 4 requirement)
 * - Display latitude and longitude (Iteration 4 requirement)
 * - Interactive map with marker
 * - Proper error handling and loading states
 *
 * @author CS427 Team 407
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "MapFragment";
    private static final float DEFAULT_ZOOM = 10f;
    private static final String ARG_LATITUDE = "latitude";
    private static final String ARG_LONGITUDE = "longitude";
    private static final String ARG_CITY_NAME = "cityName";

    private MapView mapView;
    private GoogleMap googleMap;
    private TextView tvCityName;
    private TextView tvCoordinates;
    private ProgressBar progressBar;
    private TextView tvError;

    private double latitude;
    private double longitude;
    private String cityName;

    /**
     * Factory method to create a new instance of MapFragment.
     *
     * This is the recommended way to create Fragment instances with arguments,
     * as it ensures proper handling of configuration changes.
     *
     * @param latitude  The latitude coordinate of the city (-90 to 90)
     * @param longitude The longitude coordinate of the city (-180 to 180)
     * @param cityName  The name of the city to display
     * @return A new MapFragment instance configured with the provided location data
     */
    public static MapFragment newInstance(double latitude, double longitude, String cityName) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putDouble(ARG_LATITUDE, latitude);
        args.putDouble(ARG_LONGITUDE, longitude);
        args.putString(ARG_CITY_NAME, cityName);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Called when the fragment is created.
     *
     * Retrieves and validates the city location data from the fragment arguments.
     * If arguments are missing or invalid, logs an error.
     *
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            latitude = getArguments().getDouble(ARG_LATITUDE, 0.0);
            longitude = getArguments().getDouble(ARG_LONGITUDE, 0.0);
            cityName = getArguments().getString(ARG_CITY_NAME, "Unknown Location");

            // Validate coordinates
            if (!isValidCoordinates(latitude, longitude)) {
                Log.e(TAG, "Invalid coordinates: lat=" + latitude + ", lng=" + longitude);
            }
        } else {
            Log.e(TAG, "No arguments provided to MapFragment");
            cityName = "Unknown Location";
        }
    }

    /**
     * Called to create the view hierarchy for this fragment.
     *
     * Inflates the layout, initializes UI components, and starts loading the map.
     * Displays city information and shows a loading indicator while the map loads.
     *
     * @param inflater           The LayoutInflater to inflate views
     * @param container          The parent view that this fragment's UI will be attached to
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * @return The root View for the fragment's UI
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Initialize UI components
        mapView = view.findViewById(R.id.mapView);
        tvCityName = view.findViewById(R.id.tvCityName);
        tvCoordinates = view.findViewById(R.id.tvCoordinates);
        progressBar = view.findViewById(R.id.progressBar);
        tvError = view.findViewById(R.id.tvError);

        // Display city information
        if (cityName != null && !cityName.isEmpty()) {
            tvCityName.setText(cityName);
        } else {
            tvCityName.setText("Unknown Location");
        }

        // Display coordinates with proper formatting
        tvCoordinates.setText(String.format("Lat: %.4f, Lng: %.4f", latitude, longitude));

        // Show loading indicator
        progressBar.setVisibility(View.VISIBLE);
        tvError.setVisibility(View.GONE);

        // Initialize map
        try {
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing map", e);
            showError("Failed to initialize map: " + e.getMessage());
        }

        return view;
    }

    /**
     * Called when the Google Map is ready to be used.
     *
     * Validates the map and coordinates, then adds a marker at the city location
     * and moves the camera to center on it. Handles errors gracefully if the map
     * fails to load or coordinates are invalid.
     *
     * @param map The GoogleMap instance that is ready for use
     */
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        // Hide loading indicator
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        if (map == null) {
            Log.e(TAG, "GoogleMap is null in onMapReady");
            showError("Failed to load map");
            return;
        }

        // Validate coordinates before using them
        if (!isValidCoordinates(latitude, longitude)) {
            Log.e(TAG, "Invalid coordinates: lat=" + latitude + ", lng=" + longitude);
            showError("Invalid coordinates");
            return;
        }

        try {
            googleMap = map;
            LatLng cityLocation = new LatLng(latitude, longitude);

            // Enable zoom controls for better interactivity demonstration
            googleMap.getUiSettings().setZoomControlsEnabled(true);

            // Add marker with city name
            String markerTitle = (cityName != null && !cityName.isEmpty()) ? cityName : "Location";
            googleMap.addMarker(new MarkerOptions()
                    .position(cityLocation)
                    .title(markerTitle));

            // Move camera to city location with animation
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cityLocation, DEFAULT_ZOOM));

            Log.d(TAG, "Map loaded successfully for " + cityName);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up map", e);
            showError("Failed to display location");
        }
    }

    /**
     * Validates that the given coordinates are within valid ranges.
     *
     * @param lat Latitude value to validate
     * @param lng Longitude value to validate
     * @return true if coordinates are valid, false otherwise
     */
    private boolean isValidCoordinates(double lat, double lng) {
        return lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180;
    }

    /**
     * Displays an error message to the user.
     *
     * @param message The error message to display
     */
    private void showError(String message) {
        if (tvError != null) {
            tvError.setText(message);
            tvError.setVisibility(View.VISIBLE);
        }
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    /**
     * Called when the fragment is visible and ready to start interacting with the user.
     *
     * Forwards the lifecycle event to the MapView to properly manage map resources.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    /**
     * Called when the fragment is no longer in the foreground.
     *
     * Forwards the lifecycle event to the MapView to pause map rendering
     * and save battery/resources.
     */
    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    /**
     * Called when the fragment is being destroyed.
     *
     * Cleans up map resources and clears references to prevent memory leaks.
     * This is critical for proper memory management.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
        }
        // Clear reference to help garbage collection
        googleMap = null;
    }

    /**
     * Called when the system is running low on memory.
     *
     * Forwards the event to the MapView so it can release cached data
     * and reduce memory usage.
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }

    /**
     * Called to save the current state before the fragment is destroyed.
     *
     * Forwards the state saving to the MapView to preserve map state
     * (camera position, zoom level, etc.) across configuration changes.
     *
     * @param outState Bundle to save state into
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }
    }
}
