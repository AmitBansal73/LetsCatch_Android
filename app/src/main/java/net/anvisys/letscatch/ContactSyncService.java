package net.anvisys.letscatch;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import net.anvisys.letscatch.Common.DataAccess;
import net.anvisys.letscatch.Common.ImageServer;
import net.anvisys.letscatch.Common.Session;
import net.anvisys.letscatch.Common.UTILITY;
import net.anvisys.letscatch.Object.APP_CONST;
import net.anvisys.letscatch.Object.APP_SETTINGS;
import net.anvisys.letscatch.Object.APP_VARIABLES;
import net.anvisys.letscatch.Object.Contact;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Amit Bansal on 17-02-2017.
 */
public class ContactSyncService extends IntentService{

    RequestQueue localQueue;

    Context mContext;
    HashMap<String,String> numberlist;
    DataAccess da;

    public static int numMessages=0;
    public static HashMap<String,String> hmContact = new HashMap<>();
    public static String msgContact="";
    private static int NOTIFICATION_ID = 998;

 private static ContactSyncListener contactSyncListener;

    public interface ContactSyncListener
    {
     public void OnNewContact();
    }

    public static void RegisterListener(ContactSyncListener listener)
    {
        try {
            contactSyncListener = listener;
        }
        catch (Exception ex)
        {
            int a =1;
        }
    }

    public static void UNRegisterListener()
    {
        contactSyncListener = null;
    }



    public ContactSyncService() {
        super(ContactSyncService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            mContext = getApplicationContext();
            StartSyncingContact();
        }
        catch (Exception ex)
        {
            Toast.makeText(getApplicationContext(),"error in contact sync", Toast.LENGTH_LONG).show();
        }
    }

    public void StartSyncingContact()
    {
       try {

           if(APP_VARIABLES.NETWORK_STATUS!=true)
           {
               return;
           }
           String ServerSyncTime = Session.GetServerContactSyncUTCDateTime(mContext);

           if (UTILITY.MinutesDifference(UTILITY.GetUTCDateTime() ,ServerSyncTime) > APP_SETTINGS.CONTACT_SYNC_INTERVAL && !ServerSyncTime.matches("0"))
              {

               SyncServerContact();
           }
           else {
               try {

                   da = new DataAccess(mContext);
                   String time = Session.GetLocalContactSyncLocalMilliSecond(mContext);
                   GetNewContactInPhone(time);
                   da.open();
                   numberlist = da.getAllTempContact();
                   da.close();
                   if (time.matches("0")) {
                       Session.SetServerContactSyncUTCDateTime(mContext);
                   }
                   Session.SetLocalContactSyncLocalMilliSecond(mContext);

                   if (numberlist.size() > 0) {
                       localQueue = Volley.newRequestQueue(mContext);

                       for (String number : numberlist.keySet()
                               ) {
                           CheckLocalContactWithServer(number);
                       }
                   };
               }
               catch (Exception ex)
               {
                   int a=1;
               }
           }

       }

            catch (Exception ex)
            {
                Toast.makeText(getApplicationContext(),"error in contact sync", Toast.LENGTH_LONG).show();
            }

    }


    private HashMap<String,String> GetNewContactInPhone(String lastUpdate)
    {
        HashMap<String,String>  LocalNumberList = new HashMap<>();

        try {
            String[] mProjection = new String[]
                    {
                            ContactsContract.Profile._ID,
                            ContactsContract.Profile.DISPLAY_NAME_PRIMARY,
                            ContactsContract.CommonDataKinds.Phone.NUMBER,
                            ContactsContract.Data.DATA1,
                            ContactsContract.Data.DATA2,
                            ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER
                    };

            Uri uri = ContactsContract.Data.CONTENT_URI;
            Cursor cursorNew;
            cursorNew = mContext.getContentResolver().query(uri,
                    mProjection,
                    ContactsContract.PhoneLookup.CONTACT_LAST_UPDATED_TIMESTAMP + ">=?",
                    new String[]{lastUpdate},
                    null        // Ordering
            );



            if(cursorNew.getCount()>0)
            {
                da.open();
                if (cursorNew.moveToFirst()) {
                    do {
                        Integer id = (cursorNew.getInt(cursorNew.getColumnIndex(ContactsContract.Profile._ID)));
                        String display_name = (cursorNew.getString(cursorNew.getColumnIndex(ContactsContract.Profile.DISPLAY_NAME_PRIMARY)));
                        Integer hasNumber = (cursorNew.getInt(cursorNew.getColumnIndex(ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER)));
                        String number1 = (cursorNew.getString(cursorNew.getColumnIndex(ContactsContract.Data.DATA1)));
                        try {
                            if (hasNumber == 0) {
                                continue;
                            }
                            number1=number1.replaceAll("\\s+","");
                            if (number1.contains(APP_VARIABLES.MY_MOBILE_NUMBER) || APP_VARIABLES.MY_MOBILE_NUMBER.contains(number1)) {
                                continue;
                            }

                            String regexStr = "^[+]?[0-9]{8,15}$";
                            if (number1.matches(regexStr)) {
                                if(number1.startsWith("+"))
                                {
                                    number1 = number1.substring(1,number1.length());
                                }
                                if(!da.IsOldNumber(number1)) {
                                    da.insertNewPhoneContact(number1, display_name);
                                    if (!da.checkTempMobileNoExist(number1)) {
                                        da.insertNewTempContact(number1, display_name);
                                    }
                                }
                            }
                        }
                        catch (Exception ex)
                        {
                            continue;
                        }

                    } while (cursorNew.moveToNext());

                    da.close();
                }


            } else
            {

            }

        }

        catch (Exception ex)
        {
            Toast.makeText(getApplicationContext(),"error reading local contact", Toast.LENGTH_LONG).show();
        }
        return LocalNumberList ;
    }


