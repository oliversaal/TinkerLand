package com.digitalartthingy.witw;

import android.content.SharedPreferences;
import android.util.Log;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * Fetches marker details from local storage
 */

public    class MarkerFetcher   {
    /**
     * Define constants
     */
        private static final String TAG = "MainActivity";

    // Add to preferences to storage
    public void setMarkerPreference(String storedLatLng, SharedPreferences settings) {
        SharedPreferences.Editor editor;
        editor = settings.edit();
//        for(String eachLatLng : storedLatLng){
            try {
                SecretKey keyMarker = KeyGenerator.getInstance("AES").generateKey();
                editor.putString(keyMarker.toString(), storedLatLng);
                editor.apply();
            }
            catch(NoSuchAlgorithmException e){
                        Log.e(TAG, "Failure to setMarkerPreference.");
            }
//        }
    }

     //Retrieve preferences from storage
     public Map<String, ?>  getMarkerPreference(SharedPreferences settings) {
         Map<String, ?> allCoordinates =  settings.getAll();
         return allCoordinates;
    }
}
