/**
 *       Copyright (C) 2017 Digital Art Thingy Inc.
 */
package com.digitalartthingy.witw;

import android.app.Activity;
import android.content.Intent;
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
import android.widget.Toast;

import com.google.android.gms.maps.*;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "MainActivity";
    private static final String PRIVACY_POLICY_URL = "http://www.digitalartthingy.com/legal/privacy.html";
    private static final String ABOUT_URL = "http://www.digitalartthingy.com/WITW.html";

    /**
     * Debug raw data
     */
    private static final int ACTIVATE_DEBUG_KEY_PRESSES = 3;
    private int debugCount = 0;

    /**
     * This web view is used to display web content such as the privacy policy
     */
    private WebView mWebView;

    /**
     * The support map fragment is used to display the map itself
     */
    private SupportMapFragment mMapFragment;

    /**
     * The persisted GoogleMap object
     */
    private GoogleMap mGoogleMap;

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

        // Get the GoogleMap object
        mMapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        if (mMapFragment != null) {
            mMapFragment.getMapAsync(MainActivity.this);
        } else {
            Log.i(TAG, "UNEXPECTED: mMapFragment is null");
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
                debugCount = 0;
                return true;
            case R.id.action_privacy:
                debugCount = 0;
                return loadUrl(PRIVACY_POLICY_URL);
            case R.id.action_about:
                debugCount = 0;
                return loadUrl(ABOUT_URL);
            default:
                debugCount = 0;
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("SameReturnValue")
    private boolean loadUrl(String url) {
        mWebView.loadUrl(url);
        mWebView.setVisibility(View.VISIBLE);
        return true;
    }

    /**
     * Runs when the task bar is clicked.
     */
    public void OnDebugClickHandler(View view) {
        debugCount++;
        Log.i(TAG, "Triggered OnDebugClickHandler");

        if (debugCount == ACTIVATE_DEBUG_KEY_PRESSES) {
            // Start debug activity
            Intent debugIntent = new Intent(this, DebugActivity.class);
            startActivity(debugIntent);
            debugCount = 0;
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        Log.i(TAG, "Triggered OnMapReady");

        // Retain the GoogleMap object since we've using it with new coordinates
        if (mGoogleMap == null) {
            mGoogleMap = map;

            // The camera is now update with the current GPS location
            mGoogleMap.setMyLocationEnabled(true);
        }
    }
}