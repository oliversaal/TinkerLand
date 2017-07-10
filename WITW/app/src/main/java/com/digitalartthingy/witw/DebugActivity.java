/**
 *        Copyright (C) 2017 Digital Art Thingy Inc.
 */
package com.digitalartthingy.witw;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class DebugActivity extends AppCompatActivity implements
        ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private static final String TAG = "DebugActivity";

    /**
     * Constant used in the location settings dialog.
     */
    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    /**
     * Provides the entry point to Google Play services.
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000; // 5 seconds

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 5; // 1 second

    /**
     * Represents a geographical location.
     */
    private Location mCurrentLocation;

    private LocationRequest mLocationRequest;

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    private LocationSettingsRequest mLocationSettingsRequest;

    // Labels
    private String mLatitudeLabel;
    private String mLongitudeLabel;
    private String mLastUpdateTimeLabel;

    // TextViews
    private TextView mLatitudeText;
    private TextView mLongitudeText;
    private TextView mLastUpdateTimeText;

    /**
     * Location settings result variables
     */
    private Status mStatus;

    /**
     * Time when the location was updated represented as a String.
     */
    private String mLastUpdateTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Base);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.debug_activity);

        Toolbar debugToolbar = (Toolbar)findViewById(R.id.debug_toolbar);
        setSupportActionBar(debugToolbar);

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set labels for the debug activity
        mLatitudeLabel = getResources().getString(R.string.latitude_label);
        mLongitudeLabel = getResources().getString(R.string.longitude_label);
        mLastUpdateTimeLabel = getResources().getString(R.string.last_update_time_label);
        mLatitudeText = (TextView)findViewById((R.id.latitude_text));
        mLongitudeText = (TextView)findViewById((R.id.longitude_text));
        mLastUpdateTimeText = (TextView)findViewById(R.id.last_update_time_text);

        // Kick off the process of building the GoogleApiClient, LocationRequest, and
        // LocationSettingsRequest objects.
        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();
    }

    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    private synchronized void buildGoogleApiClient() {
        // Info message - log file
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     *The device needs to enable the appropriate system settings.
     *These settings are defined by the LocationRequest data object.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // This method sets the rate in milliseconds at which your app prefers to receive location updates
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // This method sets the fastest rate in milliseconds at which your app can handle location updates.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        // This method sets the priority of the request, which hints to the Google Play services to use GPS.
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Uses LocationSettingsRequest.Builder to build a LocationSettingsRequest that is used for
     *checking if a device has the needed location settings.
     */
    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    /**
     * Use the FusedLocationApi to provide location information
     */
    private void requestLocationUpdates() {
        // Initialize GPS location requests.
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, DebugActivity.this);
    }

    /**
     * Requests location updates from the FusedLocationApi.
     * If GPS is turn on it requests fine location.
     * If GPS is turned off it requests coarse location.
     */
    private void startLocationUpdates() {
        LocationServices.SettingsApi.checkLocationSettings(
                mGoogleApiClient,
                mLocationSettingsRequest
        ).setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                mStatus = result.getStatus();
                switch (mStatus.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // Info message - log file
                        Log.i(TAG, "All location settings are satisfied. Get current location");
                        requestLocationUpdates();
                        break;

                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Info message - log file
                        Log.i(TAG, "Location settings not satisfied. get last location");

                        // Prompt user to adjust device settings
                        try {
                            mStatus.startResolutionForResult(DebugActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch(IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request. Resolution required");
                        }
                        break;

                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix this
                        String errorMessage = "Location settings are inadequate, and cannot be fixed here. Fix in Settings.";

                        // Error message - log file
                        Log.e(TAG, errorMessage);
                        Toast.makeText(DebugActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }

                updateLocationUI();
            }
        });
    }

    /**
     * Callback triggered if we resolved location permission issues
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            requestLocationUpdates();
        }
    }

    private void updateLocationUI() {
        if (mCurrentLocation != null) {
            mLatitudeText.setText(String.format(Locale.getDefault(), "Current %s: %f", mLatitudeLabel,
                    mCurrentLocation.getLatitude()));
            mLongitudeText.setText(String.format(Locale.getDefault(), "Current %s: %f", mLongitudeLabel,
                    mCurrentLocation.getLongitude()));
            mLastUpdateTimeText.setText(String.format("%s: %s", mLastUpdateTimeLabel,
                    mLastUpdateTime));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Info message - log file
        Log.i(TAG, "in onConnected(), starting location updates");

        // Initialize location updates
        startLocationUpdates();
    }

    /**
     * Callback that fires when the location changes.
     */
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateLocationUI();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }
}