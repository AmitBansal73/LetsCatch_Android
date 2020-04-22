package net.anvisys.letscatch.Object;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import net.anvisys.letscatch.Common.DataAccess;
import net.anvisys.letscatch.Common.ImageServer;
import net.anvisys.letscatch.Common.Session;
import net.anvisys.letscatch.Common.UTILITY;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Amit Bansal on 04-01-2017.
 */
public class ActiveMeeting implements Parcelable {

    public String DIST_To_GO = "..";
    public String TIME_TO_GO = "..";

    int Dist= 0;
    int Duration=0;
    public  PolylineOptions lineOptions = null;
    private MeetingUpdateListener updateListener;
    private static MeetingStatusListener statusListener;
    private ChatUpdateListener chatListener;
    public String ORIGIN_LATLONG;
    public String DESTINATION_LATLONG;
    public boolean ShowMessage = false;
    boolean[] bArray = new boolean[2];
    public int newMessage=0;

    private ActiveMeeting() {

    }

    public ActiveMeeting(int meet_type ,String ORIGIN_NAME, String ORIGIN_MOBILE_NO,String ORIGIN_LATLONG, String DESTINATION_MOBILE_NO,
                          int DESTINATION_USER_ID,String DESTINATION_NAME,String DESTINATION_LATLONG ,String Start_Time) {
        this.TYPE = meet_type;
        this.ORIGIN_NAME = ORIGIN_NAME;
        this.ORIGIN_MOBILE_NO = ORIGIN_MOBILE_NO;
        this.ORIGIN_LATLONG = ORIGIN_LATLONG;
        this.DESTINATION_USER_ID = DESTINATION_USER_ID;
        this.DESTINATION_MOBILE_NO = DESTINATION_MOBILE_NO;
        this.DESTINATION_NAME = DESTINATION_NAME;
        this.DESTINATION_LATLONG = DESTINATION_LATLONG;
        this.START_TIME = Start_Time;
       }

    public ActiveMeeting( String sessionText)
    {
        String[] arrData = sessionText.split("&");
        this.TYPE = Integer.parseInt(arrData[0]);
        this.ORIGIN_NAME = arrData[1];
        this.ORIGIN_MOBILE_NO = arrData[2];
        this.ORIGIN_LATLONG = arrData[3];
        this.DESTINATION_USER_ID = Integer.parseInt(arrData[4]);
        this.DESTINATION_MOBILE_NO = arrData[5];
        this.DESTINATION_NAME = arrData[6];
        this.DESTINATION_LATLONG = arrData[7];
        this.START_TIME = arrData[8];
        this.ScheduleID = Integer.parseInt(arrData[9]);
        this.MEETING_STATUS = arrData[10];
    }

    public ActiveMeeting(String DESTINATION_MOBILE_NO, int DESTINATION_USER_ID, String DESTINATION_NAME,String STATUS,String DESTINATION_LATLONG ,String Start_time) {
        this.TYPE = APP_CONST.MEETING_TYPE_INSTANT;
        this.ORIGIN_NAME = APP_VARIABLES.MY_NAME;
        this.ORIGIN_MOBILE_NO = APP_VARIABLES.MY_MOBILE_NUMBER;
        this.MEETING_STATUS = STATUS;
        this.ORIGIN_LATLONG = APP_VARIABLES.MY_LOCATION_STRING;
        this.DESTINATION_USER_ID = DESTINATION_USER_ID;
        this.DESTINATION_MOBILE_NO = DESTINATION_MOBILE_NO;
        this.DESTINATION_NAME = DESTINATION_NAME;
        this.DESTINATION_LATLONG = DESTINATION_LATLONG;
        this.START_TIME = Start_time;
           }

    public String GetSessionString()
    {
        String sessionString = this.TYPE+"&" + this.ORIGIN_NAME + "&" + this.ORIGIN_MOBILE_NO+ "&" + this.ORIGIN_LATLONG + "&" + this.DESTINATION_USER_ID
                + "&"+this.DESTINATION_MOBILE_NO+ "&"+this.DESTINATION_NAME+ "&"+this.DESTINATION_LATLONG
                + "&"+this.START_TIME+  "&"+this.ScheduleID + "&"+this.MEETING_STATUS ;

        return sessionString;
    }

