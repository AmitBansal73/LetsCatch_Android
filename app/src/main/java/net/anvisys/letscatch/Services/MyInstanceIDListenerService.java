package net.anvisys.letscatch.Services;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.android.gms.iid.InstanceIDListenerService;

import net.anvisys.letscatch.Object.APP_CONST;
import net.anvisys.letscatch.Object.APP_VARIABLES;
import net.anvisys.letscatch.Object.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Amit Bansal on 23-02-2017.
 */
public class MyInstanceIDListenerService extends InstanceIDListenerService {

    String NewRegId;
    @Override
    public void onTokenRefresh() {
       // super.onTokenRefresh();
        Log.AddLog("GCM Registration changed",getApplicationContext());

        new  Register().execute();
    }

    private class Register extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {

            String msg = "";
            try {
                InstanceID instanceID = InstanceID.getInstance(getApplicationContext());
                NewRegId = instanceID.getToken(APP_CONST.GOOGLE_PROJ_ID, GoogleCloudMessaging.INSTANCE_ID_SCOPE);
           }
            catch (Exception ex)
            {

            }
            return msg;
        }

        @Override
        protected void onPostExecute(String s) {
            if (!TextUtils.isEmpty(NewRegId)) {

                if(!NewRegId.matches("") && NewRegId!= null)
                {
                    UpdateRegID();
                }

            } else {

                Toast.makeText(
                        getApplicationContext(),
                        "Reg ID Creation Failed.\n\nEither you haven't enabled Internet or GCM server" +
                                " is busy right now. Make sure you enabled Internet and try registering again after some time."
                                , Toast.LENGTH_LONG).show();
            }

            //prgDialog.cancel();
        }
    }



    private boolean UpdateRegID()
    {

        String url = APP_CONST.APP_SERVER_URL+ "/api/Registration";
        String reqBody = "{\"MobileNumber\":\""+ APP_VARIABLES.MY_MOBILE_NUMBER +"\",\"GCMCode\":\""+ NewRegId  + "\"}";
        try {
            JSONObject jsRequest = new JSONObject(reqBody);
            //-------------------------------------------------------------------------------------------------
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

            JsonObjectRequest jsArrayRequest = new JsonObjectRequest(Request.Method.POST, url,jsRequest, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jObj) {
                    try {
                        String Response =   jObj.getString("Response");
                        if (Response.matches("OK")) {
                            Toast.makeText(getApplicationContext(), "Registered Successfully.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else if(Response.matches("Fail"))
                        {
                            Toast.makeText(getApplicationContext(), "Could not Register.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    catch (Exception ex)
                    {

                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    String message = error.toString();

                    Toast.makeText(getApplicationContext(), "Could not Register : Try Again",
                            Toast.LENGTH_LONG).show();

                }
            });

            RetryPolicy policy = new RetryPolicy() {
                @Override
                public int getCurrentTimeout() {
                    return 8000;
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
        catch (JSONException js)
        {

            return false;

        }

        finally {
            return true;

        }

    }
}
