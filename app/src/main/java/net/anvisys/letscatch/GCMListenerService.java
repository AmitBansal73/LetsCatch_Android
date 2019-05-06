package net.anvisys.letscatch;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.gcm.GcmListenerService;

import net.anvisys.letscatch.Common.DataAccess;

import net.anvisys.letscatch.Common.ImageServer;
import net.anvisys.letscatch.Common.Session;
import net.anvisys.letscatch.Common.UTILITY;
import net.anvisys.letscatch.Object.APP_CONST;
import net.anvisys.letscatch.Object.APP_SETTINGS;
import net.anvisys.letscatch.Object.Contact;
import net.anvisys.letscatch.Object.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Amit Bansal on 15-02-2017.
 */
public class GCMListenerService extends GcmListenerService {
    final int NOTIFICATION_ID = 9100;

    private String MessageText = "";
    Handler mHandler;
    public static int numMessages=0;

    public static HashMap<String,String> hmInvite = new HashMap<>();
    public static HashMap<String,String> hmStop = new HashMap<>();
    public static HashMap<String,String> hmLocation = new HashMap<>();
    public static HashMap<String,String> hmSendLocation = new HashMap<>();
    public static HashMap<String,String> hmText = new HashMap<>();
    public static String msgInvite="";
    public static String msgStop="";

    public static String msgLocation = "";
    public static String msgSendLocation = "";
    public static String msgText = "";
    public static String fullMessage = "";

    public boolean ActivityRunning=false;

    public GCMListenerService() {


    }


    static GCMListener gcmListener;


    public interface GCMListener
    {
        void OnMessageReceived(String Message);

    }

    // Assign the listener implementing events interface that will receive the events
    public static void setGCMNotificationListener(GCMListener listener) {
        gcmListener = listener;
    }


    // Assign the listener implementing events interface that will receive the events
    public static void removeGCMNotificationListener() {
        gcmListener = null ;
    }


    @Override
    public void onMessageReceived(String from, Bundle data) {
        super.onMessageReceived(from, data);

        ActivityRunning = Session.GetApplicationStatus(this);

        final String message = data.getString("msg");


        if (ActivityRunning == true) {
            try {
                if(gcmListener!=null) {

                    mHandler = new Handler(Looper.getMainLooper()) {
                        @Override
                        public  void handleMessage(android.os.Message message) {
                            // This is where you do your work in the UI thread.
                            // Your worker tells you in the message what to do.
                            try {
                                String str = (String) message.obj;
                                //  FilterMessage(str);
                                gcmListener.OnMessageReceived(str);
                            }
                            catch (Exception ex)
                            {
                                int a=1;
                            }
                        }
                    };

                    new Thread()
                    {
                        @Override
                        public void run()
                        {
                            Looper.prepare();

                            //gcmListener.OnMessageReceived(message);
                            android.os.Message msg = mHandler.obtainMessage(1, message);
                            msg.sendToTarget();

                            Looper.loop();
                        }
                    }.start();
                }
               // FilterMessage(message);
              /*  new Thread() {
                    public void run() {
                        LoopMessage(message);
                    }
                }.start();
*/

            }
            catch (Exception ex)
            {
                Toast.makeText(getApplicationContext(), "Error in handling message thread", Toast.LENGTH_SHORT).show();
            }

        } else {
            try{
                String[] msgArray = message.split("&");
                String msgType = msgArray[0];
                final int ID = Integer.parseInt(msgArray[1]);
                String senderMobile = msgArray[2];
                String senderName = msgArray[3];
                String DataReceived = msgArray[4];
                String contImage = "";
                Contact cont;
                DataAccess da;
                switch (msgType) {

                    case Message.SEND_LOCATION:
                        da = new DataAccess(getApplicationContext());
                        da.open();
                        cont = da.getContactByMobile(senderMobile);
                        if (cont != null) {
                            senderName = cont.userName;
                            contImage = cont.strImage;
                        }
                        da.InsertMeeting(senderMobile, senderName, APP_CONST.MEETING_STATUS_RECEIVING_LOCATION, DataReceived);
                        da.close();
                        MessageText = "Invite from " + senderName + "(" + senderMobile + ")";

                        sendNotificationWithAction(Message.SEND_LOCATION, DataReceived, senderMobile, senderName, MessageText, contImage);
                        break;

                    case Message.LOCATION:
                        da = new DataAccess(getApplicationContext());
                        da.open();
                        cont = da.getContactByMobile(senderMobile);
                        if (cont != null) {
                            senderName = cont.userName;
                            contImage = cont.strImage;
                        }
                        da.UpdateLocation(senderMobile, DataReceived);
                        da.close();
                        sendNotificationWithAction(Message.LOCATION,DataReceived, senderMobile, senderName,"", contImage );
                        break;

                    case Message.STOP_TRACKING: {

                        da = new DataAccess(this);
                        da.open();
                        da.RemoveActiveMeeting(senderMobile);
                        da.deleteMessage(senderMobile);
                        da.InsertLog(MessageText);
                        cont = da.getContactByMobile(senderMobile);
                        if (cont != null) {
                            senderName = cont.userName;
                            contImage = cont.strImage;
                        }
                        da.close();
                        MessageText = "Tracking Stopped with" + senderName + "(" + senderMobile + ")";
                       sendNotificationWithAction(Message.STOP_TRACKING, DataReceived, senderMobile, senderName, MessageText, contImage);

                        break;
                    }
                    case Message.TEXT_MESSAGE: {

                        da = new DataAccess(this);
                        da.open();
                        da.insertNewMessage(senderMobile,senderMobile,Message.TEXT_MESSAGE,DataReceived);
                        cont = da.getContactByMobile(senderMobile);
                        if (cont != null) {
                            senderName = cont.userName;
                            contImage = cont.strImage;
                        }
                        da.close();
                        sendNotificationWithAction(Message.TEXT_MESSAGE, DataReceived, senderMobile, senderName, DataReceived, contImage);

                        break;
                    }
                    case Message.IMAGE_MESSAGE: {
                        GetImage(senderMobile,DataReceived);
                      /*  da = new DataAccess(this);
                        da.open();
                        da.insertNewMessage(senderMobile,senderMobile,Message.IMAGE_MESSAGE,DataReceived);
                        cont = da.getContactByMobile(senderMobile);
                        if (cont != null) {
                            senderName = cont.userName;
                            contImage = cont.strImage;
                        }
                        da.close();
                        sendNotificationWithAction(Message.IMAGE_MESSAGE, DataReceived, senderMobile, senderName, MessageText, contImage);
*/
                        break;
                    }
                    default:
                        MessageText = "";
                        break;
                }
                }
            catch (Exception ex)
            {
                Toast.makeText(getApplicationContext(),"error while receiving message", Toast.LENGTH_LONG).show();
            }
        }
    }

