package net.anvisys.letscatch.Register;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import net.anvisys.letscatch.Common.ImageServer;
import net.anvisys.letscatch.Common.Session;
import net.anvisys.letscatch.Object.APP_CONST;
import net.anvisys.letscatch.Object.Profile;
import net.anvisys.letscatch.R;
import net.anvisys.letscatch.Shape.OvalImageView;
import net.anvisys.letscatch.StartActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    TextView MobileNo;
    TextView name;
    TextView txtEmail;
    TextView Location;
    OvalImageView profileImage;
    EditText txtPassword;
    TextView txtMessage;
    TextView txtForgotPassword;
    RadioGroup radioGroup;
    RadioButton radioSMS;
    RadioButton radioEMAIL;

    EditText NewPswd;
    EditText ConfirmPswd;
    Button Reset,btnLogin;
    ProgressBar prgBar;

    String Mobile;
    String Password;
    String Name;
    String Email;
    String SendPasswordTo;
    String strImage="";
    String RegID="", NewRegId="";
    static final int REQUEST_IMAGE_GET = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prgBar=(ProgressBar)findViewById(R.id.progressBar);
        prgBar.setVisibility(View.GONE);

        MobileNo = (TextView)findViewById(R.id.mobile);
        name = (TextView)findViewById(R.id.txtName);
        txtEmail = (TextView)findViewById(R.id.txtEmail);

        Location = (TextView)findViewById(R.id.txtLocation);
        profileImage = (OvalImageView)findViewById(R.id.profile_image);
        txtPassword = (EditText)findViewById(R.id.txtPassword);
        txtMessage = (TextView)findViewById(R.id.txtMessage);
        txtForgotPassword = (TextView)findViewById(R.id.txtForgotPassword);
        //  NewPswd = (EditText) findViewById(R.id.NewPassword);
        // ConfirmPswd = (EditText) findViewById(R.id.txtConfirmPassword);
        radioGroup = (RadioGroup)findViewById(R.id.radioGroup);
        radioSMS = (RadioButton)findViewById(R.id.sentToSMS);
        radioEMAIL = (RadioButton)findViewById(R.id.sentToEmail);
        Reset = (Button)findViewById(R.id.btnReset);
        btnLogin = (Button)findViewById(R.id.btnLogin);

        Intent mIntent = getIntent();
        Mobile = mIntent.getStringExtra("Mobile");
        MobileNo.setText(Mobile);
        Name = mIntent.getStringExtra("Name");
        name.setText(Name);

        Email = mIntent.getStringExtra("Email");
        txtEmail.setText(Email);
        Location.setText(mIntent.getStringExtra("Location"));
        RegID = mIntent.getStringExtra("GCMCode");

        strImage = mIntent.getStringExtra("Image");
        if (!strImage.matches("")&& strImage != null) {
            ImageServer.SaveImageString(strImage,Mobile,this);
            profileImage.setImageBitmap(ImageServer.GetImageBitmap(Mobile,this));
        }
        else
        {
            Bitmap bitmap =  ImageServer.GetDefaultImage(this);
            profileImage.setImageBitmap(bitmap);
        }
        txtForgotPassword.setText("Forgot Password?");


        radioSMS.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b == true) {
                    SendPasswordTo = "SMS";
                } else {
                    SendPasswordTo = "MAIL";
                }
            }
        });

        radioEMAIL.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b == true) {
                    SendPasswordTo = "MAIL";
                } else {
                    SendPasswordTo = "SMS";
                }
            }
        });
    }


    public void Login(View view)
    {

        Password = txtPassword.getText().toString();
        validateProfile();
    }

    public void Forgot(View view)
    {
        txtForgotPassword.setText("New Password will be sent to ");
        // NewPswd.setVisibility(View.VISIBLE);
        //ConfirmPswd.setVisibility(View.VISIBLE);
        radioGroup.setVisibility(View.VISIBLE);
        radioSMS.setEnabled(false);
        radioEMAIL.setEnabled(true);
        Reset.setVisibility(View.VISIBLE);

    }

    private boolean validateProfile()
    {
        btnLogin.setEnabled(false);
        prgBar.setVisibility(View.VISIBLE);
        String url = APP_CONST.APP_SERVER_URL+ "/api/Validate";
        String reqBody = "{\"MobileNumber\":\""+ Mobile +"\",\"UserName\":\""+ Name + "\",\"Password\":\""+ Password + "\"}";
        try {
            JSONObject jsRequest = new JSONObject(reqBody);
            //-------------------------------------------------------------------------------------------------
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

            JsonObjectRequest jsArrayRequest = new JsonObjectRequest(Request.Method.POST, url,jsRequest, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jObj) {
                    try {
                        // JSONObject jO = jArray.getJSONObject(0);
                        btnLogin.setEnabled(true);
                        prgBar.setVisibility(View.GONE);
                        String name = jObj.getString("Name");
                        if (jObj.getString("Name") == "null") {
                            txtMessage.setVisibility(View.VISIBLE);
                            txtMessage.setText("Wrong Id or Password");
                        } else {
                            Intent loginIntent = new Intent(LoginActivity.this, StartActivity.class);
                            Profile myProfile = new Profile();
                            myProfile.NAME = jObj.getString("Name");
                            myProfile.strImage = jObj.getString("Image");
                            myProfile.MOB_NUMBER = Mobile;
                            myProfile.LOCATION = jObj.getString("Location");
                            myProfile.E_MAIL = Email;
                            myProfile.REG_ID = jObj.getString("GCMCode");

                              new  Register().execute();

                            loginIntent.putExtra("parent", "Login");
                            Session.AddUser(getApplicationContext(), myProfile);
                            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(loginIntent);
                            //LoginActivity.this.finish();
                        }
                    }
                    catch (JSONException js) {
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    String message = error.toString();
                    prgBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "Post could not be submitted : Try Again",Toast.LENGTH_LONG).show();

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
        catch (JSONException js)
        {
            btnLogin.setEnabled(true);
            prgBar.setVisibility(View.GONE);
            return false;

        }

        finally {
            return true;

        }

    }

    public void Reset(View view)
    {
        Reset.setEnabled(false);
        prgBar.setVisibility(View.VISIBLE);
        String url = APP_CONST.APP_SERVER_URL+ "/api/Password";
        String reqBody = "{\"MobileNumber\":\""+ Mobile +"\",\"Email\":\""+ Email + "\",\"UserName\":\""+ Name + "\"}";
        try {
            JSONObject jsRequest = new JSONObject(reqBody);
            //-------------------------------------------------------------------------------------------------
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

            JsonObjectRequest jsArrayRequest = new JsonObjectRequest(Request.Method.POST, url,jsRequest, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jObj) {
                    Reset.setEnabled(true);
                    prgBar.setVisibility(View.GONE);
                    txtForgotPassword.setText("Password has been sent. Login using New Password");
                    // NewPswd.setVisibility(View.VISIBLE);
                    //ConfirmPswd.setVisibility(View.VISIBLE);
                    radioGroup.setVisibility(View.VISIBLE);
                    radioEMAIL.setEnabled(true);
                    Reset.setVisibility(View.VISIBLE);

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    String message = error.toString();
                    Reset.setEnabled(true);
                    prgBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Operation failed try again : Try Again",
                            Toast.LENGTH_LONG).show();

                }
            });

            RetryPolicy rPolicy = new DefaultRetryPolicy(0,-1,0);
            jsArrayRequest.setRetryPolicy(rPolicy);
            queue.add(jsArrayRequest);

            //*******************************************************************************************************
        }
        catch (JSONException js)
        {
            Reset.setEnabled(true);
            prgBar.setVisibility(View.GONE);

        }

        finally {

        }

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

                if(!RegID.matches(NewRegId))
                {
                    UpdateRegID();
                }

            } else {
                prgBar.setVisibility(View.INVISIBLE);
                Toast.makeText(
                        getApplicationContext(),
                        "Reg ID Creation Failed.\n\nEither you haven't enabled Internet or GCM server" +
                                " is busy right now. Make sure you enabled Internet and try registering again after some time."
                                + RegID, Toast.LENGTH_LONG).show();
            }

            //prgDialog.cancel();
        }
    }



    private boolean UpdateRegID()
    {
        prgBar.setVisibility(View.VISIBLE);
        String url = APP_CONST.APP_SERVER_URL+ "/api/Registration";
        String reqBody = "{\"MobileNumber\":\""+ Mobile +"\",\"GCMCode\":\""+ NewRegId  + "\"}";
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
                    prgBar.setVisibility(View.GONE);
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
            prgBar.setVisibility(View.GONE);
            return false;

        }

        finally {
            return true;

        }

    }

}
