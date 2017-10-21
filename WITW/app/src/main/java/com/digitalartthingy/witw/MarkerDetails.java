/**
 *       Copyright (C) 2017 Digital Art Thingy Inc.
 */
package com.digitalartthingy.witw;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 *
 * Displays details about a given custom marker in a dismissable popup.
 *
 */
public class MarkerDetails {
    private static final String TAG = "MarkerDetails";

    private static final int POPUP_WIDTH_DIPS = 270;
    private static final int POPUP_HEIGHT_DIPS = 350;

    private PopupWindow mPopup;
    private Context mContext;
    private Intent mCallIntent;

    private UserSuppliedDetails mUserSuppliedDetails;

    public MarkerDetails(Context context, UserSuppliedDetails usd) {
        mContext = context;
        mUserSuppliedDetails = usd;
    }

    /**
     * Display marker with details
     */
    public final void display(final CustomMarker marker) {
        Log.i(TAG, "Triggered display");

        final Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + marker.getPhone()));

        PackageManager packageManager = mContext.getPackageManager();
        if (packageManager.queryIntentActivities(callIntent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0) {
            mCallIntent = callIntent;
        }

        // Inflate the layout
        final LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.marker_details, null);

        // Add title
        TextView textView = (TextView)layout.findViewById(R.id.marker_title);
        textView.setText(marker.getTitle());

        // Add clickable phone number
        textView = (TextView)layout.findViewById(R.id.marker_phone);
        if (mCallIntent == null) {
            textView.setText(marker.getPhone());
        } else {
            final SpannableString phone = new SpannableString(marker.getPhone());
            phone.setSpan(new UnderlineSpan(), 0, phone.length(), 0);

            // Hook up listener when user clicks on a phone number
            textView.setText(phone);
            textView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View v) {
                    if (mCallIntent != null) {
                        mContext.startActivity(mCallIntent);
                    }
                }
            });
        }

        // Convert dips to actual pixels
        final float widthPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, POPUP_WIDTH_DIPS, mContext.getResources().getDisplayMetrics());
        final float heightPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, POPUP_HEIGHT_DIPS, mContext.getResources().getDisplayMetrics());

        // Display popup
        mPopup = new PopupWindow(layout, (int)widthPx, (int)heightPx, true);
        mPopup.showAtLocation(layout, Gravity.CENTER, 0, 0);

        // Add OK button and dismiss popup when user clicks OK
        final Button dismissButton = (Button)layout.findViewById(R.id.dismiss_button);
        dismissButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                mPopup.dismiss();
            }
        });
    }

    /**
     * Duplicated the popup above for user to input marker details
     */
    public final void enterDetails(final CustomMarker marker) {
        Log.i(TAG, "Triggered enter details");

        final Intent callIntent = new Intent(Intent.ACTION_CALL);

        PackageManager packageManager = mContext.getPackageManager();
        if (packageManager.queryIntentActivities(callIntent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0) {
            mCallIntent = callIntent;
        }

        // Inflate the layout
        final LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.set_marker_details, null);

        // Convert dips to actual pixels
        final float widthPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, POPUP_WIDTH_DIPS, mContext.getResources().getDisplayMetrics());
        final float heightPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, POPUP_HEIGHT_DIPS, mContext.getResources().getDisplayMetrics());

        // Display popup - overriding heightPx so we can show our custom dialog in its entirety
        mPopup = new PopupWindow(layout, (int)widthPx, 420 /* heightPx */, true);
        mPopup.showAtLocation(layout, Gravity.CENTER, 0, 0);

        // Dismiss popup when user clicks SUBMIT and set the marker's details
        final Button submitButton = (Button)layout.findViewById(R.id.submit_button);
        submitButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                mPopup.dismiss();

                // Fetch entered text
                EditText markerName = (EditText)layout.findViewById(R.id.marker_details_name);
                EditText markerPhone = (EditText)layout.findViewById(R.id.marker_details_phone);

                // Set marker details
                marker.setMarkerDetails(markerName.getText().toString(), markerPhone.getText().toString());

                // Signal that the user has supplied information
                mUserSuppliedDetails.signalCompletion();
            }
        });
    }
}