    public static void ClearNotificationMessage()
    {
        hmInvite.clear();
        hmStop.clear();
        hmLocation.clear();
        hmSendLocation.clear();
        hmText.clear();
        msgInvite="";
        msgStop="";
        msgLocation = "";
        numMessages=0;
        msgText="";
        msgSendLocation="";
    }

        private String CreateMessage(String Action, String MobileNumber, String Name, String message)
        {
            String htmlText="";
            try {
                if (Action.matches(Message.SEND_LOCATION)) {
                    if (hmSendLocation.size() == 0) {
                        htmlText = msgSendLocation = "Invite Received from " + Name;
                        hmSendLocation.put(MobileNumber, Name);
                    } else {
                        if (!hmSendLocation.containsKey(MobileNumber)) {
                            hmSendLocation.put(MobileNumber, Name);
                            msgSendLocation = hmSendLocation.size() + " Invite waiting response";
                        } else {
                            htmlText = msgSendLocation = "Invite Received from " + Name;
                        }
                    }
                } else if (Action.matches(Message.STOP_TRACKING)) {
                    if (hmStop.size() == 0) {
                        htmlText = msgStop = "Meeting Stopped By " + Name;
                        hmStop.put(MobileNumber, Name);
                    } else {
                        if (!hmStop.containsKey(MobileNumber)) {
                            hmStop.put(MobileNumber, Name);
                            msgStop = hmStop.size() + " Contact Stopped Meeting";
                        } else {
                            htmlText = msgStop = "Meeting Stopped By " + Name;
                        }
                    }
                } else if (Action.matches(Message.LOCATION)) {
                    if (hmLocation.size() == 0) {
                        htmlText = msgLocation = "New Location Received from " + Name;
                        hmLocation.put(MobileNumber, Name);
                    } else {
                        if (!hmLocation.containsKey(MobileNumber)) {
                            hmLocation.put(MobileNumber, Name);
                            msgLocation = hmLocation.size() + " updated location";
                        } else {
                            htmlText = msgLocation = "New Location Received from " + Name;
                        }
                    }
                } else if (Action.matches(Message.TEXT_MESSAGE)) {
                    if (hmText.size() == 0) {
                        htmlText = msgText = "@" + Name + ":" + message;
                        hmText.put(MobileNumber, Name);
                    } else {
                        if (!hmText.containsKey(MobileNumber)) {
                            hmText.put(MobileNumber, Name);
                            msgText = hmText.size() + " message received";
                        } else {
                            htmlText = msgText = "@" + Name + ":" + message;
                        }
                    }
                }
                numMessages = hmLocation.size() + hmStop.size() + hmInvite.size() + hmSendLocation.size();
            }
            catch (Exception ex)
            {
                Toast.makeText(getApplicationContext(),"error in creating message", Toast.LENGTH_LONG).show();
            }

                return htmlText;
        }