    private void CheckLocalContactWithServer(final String number)
    {

        String url = APP_CONST.APP_SERVER_URL+ "/api/Contact/" + number;
        //  String reqBody = "{\"MobileNumber\":\""+ MobileNumber + "\"}";
        try {
            //  JSONObject jsRequest = new JSONObject(reqBody);
            //-------------------------------------------------------------------------------------------------

            JsonObjectRequest jsArrayRequest = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    DataAccess dAccess = new DataAccess(mContext);
                    dAccess.open();
                    try{
                        JSONArray json = response.getJSONArray("$values");
                        int x = json.length();
                        for(int i = 0; i < x; i++){
                            JSONObject jObj = json.getJSONObject(i);
                            int Id=(jObj.getInt("ID"));
                            String Function=(jObj.getString("Function"));
                            String name=(jObj.getString("UserName"));
                            String Mobile=jObj.getString("MobileNumber");
                            String Location=jObj.getString("Location");

                            if(Function.matches("ADD")||Function.matches("EDIT")||Function.matches("EDIT_IMAGE"))
                            {
                                Contact tempContact = new Contact();
                                tempContact.ID = Id;
                                tempContact.userName=numberlist.get(number);
                                tempContact.MobileNumber = Mobile;
                                tempContact.strImage = "";
                                tempContact.location = Location;
                                dAccess.insertNewContact(tempContact);
                                GetImage(tempContact.ID,tempContact.userName, tempContact.MobileNumber);

                            }
                        }
                    }
                    catch (JSONException ex)
                    {
                        //Toast.makeText(getActivity().getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    dAccess.deleteTempContact(number);
                    dAccess.close();
                    return;

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    String message = error.toString();

                }
            });


            RetryPolicy rPolicy = new DefaultRetryPolicy(0,-1,0);
            jsArrayRequest.setRetryPolicy(rPolicy);
            //jsArrayRequest.setRetryPolicy(policy);
            localQueue.add(jsArrayRequest);

