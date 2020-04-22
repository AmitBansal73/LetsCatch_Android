package net.anvisys.letscatch;


import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import net.anvisys.letscatch.Common.DataAccess;
import net.anvisys.letscatch.Common.ImageServer;
import net.anvisys.letscatch.Common.Session;
import net.anvisys.letscatch.Common.UTILITY;
import net.anvisys.letscatch.Object.APP_CONST;
import net.anvisys.letscatch.Object.APP_VARIABLES;
import net.anvisys.letscatch.Object.ActiveMeeting;
import net.anvisys.letscatch.Object.ActiveMeetingGroup;
import net.anvisys.letscatch.Object.ChatMessage;
import net.anvisys.letscatch.Object.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Timer;

import static com.android.volley.VolleyLog.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements
        OnMapReadyCallback,
        LocationListener,
        ActiveMeeting.ChatUpdateListener
      {

    private static GoogleMap mMap;
    private LatLng mapCenter = null;
    private static HashMap<String,Marker> currentMeetingsMarker;
    private String myMobile="";
    private static HashMap<String,Polyline> currentMeetingsPolyline = new HashMap<String,Polyline>();
          private    ChatMessage newChat;
    private static LatLngBounds mapBounds;
    private Marker selectedMarker;
    private ActiveMeeting selectedMeeting;
    public Timer  markerTimer;
          View myReply;
          EditText myMessage;
          ImageView sendButton;
          private      int    MSG_ID =100;

    public MapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_map, container, false);
        myReply = rootView.findViewById(R.id.myReply);
        myMessage = (EditText)rootView.findViewById(R.id.myMessage);
        sendButton = (ImageView)rootView.findViewById(R.id.sendImage);
        currentMeetingsMarker = new HashMap<String,Marker>();
        myMessage.requestFocus();
        myMessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    //getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);

                    imm.showSoftInput(view,0);
                } else {
                    // getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(myReply.getWindowToken(),0);
                }
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = myMessage.getText().toString();
                     if(!msg.matches(""))
                    {
                        try {
                            MSG_ID++;
                            newChat = new ChatMessage();
                            newChat.ID = MSG_ID;
                            newChat.Dest_Mobile = selectedMeeting.DESTINATION_MOBILE_NO;
                            newChat.Mobile = "ME";
                            newChat.Message = msg;
                            newChat.dateTime = UTILITY.CurrentLocalDateTimeString();
                            myMessage.setText("");
                            newChat.Delivery_Status = 0;
                            OnTextSent(newChat.Dest_Mobile, "ME", newChat.Message);
                            sendMessage(getContext(), newChat, Message.TEXT_MESSAGE);
                        }
                        catch (Exception ex)
                        {
                            UTILITY.HandleException(getContext(),"SentMessage",ex.toString());
                        }

                    /*    if (ActiveMeetingGroup.GetInstance(getContext()).RunningMeetings.containsKey(selectedMeeting.DESTINATION_MOBILE_NO)) {
                            ActiveMeeting meeting = ActiveMeetingGroup.GetInstance(getContext()).RunningMeetings.get(selectedMeeting.DESTINATION_MOBILE_NO);
                            meeting.RegisterChatListener(MapFragment.this);
                            meeting.sendNotification(getContext(),msg, Message.TEXT_MESSAGE);
                        }
                        */
                }
            }
        });

        try {

            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
            if (mapFragment == null) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                mapFragment = SupportMapFragment.newInstance();
                fragmentTransaction.replace(R.id.map_fragment, mapFragment).commit();
            }
            mapFragment.getMapAsync(this);
        }
        catch (Exception ex)
        {
            Toast.makeText(getContext(), "Could not create Map", Toast.LENGTH_LONG).show();
        }

        setHasOptionsMenu(true);
       // getActivity().invalidateOptionsMenu();
        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
            try {
                mMap = googleMap;
                mMap.setMyLocationEnabled(true);
                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {

                        if (!marker.getTitle().matches("ME")) {
                            String text="";
                            try {
                                selectedMarker = marker;
                                selectedMeeting = ActiveMeetingGroup.GetInstance(getContext()).GetMeetingByName(marker.getTitle());
                                getActivity().invalidateOptionsMenu();

                                DataAccess da = new DataAccess(getContext());
                                da.open();
                                ChatMessage chat = da.GetLastMessage(selectedMeeting.DESTINATION_MOBILE_NO);
                                da.close();

                               text = chat.Message;
                                if (text == null || text.matches("")) {
                                    text = selectedMeeting.TIME_TO_GO;
                                } else {
                                    text = text + "\n" + selectedMeeting.TIME_TO_GO;
                                }
                            }
                            catch (Exception ex)
                            {
                                text="";
                            }
                            selectedMarker.setSnippet(text);
                            selectedMarker.showInfoWindow();
                            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
                            imm.showSoftInput(myReply,0);
                            myReply.setVisibility(View.VISIBLE);
                            myMessage.requestFocus();

                        }
                        return false;
                    }
                });

                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        selectedMarker = null;
                        selectedMeeting = null;
                        myReply.setVisibility(View.GONE);
                        getActivity().invalidateOptionsMenu();
                    }
                });

                mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                    @Override
                    public View getInfoWindow(Marker marker) {
                        return null;
                    }

                    @Override
                    public View getInfoContents(Marker marker) {
                        LinearLayout info = new LinearLayout(getContext());
                        info.setOrientation(LinearLayout.VERTICAL);

                        TextView title = new TextView(getContext());
                        title.setTextColor(Color.BLACK);
                        title.setGravity(Gravity.CENTER);
                        title.setTypeface(null, Typeface.BOLD);
                        title.setText(marker.getTitle());

                        TextView snippet = new TextView(getContext());
                        snippet.setTextColor(Color.GRAY);
                        snippet.setText(marker.getSnippet());

                        info.addView(title);
                        info.addView(snippet);

                        return info;
                    }
                });

                if (APP_VARIABLES.MY_LOCATION_STRING != null && !APP_VARIABLES.MY_LOCATION_STRING.matches("")) {
                    String[] LatLong = APP_VARIABLES.MY_LOCATION_STRING.split(",");
                    double Lat = Double.parseDouble(LatLong[0]);
                    double Long = Double.parseDouble(LatLong[1]);
                    mapCenter = new LatLng(Lat, Long);
                } else {

                    mapCenter = new LatLng(28.6155, 77.3907);
                }
                AddUserMarkup(APP_VARIABLES.MY_MOBILE_NUMBER, Session.GetUser(getContext()).UserID,"ME" ,"",APP_VARIABLES.MY_LOCATION_STRING,getContext());
                mMap.moveCamera(CameraUpdateFactory.newLatLng(mapCenter));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

                ((MainActivity)getActivity()).MapReady();
            }
            catch (SecurityException ex)
            {
                UTILITY.HandleException(getContext(),"onMapReady ",ex.toString());
            }

    }

    @Override
    public void onLocationChanged(Location location) {

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

      @Override
      public void OnTextSent(String MeetingMobile, String MsgMobile, String message) {
          try {
              selectedMarker = currentMeetingsMarker.get(MeetingMobile);
              if(selectedMarker!=null) {
                  selectedMeeting = ActiveMeetingGroup.GetInstance(getContext()).GetMeetingByName(selectedMarker.getTitle());
                  String text = message + "\n" + selectedMeeting.TIME_TO_GO;
                  selectedMarker.setSnippet(text);
                  selectedMarker.showInfoWindow();
                  myMessage.setText("");
                  myReply.setVisibility(View.VISIBLE);
              }
              // getActivity().invalidateOptionsMenu();
          }
          catch (Exception ex)
          {
             UTILITY.HandleException(getContext(),"OnTextSent",ex.toString());
          }
      }



          @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
              ((MainActivity)getActivity()).CurrentFragment = MapFragment.this;
              menu.clear();
        if(selectedMarker != null)
        {
            if(selectedMeeting.MEETING_STATUS.matches(APP_CONST.MEETING_STATUS_PAUSE))
            {
                inflater.inflate(R.menu.menu_pause_meeting, menu);
            }
            else if (selectedMeeting.MEETING_STATUS.matches(APP_CONST.MEETING_STATUS_SHARING_LOCATION)) {
                inflater.inflate(R.menu.menu_sending_location, menu);
            }
            else if (selectedMeeting.MEETING_STATUS.matches(APP_CONST.MEETING_STATUS_RECEIVING_LOCATION)) {
                inflater.inflate(R.menu.menu_receiving_location, menu);
            }

            else if (selectedMeeting.MEETING_STATUS.matches(APP_CONST.MEETING_STATUS_SENDING_LOCATION)) {
                inflater.inflate(R.menu.menu_sending_location, menu);
            }
            else
            {
                inflater.inflate(R.menu.menu_meeting_no_select, menu);
            }

        }
        else
        {
            inflater.inflate(R.menu.menu_meeting_no_select, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        try {
            int id = item.getItemId();
            if (id == R.id.action_Share) {
               // RemindSelectedMeeting();

            }

            else if (id == R.id.action_Stop) {
                StopSelectedMeetings();
            }

            else if (id == R.id.action_StopAll) {
                StopAllMeetings();
            }
            else if (id == R.id.action_Pause) {

                PauseSelectedMeeting();
            }

            else if (id == R.id.action_Resume) {

                ResumeSelectedMeeting();
            }


            return true;
        }
        catch (Exception ex)
        {
            Toast.makeText(getContext(),"Error in completing operation",Toast.LENGTH_LONG).show();
            return false;
        }
    }



            public void AddOnMap( ActiveMeeting meeting)
            {
                    AddInstantMeetingOnMap(meeting);

            }

            private void AddInstantMeetingOnMap(ActiveMeeting meeting)
            {
                try {
                    if (currentMeetingsPolyline.containsKey(meeting.DESTINATION_MOBILE_NO)) {
                        Polyline line = currentMeetingsPolyline.get(meeting.DESTINATION_MOBILE_NO);
                        line.remove();
                        currentMeetingsPolyline.remove(meeting.DESTINATION_MOBILE_NO);
                    }
                    Polyline line = mMap.addPolyline(meeting.lineOptions);
                    currentMeetingsPolyline.put(meeting.DESTINATION_MOBILE_NO, line);
                    AddUserMarkup(meeting.DESTINATION_MOBILE_NO,meeting.DESTINATION_USER_ID, meeting.DESTINATION_NAME, meeting.TIME_TO_GO, meeting.DESTINATION_LATLONG, getContext());

                }
                catch (Exception ex)
                {
                    Toast.makeText(getContext(),"Error occurred in AddOnMap", Toast.LENGTH_LONG).show();
                }
            }


            public void AddUserMarkup(final String MobileNumber, int User_ID,final String Name,final String Time ,String Location,final Context context)
            {
                try {
                    String[] arrTargetLoc = Location.split(",");

                  final LatLng latLong = new LatLng(Double.parseDouble(arrTargetLoc[0]), Double.parseDouble(arrTargetLoc[1]));
                    /*  Bitmap bitMap= ImageServer.GetImageBitmap(MobileNumber, context);*/



                  //  Bitmap markerImage = ImageServer.GetBitmapFromDrawable(bitMap, context);

                    if (currentMeetingsMarker.containsKey(MobileNumber)) {
                        Marker mar = currentMeetingsMarker.get(MobileNumber);
                        mar.setPosition(latLong);
                       // mar.remove();
                        //currentMeetingsMarker.remove(MobileNumber);
                    }
                    else {

                        String url1 = APP_CONST.IMAGE_URL + User_ID +".png";

                        Target target = new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                                Bitmap markerImage = ImageServer.GetBitmapFromDrawable(bitmap, context);

                                Marker pMarker = mMap.addMarker(new MarkerOptions()
                                        .position(latLong)
                                        .icon(BitmapDescriptorFactory.fromBitmap(markerImage))
                                        .snippet(Time));
                                pMarker.setTitle(Name);
                                currentMeetingsMarker.put(MobileNumber, pMarker);
                            }

                            @Override
                            public void onBitmapFailed(Drawable errorDrawable) {
                                Bitmap bmp = ImageServer.GetDefaultImage(context);
                                Bitmap markerImage = ImageServer.GetBitmapFromDrawable(bmp, context);
                                Marker pMarker = mMap.addMarker(new MarkerOptions()
                                        .position(latLong)
                                        .icon(BitmapDescriptorFactory.fromBitmap(markerImage))
                                        .snippet(Time));
                                pMarker.setTitle(Name);
                                currentMeetingsMarker.put(MobileNumber, pMarker);
                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {
                            }
                        };

                        Picasso.with(getContext()).load(url1)
                                .error(R.drawable.user_image)
                                .resize(100,100)
                                .into(target);


                    }
               }
                catch (Exception ex)
                {
                int a= 5;
                }

            }




          public void BlinkMarkup(String MobileNumber, int User_ID,String Name, String Time ,String Location,Context context)
          {
              Marker mar;
              try {
                  if(mMap== null)
                  {
                      return;
                  }
                  else {
                      String[] arrTargetLoc = Location.split(",");
                      LatLng latLong = new LatLng(Double.parseDouble(arrTargetLoc[0]), Double.parseDouble(arrTargetLoc[1]));

                      if (currentMeetingsMarker.containsKey(MobileNumber)) {
                          mar = currentMeetingsMarker.get(MobileNumber);
                          mar.setPosition(latLong);

                      } else {
                         // Bitmap bitMap = ImageServer.GetImageBitmap(MobileNumber, context);

                          String url1 = APP_CONST.IMAGE_URL + User_ID +".png";
                          Bitmap bitMap = Picasso.with(getContext()).load(url1).get();

                          Bitmap markerImage = ImageServer.GetBitmapFromDrawable(bitMap, context);


                          mar = mMap.addMarker(new MarkerOptions()
                                  .position(latLong)
                                  .icon(BitmapDescriptorFactory.fromBitmap(markerImage))
                                  .snippet(Time));
                          mar.setTitle(Name);
                          currentMeetingsMarker.put(MobileNumber, mar);
                      }

                      if (mar.isVisible()) {
                          mar.setVisible(false);
                      }
                     else {
                          mar.setVisible(true);
                      }

                  }
              }
              catch (Exception ex)
              {
                  int a= 5;
              }

          }


          public void SetMapBound(String Location)
            {
                try {
                    String[] arrTargetLoc = Location.split(",");
                    LatLng latLong = new LatLng(Double.parseDouble(arrTargetLoc[0]), Double.parseDouble(arrTargetLoc[1]));
                    if (currentMeetingsMarker.size() == 1) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLong, 15));
                    } else if (mapBounds == null) {
                        mapBounds = new LatLngBounds(latLong, latLong);
                        for (Marker m : currentMeetingsMarker.values()
                                ) {
                            mapBounds = mapBounds.including(m.getPosition());
                        }
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mapBounds, 10));

                    } else if (!mapBounds.contains(latLong)) {
                        mapBounds = mapBounds.including(latLong);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mapBounds, 10));
                    }
                }
                catch (Exception ex)
                {
                    int a =5;
                }


            }

          public void  AddMeetingRoutes(ActiveMeeting meeting)
            {

                try {
                    if (currentMeetingsPolyline.containsKey(meeting.DESTINATION_MOBILE_NO)) {
                        Polyline line = currentMeetingsPolyline.get(meeting.DESTINATION_MOBILE_NO);
                        line.remove();
                        currentMeetingsPolyline.remove(meeting.DESTINATION_MOBILE_NO);
                    }
                    Polyline line = mMap.addPolyline(meeting.lineOptions);
                    currentMeetingsPolyline.put(meeting.DESTINATION_MOBILE_NO, line);



                }
                catch (Exception ex)
                {
                    Toast.makeText(getContext(),"Error occurred in AddOnMap", Toast.LENGTH_LONG).show();
                }

            }

    public void RemoveFromMap(String MobileNo)
    {
        try {
            if (currentMeetingsPolyline.containsKey(MobileNo)) {
                Polyline line = currentMeetingsPolyline.get(MobileNo);
                line.remove();

                currentMeetingsPolyline.remove(MobileNo);
            }
            if (currentMeetingsMarker.containsKey(MobileNo)) {
                Marker mar = currentMeetingsMarker.get(MobileNo);
                mar.remove();
                currentMeetingsMarker.remove(MobileNo);
            }

        }
        catch (Exception ex)
        {
            Toast.makeText(getContext(),"Error occurred in Remove Map", Toast.LENGTH_LONG).show();
        }
    }

    public void ClearMap()
    {
        for (Polyline line:currentMeetingsPolyline.values()
             ) {
            line.remove();
        }
        currentMeetingsPolyline.clear();

        for (Marker marker:currentMeetingsMarker.values()
                ) {
            marker.remove();
        }
        currentMeetingsMarker.clear();
    }

    private void StopSelectedMeetings()
    {
        String strName ="<p>Following Meetings will be Stopped</p></br>"+"<p>"+ selectedMarker.getTitle() + "</p></br>";

        strName = strName.substring(0, strName.length()-6);
        Spanned result = Html.fromHtml(strName);
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("STOP");
        dialog.setMessage(result);
        dialog.setCancelable(false);
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedMarker.hideInfoWindow();
                getActivity().invalidateOptionsMenu();
            }
        });
        dialog.setPositiveButton("Stop", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String mobile = "";

                for (String key : currentMeetingsMarker.keySet()
                        ) {

                    Marker mar = currentMeetingsMarker.get(key);
                    if (mar.getTitle().matches(selectedMarker.getTitle())) {
                        mobile = key;
                    }
                }
                ActiveMeetingGroup.GetInstance(getContext()).StopMeetingByMobile(mobile, getContext());
                selectedMarker.hideInfoWindow();
                selectedMarker = null;

                getActivity().invalidateOptionsMenu();
            }
        });
        AlertDialog alert = dialog.create();
        alert.show();
    }

    private void StopAllMeetings()
    {
        String strName ="<p>All Running Meetings will be Stopped</p></br>";

        Spanned result = Html.fromHtml(strName);
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("STOP ALL");
        dialog.setMessage(result);
        dialog.setCancelable(false);
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(selectedMarker!=null) {
                    selectedMarker.hideInfoWindow();
                    selectedMarker= null;
                    getActivity().invalidateOptionsMenu();
                }

            }
        });
        dialog.setPositiveButton("Stop", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(selectedMarker!=null) {
                    selectedMarker.hideInfoWindow();
                    selectedMarker= null;
                    getActivity().invalidateOptionsMenu();
                }
                ActiveMeetingGroup.GetInstance(getContext()).StopAllRunningMeetings(getContext());

            }
        });
        AlertDialog alert = dialog.create();
        alert.show();
    }

    private void PauseSelectedMeeting()
    {
        String strName ="<p>Will Not send location and Route</p></br>"+"<p>"+ selectedMarker.getTitle() + "</p></br>";

        strName = strName.substring(0, strName.length()-6);
        Spanned result = Html.fromHtml(strName);
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("PAUSE");
        dialog.setMessage(result);
        dialog.setCancelable(false);
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog.setPositiveButton("Pause", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String mobile = "";
                try {
                    for (String key : currentMeetingsMarker.keySet()
                            ) {

                        Marker mar = currentMeetingsMarker.get(key);
                        if (mar.getTitle().matches(selectedMarker.getTitle())) {
                            mobile = key;
                        }
                    }
                    selectedMarker.hideInfoWindow();
                    selectedMarker = null;
                    getActivity().invalidateOptionsMenu();
                    ActiveMeetingGroup.GetInstance(getContext()).PauseMeetingByMobile(mobile, getContext());

                } catch (Exception ex) {
                    Toast.makeText(getContext(),"Error in Pause", Toast.LENGTH_LONG).show();
                }

            }
        });
        AlertDialog alert = dialog.create();
        alert.show();
    }

    private void ResumeSelectedMeeting()
    {
        String strName ="<p>Meeting will resume with</p></br>"+"<p>"+ selectedMarker.getTitle() + "</p></br>";

        strName = strName.substring(0, strName.length()-6);
        Spanned result = Html.fromHtml(strName);
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("RESUME");
        dialog.setMessage(result);
        dialog.setCancelable(false);
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog.setPositiveButton("Resume", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String mobile = "";
                try {
                    for (String key : currentMeetingsMarker.keySet()
                            ) {

                        Marker mar = currentMeetingsMarker.get(key);
                        if (mar.getTitle().matches(selectedMarker.getTitle())) {
                            mobile = key;
                        }
                    }
                    selectedMarker.hideInfoWindow();
                    selectedMarker = null;
                    getActivity().invalidateOptionsMenu();
                    ActiveMeetingGroup.GetInstance(getContext()).ResumeMeetingByMobile(mobile, getContext());

                } catch (Exception ex) {
                    Toast.makeText(getContext(),"Error in Pause", Toast.LENGTH_LONG).show();
                }

            }
        });
        AlertDialog alert = dialog.create();
        alert.show();
    }

    private void RemindSelectedMeeting()
    {
        String strName ="<p>Send Reminder to</p></br>"+"<p>"+ selectedMarker.getTitle() + "</p></br>";

        strName = strName.substring(0, strName.length()-6);
        Spanned result = Html.fromHtml(strName);
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("Reminder");
        dialog.setMessage(result);
        dialog.setCancelable(false);
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog.setPositiveButton("Remind", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String mobile = "";
                try {
                    for (String key : currentMeetingsMarker.keySet()
                            ) {

                        Marker mar = currentMeetingsMarker.get(key);
                        if (mar.getTitle().matches(selectedMarker.getTitle())) {
                            mobile = key;
                        }
                    }
                   // ActiveMeetingGroup.GetInstance(getContext()).RemindMeetingByMobile(mobile, getContext());
                    selectedMarker.hideInfoWindow();
                    selectedMarker = null;
                    getActivity().invalidateOptionsMenu();
                } catch (Exception ex) {
                    Toast.makeText(getContext(),"Error in Pause", Toast.LENGTH_LONG).show();
                }

            }
        });
        AlertDialog alert = dialog.create();
        alert.show();
    }

          public void sendMessage( final Context context, final ChatMessage chat, final  String msg) {


              int RequestID=1;
              String reqBody = "{\"hostMobile\":\"" + APP_VARIABLES.MY_MOBILE_NUMBER + "\",\"hostName\":\"" + APP_VARIABLES.MY_NAME + "\",\"trackerID\":"
                      + RequestID + ",\"Type\":\"" + msg + "\",\"hostLocation\":\"" + chat.Message + "\",\"inviteeMobile\":\"" + chat.Dest_Mobile
                      + "\",\"hostUserId\":" + APP_VARIABLES.MY_USER_ID + "\",\"inviteeLocation\":\"" + chat.Message + "\"}";

              String url = APP_CONST.APP_SERVER_URL + "/api/Tracker";

              try {
                  JSONObject jsRequest = new JSONObject(reqBody);
                  //-------------------------------------------------------------------------------------------------
                  RequestQueue queue = Volley.newRequestQueue(context);

                  JsonObjectRequest jsArrayRequest = new JsonObjectRequest(Request.Method.POST, url, jsRequest, new Response.Listener<JSONObject>() {
                      @Override
                      public void onResponse(JSONObject jObj) {
                          try {
                              String Response = jObj.getString("Response");

                              if(msg.matches(Message.TEXT_MESSAGE))
                              {
                                  if (Response.matches("OK")) {
                                      chat.Delivery_Status=1;
                                      DataAccess da = new DataAccess(context);
                                      da.open();
                                      da.insertNewMessage(chat.Dest_Mobile, "ME",Message.TEXT_MESSAGE, chat.Message);
                                      da.close();


                                  } else if (Response.matches("Fail")) {
                                      chat.Delivery_Status=2;
                                      Toast.makeText(context, "Error in Sending Notification", Toast.LENGTH_SHORT).show();
                                  }
                                  ((MainActivity)getActivity()).OnTextSentByMap(chat.Dest_Mobile,chat.Message);
                              }

                          } catch (JSONException jEx) {

                          }
                      }
                  }, new Response.ErrorListener() {
                      @Override
                      public void onErrorResponse(VolleyError error) {
                          chat.Delivery_Status=2;
                          String message = error.toString();
                          // if(message.equals("com.android.volley.TimeoutError")&&networkCounter<5)if(msg.matches(Message.SEND_INVITE))
                      }
                  });


                  RetryPolicy rPolicy = new DefaultRetryPolicy(0,-1,0);
                  jsArrayRequest.setRetryPolicy(rPolicy);
                  queue.add(jsArrayRequest);


                  //*******************************************************************************************************
              } catch (JSONException js) {
                  // prgBar.setVisibility(View.GONE);
                  chat.Delivery_Status=2;
                  //  Toast.makeText(getApplicationContext(), "Post could not be submitted : Try Again",Toast.LENGTH_LONG).show();
              } finally {

              }
          }
}
