/**
 *       Copyright (C) 2017 Digital Art Thingy Inc.
 */
package com.digitalartthingy.witw;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import com.amazon.geo.mapsv2.*;
import com.amazon.geo.mapsv2.model.*;
import com.amazon.geo.mapsv2.util.*;

public class MainActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback,
        OnMapReadyCallback {
    private static final String TAG = "MainActivity";
    private static final String PRIVACY_POLICY_URL = "http://www.digitalartthingy.com/legal/privacy.html";
    private static final String ABOUT_URL = "http://www.digitalartthingy.com/WITW.html";

    // Permission request for location (support Android 6.0)
    private static final int MY_PERMISSIONS_REQUEST_READ_FINE_LOCATION = 101;

    /**
     * This web view is used to display web content such as the privacy policy
     */
    private WebView mWebView;

    /**
     * The support map fragment is used to display the map itself
     */
    private MapFragment mMapFragment;

    /**
     * The persisted AmazonMap object
     */
    private AmazonMap mMap;

    /**
     * The list of map markers
     */
    private MarkerStorage mMarkerStorage;


    /**
     * The zoom level needs to be remembered if the user decides to change it (default 15.0f - street level)
     */
    private static final float DEFAULT_ZOOM_LEVEL_PREFERENCE = 15.0f;
    private static float mZoomLevel = DEFAULT_ZOOM_LEVEL_PREFERENCE;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Base);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        Toolbar mainToolbar = (Toolbar)findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);

        // Create a web view so we can open web resources such as the privacy policy within the app
        mWebView = (WebView)findViewById(R.id.webView);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setBuiltInZoomControls(true);

        final Activity activity = this;
        mWebView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Log.i(TAG, description);
                Toast.makeText(activity, description, Toast.LENGTH_SHORT).show();
            }
        });

        // Get the map fragment
        mMapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.map);

        // For Android 6.0 and above, verify that we have sufficient privileges and permission to access location information
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{ Manifest.permission.ACCESS_FINE_LOCATION },
                    MY_PERMISSIONS_REQUEST_READ_FINE_LOCATION);
        } else {
            displayMap();
        }
    }

    private void displayMap() {
        int resultCode = AmazonMapsRuntimeUtil
                .isAmazonMapsRuntimeAvailable(getApplicationContext());
        boolean mapsRuntimeAvailable = resultCode == ConnectionResult.SUCCESS;

        if (mapsRuntimeAvailable) {
            if (mMapFragment != null) {
                mMapFragment.getMapAsync(MainActivity.this);
            } else {
                Log.i(TAG, "UNEXPECTED: mMapFragment is null");
            }
        } else {
            AmazonMapsRuntimeUtil.getErrorDialog(resultCode, this, 0).show();
        }
    }

    /**
     * Inflates the menu. This adds items to the action bar if it is present
     **/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // If we opened up the web view previously, hide it now
        mWebView.setVisibility(View.GONE);

        // Check which menu option was selected by the user
        switch (item.getItemId()) {
            case R.id.action_map:
                return true;
            case R.id.action_toggle_location_updates:
                if (mMap != null) {
                    boolean isEnabled = mMap.isMyLocationEnabled();
                    mMap.setMyLocationEnabled(!isEnabled);
                }
                return true;
            case R.id.action_reset_markers:
                mMarkerStorage.removeMarkers();
                return true;
            case R.id.action_privacy:
                return loadUrl(PRIVACY_POLICY_URL);
            case R.id.action_about:
                return loadUrl(ABOUT_URL);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     *  This method is required because Android 6.0 and above evaluate permissions at run time
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    displayMap();
                } else {
                    // Location settings are not satisfied. However, we have no way to fix this
                    String errorMessage = "Location settings are inadequate, and cannot be fixed here.";
                    Log.e(TAG, errorMessage);
                    Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @SuppressWarnings("SameReturnValue")
    private boolean loadUrl(String url) {
        Log.i(TAG, "Triggered loadUrl");

        mWebView.loadUrl(url);
        mWebView.setVisibility(View.VISIBLE);

        return true;
    }

    /**
     * Triggered when the AmazonMap object is returned
     */
    @Override
    public void onMapReady(AmazonMap map) {
        Log.i(TAG, "Triggered onMapReady");

        // Retain the map object since we've using it with new coordinates
        if (mMap == null) {
            mMap = map;

            // The camera is now update with the current GPS location
            mMap.setMyLocationEnabled(true);

            // Initialize our fetcher to retrieve markers
            mMarkerStorage = new MarkerStorage(this, mMap);

            // Populate the coffee shop markers on the map when clicked
            final ImageView coffeeImage = (ImageView)findViewById(R.id.find_coffee);
            coffeeImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                mMarkerStorage.saveMarkersToLocalStorage();
                }
            });

            // Set up a handler for adding markers
            mMap.setOnMapLongClickListener(new AmazonMap.OnMapLongClickListener() {
                 @Override
                 public void onMapLongClick(LatLng ll) {
                 //TODO: Pop up a MarkerDetails dialog to collect information about new marker
                 CustomMarker customMarker = new CustomMarker("newTitle", "newAddress", "newPhone", ll);
                 mMarkerStorage.addNewMarker(customMarker);
                 }
             });

            //
            // The following camera methods have been deprecated - we need to find alternates
            //

            // Remember the zoom level if the camera position is changed
            mMap.setOnCameraChangeListener(new AmazonMap.OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition cameraPosition) {
                // TODO: Once we move to higher minSDK then we can use setMaxZoomPreference instead
                if (cameraPosition.zoom != mZoomLevel) {
                    mZoomLevel = cameraPosition.zoom;
                }
                }
            });

            // Center the camera on the current position and adjust to the desired zoom level
            mMap.setOnMyLocationChangeListener(new AmazonMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(Location lastKnownLocation) {
                if (lastKnownLocation != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()), mZoomLevel));
                }
                }
            });

            // Reset the zoom level to street level when the MyLocation button is clicked
            mMap.setOnMyLocationButtonClickListener(new AmazonMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                mZoomLevel = DEFAULT_ZOOM_LEVEL_PREFERENCE;

                // Temporary location to invoke populating the coffee shop markers
                findCoffee();

                return false;
                }
            });
        }
    }

    private void findCoffee() {
        Log.i(TAG, "Triggered findCoffee");

        // Set center and zoom so that capitol hill is visible
        LatLngBounds coffeeBounds = mMarkerStorage.displayMarkers();

        // Animate the camera to the coffee shop markers.
        final CameraUpdate update = CameraUpdateFactory.newLatLngBounds(coffeeBounds, 100 /* cameraPadding */);
        mMap.animateCamera(update);
    }
}