            //*******************************************************************************************************
        }

        catch (Exception js)
        {
          int a =1;
        }

    }

    public void SyncServerContact()
    {
        String url = APP_CONST.APP_SERVER_URL + "api/Contact";
       // RequestQueue queue = Volley.newRequestQueue(mContext);
        RequestQueue  queue = Volley.newRequestQueue(mContext);
        String reqBody = "{\"DateTime\":\""+Session.GetServerContactSyncUTCDateTime(mContext)+"\"}";
        JSONObject jsRequest=null;

        try {
            jsRequest = new JSONObject(reqBody);
        }
        catch (JSONException jex)
        {
        }

        JsonObjectRequest jObjRequest = new JsonObjectRequest(Request.Method.POST, url,jsRequest, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try{
                    Session.SetServerContactSyncUTCDateTime(mContext);
                    JSONArray json = response.getJSONArray("$values");
                    int x = json.length();
                    for(int i = 0; i < x; i++){
                        JSONObject jObj = json.getJSONObject(i);
                        String Mobile=jObj.getString("MobileNumber");
                        if (Mobile.contains(APP_VARIABLES.MY_MOBILE_NUMBER) || APP_VARIABLES.MY_MOBILE_NUMBER.contains(Mobile))
                        {
                            continue;
                        }

                        Contact tempContact = new Contact();
                        tempContact.ID=(jObj.getInt("ID"));
                        String Function=(jObj.getString("Function"));
                        tempContact.userName=(jObj.getString("UserName"));
                        tempContact.MobileNumber = Mobile;

                        tempContact.location=jObj.getString("Location");
                        String LocalName = contactName(mContext,Mobile);
                        if(!LocalName.matches(""))
                        {
                            DataAccess da = new DataAccess(mContext);
                            da.open();
                            if(!da.checkMobileNoExist(Mobile))
                            {
                                da.insertNewContact(tempContact);
                                GetImage(tempContact.ID, tempContact.userName, tempContact.MobileNumber);
                            }
                            else
                            {
                                if(Function.matches("EDIT")) {
                                    da.updateContact(tempContact);
                                }
                                else if(Function.matches("EDIT_IMAGE"))
                                {
                                    GetImage(tempContact.ID,tempContact.userName, tempContact.MobileNumber);
                                }
                            }

                            da.close();
                        }

                       }
                }
                catch (JSONException jex)
                {
                    Toast.makeText(mContext, "JSON Exception", Toast.LENGTH_LONG).show();
                }
                catch (Exception ex)
                {
                    Toast.makeText(mContext, "Error adding contacts", Toast.LENGTH_LONG).show();
                }

                return;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        RetryPolicy rPolicy = new DefaultRetryPolicy(0,-1,0);
        jObjRequest.setRetryPolicy(rPolicy);
       // jObjRequest.setRetryPolicy(policy);
        queue.add(jObjRequest);
    }


    public String contactName(Context context, String number) {

        String name="";
        Cursor cur=null;
        try{
            Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,Uri.encode(number));
            String[] mPhoneNumberProjection = { ContactsContract.PhoneLookup.NUMBER,
                    ContactsContract.Profile.DISPLAY_NAME_PRIMARY
            };
            cur = context.getContentResolver().query(lookupUri,mPhoneNumberProjection, null, null, null);

            if (cur.moveToFirst()) {
                do {
                    name = (cur.getString(cur.getColumnIndex(ContactsContract.Profile.DISPLAY_NAME_PRIMARY)));
                    String number1 = (cur.getString(cur.getColumnIndex(ContactsContract.PhoneLookup.NUMBER)));

                } while (cur.moveToNext());


                return name;
            }
        }
        catch ( Exception ex)
        {
            return name;
        }
        finally {
            if (cur != null)
                cur.close();
        }
        return name;
    }

    public void GetImage(int ID,final String Name,final String MobileNumber)
    {
        String url = APP_CONST.APP_SERVER_URL+ "/api/Image/" + ID ;
        //-------------------------------------------------------------------------------------------------
        RequestQueue queue = Volley.newRequestQueue(mContext);
        JsonObjectRequest jsArrayRequest = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jObject) {

                try{
                    String strImage ="";
                    JSONArray jArray =  jObject.getJSONArray("$values");
                    DataAccess da = new DataAccess(mContext);
                    da.open();
                    int x = jArray.length();
                    for(int i = 0; i < x; i++){
                        JSONObject jTypeObj = jArray.getJSONObject(i);
                        int ID = jTypeObj.getInt("ID");
                        strImage=  jTypeObj.getString("ImageString");
                        da.insertContactImage(ID, strImage);
                    }
                    da.close();

                    if(contactSyncListener!=null)
                    {
                        contactSyncListener.OnNewContact();
                    }
                    else
                    {
                        sendNotificationWithAction(MobileNumber,Name,strImage);
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

        RetryPolicy rPolicy = new DefaultRetryPolicy(0,-1,0);

        jsArrayRequest.setRetryPolicy(rPolicy);
        queue.add(jsArrayRequest);

    }


    private void sendNotificationWithAction(String MobileNumber, String Name, String strImage ) {
        try {
              if(hmContact.size()==0)
                {
                    msgContact = "Your contact " + Name + " is now on LetsMeet";
                    hmContact.put(MobileNumber,Name);
                }
                else
                {
                    if(!hmContact.containsKey(MobileNumber))
                    {
                        hmContact.put(MobileNumber,Name);
                        msgContact = hmContact.size() + "of your contacts now joined LetsMeet";
                    }
                    else
                    {
                        msgContact = "Your contact " + Name + " is now on LetsMeet";
                    }

            }


            Intent noticeIntent = new Intent(this, MainActivity.class);
            noticeIntent.putExtra("IntentType", "CONTACT");
            noticeIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(noticeIntent);
            ++numMessages;
            PendingIntent noticePendingIntent = stackBuilder.getPendingIntent(numMessages, PendingIntent.FLAG_UPDATE_CURRENT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder =  new NotificationCompat.Builder(this);

            notificationBuilder.setSmallIcon(R.drawable.letsmeet)
                    .setContentTitle("Lets Meet")
                    .setContentText(msgContact)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(noticePendingIntent);


            // Set Vibrate, Sound and Light
            int defaults = 0;
            defaults = defaults | Notification.DEFAULT_LIGHTS;
            defaults = defaults | Notification.DEFAULT_VIBRATE;
            defaults = defaults | Notification.DEFAULT_SOUND;
            //notificationBuilder.setNumber(++numMessages);
            notificationBuilder.setDefaults(defaults);


            if (strImage !=null)
            {
                Bitmap bitmap = ImageServer.getBitmapFromString(strImage,getApplicationContext());
                notificationBuilder.setLargeIcon(bitmap);
            }

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


            notificationManager.notify(NOTIFICATION_ID /* ID of notification */, notificationBuilder.build());
        }
        catch (Exception ex)
        {
         int a =1;
        }
    }

}
