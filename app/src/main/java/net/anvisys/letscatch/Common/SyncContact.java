package net.anvisys.letscatch.Common;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import net.anvisys.letscatch.Object.APP_CONST;
import net.anvisys.letscatch.Object.APP_SETTINGS;
import net.anvisys.letscatch.Object.APP_VARIABLES;
import net.anvisys.letscatch.Object.Contact;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Amit Bansal on 13-02-2017.
 */
public class SyncContact {

  static SyncContactListener ContactSyncListener;
    RequestQueue queue;

    Context mContext;
    HashMap<String,String> numberlist;
   // List<LocalContact> localContList = new ArrayList<>();

    public SyncContact(Context mContext) {
        this.mContext = mContext;
    }

    public void StartSyncingContact()
    {
       if(Session.GetLocalContactSyncLocalMilliSecond(mContext).matches(""))
        {
            try {
                // This is first synchronization at Login
                GetNewContactInPhone("0");
                if (numberlist.size() > 0) {
                    queue = Volley.newRequestQueue(mContext);
                    for (String number : numberlist.keySet()
                            ) {
                        CheckLocalContactWithServer(number);
                        Thread.sleep(5000);
                    }
                    Session.SetLocalContactSyncLocalMilliSecond(mContext);
                    Session.SetServerContactSyncUTCDateTime(mContext);
                }
            }
            catch (Exception ex)
            {

            }

        }
       else if(UTILITY.MinutesDifference(UTILITY.GetUTCDateTime(), Session.GetServerContactSyncUTCDateTime(mContext))> APP_SETTINGS.CONTACT_SYNC_INTERVAL)
        {
            SyncServerContact();
            Session.SetServerContactSyncUTCDateTime(mContext);
        }
        else {

            GetNewContactInPhone(Session.GetLocalContactSyncLocalMilliSecond(mContext));

            if (numberlist.size() > 0) {
                for (String  number : numberlist.keySet()
                        ) {
                    CheckLocalContactWithServer(number);
                }

                Session.SetLocalContactSyncLocalMilliSecond(mContext);
            }
        }
    }


    private void GetNewContactInPhone(String lastUpdate)
    {
        numberlist = new HashMap<>();

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

            if (cursorNew.moveToFirst()) {
                do {
                    Integer id = (cursorNew.getInt(cursorNew.getColumnIndex(ContactsContract.Profile._ID)));
                    String display_name = (cursorNew.getString(cursorNew.getColumnIndex(ContactsContract.Profile.DISPLAY_NAME_PRIMARY)));
                    Integer hasNumber = (cursorNew.getInt(cursorNew.getColumnIndex(ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER)));
                    String number1 = (cursorNew.getString(cursorNew.getColumnIndex(ContactsContract.Data.DATA1)));


                        try {
                            if (hasNumber == 0 || number1.contains(APP_VARIABLES.MY_MOBILE_NUMBER) || APP_VARIABLES.MY_MOBILE_NUMBER.contains(number1)) {
                                continue;
                            }
                            number1=number1.replaceAll("\\s+","");
                            String regexStr = "^[+]?[0-9]{8,15}$";
                            if (number1.matches(regexStr)) {

                                if(!numberlist.containsKey(number1))
                                {
                                    numberlist.put(number1,display_name);
                                }

                            }
                        }
                        catch (Exception ex)
                        {
                            continue;
                        }

                } while (cursorNew.moveToNext());
            }

                return;
            } else
                return;
        }
        catch (Exception ex)
        {
            return ;
        }
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

                            if(Function.matches("ADD")||Function.matches("EDIT_PROFILE")||Function.matches("EDIT_IMAGE"))
                            {
                                Contact tempContact = new Contact();
                                tempContact.ID = Id;
                                tempContact.userName=numberlist.get(number);
                                tempContact.MobileNumber = Mobile;
                                tempContact.strImage = "";
                                tempContact.location = Location;
                                DataAccess da = new DataAccess(mContext);
                                da.open();

                                da.insertNewContact(tempContact);

                                da.close();
                                Session.SetLocalContactSyncLocalMilliSecond(mContext);
                              //  GetImage(tempContact.ID);
                            }
                        }
                    }
                    catch (JSONException ex)
                    {
                        //Toast.makeText(getActivity().getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    return;

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    String message = error.toString();
                    Toast.makeText(mContext, "Network Error : While Synching Contact", Toast.LENGTH_LONG).show();
                }
            });

            RetryPolicy policy = new RetryPolicy() {
                @Override
                public int getCurrentTimeout() {
                    return 5000;
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

            //*******************************************************************************************************
        }

        catch (Exception js)
        {

        }

    }


    public void SyncServerContact()
    {
        String url = APP_CONST.APP_SERVER_URL + "api/Contact";
        RequestQueue queue = Volley.newRequestQueue(mContext);

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
                                GetImage(tempContact.ID);
                            }
                            else
                            {
                                if(Function.matches("EDIT_PROFILE")) {
                                    da.updateContact(tempContact);
                                }
                                else if(Function.matches("EDIT_IMAGE"))
                                {
                                    GetImage(tempContact.ID);
                                }
                            }

                            da.close();
                        }

                        if( ContactSyncListener !=null)
                        {
                            ContactSyncListener.ContactAdded("");
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

        RetryPolicy policy = new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 5000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 3;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        };
        jObjRequest.setRetryPolicy(policy);
        queue.add(jObjRequest);
    }

    public void GetImage(int ID)
    {


        String url = APP_CONST.APP_SERVER_URL+ "/api/Image/" + ID ;


        //-------------------------------------------------------------------------------------------------
        RequestQueue queue = Volley.newRequestQueue(mContext);
        JsonObjectRequest jsArrayRequest = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jObject) {

                try{

                    JSONArray jArray =  jObject.getJSONArray("$values");
                    DataAccess da = new DataAccess(mContext);
                    da.open();
                    int x = jArray.length();
                    for(int i = 0; i < x; i++){
                        JSONObject jTypeObj = jArray.getJSONObject(i);
                        int ID = jTypeObj.getInt("ID");
                        String strImage=  jTypeObj.getString("ImageString");
                        da.insertContactImage(ID, strImage);
                    }

                    da.close();

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

    private class LocalContact
    {
        public String LocalName;
        public String Number;
    }



    public interface SyncContactListener
    {
        public void ContactAdded(String message);
    }

    // Assign the listener implementing events interface that will receive the events
    public static void RegisterCustomObjectListener(SyncContactListener listener) {
        ContactSyncListener = listener;
    }
}
