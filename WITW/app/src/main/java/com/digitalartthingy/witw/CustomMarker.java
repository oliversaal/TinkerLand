/**
 *       Copyright (C) 2017 Digital Art Thingy Inc.
 */
package com.digitalartthingy.witw;

import com.amazon.geo.mapsv2.model.LatLng;

/**
 * Represents a custom marker.
 */
public class CustomMarker {
    private final String title;
    private final String address;
    private final String phone;
    private final LatLng location;

    public CustomMarker(final String title, final String address, final String phone, final LatLng location) {
        this.title = title;
        this.address = address;
        this.phone = phone;
        this.location = location;
    }

    public String getTitle() {
        return title;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public LatLng getLocation() {
        return location;
    }
}
