/**
 *       Copyright (C) 2017 Digital Art Thingy Inc.
 */
package com.digitalartthingy.witw;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.amazon.geo.mapsv2.AmazonMap;
import com.amazon.geo.mapsv2.AmazonMap.OnInfoWindowClickListener;
import com.amazon.geo.mapsv2.model.BitmapDescriptor;
import com.amazon.geo.mapsv2.model.BitmapDescriptorFactory;
import com.amazon.geo.mapsv2.model.LatLng;
import com.amazon.geo.mapsv2.model.LatLngBounds;
import com.amazon.geo.mapsv2.model.Marker;
import com.amazon.geo.mapsv2.model.MarkerOptions;

/**
 * 
 * Fetches marker details
 * 
 */
public class MarkerFetcher {
    final Map<Marker, CustomMarker> mMarkers = new HashMap<Marker, CustomMarker>();
    LatLngBounds.Builder mMarkerBounds = LatLngBounds.builder();

    private static final CustomMarker[] COFFEE_SHOPS_MARKERS = {
            new CustomMarker("Joe Bar Cafe", "810 East Roy Street",
                    "(206) 324-0407", new LatLng(47.625148, -122.321623)),
            new CustomMarker("Roy Street Coffee and Tea",
                    "700 Broadway Ave East", "(206) 325-2211", new LatLng(
                            47.625206, -122.321108)),
            new CustomMarker("Vivace Espresso Bar", "532 Broadway Ave East",
                    "(206) 860-2722", new LatLng(47.623749, -122.320861)),
            new CustomMarker("Dilettante Mocha Cafe", "538 Broadway Ave East",
                    "(206) 329-6463", new LatLng(47.623922, -122.320861)),
            new CustomMarker("Starbucks", "700 Broadway Ave East",
                    "(206) 325-2211", new LatLng(47.625495, -122.321287)),
            new CustomMarker("Starbucks", "434 Broadway Ave East",
                    "(206) 323-7888", new LatLng(47.623058, -122.320792)),
            new CustomMarker("Cafe Canape", "700 Broadway Ave East",
                    "(206) 708-1210", new LatLng(47.625401, -122.320791))
    };

    public void loadMarkersFromLocalStorage(AmazonMap map) {
        // TODO: Load the markers from the local storage
        // Iterate over the coffee shop markers and add them to the hash map
        for (CustomMarker customMarker : COFFEE_SHOPS_MARKERS) {
            addNewMarker(customMarker, map);
        }
    }

    public void addNewMarker(CustomMarker customMarker, AmazonMap map) {
        final BitmapDescriptor coffeeMarkerIcon = BitmapDescriptorFactory
                .fromResource(R.drawable.coffeeshop);

        MarkerOptions coffeeMarkerOptions = new MarkerOptions()
                .icon(coffeeMarkerIcon)
                .position(customMarker.getLocation())
                .title(customMarker.getTitle())
                .snippet(customMarker.getAddress());

        // Update the map with the new marker and associate it with the custom marker
        Marker mapMarker = map.addMarker(coffeeMarkerOptions);
        mMarkers.put(mapMarker, customMarker);

        // Extending the bounding box to include the new location
        mMarkerBounds.include(customMarker.getLocation());
    }

    public void removeMarkers() {
        // Remove the old markers, if any.
        for (Marker marker : mMarkers.keySet()) {
            marker.remove();
        }

        // Clear our list of markers
        mMarkers.clear();
    }

    public LatLngBounds addMarkers(final Context context, AmazonMap map) {
        // Clear existing markers.
        removeMarkers();

        map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
            MarkerDetails details = new MarkerDetails();

            @Override
            public void onInfoWindowClick(Marker marker) {
                if (mMarkers.containsKey(marker)) {
                    details.display(context, mMarkers.get(marker));
                }
            }
        });

        // Retrieve stored markers and add them to the map
        loadMarkersFromLocalStorage(map);

        // Calculate the bounds that encapsulate all the markers
        return mMarkerBounds.build();
    }
}
