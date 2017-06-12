/**
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.gms.location.sample.basiclocationsample;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Location sample.
 *
 * Demonstrates use of the Location API to retrieve the last known location for a device.
 * This sample uses Google Play services (GoogleApiClient) but does not need to authenticate a user.
 * See https://github.com/googlesamples/android-google-accounts/tree/master/QuickStart if you are
 * also using APIs that need authentication.
 */
public class MainActivity extends AppCompatActivity implements
        ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
    protected static final String TAG = "MainActivity";

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000; //5 seconds

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 5; //1 second

    // Keys for storing activity state in the Bundle.
    protected final static String KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string";
    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;
    protected Location mCurrentLocation;

    protected LocationRequest mLocationRequest;

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    protected LocationSettingsRequest mLocationSettingsRequest;

    // Labels
    protected String mLatitudeLabel;
    protected String mLongitudeLabel;
    protected String mLastUpdateTimeLabel;

    // TextViews
    protected TextView mLatitudeText;
    protected TextView mLongitudeText;
    protected TextView mLastUpdateTimeText;

    /**
     * Location settings result variables
     */
    protected Status status;
    protected LocationSettingsStates settingStates;

    /**
     * Time when the location was updated represented as a String.
     */
    protected String mLastUpdateTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mLatitudeLabel = getResources().getString(R.string.latitude_label);
        mLongitudeLabel = getResources().getString(R.string.longitude_label);
        mLatitudeText = (TextView) findViewById((R.id.latitude_text));
        mLongitudeText = (TextView) findViewById((R.id.longitude_text));
        mLastUpdateTimeText = (TextView) findViewById(R.id.last_update_time_text);

        // Kick off the process of building the GoogleApiClient, LocationRequest, and
        // LocationSettingsRequest objects.
        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();
    }

    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        //info message - log file
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
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        //This method sets the rate in milliseconds at which your app prefers to receive location updates
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        //This method sets the fastest rate in milliseconds at which your app can handle location updates.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        //This method sets the priority of the request, which hints to the Google Play services to use GPS.
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
    * Uses LocationSettingsRequest.Builder to build a LocationSettingsRequest that is used for
    *checking if a device has the needed location settings.
    */
    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    /**
     * Requests location updates from the FusedLocationApi.
     * If GPS is turn on it requests fine location.
     * If GPS is turned off it requests coarse location.
     */
    protected void startLocationUpdates() {
        LocationServices.SettingsApi.checkLocationSettings(
                mGoogleApiClient,
                mLocationSettingsRequest
        ).setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                status = result.getStatus();
                settingStates = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // info message -log file
                        Log.i(TAG, "All location settings are satisfied. Get current location");
                        // initialize GPS location requests.
                        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, MainActivity.this);
                        break;

                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // info message -log file
                        Log.i(TAG, "Location settings not satisfied. get last location");
                        // initialize approximate location request
                        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix this
                        String errorMessage = "Location settings are inadequate, and cannot be " +
                                "fixed here. Fix in Settings.";
                        // error message -log file
                        Log.e(TAG, errorMessage);
                        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
                updateLocationUI();
            }
        });
    }

    private void updateLocationUI() {
        if (mCurrentLocation != null) {
            mLatitudeText.setText(String.format(Locale.getDefault(), "Current %s: %f", mLatitudeLabel,
                    mCurrentLocation.getLatitude()));
            mLongitudeText.setText(String.format(Locale.getDefault(), "Current %s: %f", mLongitudeLabel,
                    mCurrentLocation.getLongitude()));
            mLastUpdateTimeText.setText(String.format("%s: %s", mLastUpdateTimeLabel,
                    mLastUpdateTime));
        } else{
            mLatitudeText.setText(String.format(Locale.getDefault(), "Last known %s: %f", mLatitudeLabel,
                    mLastLocation.getLatitude()));
            mLongitudeText.setText(String.format(Locale.getDefault(), "Last known %s: %f", mLongitudeLabel,
                    mLastLocation.getLongitude()));
            mLastUpdateTimeText.setText(String.format(Locale.getDefault(), "%s: %s", mLastUpdateTimeLabel,
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
        //info message - log file
        Log.i(TAG, "in onConnected(), starting location updates");

        //initialize location updates
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
