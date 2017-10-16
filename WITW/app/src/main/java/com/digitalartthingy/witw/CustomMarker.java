/**
 *       Copyright (C) 2017 Digital Art Thingy Inc.
 */
package com.digitalartthingy.witw;

import com.google.android.gms.maps.model.LatLng;

/**
 * Represents a custom marker.
 */
public class CustomMarker {
    private String title;
    private String address;
    private String phone;
    private final LatLng location;

    public CustomMarker(final LatLng location) {
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

    public void setmarkerDetails(String title, String phone) {
        this.title = title;
        this.phone = phone;
    }

}
