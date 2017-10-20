/**
 *       Copyright (C) 2017 Digital Art Thingy Inc.
 */
package com.digitalartthingy.witw;

import com.amazon.geo.mapsv2.model.LatLng;

/**
 * Represents a custom marker.
 */
public class CustomMarker {
    private String mTitle;
    private String mPhone;
    private final LatLng mLocation;

    public CustomMarker(final LatLng location) {
         mLocation = location;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getPhone() {
        return mPhone;
    }

    public LatLng getLocation() {
        return mLocation;
    }

    public void setMarkerDetails(String title, String phone) {
        mTitle = title;
        mPhone = phone;
    }

}
