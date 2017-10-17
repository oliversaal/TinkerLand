/**
 *       Copyright (C) 2017 Digital Art Thingy Inc.
 */
package com.digitalartthingy.witw;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * 
 * Store and retrieves marker details
 * 
 */
public class MarkerStorage implements UserSuppliedDetails {
    private static final String TAG = "MarkerStorage";
    private static final String PREF_FILE_NAME = "Markers";
    private static final String PREF_MARKER_KEY = "WITWMarkers";

    private final SharedPreferences mSharedPreferences;

    private final Context mContext;
    private final GoogleMap mMap;

    private final Map<Marker, CustomMarker> mMarkers = new HashMap<Marker, CustomMarker>();
    private final LatLngBounds.Builder mMarkerBounds = LatLngBounds.builder();

    private MarkerDetails mDetails;

    public MarkerStorage(final Context context, GoogleMap map) {
        mContext = context;
        mMap = map;

        mSharedPreferences = mContext.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);

        mDetails = new MarkerDetails(mContext, this);
    }

    private void loadMarkersFromLocalStorage() {
        Log.i(TAG, "Triggered loadMarkersFromLocalStorage");

        try {
            // Gson allow us to serialize and deserialize our custom objects so that it can be
            // saved to the local storage
            Gson gson = new Gson();

            String json = mSharedPreferences.getString(PREF_MARKER_KEY, null);
            if (json != null) {
                // gson.fromJson needs to know the type of data to deserialize
                // I had to research this online and I found a simple example on SO.
                Type listType = new TypeToken<ArrayList<CustomMarker>>(){}.getType();
                List<CustomMarker> listCustomMarkers = gson.fromJson(json, listType);

                // Iterate over the retrieved markers and add them to the map
                for (CustomMarker customMarker : listCustomMarkers) {
                    addNewMarker(customMarker);
                }
            }
        } catch (Exception ex){
            Log.e(TAG,"Failed to deserialize and retrieve markers");
        }
    }

    public void saveMarkersToLocalStorage() {
        Log.i(TAG, "Triggered saveMarkersToLocalStorage");

        try {
            // Gson allow us to serialize and deserialize our custom objects so that it can be
            // saved to the local storage
            Gson gson = new Gson();

            // JSON gives us a string representation that can represent simple primitive types
            // as well as an array of primitive types. In this case our List<CustomMarkers> is
            // represented as a JSON string and we need to convert it back to a list.
            String json = gson.toJson(mMarkers.values().toArray());
            if (json != null) {
                SharedPreferences.Editor preferencesEditor = mSharedPreferences.edit();
                preferencesEditor.putString(PREF_MARKER_KEY, json);
                preferencesEditor.apply();
            }
        } catch (Exception ex) {
            Log.e(TAG,"Failed to serialize and save markers");
        }
    }

    public void addNewMarker(final CustomMarker customMarker) {
        Log.i(TAG, "Triggered addNewMarker");

        final BitmapDescriptor markerIcon = BitmapDescriptorFactory
                .fromResource(R.drawable.gastbyflag);

        MarkerOptions options = new MarkerOptions()
                .icon(markerIcon)
                .position(customMarker.getLocation())
                .title(customMarker.getTitle());

        // Update the map with the new marker and associate it with the custom marker
        Marker mapMarker = mMap.addMarker(options);
        mMarkers.put(mapMarker, customMarker);

        // Extending the bounding box to include the new location
        mMarkerBounds.include(customMarker.getLocation());
    }

    public void removeMarkers() {
        Log.i(TAG, "Triggered removeMarkers");

        // Remove the old markers, if any.
        for (Marker marker : mMarkers.keySet()) {
            marker.remove();
        }

        // Clear our list of markers
        mMarkers.clear();

        // Remove markers from map
        mMap.clear();
    }

    public LatLngBounds displayMarkers() {
        Log.i(TAG, "Triggered displayMarkers");

        // Clear existing markers.
        removeMarkers();

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if (mMarkers.containsKey(marker)) {
                    mDetails.display(mMarkers.get(marker));
                }
            }
        });

        // Retrieve stored markers and add them to the map
        loadMarkersFromLocalStorage();

        // CAVEAT: For some reason if we don't have any previous markers, we insert one to prevent
        // LatLngBounds from throwing an exception when building the bounding box. Since we're
        // looking for coffee, we zoom in on Starbucks HQ
        if (mMarkers.isEmpty()) {
            mMarkerBounds.include(new LatLng(47.58181, -122.33534));
        }

        // Calculate the bounds that encapsulate all the markers
        return mMarkerBounds.build();
    }

    @Override
    public void signalCompletion() {
        // TODO: This seems inefficient to save the entire map for each new addition - maybe batch them
        saveMarkersToLocalStorage();
    }
}
