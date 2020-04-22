package net.anvisys.letscatch;


import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;


import net.anvisys.letscatch.Application.AboutActivity;
import net.anvisys.letscatch.Application.HelpActivity;
import net.anvisys.letscatch.Application.LogActivity;
import net.anvisys.letscatch.Common.DataAccess;
import net.anvisys.letscatch.Common.ImageServer;
import net.anvisys.letscatch.Common.Session;
import net.anvisys.letscatch.Common.SyncContact;
import net.anvisys.letscatch.Common.UTILITY;
import net.anvisys.letscatch.Object.APP_CONST;
import net.anvisys.letscatch.Object.APP_SETTINGS;
import net.anvisys.letscatch.Object.APP_VARIABLES;
import net.anvisys.letscatch.Object.ActiveMeeting;
import net.anvisys.letscatch.Object.ActiveMeetingGroup;

import net.anvisys.letscatch.Object.ChatMessage;
import net.anvisys.letscatch.Object.Contact;
import net.anvisys.letscatch.Object.Log;
import net.anvisys.letscatch.Object.Message;
import net.anvisys.letscatch.Object.Profile;
import net.anvisys.letscatch.Shape.OvalImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity implements
        ActiveMeetingGroup.MeetingGroupListener,
        ActiveMeeting.MeetingUpdateListener,
        LocationListener,
        SyncContact.SyncContactListener,
        GCMListenerService.GCMListener,
        ContactSyncService.ContactSyncListener,
        ActiveMeeting.MeetingStatusListener{
    private CoordinatorLayout coordinatorLayout;
    private Toolbar myToolbar;
    private ViewPager viewPager;
    private FragmentPagerAdapter mAdapter;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView navigationView;
    private OvalImageView profileImage;
    private TextView MyName;
    private TextView MyEmail;
    private int CONTACT_PICKER_REQUEST = 1;

    private Integer ClickCount=0;
    private long prevTime = 0;
    private Location myLocation=null;
    String PreviousLocation="0.0,0.0";
    Intent NotificationIntent;
    LocationManager locationManager;
    Handler mHandler;
    public Timer  markerTimer;
    private TimerTask timeTask;
    private int Count =1;
    public static Fragment CurrentFragment;
    private String CurrentPage="";

    Profile myProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            //region Initiate View
            myToolbar = (Toolbar) findViewById(R.id.my_toolbar);

            setSupportActionBar(myToolbar);
            ActionBar actionBar = getSupportActionBar();
            actionBar.setTitle(" LetsCatch ");
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.show();

            coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
            navigationView = (NavigationView) findViewById(R.id.navigation_view);
            navigationView.setNavigationItemSelectedListener(new DrawerItemSelected());
            mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            View header = navigationView.getHeaderView(0);
            profileImage = (OvalImageView) header.findViewById(R.id.profile_image);
            MyName = (TextView) header.findViewById(R.id.username);
            MyEmail = (TextView) header.findViewById(R.id.email);

            myProfile = Session.GetUser(getApplicationContext());

            mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            mDrawerToggle = new ActionBarDrawerToggle(
                    this,
                    mDrawerLayout,
                    myToolbar,
                    R.string.drawer_Open,
                    R.string.drawer_Close
            ) {
                public void onDrawerClosed(View view) {
                    getSupportActionBar().setTitle("LetsCatch");
                    invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                }

                public void onDrawerOpened(View drawerView) {
                    getSupportActionBar().setTitle("LetsCatch");
                    invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                }
            };
            mDrawerLayout.setDrawerListener(mDrawerToggle);
            mDrawerToggle.syncState();
            try {
                //Profile myProfile = Session.GetUser(this);
                MyName.setText(myProfile.NAME);
                MyEmail.setText(myProfile.E_MAIL);
                APP_VARIABLES.MY_MOBILE_NUMBER = myProfile.MOB_NUMBER;
                APP_VARIABLES.MY_NAME = myProfile.NAME;
                APP_VARIABLES.MY_USER_ID = myProfile.UserID;
               /* Bitmap bmp = ImageServer.GetImageBitmap(myProfile.MOB_NUMBER, this);
                if (bmp != null) {
                    profileImage.setImageBitmap(bmp);
                }
                */
                String url1 = APP_CONST.IMAGE_URL + myProfile.UserID +".png";
                Picasso.with(getApplicationContext()).load(url1).error(R.drawable.user_image).into(profileImage);


            } catch (Exception ex) {
                int a = 1;
            }





            //  ActiveMeetingGroup.GetInstance(this).RunningMeetings = Session.InitiateMeetingsFromSession(this);

            DataAccess da = new DataAccess(getApplicationContext());
            da.open();
            ActiveMeetingGroup.GetInstance(this).RunningMeetings = da.GetAllActiveMeetings();
            da.close();

            TabLayout tabs = (TabLayout) findViewById(R.id.tabs);

            tabs.addTab(tabs.newTab().setText("Map"));
            tabs.addTab(tabs.newTab().setText("Meeting"));
            tabs.addTab(tabs.newTab().setText("Contact"));

            viewPager = (ViewPager) findViewById(R.id.pager);
            mAdapter = new TabsPagerAdapter(getSupportFragmentManager());
            viewPager.setOffscreenPageLimit(3);
            viewPager.setAdapter(mAdapter);

            //tabs.setShouldExpand(true);
            //tabs.setViewPager(viewPager);
            viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));

            tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {

                    viewPager.setCurrentItem(tab.getPosition());

                    if(tab.getPosition() == 0)
                    {
                        CurrentPage = "MapFragment";
                    }
                    else if(tab.getPosition() == 1)
                    {
                        CurrentPage = "fragmentFragment";
                    }
                    else if(tab.getPosition() == 2)
                    {
                        CurrentPage = "ContactFragment";
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });

          /*
            tabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    if(position == 0)
                    {
                        CurrentPage = "MapFragment";
                    }

                }

                @Override
                public void onPageSelected(int position) {
                    if(position == 0)
                    {
                        CurrentPage = "MapFragment";
                    }
                    else if(position == 1)
                    {
                        CurrentPage = "fragmentFragment";
                    }
                    else if(position == 2)
                    {
                        CurrentPage = "ContactFragment";
                    }

                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });*/

            ActiveMeetingGroup.GetInstance(this).RegisterMeetingListener(MainActivity.this);
            // ActiveMeeting.RegisterMeetingListener(MainActivity.this);
            registerLocationListener();
            SyncContact.RegisterCustomObjectListener(this);
            GCMListenerService.setGCMNotificationListener(this);
            ActiveMeeting.RegisterStatusListener(this);
            ContactSyncService.RegisterListener(this);

            Session.SetApplicationStatus(this, true);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancelAll();
            GCMListenerService.ClearNotificationMessage();

            NotificationIntent = getIntent();
            String intentType = NotificationIntent.getStringExtra("IntentType");
            if (intentType != null && !intentType.isEmpty() && intentType.matches("GCM")) {

                viewPager.setCurrentItem(1);
            }
           else if (intentType != null && !intentType.isEmpty() && intentType.matches("CONTACT")) {

                Fragment fragment2 = mAdapter.getItem(2);
                ContactFragment contactFragment = (ContactFragment) fragment2;
                contactFragment.Refresh();
                viewPager.setCurrentItem(2);
            }


            AlarmReceiver.setAlarm(this);
        }
        catch (Exception ex)
        {
            Toast.makeText(this, "Error Creating Main Page", Toast.LENGTH_LONG).show();
        }
    }

    public void MapReady()
    {
        try {

            RouteAllMeetings();
        }
        catch (Exception ex)
        {

        }
    }


      //  mAdapter.notifyDataSetChanged();

    @Override
    public void OnRouteCreated(ActiveMeeting meeting) {
        try {
            Fragment Fragment0 = mAdapter.getItem(0);
            MapFragment mapFragment = (MapFragment) Fragment0;
            mapFragment.AddOnMap(meeting);

           // RefreshActiveFragment();

            Fragment fragment1 = mAdapter.getItem(1);
            fragmentFragment fFragment = (fragmentFragment) fragment1;
            if (fFragment.CurrentFragment instanceof ActiveFragment) {
                ActiveFragment activeFragment = (ActiveFragment) fFragment.CurrentFragment;
                activeFragment.DataChanged();

            } else if (fFragment.CurrentFragment instanceof ChatFragment) {
                ChatFragment chatFragment = (ChatFragment) fFragment.CurrentFragment;
                if (chatFragment.MobileNumber.equalsIgnoreCase(meeting.DESTINATION_MOBILE_NO)) {
                    chatFragment.OnRouteCalculated(meeting);
                } else {
                    ActiveMeetingGroup.GetInstance(getApplicationContext()).RunningMeetings.get(meeting.DESTINATION_MOBILE_NO).newMessage++;
                    ActiveFragment activeFragment = (ActiveFragment) fFragment.CurrentFragment;
                    activeFragment.DataChanged();
                }
            }

        }
        catch (Exception ex)
        {
            UTILITY.HandleException(getApplicationContext(),"OnRouteCreated",ex.toString());
        }
    }

    //region GCM Listener


    @Override
    public void OnMessageReceived(String Message) {
        FilterMessage(Message);
    }

    @Override
    public void OnMeetingStatusChanged() {
        try {

            RefreshActiveFragment();
            RefreshContactFragment();

        }
        catch (Exception ex)
        {
            UTILITY.HandleException(getApplicationContext(), "OnMeetingStatusChanged", ex.toString());
        }
    }

    private void RefreshActiveFragment()
    {
        Fragment Fragment1 = mAdapter.getItem(1);
        fragmentFragment fFragment = (fragmentFragment) Fragment1;
        if(fFragment.CurrentFragment instanceof ActiveFragment)
        {
            ActiveFragment activeFragment = (ActiveFragment) fFragment.CurrentFragment;
            activeFragment.DataChanged();
        }

    }

    private void RefreshContactFragment()
    {
        Fragment fragment2 = mAdapter.getItem(2);
        ContactFragment contactFragment = (ContactFragment) fragment2;
        contactFragment.Refresh();
    }

    @Override
    public void OnMeetingForcedClear(String MobileNumber) {
       // OnMeetingComplete(MobileNumber);
        StopErrorDialog(this, MobileNumber);
    }



    @Override
    public void OnNewContact() {
        Fragment fragment2 = mAdapter.getItem(2);
        ContactFragment contactFragment = (ContactFragment) fragment2;
        contactFragment.NewContact(getApplicationContext());
    }

    //endregion

    @Override
    public void OnMeetingComplete(String MobileNumber) {
        try {
            DataAccess da = new DataAccess(getApplicationContext());
            da.open();
            da.deleteMessage(MobileNumber);
            da.UpdateAllActiveMeetings();
            da.close();
            ImageServer.DeleteBitmapImage(MobileNumber,this);
            Fragment fragment = mAdapter.getItem(0);
            MapFragment mapFragment = (MapFragment) fragment;
            mapFragment.RemoveFromMap(MobileNumber);

            Fragment Fragment1 = mAdapter.getItem(1);
            fragmentFragment fFragment = (fragmentFragment) Fragment1;
            if(fFragment.CurrentFragment instanceof ActiveFragment)
            {
                ActiveFragment activeFragment = (ActiveFragment) fFragment.CurrentFragment;
                activeFragment.DataChanged();
            }
           else if (fFragment.CurrentFragment instanceof ChatFragment) {
                ChatFragment chatFragment = (ChatFragment) fFragment.CurrentFragment;
                if (chatFragment.MobileNumber.equalsIgnoreCase(MobileNumber)) {
                    chatFragment.OnStatusChanged(APP_CONST.MEETING_STATUS_NONE);
                } else {
                    ActiveFragment activeFragment = (ActiveFragment) fFragment.CurrentFragment;
                    activeFragment.DataChanged();
                }
            }

            Fragment fragment2 = mAdapter.getItem(2);
            ContactFragment contactFragment = (ContactFragment) fragment2;
            contactFragment.Refresh();
        }
        catch (Exception ex)
        {
            Toast.makeText(this, "Error on Meeting Complete", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void OnStopAllMeetings() {
        try {
            Fragment fragment = mAdapter.getItem(0);
            MapFragment mapFragment = (MapFragment) fragment;
            mapFragment.ClearMap();

            Fragment fragment1 = mAdapter.getItem(1);
            ActiveFragment activeFragment = (ActiveFragment) fragment1;
            activeFragment.DataChanged();

            Fragment fragment2 = mAdapter.getItem(2);
            ContactFragment contactFragment = (ContactFragment) fragment2;
            contactFragment.Refresh();
        }
        catch (Exception ex)
        {
            UTILITY.HandleException(getApplicationContext(), "OnStopAllMeetings", ex.toString());
        }
    }

    @Override
    public void OnMeetingInitiate(String MobileNumber) {
        try{
        viewPager.setCurrentItem(1);
            Fragment Fragment1 = mAdapter.getItem(1);
            fragmentFragment fFragment = (fragmentFragment) Fragment1;
            if(fFragment.CurrentFragment instanceof ActiveFragment)
            {
                ActiveFragment activeFragment = (ActiveFragment)fFragment.CurrentFragment;
                activeFragment.DataChanged();
            }
        Fragment fragment2 = mAdapter.getItem(2);
        ContactFragment contactFragment = (ContactFragment) fragment2;
        contactFragment.Refresh();
        }
        catch (Exception ex)
        {
            UTILITY.HandleException(getApplicationContext(),"OnMeetingInitiate",ex.toString());
        }
    }

    @Override
    public void OnMeetingPause(String MobileNumber) {
        try{
            Fragment Fragment1 = mAdapter.getItem(1);
            fragmentFragment fFragment = (fragmentFragment) Fragment1;
            if(fFragment.CurrentFragment instanceof ActiveFragment)
            {
                ActiveFragment activeFragment = (ActiveFragment)fFragment.CurrentFragment;
                activeFragment.DataChanged();
            }
            }
            catch (Exception ex)
            {
                UTILITY.HandleException(getApplicationContext(),"OnMeetingPause",ex.toString());
            }
    }

    @Override
    public void OnMeetingResume(String MobileNumber) {
        try {
            Fragment Fragment1 = mAdapter.getItem(1);
            fragmentFragment fFragment = (fragmentFragment) Fragment1;
            if(fFragment.CurrentFragment instanceof ActiveFragment)
            {
                ActiveFragment activeFragment = (ActiveFragment)fFragment.CurrentFragment;
                activeFragment.DataChanged();
            }
        }
        catch (Exception ex)
        {
            UTILITY.HandleException(getApplicationContext(), "OnMeetingResume", ex.toString());
        }
    }

    public void OnTextSentByMap(String Mobile, String Message)
    {
        try {
            Fragment Fragment1 = mAdapter.getItem(1);
            fragmentFragment fFragment = (fragmentFragment) Fragment1;
            if (fFragment.CurrentFragment instanceof ActiveFragment) {
                ActiveFragment activeFragment = (ActiveFragment) fFragment.CurrentFragment;
                activeFragment.DataChanged();
            } else if (fFragment.CurrentFragment instanceof ChatFragment) {
                ChatFragment chatFragment = (ChatFragment) fFragment.CurrentFragment;
                if (chatFragment.MobileNumber.equalsIgnoreCase(Mobile)) {
                    chatFragment.OnTextSent(Mobile, "ME", Message);
                } else {
                    ActiveFragment activeFragment = (ActiveFragment) fFragment.CurrentFragment;
                    activeFragment.DataChanged();
                }
            }
        }
        catch (Exception ex)
        {
            UTILITY.HandleException(getApplicationContext(), "OnTextSentByMap", ex.toString());
        }
    }


    //region Location Listener

    @Override
    public void onLocationChanged(Location location) {
        try {
            myLocation = location;
            // Toast.makeText(this, "My Location Latitude : " + Double.toString(location.getLatitude()) + " Longitude : " + Double.toString(location.getLongitude()), Toast.LENGTH_LONG).show();

           APP_VARIABLES.MY_LOCATION_STRING = Double.toString(location.getLatitude()) + "," + Double.toString(location.getLongitude());

            Fragment fragment = mAdapter.getItem(0);
            MapFragment mapFragment = (MapFragment) fragment;

            mapFragment.AddUserMarkup(APP_VARIABLES.MY_MOBILE_NUMBER, myProfile.UserID ,"ME","",APP_VARIABLES.MY_LOCATION_STRING, this);
            mapFragment.SetMapBound(APP_VARIABLES.MY_LOCATION_STRING);

            RouteAllMeetings();

        }
        catch (Exception ex)
        {
            Toast.makeText(this,"Error occurred on Location Changed", Toast.LENGTH_LONG).show();
        }

    }


    private void RouteAllMeetings()
    {
        try {
            ActiveMeetingGroup meetGroup = ActiveMeetingGroup.GetInstance(getApplicationContext());
            if (meetGroup.RunningMeetings.size() > 0) {

                for (ActiveMeeting meeting : meetGroup.RunningMeetings.values())
                {
                  /*  if(UTILITY.MinutesDifference(UTILITY.CurrentLocalDateTimeString(),meeting.UPDATE_TIME)>15)
                    {
                        if(meeting.MEETING_STATUS.matches(APP_CONST.MEETING_STATUS_RECEIVING_LOCATION))
                        {
                            ActiveMeetingGroup.GetInstance(getApplicationContext()).RunningMeetings.remove(meeting.DESTINATION_MOBILE_NO);
                            ImageServer.DeleteBitmapImage(meeting.DESTINATION_MOBILE_NO, getApplicationContext());
                            OnMeetingComplete(meeting.DESTINATION_MOBILE_NO);
                        }
                    }
                    */

                    if (UTILITY.MinutesDifference(UTILITY.CurrentLocalDateTimeString(), meeting.START_TIME) > APP_SETTINGS.MEETING_LIFE_MINUTE) {
                        if (UTILITY.MinutesDifference(UTILITY.CurrentLocalDateTimeString(), meeting.START_TIME) >( APP_SETTINGS.MEETING_LIFE_MINUTE +15))
                            {
                                ShowSnackBar("Meeting with " + meeting.DESTINATION_NAME + " closing");
                                Log.AddLog("Meeting with " + meeting.DESTINATION_NAME + " closed for exceeding time!", this);
                                meetGroup.RunningMeetings.remove(meeting.DESTINATION_MOBILE_NO);
                                OnMeetingComplete(meeting.DESTINATION_MOBILE_NO);
                            }
                            else
                            {
                                break;
                            }

                    }
                    else if (APP_VARIABLES.NETWORK_STATUS == false) {
                        ShowSnackBar("Network unavailable!");
                        break;
                    }
                    else if (meeting.MEETING_STATUS.matches(APP_CONST.MEETING_STATUS_SHARING_LOCATION)) {

                        if(GetAerialDistance(PreviousLocation,APP_VARIABLES.MY_LOCATION_STRING)> APP_SETTINGS.LOCATION_UPDATE_DISTANCE_INTERVAL) {
                            meeting.sendNotification(this, APP_VARIABLES.MY_LOCATION_STRING, Message.LOCATION);
                            InitiateRouting(meeting.DESTINATION_MOBILE_NO,meeting.DESTINATION_USER_ID , meeting.DESTINATION_LATLONG);
                            PreviousLocation  = APP_VARIABLES.MY_LOCATION_STRING;
                        }
                    }
                    else if(meeting.MEETING_STATUS.matches(APP_CONST.MEETING_STATUS_RECEIVING_LOCATION))
                    {
                        if(GetAerialDistance(PreviousLocation,APP_VARIABLES.MY_LOCATION_STRING)> APP_SETTINGS.LOCATION_UPDATE_DISTANCE_INTERVAL) {
                            InitiateRouting(meeting.DESTINATION_MOBILE_NO,meeting.DESTINATION_USER_ID, meeting.DESTINATION_LATLONG);
                            PreviousLocation  = APP_VARIABLES.MY_LOCATION_STRING;
                        }
                    }
                    else if(meeting.MEETING_STATUS.matches(APP_CONST.MEETING_STATUS_SENDING_LOCATION))
                    {
                        if(GetAerialDistance(PreviousLocation,APP_VARIABLES.MY_LOCATION_STRING)> APP_SETTINGS.LOCATION_UPDATE_DISTANCE_INTERVAL) {
                            meeting.sendNotification(this, APP_VARIABLES.MY_LOCATION_STRING, Message.LOCATION);
                            PreviousLocation  = APP_VARIABLES.MY_LOCATION_STRING;
                        }
                    }
                }

            }
        }
        catch (Exception ex)
        {
         UTILITY.HandleException(getApplicationContext(),"RouteAllMeeting",ex.toString());
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    //endregion

    private void registerLocationListener()
    {
            locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        try{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,APP_SETTINGS.LOCATION_UPDATE_INTERVAL_MILLISECONDS, APP_SETTINGS.LOCATION_UPDATE_DISTANCE_INTERVAL, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, APP_SETTINGS.LOCATION_UPDATE_INTERVAL_MILLISECONDS, APP_SETTINGS.LOCATION_UPDATE_DISTANCE_INTERVAL, this);

        }
        catch (SecurityException sEx)
        {
            Toast.makeText(this, "Location permission Not available", Toast.LENGTH_SHORT);
        }
        catch(Exception ex)
        {
            Toast.makeText(this, "Location Not available", Toast.LENGTH_SHORT);
        }
    }


    private class DrawerItemSelected implements NavigationView.OnNavigationItemSelectedListener
    {
        @Override
        public boolean onNavigationItemSelected(MenuItem item) {

            switch (item.getItemId())
            {
                case R.id.invite_friend:
                    SendMessage();
                    mDrawerLayout.closeDrawers();
                    return true;
                case R.id.show_log:
                    Intent logIntent = new Intent(MainActivity.this,LogActivity.class);
                    startActivity(logIntent);
                    mDrawerLayout.closeDrawers();
                    return true;
                case R.id.Logoff:
                    LogOff();
                    mDrawerLayout.closeDrawers();
                    return true;
                case R.id.edit_profile:
                    Intent registerIntent = new Intent(MainActivity.this, net.anvisys.letscatch.Application.ProfileActivity.class);
                    startActivity(registerIntent);
                    mDrawerLayout.closeDrawers();
                    return true;
                case R.id.help:
                    Intent helpIntent = new Intent(MainActivity.this,HelpActivity.class);
                    startActivity(helpIntent);
                    mDrawerLayout.closeDrawers();
                    return true;
                case R.id.about:
                    Intent aboutIntent = new Intent(MainActivity.this,AboutActivity.class);
                    startActivity(aboutIntent);
                    mDrawerLayout.closeDrawers();
                    return true;
                case R.id.setting:
                    Intent settingIntent = new Intent(MainActivity.this, net.anvisys.letscatch.Application.SettingActivity.class);
                    startActivity(settingIntent);
                    mDrawerLayout.closeDrawers();
                    return true;
                default:
                    return true;
            }
        }

    }

    public void LogOff()
    {

        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle("LogOff");
        dialog.setMessage("Confirm");
        dialog.setCancelable(false);
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                return;
            }
        });

        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    if (Session.LogOff(getApplicationContext())) {
                        Toast.makeText(getApplicationContext(), "Successfully Un-Registered", Toast.LENGTH_LONG).show();
                        MainActivity.this.finish();
                        AlarmReceiver.cancelAlarm(getApplicationContext());
                        DataAccess da = new DataAccess(getApplicationContext());
                        da.open();
                        da.ClearAll();
                        da.close();

                        return;
                    }
                }
                catch (Exception ex)
                {

                }
            }
        });
        dialog.show();

    }

   public void SendMessage()
   {
       try {
           String InviteMessage = "Try the Lets Catch app to Locate and track your friend  https://play.google.com/store/apps/details?id=net.anvisys.letscatch";
           Intent sendIntent = new Intent();
           sendIntent.setAction(Intent.ACTION_SEND);
           sendIntent.putExtra(Intent.EXTRA_TEXT,InviteMessage);
           sendIntent.setType("text/plain");
           startActivity(Intent.createChooser(sendIntent,"SEND"));
       }
       catch (Exception ex)
       {
           Toast.makeText(this, "Error sending Text", Toast.LENGTH_LONG).show();
       }
   }

    @Override
    public void ContactAdded(String message) {
        Fragment fragment2 = mAdapter.getItem(2);
        ContactFragment contactFragment = (ContactFragment) fragment2;
        contactFragment.Refresh();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        try {
            DataAccess da = new DataAccess(getApplicationContext());
            da.open();
            da.UpdateAllActiveMeetings();
            da.close();
            ContactSyncService.UNRegisterListener();
            Session.SetApplicationStatus(this, false);
            if (markerTimer != null) {
                markerTimer.cancel();
                markerTimer.purge();
                markerTimer = null;
            }
            super.onDestroy();
        }
        catch (Exception ex)
        {
            UTILITY.HandleException(getApplicationContext(),"OnDestroy",ex.toString());
        }
    }



    public void OnClick(View v)
    {

        if(v == profileImage)
        {
            try {
                long time = SystemClock.currentThreadTimeMillis();

                if (prevTime == 0) {
                    prevTime = SystemClock.currentThreadTimeMillis();
                }

                if (time - prevTime > 1000) {
                    prevTime = time;
                    ClickCount=0;
                }
                if (time - prevTime < 1000 && time > prevTime) {
                    ClickCount++;
                    if (ClickCount == 5) {
                        Toast.makeText(getApplicationContext(), "Clicked " + ClickCount + " times ", Toast.LENGTH_LONG).show();
                        mDrawerLayout.closeDrawers();
                        ClickCount = 0;
                        prevTime = 0;
                    } else {
                        prevTime = time;
                        String msg = (5-ClickCount) + " more times to send sos";
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                    }
                }
            }
            catch (Exception ex)
            {
                Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

    }



    private void FilterMessage(String message)
    {
        try {
            String[] msgArray = message.split("&");
            String msgType = msgArray[0];
            final int senderID = Integer.parseInt(msgArray[1]);
            final String senderMobile = msgArray[2];
            final String senderName = msgArray[3];
            final String  DataReceived = msgArray[4];
            ActiveMeetingGroup meetingGroup = ActiveMeetingGroup.GetInstance(getApplicationContext());

            switch (msgType) {
                case Message.SEND_LOCATION: {
                    DataAccess da = new DataAccess(getApplicationContext());
                    da.open();
                    if(meetingGroup.RunningMeetings.containsKey(senderMobile))
                    {
                        ActiveMeeting activeMeeting = meetingGroup.RunningMeetings.get(senderMobile);
                        if(activeMeeting.MEETING_STATUS.matches(APP_CONST.MEETING_STATUS_SENDING_LOCATION))
                        {
                            activeMeeting.MEETING_STATUS = APP_CONST.MEETING_STATUS_SHARING_LOCATION;
                        }
                    }
                    else {
                        ActiveMeeting tempMeeting = new ActiveMeeting(APP_CONST.MEETING_TYPE_INSTANT,"I Am", APP_VARIABLES.MY_MOBILE_NUMBER, APP_VARIABLES.MY_LOCATION_STRING,senderMobile, senderID ,senderName, DataReceived, UTILITY.CurrentLocalDateTimeString());
                        tempMeeting.MEETING_STATUS =APP_CONST.MEETING_STATUS_RECEIVING_LOCATION;
                        RefreshContactFragment();

                        Contact cont =da.getContactByMobile(senderMobile);

                        if(cont!=null) {
                            ImageServer.SaveBitmapImage(ImageServer.getBitmapFromString(cont.strImage, getApplicationContext()), cont.MobileNumber, getApplicationContext());
                        }
                        ActiveMeetingGroup.GetInstance(getApplicationContext()).RunningMeetings.put(senderMobile, tempMeeting);
                    }
                    da.UpdateAllActiveMeetings();
                    da.close();
                    InitiateRouting(senderMobile,senderID, DataReceived);
                    break;
                }
                case Message.LOCATION: {

                    if(!meetingGroup.RunningMeetings.containsKey(senderMobile))
                    {
                       // InviteReceived(Name, Mobile, strYourLocation);
                        ActiveMeeting tempMeeting = new ActiveMeeting(APP_CONST.MEETING_TYPE_INSTANT,"I Am", APP_VARIABLES.MY_MOBILE_NUMBER , APP_VARIABLES.MY_LOCATION_STRING,senderMobile,senderID,senderName,
                                DataReceived, UTILITY.CurrentLocalDateTimeString());
                        tempMeeting.MEETING_STATUS =APP_CONST.MEETING_STATUS_RECEIVING_LOCATION;
                        DataAccess da = new DataAccess(getApplicationContext());
                        da.open();
                        Contact cont =da.getContactByMobile(senderMobile);
                        da.close();
                        if(cont!=null) {
                           // ImageServer.SaveBitmapImage(ImageServer.getBitmapFromString(cont.strImage, getApplicationContext()), cont.MobileNumber, getApplicationContext());
                        }
                        ActiveMeetingGroup.GetInstance(getApplicationContext()).RunningMeetings.put(senderMobile, tempMeeting);
                        InitiateRouting(senderMobile,senderID, DataReceived);
                     }

                    else
                    {
                      ActiveMeeting meeting  =  meetingGroup.RunningMeetings.get(senderMobile);

                        if(meeting.MEETING_STATUS.matches(APP_CONST.MEETING_STATUS_SENDING_LOCATION))
                        {
                            meeting.MEETING_STATUS = APP_CONST.MEETING_STATUS_SHARING_LOCATION;
                        }

                        InitiateRouting(senderMobile,senderID, DataReceived);
                    }

                    break;
                    }


                case Message.STOP_TRACKING: {

                    if(meetingGroup.RunningMeetings.containsKey(senderMobile)) {
                        ActiveMeeting meeting = meetingGroup.RunningMeetings.get(senderMobile);
                      //  meetingGroup.RunningMeetings.remove(Mobile);
                        DataAccess da = new DataAccess(getApplicationContext());
                        da.open();

                        if(meeting.MEETING_STATUS.matches(APP_CONST.MEETING_STATUS_SHARING_LOCATION))
                        {
                            meeting.MEETING_STATUS = APP_CONST.MEETING_STATUS_SENDING_LOCATION;
                            da.UpdateActiveMeeting(meeting.DESTINATION_MOBILE_NO, meeting.MEETING_STATUS);

                        }
                        else {
                            ActiveMeetingGroup.GetInstance(getApplicationContext()).RunningMeetings.remove(meeting.DESTINATION_MOBILE_NO);
                            meeting.MEETING_STATUS = APP_CONST.MEETING_STATUS_NONE;
                            da.deleteMessage(meeting.DESTINATION_MOBILE_NO);
                            ImageServer.DeleteBitmapImage(meeting.DESTINATION_MOBILE_NO, getApplicationContext());

                        }
                        da.UpdateAllActiveMeetings();
                        da.close();
                        //Session.UpdateSessionMeeting(getApplicationContext());
                        Fragment fragment = mAdapter.getItem(0);
                        MapFragment mapFragment = (MapFragment) fragment;
                        mapFragment.RemoveFromMap(senderMobile);
                        OnMeetingStatusChanged();

                        Fragment fragment1 = mAdapter.getItem(1);
                        fragmentFragment fFragment = (fragmentFragment) fragment1;
                        if (fFragment.CurrentFragment instanceof ActiveFragment) {
                            ActiveFragment activeFragment = (ActiveFragment) fFragment.CurrentFragment;
                            activeFragment.DataChanged();
                        }
                        else if (fFragment.CurrentFragment instanceof ChatFragment) {
                            ChatFragment chatFragment = (ChatFragment) fFragment.CurrentFragment;
                            if (chatFragment.MobileNumber.matches(senderMobile)) {
                                chatFragment.OnStatusChanged(meeting.MEETING_STATUS);
                            } else {
                                ActiveFragment activeFragment = (ActiveFragment) fFragment.CurrentFragment;
                                activeFragment.DataChanged();
                            }
                        }
                    }
                    break;
                }

                case Message.TEXT_MESSAGE: {
                    try {
                        ChatMessage chat = new ChatMessage();
                        chat.Dest_Mobile = senderMobile;
                        chat.Mobile = senderMobile;
                        chat.Message = DataReceived;
                        chat.dateTime = UTILITY.CurrentLocalDateTimeString();
                        DataAccess da = new DataAccess(getApplicationContext());
                        da.open();
                        da.insertNewMessage(senderMobile, senderMobile,Message.TEXT_MESSAGE ,DataReceived);
                        da.close();

                        if (CurrentPage.matches("MapFragment")) {
                            Fragment fragment0 = mAdapter.getItem(0);
                            MapFragment mapFragment = (MapFragment) fragment0;
                            mapFragment.OnTextSent(senderMobile, senderMobile, DataReceived);

                            Fragment fragment1 = mAdapter.getItem(1);
                            fragmentFragment fFragment = (fragmentFragment) fragment1;
                            if (fFragment.CurrentFragment instanceof ActiveFragment) {
                                ActiveFragment activeFragment = (ActiveFragment) fFragment.CurrentFragment;
                                ActiveMeetingGroup.GetInstance(getApplicationContext()).RunningMeetings.get(senderMobile).newMessage++;
                                activeFragment.DataChanged();
                            } else if (fFragment.CurrentFragment instanceof ChatFragment) {
                                ChatFragment chatFragment = (ChatFragment) fFragment.CurrentFragment;
                                if (chatFragment.MobileNumber.equalsIgnoreCase(senderMobile)) {
                                    chatFragment.OnTextSent(senderMobile, senderMobile, DataReceived);
                                } else {
                                    ActiveMeetingGroup.GetInstance(getApplicationContext()).RunningMeetings.get(senderMobile).newMessage++;
                                    RefreshActiveFragment();
                                }
                            }
                        } else if (CurrentPage.matches("fragmentFragment")) {
                            Fragment fragment1 = mAdapter.getItem(1);
                            fragmentFragment fFragment = (fragmentFragment) fragment1;
                            if (fFragment.CurrentFragment instanceof ActiveFragment) {
                                ActiveFragment activeFragment = (ActiveFragment) fFragment.CurrentFragment;
                                ActiveMeetingGroup.GetInstance(getApplicationContext()).RunningMeetings.get(senderMobile).newMessage++;
                                activeFragment.DataChanged();
                            } else if (fFragment.CurrentFragment instanceof ChatFragment) {
                                ChatFragment chatFragment = (ChatFragment) fFragment.CurrentFragment;
                                if (chatFragment.MobileNumber.equalsIgnoreCase(senderMobile)) {
                                    chatFragment.OnTextSent(senderMobile, senderMobile, DataReceived);
                                } else {
                                    ActiveMeetingGroup.GetInstance(getApplicationContext()).RunningMeetings.get(senderMobile).newMessage++;
                                    RefreshActiveFragment();
                                }
                            }

                        } else if (CurrentPage.matches("ContactFragment")) {
                            Fragment fragment1 = mAdapter.getItem(1);
                            fragmentFragment fFragment = (fragmentFragment) fragment1;
                            if (fFragment.CurrentFragment instanceof ActiveFragment) {
                                ActiveFragment activeFragment = (ActiveFragment) fFragment.CurrentFragment;
                                ActiveMeetingGroup.GetInstance(getApplicationContext()).RunningMeetings.get(senderMobile).newMessage++;
                                activeFragment.DataChanged();
                            } else if (fFragment.CurrentFragment instanceof ChatFragment) {
                                ChatFragment chatFragment = (ChatFragment) fFragment.CurrentFragment;
                                if (chatFragment.MobileNumber.matches(senderMobile)) {
                                    chatFragment.OnTextSent(senderMobile, senderMobile, DataReceived);
                                } else {
                                    ActiveMeetingGroup.GetInstance(getApplicationContext()).RunningMeetings.get(senderMobile).newMessage++;
                                    RefreshActiveFragment();
                                }
                            }
                        }
                    }
                        catch (Exception ex)
                        {
                            UTILITY.HandleException(getApplicationContext(),"Message Received",ex.toString());
                        }
                    break;
                    }
                case Message.IMAGE_MESSAGE: {
                    try {
                        String Name = "temp_" + UTILITY.CurrentLocalTimeString();
                        GetImage(senderMobile,Name,DataReceived);
                      /*
                        ChatMessage chat = new ChatMessage();
                        chat.Dest_Mobile = senderMobile;
                        chat.Mobile = senderMobile;
                        chat.ImageName = DataReceived;

                       // chat.ImageName = "temp_" + UTILITY.CurrentLocalTimeString();
                        chat.dateTime = UTILITY.CurrentLocalDateTimeString();


                        if (CurrentPage.matches("MapFragment")) {
                            Fragment fragment0 = mAdapter.getItem(0);
                            MapFragment mapFragment = (MapFragment) fragment0;
                            mapFragment.OnTextSent(senderMobile, senderMobile, "image..");

                            Fragment fragment1 = mAdapter.getItem(1);
                            fragmentFragment fFragment = (fragmentFragment) fragment1;
                            if (fFragment.CurrentFragment instanceof ActiveFragment) {
                                ActiveFragment activeFragment = (ActiveFragment) fFragment.CurrentFragment;
                                ActiveMeetingGroup.GetInstance(getApplicationContext()).RunningMeetings.get(senderMobile).newMessage++;
                                activeFragment.DataChanged();
                                GetImage(senderMobile,Name,DataReceived);
                            } else if (fFragment.CurrentFragment instanceof ChatFragment) {
                                ChatFragment chatFragment = (ChatFragment) fFragment.CurrentFragment;
                                if (chatFragment.MobileNumber.equalsIgnoreCase(senderMobile)) {
                                    chatFragment.OnImageReceived(senderMobile, senderMobile, chat.ImageName);
                                } else {
                                    ActiveMeetingGroup.GetInstance(getApplicationContext()).RunningMeetings.get(senderMobile).newMessage++;
                                    RefreshActiveFragment();
                                    GetImage(senderMobile,Name, DataReceived);
                                }
                            }
                        } else if (CurrentPage.matches("fragmentFragment")) {
                            Fragment fragment1 = mAdapter.getItem(1);
                            fragmentFragment fFragment = (fragmentFragment) fragment1;
                            if (fFragment.CurrentFragment instanceof ActiveFragment) {
                                ActiveFragment activeFragment = (ActiveFragment) fFragment.CurrentFragment;
                                ActiveMeetingGroup.GetInstance(getApplicationContext()).RunningMeetings.get(senderMobile).newMessage++;
                                activeFragment.DataChanged();
                                GetImage(senderMobile,Name, DataReceived);
                            } else if (fFragment.CurrentFragment instanceof ChatFragment) {
                                ChatFragment chatFragment = (ChatFragment) fFragment.CurrentFragment;
                                if (chatFragment.MobileNumber.equalsIgnoreCase(senderMobile)) {
                                    chatFragment.OnImageReceived(senderMobile, senderMobile, chat.ImageName);
                                } else {
                                    ActiveMeetingGroup.GetInstance(getApplicationContext()).RunningMeetings.get(senderMobile).newMessage++;
                                    RefreshActiveFragment();
                                    GetImage(senderMobile,Name, DataReceived);
                                }
                            }

                        } else if (CurrentPage.matches("ContactFragment")) {
                            Fragment fragment1 = mAdapter.getItem(1);
                            fragmentFragment fFragment = (fragmentFragment) fragment1;
                            if (fFragment.CurrentFragment instanceof ActiveFragment) {
                                ActiveFragment activeFragment = (ActiveFragment) fFragment.CurrentFragment;
                                ActiveMeetingGroup.GetInstance(getApplicationContext()).RunningMeetings.get(senderMobile).newMessage++;
                                activeFragment.DataChanged();
                                GetImage(senderMobile,Name, DataReceived);
                            } else if (fFragment.CurrentFragment instanceof ChatFragment) {
                                ChatFragment chatFragment = (ChatFragment) fFragment.CurrentFragment;
                                if (chatFragment.MobileNumber.matches(senderMobile)) {
                                    chatFragment.OnImageReceived(senderMobile, senderMobile, chat.ImageName);
                                } else {
                                    ActiveMeetingGroup.GetInstance(getApplicationContext()).RunningMeetings.get(senderMobile).newMessage++;
                                    RefreshActiveFragment();
                                    GetImage(senderMobile,Name, DataReceived);
                                }
                            }
                        }*/
                    }
                    catch (Exception ex)
                    {
                        UTILITY.HandleException(getApplicationContext(),"Message Received",ex.toString());
                    }
                }
                    break;
                }


        }
        catch (Exception ex)
        {
           UTILITY.HandleException(getApplicationContext(),"FilterMessage",ex.toString());
        }

    }



    public void ShowSnackBar(String msg)
    {
        try {
            Snackbar sBar = Snackbar
                    .make(coordinatorLayout, msg, Snackbar.LENGTH_LONG);

            View snackBarView = sBar.getView();
            //  snackBarView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            snackBarView.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
            sBar.show();
        }
        catch (Exception ex)
        {
            UTILITY.HandleException(getApplicationContext(),"ShowSnackBar",ex.toString());
        }

    }

public void InitiateRouting(String Mobile, int UserID, String Location)
{
    try {
        if (ActiveMeetingGroup.GetInstance(this).RunningMeetings.containsKey(Mobile)) {
            ActiveMeeting meeting = ActiveMeetingGroup.GetInstance(this).RunningMeetings.get(Mobile);
            Fragment fragment = mAdapter.getItem(0);
            MapFragment mapFragment = (MapFragment) fragment;
            mapFragment.AddUserMarkup(Mobile,UserID, meeting.DESTINATION_NAME, "", Location, this);
            mapFragment.SetMapBound(Location);
            // meeting.ORIGIN_LATLONG = APP_VARIABLES.MY_LOCATION_STRING;
            meeting.DESTINATION_LATLONG = Location;
            meeting.RegisterMeetingListener(this);
            meeting.SetMapFeatures(Location);
        }
    }
    catch (Exception ex)
    {

    }

}

    private void StopErrorDialog(final Context context, final String MobileNo)
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("STOP/Reject Error");
        dialog.setMessage("Error in Stopping/Rejecting");
        dialog.setCancelable(false);
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


            }
        });
        dialog.setPositiveButton("Clear Anyway", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ActiveMeetingGroup.GetInstance(context).RunningMeetings.remove(MobileNo);
                DataAccess da = new DataAccess(context);
                da.open();
                da.deleteMessage(MobileNo);
                da.close();
                ImageServer.DeleteBitmapImage(MobileNo, context);
                // Session.UpdateSessionMeeting(context);
                OnMeetingComplete(MobileNo);
            }
        });
        AlertDialog alert = dialog.create();
        alert.show();
    }




    public  Double GetAerialDistance( String previousLocation, String newLocation)
    {
        Double dis=0.0;
        try {
            String[] previous = previousLocation.split(",");
            String[] newLoc = newLocation.split(",");

            Double prevLat =  Double.parseDouble(previous[0]);
            Double prevLong =  Double.parseDouble(previous[1]);

            Double newLat =  Double.parseDouble(newLoc[0]);
            Double newLong =  Double.parseDouble(newLoc[1]);

            double factor = 2*3.14*6370*1000/360;

            Double diffLat = newLat- prevLat;

            Double diffLong = newLong - prevLong;

            dis = (Math.sqrt( diffLat*diffLat + diffLong*diffLong))*factor;

            return dis;

        }
        catch (Exception ex)
        {
            return dis;
        }

    }



    public void GetImage(final String senderMobile, final  String name ,final String file)
    {
        try {
            String escapedFilepath = file.replace("\\", "\\\\");
            String reqBody = "{\"FilePath\":\"" + escapedFilepath + "\"}";
            String url = APP_CONST.APP_SERVER_URL + "api/ImageMessage";
            JSONObject jsRequest = new JSONObject(reqBody);

            //-------------------------------------------------------------------------------------------------
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            JsonObjectRequest jsArrayRequest = new JsonObjectRequest(Request.Method.POST, url,jsRequest, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jObject) {

                    try{
                        String strImage =  jObject.getString("ImageString");
                        Bitmap bmp = ImageServer.getBitmapFromString(strImage, getApplicationContext());
                        ImageServer.SaveBitmapToExternal(bmp, name, getApplicationContext());

                        DataAccess da = new DataAccess(getApplicationContext());
                        da.open();
                        da.insertNewMessage(senderMobile, senderMobile, Message.IMAGE_MESSAGE, name);
                        da.close();
                        if (CurrentPage.matches("MapFragment")) {
                            Fragment fragment0 = mAdapter.getItem(0);
                            MapFragment mapFragment = (MapFragment) fragment0;
                            mapFragment.OnTextSent(senderMobile, senderMobile, "image..");
                        }

                        Fragment fragment1 = mAdapter.getItem(1);
                        fragmentFragment fFragment = (fragmentFragment) fragment1;
                         if (fFragment.CurrentFragment instanceof ChatFragment) {
                            ChatFragment chatFragment = (ChatFragment) fFragment.CurrentFragment;
                            if (chatFragment.MobileNumber.equalsIgnoreCase(senderMobile)) {
                              chatFragment.OnImageReceived(senderMobile, senderMobile, name);
                            }
                             else
                            {
                                ActiveMeetingGroup.GetInstance(getApplicationContext()).RunningMeetings.get(senderMobile).newMessage++;
                            }
                        }
                        else
                         {
                             ActiveFragment activeFragment = (ActiveFragment) fFragment.CurrentFragment;
                             ActiveMeetingGroup.GetInstance(getApplicationContext()).RunningMeetings.get(senderMobile).newMessage++;
                             activeFragment.DataChanged();
                         }

                    }
                    catch (JSONException e)
                    {
                        // HideSnackBar();
                    }

                    catch (Exception ex)
                    {
                        int b =8;
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //   ShowSnackBar("Could not refresh data", "Retry");
                }
            });
            RetryPolicy policy = new RetryPolicy() {
                @Override
                public int getCurrentTimeout() {
                    return 10000;
                }

                @Override
                public int getCurrentRetryCount() {
                    return 3;
                }

                @Override
                public void retry(VolleyError error) throws VolleyError {

                }
            };

            jsArrayRequest.setRetryPolicy(policy);
            queue.add(jsArrayRequest);
        }
        catch (JSONException e)
        {
            // HideSnackBar();
        }
    }


    private void StopBlinkTimer()
    {
        if(markerTimer!= null)
        {
            timeTask.cancel();
            markerTimer.cancel();
            markerTimer.purge();
            markerTimer = null;
        }
    }

    private void InitiateScheduleBlink(final String mobile, final String Name,final String time, final String location)
    {
        ScheduledExecutorService scheduleTaskExecutor = Executors.newScheduledThreadPool(10);
        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Fragment fragment = mAdapter.getItem(0);
                            MapFragment mapFragment = (MapFragment) fragment;
                         //   mapFragment.BlinkMarkup(mobile, Name, time, location, getApplicationContext());
                        } catch (Exception ex) {
                            int a = 1;
                        }
                    }
                });

            }
        },0,5, TimeUnit.SECONDS);
    }

    private void InitiateBlink(final String mobile, final String Name,final String time, final String location) {
        try {
            markerTimer = new Timer(mobile,true);


            timeTask = new TimerTask() {
                @Override
                public void run() {
                    Count++;
                    if(Count >10)
                    {
                        StopBlinkTimer();
                        Count=0;
                    }
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run() {
                            try {

                                Fragment fragment = mAdapter.getItem(0);
                                MapFragment mapFragment = (MapFragment) fragment;
                               // mapFragment.BlinkMarkup(mobile, Name, time, location, getApplicationContext());
                            } catch (Exception ex) {
                                int a = 1;
                            }
                        }
                    });

                }
            };
            markerTimer.scheduleAtFixedRate(timeTask,0,1000);

/*
            markerTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Count++;
                                if(Count >10)
                                {
                                    markerTimer.cancel();
                                    markerTimer.purge();
                                }
                                Fragment fragment = mAdapter.getItem(0);
                                MapFragment mapFragment = (MapFragment) fragment;
                                mapFragment.BlinkMarkup(mobile, Name, time, location, getApplicationContext());
                            } catch (Exception ex) {
                                int a = 1;
                            }
                        }
                    });
                }
            }, 0, 1000);

            */
        }
        catch (Exception Ex)
        {
            int a =1;
        }
    }



}