    //region parcelable methods

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        bArray[0]=ShowMessage;
        parcel.writeString(DESTINATION_NAME);
        parcel.writeString(MEETING_STATUS);
        parcel.writeString(START_TIME);
        parcel.writeString(UPDATE_TIME);
        parcel.writeString(DIST_To_GO);
        parcel.writeString(TIME_TO_GO);
        parcel.writeInt(DESTINATION_USER_ID);
        parcel.writeString(DESTINATION_MOBILE_NO);
        parcel.writeString(DESTINATION_LATLONG);
        parcel.writeBooleanArray(bArray);
    }

    public ActiveMeeting(Parcel in) {

        this.DESTINATION_NAME = in.readString();
        this.MEETING_STATUS = in.readString();
        this.START_TIME = in.readString();
        this.UPDATE_TIME = in.readString();
        this.DIST_To_GO = in.readString();
        this.TIME_TO_GO = in.readString();
        this.DESTINATION_USER_ID = in.readInt();
        this.DESTINATION_MOBILE_NO = in.readString();
        this.DESTINATION_LATLONG = in.readString();
        in.readBooleanArray(bArray);
        this.ShowMessage = bArray[0];
    }

    public static final Parcelable.Creator<ActiveMeeting> CREATOR = new Creator<ActiveMeeting>() {
        @Override
        public ActiveMeeting createFromParcel(Parcel parcel) {
            return new ActiveMeeting(parcel);
        }

        @Override
        public ActiveMeeting[] newArray(int size) {
            return new ActiveMeeting[size];
        }
    };

    // endregion

    public interface ChatUpdateListener
    {
        public void OnTextSent(String MeetingMobile,String MsgMobile, String message);

    }

    public  void RegisterChatListener( ChatUpdateListener listener)
    {

        chatListener = listener;
    }

    public interface MeetingUpdateListener
    {
        public void OnRouteCreated(ActiveMeeting meeting);

    }

    public  void RegisterMeetingListener( MeetingUpdateListener listener)
    {

        updateListener = listener;
    }

    public interface MeetingStatusListener
    {
        public void OnMeetingStatusChanged();
        public void OnMeetingForcedClear(String MobileNumber);


    }

    public static void RegisterStatusListener( MeetingStatusListener listener)
    {

        statusListener = listener;
    }

    public int  GET_SCHEDULE_TYPE()
    {
        return TYPE;
    }

    protected String MEETING_NAME;

    public int ORIGIN_USER_ID =0;
    public String ORIGIN_MOBILE_NO;
    public String ORIGIN_NAME;

    public int DESTINATION_USER_ID =0;
    public String DESTINATION_MOBILE_NO;
    public String DESTINATION_NAME;

    public String START_TIME = "..";
    public String UPDATE_TIME = "..";

    public int ScheduleID;
    private int TYPE;
    public String MEETING_STATUS;

    String prevOriginLocation="0.0,0.0";
    String prevDestinationLocation="0.0,0.0";


    public void SetMapFeatures(String strMyLocation)
    {
        try {
           // this.DESTINATION_LATLONG = meetingContact.Latitude + "," + meetingContact.Longitude;
            ORIGIN_LATLONG = APP_VARIABLES.MY_LOCATION_STRING;
            String  url = APP_CONST.DIRECTION_URL + "json?";
            String Origin = "origin=" + ORIGIN_LATLONG, destination="";

            destination = "destination=" + DESTINATION_LATLONG;
            url = url + Origin + "&" + destination;

            url = url + "&sensor=false";
            String key = "&key=" + APP_CONST.GOOGLE_API_KEY;
            url = url+ key;


            if((ShouldReRoute(prevOriginLocation,ORIGIN_LATLONG) || ShouldReRoute(prevDestinationLocation,DESTINATION_LATLONG))&& APP_SETTINGS.ENABLE_ROUTING==true)
            {
                prevOriginLocation = ORIGIN_LATLONG;
                prevDestinationLocation = DESTINATION_LATLONG;
                SetRoute route = new SetRoute();
                route.execute(url);
            }
            else
            {
                return;
            }


        }
        catch (Exception ex)
        {}

    }

    public class SetRoute extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {
            // For storing data from web service
            String data = "";
            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                android.util.Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);
            try {
                JSONObject obj = new JSONObject(data);
                //  MapDisplayFeature.setBoundFromJSON(obj);
                // DisplayMap();
            } catch (JSONException jex) {
                //Toast.makeText(getApplicationContext(), "Error 2: Could Not create Bound", Toast.LENGTH_LONG).show();
            }

            Parser parser = new Parser();
            // parser.RegisterRouteReadyListener(MainActivity.this);
            //ParserTask parser = new ParserTask();
            parser.execute(data);
        }

        private String downloadUrl(String strUrl) throws IOException {
            String data = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(strUrl);
                // Creating an http connection to communicate with url
                urlConnection = (HttpURLConnection) url.openConnection();
                // Connecting to url
                urlConnection.connect();
                // Reading data from url
                iStream = urlConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
                StringBuffer sb = new StringBuffer();
                String line = "";
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                data = sb.toString();
                br.close();
            } catch (Exception e) {
                android.util.Log.d("Exception :", e.toString());
            } finally {
                iStream.close();
                urlConnection.disconnect();
            }
            return data;
        }


        public class Parser extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

            @Override
            protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
                JSONObject jObject;
                List<List<HashMap<String, String>>> routes = null;
                try {
                    jObject = new JSONObject(jsonData[0]);
                    JSONDirectionParser parser = new JSONDirectionParser();
                    // Starts parsing data
                    routes = parser.parse(jObject);
                    ReadDistanceAndTime(jObject);


                    if (Integer.toString(Dist).length() >3)
                    {
                        double dis = Dist/1000;
                        DIST_To_GO = Double.toString(dis) + " km";
                    }
                    else
                    {
                        DIST_To_GO = Integer.toString(Dist) + " m";
                    }
                    TIME_TO_GO = Integer.toString(Duration/60) + " min";


                } catch (Exception e) {

                    e.printStackTrace();
                }
                return routes;
            }

            @Override
            protected void onPostExecute(List<List<HashMap<String, String>>> result) {
                ArrayList<LatLng> points = null;

                try {
                    // Traversing through all the routes
                    for (int i = 0; i < result.size(); i++) {
                        points = new ArrayList<LatLng>();
                        lineOptions = new PolylineOptions();
                        // Fetching i-th route
                        List<HashMap<String, String>> path = result.get(i);
                        // Fetching all the points in i-th route
                        for (int j = 0; j < path.size(); j++) {
                            HashMap<String, String> point = path.get(j);
                            double lat = Double.parseDouble(point.get("lat"));
                            double lng = Double.parseDouble(point.get("lng"));
                            LatLng position = new LatLng(lat, lng);
                            points.add(position);
                        }
                        // Adding all the points in the route to LineOptions
                        lineOptions.addAll(points);
                        lineOptions.width(5);
                        lineOptions.color(Color.RED);
                        UPDATE_TIME= UTILITY.CurrentLocalDateTimeString();
                        if(updateListener!=null) {
                            updateListener.OnRouteCreated(ActiveMeeting.this);
                        }

                    }
                } catch (Exception ex) {

                    int a=1;
                }
            }
        }

        private void ReadDistanceAndTime(JSONObject jObject)
        {
            JSONArray jRoutes = null;
            JSONArray jLegs = null;
            JSONObject jDistance = null;
            JSONObject JDuration = null;
            try {

                jRoutes = jObject.getJSONArray("routes");
                Dist = 0;
                Duration=0;
                /** Traversing all routes */
                for(int i=0;i<jRoutes.length();i++){
                    jLegs = ( (JSONObject)jRoutes.get(i)).getJSONArray("legs");
                    /** Traversing all legs */
                    for(int j=0;j<jLegs.length();j++){
                        // added to get distance and duration
                        jDistance = ( (JSONObject)jLegs.get(j)).getJSONObject("distance");
                        Dist = Dist+  jDistance.getInt("value");
                        JDuration = ( (JSONObject)jLegs.get(j)).getJSONObject("duration");
                        Duration = Duration + JDuration.getInt("value");
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }catch (Exception e){

                int a =1;
            }
        }
    }




    public  boolean ShouldReRoute( String previousLocation, String newLocation)
    {
        try {

            if (previousLocation == null)
            {
                return true;
            }

            String[] previous = previousLocation.split(",");
            String[] newLoc = newLocation.split(",");

            Double prevLat =  Double.parseDouble(previous[0]);
            Double prevLong =  Double.parseDouble(previous[1]);

            Double newLat =  Double.parseDouble(newLoc[0]);
            Double newLong =  Double.parseDouble(newLoc[1]);

            double factor = 2*3.14*6370*1000/360;

            Double diffLat = newLat- prevLat;

            Double diffLong = newLong - prevLong;

            Double dis = (Math.sqrt( diffLat*diffLat + diffLong*diffLong))*factor;

            if (dis >APP_SETTINGS.ROUTING_DISTANCE)
            {
                return true;}
            else
            {
                return false;

            }

        }
        catch (Exception ex)
        {
            return false;
        }

    }


    public void StopSending(final Context context)
    {
        try {
            DataAccess da = new DataAccess(context);
            da.open();
            if (MEETING_STATUS.matches(APP_CONST.MEETING_STATUS_SHARING_LOCATION)) {
                MEETING_STATUS = APP_CONST.MEETING_STATUS_RECEIVING_LOCATION;
                da.UpdateActiveMeeting(DESTINATION_MOBILE_NO, MEETING_STATUS);
            } else if (MEETING_STATUS.matches(APP_CONST.MEETING_STATUS_SENDING_LOCATION)) {
                ActiveMeetingGroup.GetInstance(context).RunningMeetings.remove(DESTINATION_MOBILE_NO);
                MEETING_STATUS = APP_CONST.MEETING_STATUS_NONE;
                da.deleteMessage(DESTINATION_MOBILE_NO);
                ImageServer.DeleteBitmapImage(DESTINATION_MOBILE_NO, context);
            }
            da.close();
            sendNotification(context, DESTINATION_MOBILE_NO, Message.STOP_TRACKING);

        }
        catch (Exception ex)
        {
            UTILITY.HandleException(context,"StopSending", ex.toString());
        }
    }




    public void sendNotification( final Context context, final String text, final  String msg) {

        if (statusListener != null) {
            statusListener.OnMeetingStatusChanged();
        }

        int RequestID=1;
        String reqBody = "{\"hostMobile\":\"" + ORIGIN_MOBILE_NO + "\",\"hostName\":\"" + APP_VARIABLES.MY_NAME + "\",\"trackerID\":" + RequestID + "\",\"hostUserId\":" + APP_VARIABLES.MY_USER_ID +
                ",\"Type\":\"" + msg + "\",\"hostLocation\":\"" + text + "\",\"inviteeMobile\":\"" + DESTINATION_MOBILE_NO + "\",\"inviteeLocation\":\"" + text + "\"}";

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

                    } catch (JSONException jEx) {

                    }

                    if (statusListener != null) {
                        statusListener.OnMeetingStatusChanged();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {



                }
            });


            RetryPolicy rPolicy = new DefaultRetryPolicy(0,-1,0);
            jsArrayRequest.setRetryPolicy(rPolicy);
            queue.add(jsArrayRequest);


            //*******************************************************************************************************
        } catch (JSONException js) {
            // prgBar.setVisibility(View.GONE);

            //  Toast.makeText(getApplicationContext(), "Post could not be submitted : Try Again",Toast.LENGTH_LONG).show();
        } finally {

        }
    }

    private static void StopErrorDialog(final Context context, final String MobileNo)
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
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
                ImageServer.DeleteBitmapImage(MobileNo,context);
                Session.UpdateSessionMeeting(context);
                if (statusListener != null) {
                    statusListener.OnMeetingForcedClear(MobileNo);
                }

            }
        });
        AlertDialog alert = dialog.create();
        alert.show();
    }

}
