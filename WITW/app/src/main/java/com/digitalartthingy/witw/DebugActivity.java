package com.digitalartthingy.witw;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class DebugActivity extends AppCompatActivity implements
        ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    protected static final String TAG = "DebugActivity";
    protected static final String PRIVACY_POLICY_URL = "http://www.digitalartthingy.com/legal/privacy.html";
    protected static final String ABOUT_URL = "http://www.digitalartthingy.com/WITW.html";

    /**
     * Constant used in the location settings dialog.
     */
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000; // 5 seconds

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 5; // 1 second

    /**
     * This webview is used to display web content such as the privacy policy
     */
    protected WebView mWebView;

    /**
     * Represents a geographical location.
     */
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
    protected Status mstatus;
    protected LocationSettingsStates mSettingStates;

    /**
     * Time when the location was updated represented as a String.
     */
    protected String mLastUpdateTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Base);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.debug_activity);

        Toolbar mainToolbar = (Toolbar)findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // Create a webview so we can open web resources such as the privacy policy within the app
        mWebView = (WebView)findViewById(R.id.webView);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setBuiltInZoomControls(true);

        final Activity activity = this;
        mWebView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(activity, description, Toast.LENGTH_SHORT).show();
            }
        });

        // Set labels for the debug activity
        mLatitudeLabel = getResources().getString(R.string.latitude_label);
        mLongitudeLabel = getResources().getString(R.string.longitude_label);
        mLastUpdateTimeLabel = getResources().getString(R.string.last_update_time_label);

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
                Intent mainIntent = new Intent(this, MainActivity.class);
                startActivity(mainIntent);
                return true;
            case R.id.action_privacy:
                return loadUrl(PRIVACY_POLICY_URL);
            case R.id.action_about:
                return loadUrl(ABOUT_URL);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean loadUrl(String url) {
        mWebView.loadUrl(url);
        mWebView.setVisibility(View.VISIBLE);
        return true;
    }

    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
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
    protected void createLocationRequest() {
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
                mstatus = result.getStatus();
                mSettingStates = result.getLocationSettingsStates();
                switch (mstatus.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // Info message - log file
                        Log.i(TAG, "All location settings are satisfied. Get current location");

                        // Initialize GPS location requests.
                        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, DebugActivity.this);
                        break;

                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Info message - log file
                        Log.i(TAG, "Location settings not satisfied. get last location");

                        // Prompt user to adjust device settings
                        try {
                            mstatus.startResolutionForResult(DebugActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch(IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request. Resolution required");
                        }
                        break;

                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix this
                        String errorMessage = "Location settings are inadequate, and cannot be " +
                                "fixed here. Fix in Settings.";

                        // Error message - log file
                        Log.e(TAG, errorMessage);
                        Toast.makeText(DebugActivity.this, errorMessage, Toast.LENGTH_LONG).show();
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
        // Attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }
}