    private void sendNotificationWithAction(String Action, String data ,String MobileNumber, String Name, String message, String strImage ) {
        try {

            String ContentText = CreateMessage(Action,MobileNumber,Name, message);
            if(numMessages >1 )
            {
                if(hmLocation.size()== numMessages)
                {
                    ContentText = " Location received from " + numMessages + " Contacts";
                }
                else if(hmText.size()== numMessages)
                {
                    ContentText = " Message received from " + numMessages + " Contacts";
                }
                else {
                    ContentText = numMessages + " Notification waiting response";
                }
            }

            Intent noticeIntent = new Intent(this, MainActivity.class);
            noticeIntent.putExtra("IntentType", "GCM");
            noticeIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(noticeIntent);
            ++numMessages;
            PendingIntent noticePendingIntent = stackBuilder.getPendingIntent(numMessages, PendingIntent.FLAG_UPDATE_CURRENT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            NotificationCompat.Builder notificationBuilder =  new NotificationCompat.Builder(this);

                notificationBuilder .setSmallIcon(R.drawable.letsmeet)
                        .setContentTitle("Lets Catch")
                        .setContentText(ContentText)
                        .setAutoCancel(true)
                        .setGroup(Action)
                        .setGroupSummary(true)
                        //.setStyle(new NotificationCompat.BigTextStyle().bigText(result))
                        .setContentIntent(noticePendingIntent);

            if(APP_SETTINGS.LOCATION_NOTIFICATION_SOUND)
            {
                notificationBuilder .setSound(defaultSoundUri);
            }

            NotificationCompat.InboxStyle inboxStyle =  new NotificationCompat.InboxStyle();
            // Sets a title for the Inbox in expanded layout
            inboxStyle.setBigContentTitle("LetsCatch - Notifications");
            inboxStyle.setSummaryText("You have " +numMessages+ " Notifications.");

            if(!msgInvite.matches(""))
            {
                inboxStyle.addLine(msgInvite);
            }
            if(!msgStop.matches(""))
            {
                inboxStyle.addLine(msgStop);
            }
            if(!msgLocation.matches(""))
            {
                inboxStyle.addLine(msgLocation);
            }

            if(!msgSendLocation.matches(""))
            {
                inboxStyle.addLine(msgSendLocation);
            }
            notificationBuilder.setStyle(inboxStyle);

            /* Set Vibrate, Sound and Light
            int defaults = 0;
            defaults = defaults | Notification.DEFAULT_LIGHTS;
            defaults = defaults | Notification.DEFAULT_VIBRATE;
            defaults = defaults | Notification.DEFAULT_SOUND;

            //notificationBuilder.setNumber(++numMessages);
            notificationBuilder.setDefaults(defaults);
 */

            if (strImage !=null)
            {
                Bitmap bitmap = ImageServer.getBitmapFromString(strImage, getApplicationContext());
                notificationBuilder.setLargeIcon(bitmap);
            }

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


            notificationManager.notify(NOTIFICATION_ID /* ID of notification */, notificationBuilder.build());
        }
        catch (Exception ex)
        {
          Toast.makeText(getApplicationContext(), "Error while sending Notification", Toast.LENGTH_LONG);
        }
    }

    private void sendNotification(String message)
    {
        try {

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.icletsmeet1)
                    .setContentTitle("Meet Us")
                    .setContentText(MessageText)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri);

            // Set Vibrate, Sound and Light
            int defaults = 0;
            defaults = defaults | Notification.DEFAULT_LIGHTS;
            defaults = defaults | Notification.DEFAULT_VIBRATE;
            defaults = defaults | Notification.DEFAULT_SOUND;
            notificationBuilder.setNumber(++numMessages);
            notificationBuilder.setDefaults(defaults);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(NOTIFICATION_ID /* ID of notification */, notificationBuilder.build());
        }
        catch (Exception ex)
        {

        }

    }


    private void LoopMessage(String YourMobile)
    {
        android.os.Message message = mHandler.obtainMessage(1, YourMobile);
        message.sendToTarget();

    }

    public void GetImage(final String senderMobile,final String file)
    {
        try {
            String escapedFilepath = file.replace("\\","\\\\");
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
                        String Name = "temp_" + UTILITY.CurrentLocalTimeString();
                        ImageServer.SaveBitmapToExternal(bmp, Name, getApplicationContext());
                        String senderName="";
                        String contImage ="";
                        DataAccess  da = new DataAccess(getApplicationContext());
                        da.open();
                        da.insertNewMessage(senderMobile, senderMobile, Message.IMAGE_MESSAGE, Name);
                       Contact cont = da.getContactByMobile(senderMobile);
                        if (cont != null) {
                            senderName = cont.userName;
                            contImage = cont.strImage;
                        }
                        da.close();
                        sendNotificationWithAction(Message.IMAGE_MESSAGE, Name, senderMobile, senderName, MessageText, contImage);

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

}
