package net.anvisys.letscatch.Register;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.squareup.picasso.Picasso;

import net.anvisys.letscatch.Common.ImageServer;
import net.anvisys.letscatch.Common.Session;
import net.anvisys.letscatch.Object.APP_CONST;
import net.anvisys.letscatch.Object.Profile;
import net.anvisys.letscatch.R;
import net.anvisys.letscatch.Shape.OvalImageView;
import net.anvisys.letscatch.StartActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;

public class RegisterActivity extends AppCompatActivity {

    OvalImageView profileImage;
    TextView Mobile;
    EditText txtName;
    EditText txtEmail;
    EditText txtLocation;
    EditText txtPassword;
    EditText txtConfirmPassword;
    TextView txtMessage;
    Button btnRegister;
    ProgressBar prgBar;
    String MobileNumber="",profileName="",GCMCode="",profileLocation="",strImage="",email="",newPassword="";
    static final int REQUEST_IMAGE_GET = 1;
    static final int REQUEST_IMAGE_CROP = 2;
    private static String FileName = "Profile.png";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        prgBar = (ProgressBar) findViewById(R.id.progressBar);
        prgBar.setVisibility(View.GONE);

        profileImage = (OvalImageView) findViewById(R.id.profile_image);
        Mobile = (TextView) findViewById(R.id.txtmobile);
        txtName = (EditText) findViewById(R.id.txtName);
        txtLocation = (EditText) findViewById(R.id.Location);
        txtEmail = (EditText) findViewById(R.id.txtEmail);
        txtMessage = (TextView) findViewById(R.id.txtMessage);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        txtPassword = (EditText) findViewById(R.id.Password);
        txtConfirmPassword = (EditText) findViewById(R.id.txtConfirmPassword);


        Intent mIntent = getIntent();
        MobileNumber = mIntent.getStringExtra("Mobile");

