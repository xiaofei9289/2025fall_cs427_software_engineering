package edu.uiuc.cs427app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.uiuc.cs427app.R;
import edu.uiuc.cs427app.data.entity.City;
import edu.uiuc.cs427app.data.entity.Theme;

import android.graphics.Color;

/**
 * CityAdapter - RecyclerView adapter for displaying list of cities (Iteration 3).
 *
 * This adapter displays the user's list of cities in the CityListFragment.
 * Each item shows the city name and a "SHOW DETAILS" button.
 *
 * TODO: Team member xincai3/twwang2 to implement:
 * 1. Implement onBindViewHolder to display city name
 * 2. Implement click listener for "SHOW DETAILS" button
 *    - Navigate to DetailsFragment with city data
 * 3. Implement long-press listener for city deletion
 *    - Show confirmation dialog
 *    - Call delete callback
 * 4. Implement updateCities() method to refresh data
 *
 * @author CS427 Team 407
 */
public class CityAdapter extends RecyclerView.Adapter<CityAdapter.CityViewHolder> {

    private List<City> cities = new ArrayList<>();
    private Theme currentTheme;
    private int textColor = Color.BLACK;

    private OnCityClickListener clickListener;

    /**
     * Interface for handling city item clicks.
     */
    public interface OnCityClickListener {
        /**
         * Called when "SHOW DETAILS" button is clicked.
         *
         * @param city The city that was clicked
         */
        void onShowDetailsClick(City city);

        /**
         * Called when city item is long-pressed (for deletion).
         *
         * @param city The city to be deleted
         */
        void onCityLongPress(City city);
    }

    /**
     * Set the click listener for city items.
     *
     * @param listener The click listener
     */
    public void setOnCityClickListener(OnCityClickListener listener) {
        this.clickListener = listener;
    }

    /**
     * Set the current theme for city items.
     *
     * @param theme Theme object with colors
     */
    public void setTheme(Theme theme) {
        this.currentTheme = theme;
        if (theme != null) {
            try {
                this.textColor = Color.parseColor(theme.getSecondaryColor());
                notifyDataSetChanged();
            } catch (Exception e) {
                // Keep default color on error
            }
        }
    }

    /**
     * Update the list of cities and refresh the RecyclerView.
     *
     * TODO: xincai3/twwang2 - Implement this method
     * @param newCities The new list of cities
     */
    public void updateCities(List<City> newCities) {
        // TODO: Update cities list and call notifyDataSetChanged()
        this.cities = newCities;
        notifyDataSetChanged();
    }

    /**
     * Called when RecyclerView needs a new ViewHolder.
     *
     * @param parent The ViewGroup into which the new View will be added
     * @param viewType The view type of the new View
     * @return A new CityViewHolder that holds a View of the given view type
     */
    @NonNull
    @Override
    public CityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_city, parent, false);
        return new CityViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param holder The ViewHolder which should be updated
     * @param position The position of the item within the adapter's data set
     */
    @Override
    public void onBindViewHolder(@NonNull CityViewHolder holder, int position) {
        City city = cities.get(position);

        // TODO: xincai3/twwang2 - Display city name
        holder.tvCityName.setText(city.getName());

        holder.tvCityName.setTextColor(textColor);

        // TODO: xincai3/twwang2 - Implement "SHOW DETAILS" button click
        holder.btnShowDetails.setOnClickListener(v -> {
            android.util.Log.d("CityAdapter", "Show details button clicked for city: " + city.getName());
            if (clickListener != null) {
                android.util.Log.d("CityAdapter", "Calling clickListener.onShowDetailsClick");
                clickListener.onShowDetailsClick(city);
            } else {
                android.util.Log.e("CityAdapter", "clickListener is NULL!");
            }
        });

        // Delete button click listener - triggers same confirmation dialog as long-press
        holder.btnDelete.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onCityLongPress(city);
            }
        });

        // TODO: xincai3/twwang2 - Implement long-press for deletion (backup method)
        holder.itemView.setOnLongClickListener(v -> {
            if (clickListener != null) {
                clickListener.onCityLongPress(city);
                return true;
            }
            return false;
        });
    }

    /**
     * Returns the total number of items in the data set.
     *
     * @return The total number of cities in the list
     */
    @Override
    public int getItemCount() {
        return cities.size();
    }

    /**
     * ViewHolder for city items.
     *
     * Holds references to the UI elements for each city item in the RecyclerView.
     */
    static class CityViewHolder extends RecyclerView.ViewHolder {
        TextView tvCityName;
        ImageButton btnDelete;
        Button btnShowDetails;

        /**
         * Constructor for CityViewHolder.
         *
         * @param itemView The view representing a single city item
         */
        public CityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCityName = itemView.findViewById(R.id.tvCityName);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnShowDetails = itemView.findViewById(R.id.btnShowDetails);
        }
    }
}
