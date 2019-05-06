package net.anvisys.letscatch;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import net.anvisys.letscatch.Common.Session;
import net.anvisys.letscatch.Object.APP_VARIABLES;
import net.anvisys.letscatch.Register.MobileNoActivity;

public class StartActivity extends AppCompatActivity {


    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int INTERNET_PERMISSION_REQUEST_CODE = 2;
    private static final int CONTACT_PERMISSION_REQUEST_CODE = 3;
    private static final int LOCATION_ENABLE_REQUEST_CODE = 4;
    private static final int WRITE_STORAGE_REQUEST_CODE = 5;
    String registrationId;
    WebView gifWebView;
    TextView txtClose;
    LocationManager locationManager = null;
    boolean gps_enabled = false;
    boolean network_enabled = false;
    Toolbar myToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        //  myToolbar.setNavigationIcon(R.drawable.user_icon);
        setSupportActionBar(myToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Lets Catch");
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.show();

        gifWebView = (WebView) findViewById(R.id.webView);
        gifWebView.loadUrl("file:///android_asset/location.gif");
        gifWebView.invalidate();
        txtClose = (TextView) findViewById(R.id.btnClose);
        txtClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartActivity.this.finish();
            }
        });
        setContentView(R.layout.activity_start);
        checkPermission();

    }


    private void CheckLogin()
    {

        registrationId = Session.getRegistrationID(this);
        if (registrationId.matches(""))
        {
            Intent registrationIntent = new Intent(StartActivity.this,MobileNoActivity.class);
            startActivity(registrationIntent);
            StartActivity.this.finish();
        }
        else {

            try {
                checkGPSStatus();
            } catch (Exception IOEx) {
                StartActivity.this.finish();

            }
        }

       int location =1;


    }

    private void checkGPSStatus() {
        try {

            if (locationManager == null) {
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            }
            try {
                gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            }
            catch (Exception ex)
            {
                Toast.makeText(this,"GPS permission error",Toast.LENGTH_LONG).show();
            }

            try {
                if (!gps_enabled) {
                    network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                }
            }
            catch (Exception ex)
            {
                Toast.makeText(this,"Network Location permission error",Toast.LENGTH_LONG).show();
            }


            try {

                if(gps_enabled)
                {
                    APP_VARIABLES.GPS_STATUS = true;
                    APP_VARIABLES.MY_LOCATION =   locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if(APP_VARIABLES.MY_LOCATION!= null)
                    {
                       APP_VARIABLES.MY_LOCATION_STRING = Double.toString(APP_VARIABLES.MY_LOCATION.getLatitude()) + "," + Double.toString(APP_VARIABLES.MY_LOCATION.getLongitude());
                    }

                    checkNetwork();
                }

                else if (network_enabled)
                {
                    APP_VARIABLES.GPS_STATUS = true;
                    APP_VARIABLES.MY_LOCATION =   locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if(APP_VARIABLES.MY_LOCATION!= null)
                    {
                        APP_VARIABLES.MY_LOCATION_STRING = Double.toString(APP_VARIABLES.MY_LOCATION.getLatitude()) + "," + Double.toString(APP_VARIABLES.MY_LOCATION.getLongitude());
                    }

                    checkNetwork();
                }

                else if (!gps_enabled && !network_enabled) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(new ContextThemeWrapper(this,R.style.AppTheme));
                    dialog.setMessage("GPS not enabled");
                    dialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //this will navigate user to the device location settings screen
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent, LOCATION_ENABLE_REQUEST_CODE);

                            //Toast.makeText(getApplicationContext(),"Setting Changed", Toast.LENGTH_LONG).show();
                        }
                    });

                    dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //GPS_STATUS = false;
                            //checkNetworkConnectivity();

                            StartActivity.this.finish();

                        }
                    });

                    dialog.setCancelable(false);
                    AlertDialog alert = dialog.create();

                    alert.show();
                }


            } catch (SecurityException ex) {
                Toast.makeText(this,"Network position permission error",Toast.LENGTH_LONG).show();
            }



        }
        catch (Exception ex)
        {
            Toast.makeText(this, "Failed while checking GPS", Toast.LENGTH_LONG).show();
        }
    }

    private void checkGPSStatusAfterChange()
    {
        try {
            if (locationManager == null) {
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            }
            try {
                gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                if(!gps_enabled)
                {
                    network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                }
            } catch (Exception ex) {

                Toast.makeText(this, "could not get GPS Location", Toast.LENGTH_LONG).show();
            }
            try {

                if (!gps_enabled && !network_enabled) {
                   APP_VARIABLES.GPS_STATUS = false;
                    NoGPSDialog();
                }

                else if(gps_enabled)
                {
                    APP_VARIABLES.GPS_STATUS = true;
                   APP_VARIABLES.MY_LOCATION =   locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (APP_VARIABLES.MY_LOCATION != null) {
                        APP_VARIABLES.MY_LOCATION_STRING = Double.toString(APP_VARIABLES.MY_LOCATION.getLatitude()) + "," + Double.toString(APP_VARIABLES.MY_LOCATION.getLongitude());
                    }
                    checkNetwork();
                }

                else if (network_enabled)
                {
                    APP_VARIABLES.GPS_STATUS = true;
                    APP_VARIABLES.MY_LOCATION =   locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if(APP_VARIABLES.MY_LOCATION!= null)
                    {
                        APP_VARIABLES.MY_LOCATION_STRING = Double.toString(APP_VARIABLES.MY_LOCATION.getLatitude()) + "," + Double.toString(APP_VARIABLES.MY_LOCATION.getLongitude());
                    }
                    checkNetwork();
                }

            } catch (SecurityException ex) {
                Toast.makeText(this, "could not get GPS Location", Toast.LENGTH_LONG).show();
            }


        }
        catch (Exception ex)
        {
            Toast.makeText(this, "Failed while checking GPS", Toast.LENGTH_LONG).show();
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOCATION_ENABLE_REQUEST_CODE)
        {
            checkGPSStatusAfterChange();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void checkNetwork()
    {
        NetworkInfo networkInfo=null;
        try {
            ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            networkInfo = connManager.getActiveNetworkInfo();
        }
        catch (Exception ex)
        {
            Toast.makeText(this, "Network info not Known", Toast.LENGTH_SHORT).show();
        }

        if (networkInfo == null)
        {
            AlertDialog.Builder dialog = new AlertDialog.Builder(StartActivity.this);
            dialog.setMessage("Connection not Available, WorkOffline");
            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    StartActivity.this.finish();
                }
            });

            dialog.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    APP_VARIABLES.NETWORK_STATUS = false;
                    SetLocation();
                }
            });
            dialog.setCancelable(false);
            AlertDialog alert = dialog.create();
            alert.show();
        }

        else if(networkInfo.getType() != ConnectivityManager.TYPE_WIFI)
        {
            if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE)
            {
                AlertDialog.Builder dialog = new AlertDialog.Builder(StartActivity.this);
                dialog.setMessage("Mobile Data usage will be charged by provider");
                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        StartActivity.this.finish();
                    }
                });
                dialog.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        APP_VARIABLES.NETWORK_STATUS = true;
                        SetLocation();
                        return;
                    }
                });
                dialog.setCancelable(false);
                AlertDialog alert = dialog.create();
                alert.show();
            }
            else {
                AlertDialog.Builder dialog = new AlertDialog.Builder(StartActivity.this);
                dialog.setMessage("Network not Available");
                dialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });
                dialog.setCancelable(false);
                AlertDialog alert = dialog.create();
                alert.show();
            }
        }
        else {
            SetLocation();

        }
    }

    private void SetLocation()
    {
        if(!APP_VARIABLES.GPS_STATUS)
        {
            StartActivity.this.finish();
        }

        else if (APP_VARIABLES.MY_LOCATION_STRING.matches(""))
        {
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0,mLocationListener);

            }
            catch (SecurityException ex)
            {
                Toast.makeText(this,"GPS Location permission error",Toast.LENGTH_LONG).show();
            }
            catch (Exception ex)
            {
                Toast.makeText(this,"could not get location",Toast.LENGTH_LONG).show();
            }

            try {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, mLocationListener);
            }
            catch (SecurityException ex)
            {
                int a = 5;
            }
        }
        else {

            MoveToMainActivity();
        }
    }


    LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
           APP_VARIABLES.MY_LOCATION_STRING = Double.toString(location.getLatitude()) + "," + Double.toString(location.getLongitude());
            MoveToMainActivity();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };


    private void MoveToMainActivity()
    {
        try
        {
            locationManager.removeUpdates(mLocationListener);
        }
        catch (SecurityException EX)
        {

        }

        registrationId = Session.getRegistrationID(this);
        if (registrationId.matches(""))
        {
            Intent registrationIntent = new Intent(StartActivity.this,MobileNoActivity.class);
            startActivity(registrationIntent);
            StartActivity.this.finish();
        }
        else {

            Intent mainIntent = new Intent(this, MainActivity.class);
            Session.InitiateMeetingsFromSession(this);
            startActivity(mainIntent);
            StartActivity.this.finish();
        }
    }

    private void checkPermission()
    {
        if ((ContextCompat.checkSelfPermission(StartActivity.this, Manifest.permission.ACCESS_WIFI_STATE)
                != PackageManager.PERMISSION_GRANTED)
                &&(ContextCompat.checkSelfPermission(StartActivity.this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED))
        {
            ActivityCompat.requestPermissions(StartActivity.this, new String[]{Manifest.permission.ACCESS_WIFI_STATE}, INTERNET_PERMISSION_REQUEST_CODE);
        }

        else

        {
            CheckLocationPermission();
        }
    }

    private void CheckLocationPermission()
    {
        if ((ContextCompat.checkSelfPermission(StartActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(StartActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED))
        {
            ActivityCompat.requestPermissions(StartActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
          //  requestLocationPermission();
        }
        else
        {
            checkStoragePermission();
        }
   }
    private void checkStoragePermission()
    {
        if ((ContextCompat.checkSelfPermission(StartActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
                )
        {
            ActivityCompat.requestPermissions(StartActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_STORAGE_REQUEST_CODE);
            //  requestLocationPermission();
        }
        else
        {
            checkContactPermission();
        }
    }
    private void checkContactPermission()
    {
        if(ContextCompat.checkSelfPermission(StartActivity.this, android.Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(StartActivity.this, new String[]{android.Manifest.permission.READ_CONTACTS}, CONTACT_PERMISSION_REQUEST_CODE);
           // requestReadContactPermission();
        }
        else
        {
           // CheckLogin();
           // checkNetworkPermission();
            try {
                checkGPSStatus();
            } catch (Exception IOEx) {
                StartActivity.this.finish();

            }
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case INTERNET_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    CheckLocationPermission();
                } else {
                    StartActivity.this.finish();
                }
                break;

            case LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    checkStoragePermission();

                } else {

                    StartActivity.this.finish();

                }
                break;

            case WRITE_STORAGE_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    checkContactPermission();

                } else {

                    StartActivity.this.finish();

                }
                break;

            case CONTACT_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    CheckLogin();

                } else {

                    StartActivity.this.finish();

                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void NoGPSDialog()
    {
        try {
            // AlertDialog.Builder dialog = new AlertDialog.Builder(StartActivity.this);
            AlertDialog.Builder mdialog = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme));
            mdialog.setMessage("No GPS, Application will be closed");

            mdialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    StartActivity.this.finish();
                    return;
                }
            });

            mdialog.setCancelable(false);
            AlertDialog malert = mdialog.create();
            malert.show();
        }
        catch (Exception ex)
        {
            int a=5;
            a++;
        }

    }


    public void OnClick(View v)
    {
        StartActivity.this.finish();
    }
}