        Mobile.setText(MobileNumber);
        try {
           /* Bitmap myImage = ImageServer.GetImageBitmap(MobileNumber, this);
            if (myImage != null) {
                profileImage.setImageBitmap(myImage);
                strImage = ImageServer.getStringFromBitmap(myImage);
            }*/

          /*  String url1 = APP_CONST.IMAGE_URL + UserID +".png";
            Picasso.with(getApplicationContext()).load(url1).error(R.drawable.user_image).into(profileImage);*/
        }
        catch (Exception ex)
        {
            Toast.makeText(getApplicationContext(), "Error Reading Image",Toast.LENGTH_LONG).show();
        }
    }

    public void SetImage(View view)
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_GET);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        try {
            if (requestCode == REQUEST_IMAGE_GET) {

                if (data != null) {
                    Uri uri = data.getData();
                    ImageCropFunction(uri);
                }
            }
            else if (requestCode == REQUEST_IMAGE_CROP) {
                Bitmap bitmap;
                if (data != null) {
                    Bundle bundle = data.getExtras();
                    if(bundle!= null) {
                        bitmap = bundle.getParcelable("data");
                        profileImage.setImageBitmap(bitmap);
                    }
                    else
                    {
                        Uri cropUri =  data.getData();
                        InputStream image_stream = getContentResolver().openInputStream(cropUri);
                        bitmap= BitmapFactory.decodeStream(image_stream);
                        profileImage.setImageBitmap(bitmap);
                    }
                    ImageServer.SaveBitmapImage(bitmap, MobileNumber, this);
                    strImage = ImageServer.getStringFromBitmap(bitmap);
                    profileImage.invalidate();
                }
            }
        } catch (Exception ex)
        {
            int a=1;
        }


    }

    /* old OnActivityResult Method without Crop

        if (requestCode == REQUEST_IMAGE_GET && resultCode == RESULT_OK) {
            Uri fullPhotoUri;
            try {

                fullPhotoUri = data.getData();
                InputStream image_stream = getContentResolver().openInputStream(fullPhotoUri);
                Bitmap bitmap= BitmapFactory.decodeStream(image_stream);
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap,200,200,false);
                profileImage.setImageBitmap(resizedBitmap);
                ImageServer.SaveBitmapImage(resizedBitmap,MobileNumber,this);
                strImage = ImageServer.getStringFromBitmap(resizedBitmap);
                profileImage.invalidate();
            }
            catch (Exception ex)
            {
                txtMessage.setText("Error occurred reading Image");
            }
        }
    */


    public void Register(View view)
    {
        try {


            if (strImage.matches("") || strImage == null) {
                txtMessage.setVisibility(View.VISIBLE);
                txtMessage.setText("Please set Image");
                return;
            }
            newPassword = txtPassword.getText().toString();
            String confirmPassword = txtConfirmPassword.getText().toString();
            if (!newPassword.matches(confirmPassword)) {
                txtMessage.setVisibility(View.VISIBLE);
                txtMessage.setText("Password do not match");
                return;
            }
            MobileNumber = Mobile.getText().toString();
            if (MobileNumber.isEmpty() || MobileNumber == null) {
                txtMessage.setVisibility(View.VISIBLE);
                txtMessage.setText("Password do not match");
                return;
            }
            profileName = txtName.getText().toString();
            if (profileName.matches("") || profileName == null) {
                txtMessage.setVisibility(View.VISIBLE);
                txtMessage.setText("Please Provide Name");
                return;
            }


            profileLocation = txtLocation.getText().toString();
            if (profileLocation.matches("") || profileLocation == null) {
                txtMessage.setVisibility(View.VISIBLE);
                txtMessage.setText("Please Add Location");
                return;
            }
            email = txtEmail.getText().toString();
            if (email.matches("") || email == null) {
                txtMessage.setVisibility(View.VISIBLE);
                txtMessage.setText("Provide EMail ID");
                return;
            }
            if (checkPlayServices()) {
                prgBar.setVisibility(View.VISIBLE);
                new Register().execute();
            }
        }
        catch (Exception ex)
        {
            txtMessage.setText("Error occurred on Register");
        }

    }


    private boolean checkPlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if(result != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            prgBar.setVisibility(View.INVISIBLE);
            return false;
        }

        return true;
    }


    private class Register extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {

            String msg = "";
            try {
                if (GCMCode == null || GCMCode =="") {
                    InstanceID instanceID = InstanceID.getInstance(getApplicationContext());
                    GCMCode = instanceID.getToken(APP_CONST.GOOGLE_PROJ_ID, GoogleCloudMessaging.INSTANCE_ID_SCOPE);
                }
            }
            catch (Exception ex)
            {

            }
            return msg;
        }

        @Override
        protected void onPostExecute(String s) {
            if (!TextUtils.isEmpty(GCMCode)) {
                registerProfile();


            } else {
                prgBar.setVisibility(View.INVISIBLE);
                Toast.makeText(
                        getApplicationContext(),
                        "Reg ID Creation Failed.\n\nEither you haven't enabled Internet or GCM server" +
                                " is busy right now. Make sure you enabled Internet and try registering again after some time."
                                + GCMCode, Toast.LENGTH_LONG).show();
            }

            //prgDialog.cancel();
        }
    }



    private boolean registerProfile()
    {
        btnRegister.setEnabled(false);
        prgBar.setVisibility(View.VISIBLE);
        String url = APP_CONST.APP_SERVER_URL+ "/api/cuuser";
        String reqBody = "{\"MobileNumber\":\""+ MobileNumber +"\",\"userName\":\""+ profileName + "\",\"Email\":\""+ email + "\",\"Password\":\""+ newPassword + "\",\"GCMCode\":\""+ GCMCode + "\",\"Location\":\""+ profileLocation + "\",\"ImageString\":\""+ strImage + "\"}";
        try {
            JSONObject jsRequest = new JSONObject(reqBody);
            //-------------------------------------------------------------------------------------------------
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

            JsonObjectRequest jsArrayRequest = new JsonObjectRequest(Request.Method.POST, url,jsRequest, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jObj) {
                    prgBar.setVisibility(View.GONE);
                    Profile myProfile = new Profile();
                    try {
                        String response = jObj.getString("Response");
                        if(response.matches("OK")) {
                            Toast.makeText(getApplicationContext(), "Registered Successfully.",
                                    Toast.LENGTH_SHORT).show();
                            myProfile.UserID = Integer.parseInt(jObj.getString("UserID"));

                            myProfile.NAME = profileName;
                            myProfile.strImage = strImage;
                            myProfile.MOB_NUMBER = MobileNumber;
                            myProfile.LOCATION = profileLocation;
                            myProfile.E_MAIL = email;
                            myProfile.REG_ID = GCMCode;
                            Session.AddUser(getApplicationContext(), myProfile);
                            //   ImageSaver.SaveImageByte(ImageSaver.getByteFromString( myProfile.strImage),MobileNumber,getApplicationContext());
                            Intent mainIntent = new Intent(RegisterActivity.this,StartActivity.class);
                            mainIntent.putExtra("parent","Register");
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(mainIntent);
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Failed to Register.",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                    }
                    catch (JSONException jex)
                    {
                        Toast.makeText(getApplicationContext(), "Error Reading response.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }


                   // RegisterActivity.this.finish();


                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    btnRegister.setEnabled(true);
                    String message = error.toString();
                    prgBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Post could not be submitted : Try Again",
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
            prgBar.setVisibility(View.GONE);
            return false;

        }

        finally {
            return true;

        }

    }


    public void ImageCropFunction(Uri uri) {

        // Image Crop Code
        try {
            Intent CropIntent = new Intent("com.android.camera.action.CROP");

            CropIntent.setDataAndType(uri, "image/*");

            CropIntent.putExtra("crop", "true");
            CropIntent.putExtra("outputX", 100);
            CropIntent.putExtra("outputY", 100);
            CropIntent.putExtra("aspectX", 1);
            CropIntent.putExtra("aspectY", 1);
            CropIntent.putExtra("scaleUpIfNeeded", false);
            CropIntent.putExtra("return-data", true);

            startActivityForResult(CropIntent, REQUEST_IMAGE_CROP);

        } catch (Exception e) {

        }
    }
}
