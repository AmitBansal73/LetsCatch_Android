package net.anvisys.letscatch.Services;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Amit Bansal on 10-01-2017.
 */
public class FetchAddressService  extends IntentService {

    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME = "net.anvisys.meetus";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME +".LOCATION_DATA_EXTRA";

    protected ResultReceiver mReceiver;

    public FetchAddressService() {
        super("FetchAddressService");

    }

    public FetchAddressService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String errorMessage = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        // Get the location passed to this service through an extra.
        mReceiver=  intent.getParcelableExtra("RECEIVER");
        Location location = intent.getParcelableExtra("LOCATION");

        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    // In this sample, get just a single address.
                    1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            errorMessage = "service_not_available";

        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = "invalid_lat_long_used";
        }

        // Handle case where no address was found.
        try {
            if (addresses == null || addresses.size() == 0) {
                if (errorMessage.isEmpty()) {
                    errorMessage = "no_address_found";
                }
                deliverResultToReceiver(FAILURE_RESULT, errorMessage);
            } else {
                Address address = addresses.get(0);
                ArrayList<String> addressFragments = new ArrayList<String>();

                // Fetch the address lines using getAddressLine,
                // join them, and send them to the thread.
                for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    addressFragments.add(address.getAddressLine(i));
                }

                deliverResultToReceiver(SUCCESS_RESULT,
                        TextUtils.join(System.getProperty("line.separator"),
                                addressFragments));
            }
        }
        catch (Exception ex)
        {
            Toast.makeText(this,"Error in FetchAddress Service",Toast.LENGTH_LONG).show();
        }
    }

    private void deliverResultToReceiver(int resultCode, String message) {
        try {
            Bundle bundle = new Bundle();
            bundle.putString(RESULT_DATA_KEY, message);
            mReceiver.send(resultCode, bundle);
        }
        catch (Exception ex)
        {
            Toast.makeText(this,"Error in FetchAddress Service",Toast.LENGTH_LONG).show();
        }
    }
}
