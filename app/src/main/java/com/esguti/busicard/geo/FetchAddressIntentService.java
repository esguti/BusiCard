package com.esguti.busicard.geo;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by esguti on 08.05.16.
 */
public class FetchAddressIntentService extends IntentService{
    private static final String LOG_TAG = FetchAddressIntentService.class.getSimpleName();

    private ResultReceiver m_Receiver;

    public FetchAddressIntentService() {
        super(LOG_TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        m_Receiver = intent.getParcelableExtra(Constants.RECEIVER);
        // Check if receiver was properly registered.
        if (m_Receiver == null) {
            Log.w(LOG_TAG, "No receiver received.");
            return;
        }

        // Get the location passed to this service through an extra.
        Location location = intent.getParcelableExtra(
                Constants.LOCATION_DATA_EXTRA);
        if (location == null) {
            Log.wtf(LOG_TAG, "No location received");
            deliverResultToReceiver(Constants.FAILURE_RESULT, "No location received");
            return;
        }

        List<Address> addresses = null;
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());


        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    // In this sample, get just a single address.
                    1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            Log.e(LOG_TAG, "IO problems",ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            Log.e(LOG_TAG, "Wrong Location",illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size()  == 0) {
            deliverResultToReceiver(Constants.FAILURE_RESULT, "No address found");
        } else {
            Address address = addresses.get(0);
            deliverResultToReceiver(Constants.SUCCESS_RESULT, address.getCountryName());
        }
    }

    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, message);
        m_Receiver.send(resultCode, bundle);
    }

}
