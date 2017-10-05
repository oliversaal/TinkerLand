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

import com.amazon.geo.mapsv2.AmazonMap;
import com.amazon.geo.mapsv2.AmazonMap.OnInfoWindowClickListener;
import com.amazon.geo.mapsv2.model.BitmapDescriptor;
import com.amazon.geo.mapsv2.model.BitmapDescriptorFactory;
import com.amazon.geo.mapsv2.model.LatLng;
import com.amazon.geo.mapsv2.model.LatLngBounds;
import com.amazon.geo.mapsv2.model.Marker;
import com.amazon.geo.mapsv2.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * 
 * Store and retrieves marker details
 * 
 */
public class MarkerStorage {
    private static final String TAG = "MarkerStorage";
    private static final String PREF_FILE_NAME = "Markers";
    private static final String PREF_MARKER_KEY = "WITWMarkers";

    private final SharedPreferences mSharedPreferences;

    private final Context mContext;
    private final AmazonMap mMap;

    private final Map<Marker, CustomMarker> mMarkers = new HashMap<Marker, CustomMarker>();
    private final LatLngBounds.Builder mMarkerBounds = LatLngBounds.builder();

    public MarkerStorage(final Context context, AmazonMap map) {
        mContext = context;
        mMap = map;

        mSharedPreferences = mContext.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
    }

    private void loadMarkersFromLocalStorage() {
        Log.i(TAG, "Triggered loadMarkersFromLocalStorage");

        try {
            Gson gson = new Gson();

            String json = mSharedPreferences.getString(PREF_MARKER_KEY, null);
            if (json != null) {
                Type listType = new TypeToken<ArrayList<CustomMarker>>(){}.getType();
                 List<CustomMarker> listCustomMarkers = gson.fromJson(json, listType);

                // Iterate over the retrieved markers and add them to the hash map
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
            Gson gson = new Gson();

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

        final BitmapDescriptor coffeeMarkerIcon = BitmapDescriptorFactory
                .fromResource(R.drawable.coffeeshop);

        MarkerOptions coffeeMarkerOptions = new MarkerOptions()
                .icon(coffeeMarkerIcon)
                .position(customMarker.getLocation())
                .title(customMarker.getTitle())
                .snippet(customMarker.getAddress());

        // Update the map with the new marker and associate it with the custom marker
        Marker mapMarker = mMap.addMarker(coffeeMarkerOptions);
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

        mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
            final MarkerDetails details = new MarkerDetails();

            @Override
            public void onInfoWindowClick(Marker marker) {
                if (mMarkers.containsKey(marker)) {
                    details.display(mContext, mMarkers.get(marker));
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
}
