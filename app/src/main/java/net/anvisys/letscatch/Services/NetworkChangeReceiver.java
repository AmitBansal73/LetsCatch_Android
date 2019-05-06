package net.anvisys.letscatch.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.widget.Toast;

import net.anvisys.letscatch.Object.APP_VARIABLES;

/**
 * Created by Amit Bansal on 19-02-2017.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);


            final android.net.NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if(networkInfo== null)
            {
                APP_VARIABLES.NETWORK_STATUS = false;

            }
            else {
                if ((networkInfo.getType() == ConnectivityManager.TYPE_WIFI) || (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE)) {
                    APP_VARIABLES.NETWORK_STATUS = true;
                } else {
                    APP_VARIABLES.NETWORK_STATUS = false;
                }
            }
        }
        catch (Exception ex)
        {
            Toast.makeText(context,"Error on Network Change Listener", Toast.LENGTH_LONG).show();
        }

    }
}
