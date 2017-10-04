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
    final Map<Marker, CustomMarker> markers = new HashMap<Marker, CustomMarker>();

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

    public void removeMarkers() {
        // Remove the old markers, if any.
        for (Marker marker : markers.keySet()) {
            marker.remove();
        }

        // Clear our list of markers
        markers.clear();
    }

    public LatLngBounds addMarkers(final Context context, AmazonMap map) {
        // Clear existing markers.
        removeMarkers();

        map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
            MarkerDetails details = new MarkerDetails();

            @Override
            public void onInfoWindowClick(Marker marker) {
                if (markers.containsKey(marker)) {
                    details.display(context, markers.get(marker));
                }
            }
        });

        // Add markers
        final BitmapDescriptor coffeeMarkerIcon = BitmapDescriptorFactory
                .fromResource(R.drawable.coffeeshop);
        MarkerOptions coffeeMarkerOptions = new MarkerOptions()
                .icon(coffeeMarkerIcon);

        LatLngBounds.Builder markerBounds = LatLngBounds.builder();

        for (CustomMarker marker : COFFEE_SHOPS_MARKERS) {
            coffeeMarkerOptions.position(marker.getLocation())
                    .title(marker.getTitle()).snippet(marker.getAddress());
            Marker newMarker = map.addMarker(coffeeMarkerOptions);

            // Associate the marker with the shop.
            markers.put(newMarker, marker);
            markerBounds.include(marker.getLocation());
        }

        // Calculate the bounds that encapsulate all the markers
        return markerBounds.build();
    }
}
