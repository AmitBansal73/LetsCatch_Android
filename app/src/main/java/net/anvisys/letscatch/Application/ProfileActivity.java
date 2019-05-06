package net.anvisys.letscatch.Application;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import net.anvisys.letscatch.Object.APP_CONST;
import net.anvisys.letscatch.Common.ImageServer;
import net.anvisys.letscatch.MainActivity;
import net.anvisys.letscatch.Object.Profile;
import net.anvisys.letscatch.Common.Session;
import net.anvisys.letscatch.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import net.anvisys.letscatch.Shape.OvalImageView;


public class ProfileActivity extends AppCompatActivity {
    OvalImageView profileImage;
    TextView Mobile;
    TextView txtName;
    TextView txtEmail;
    TextView txtLocation;

    EditText editName;
    EditText editEmail;
    EditText editLocation;

    EditText oldPassword;
    EditText newPassword;
    EditText cnfPassword;

    TextView txtProfileMessage;
    TextView txtPasswordMessage;
    TextView txtChangePassword;
    TextView txtEditProfile;


    View txtProfile, editProfile, editPassword;
    Button btnUpdateProfile;
    Button btnUpdatePassword;

    ProgressBar prgBar;
    String MobileNumber,profileName,profileLocation,email,strImage, regID;
    static final int REQUEST_IMAGE_GET = 1;
    static final int REQUEST_IMAGE_CROP = 2;
    Profile myProfile;
    Bitmap newBitmap;
    Button btnImageUpload;

    String strPassword;
    String strNewPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        prgBar = (ProgressBar) findViewById(R.id.progressBar);
        prgBar.setVisibility(View.GONE);

        profileImage = (OvalImageView) findViewById(R.id.profile_image);
        Mobile = (TextView) findViewById(R.id.txtmobile);

        txtName = (TextView) findViewById(R.id.txtName);
        txtLocation = (TextView) findViewById(R.id.Location);
        txtEmail = (TextView) findViewById(R.id.txtEmail);

        txtChangePassword = (TextView)findViewById(R.id.txtChangePassword);
        txtEditProfile = (TextView)findViewById(R.id.txtEditProfile);

        txtProfile = findViewById(R.id.ShowProfileContent);
        editProfile = findViewById(R.id.EditProfileContent);
        editPassword = findViewById(R.id.editPasswordContent);
        btnImageUpload = (Button)findViewById(R.id.btnImageUpdate);
        myProfile = Session.GetUser(this);

        Bitmap bmp = ImageServer.GetImageBitmap(myProfile.MOB_NUMBER, this);
        if (bmp == null)
        {
            ImageServer.SaveImageString(myProfile.strImage,myProfile.MOB_NUMBER,this);
            bmp = ImageServer.GetImageBitmap(myProfile.MOB_NUMBER, this);
        }

        profileImage.setImageBitmap(bmp);

        MobileNumber = myProfile.MOB_NUMBER;
        Mobile.setText(MobileNumber);

        profileName = myProfile.NAME;
        txtName.setText(profileName);

        email= myProfile.E_MAIL;
        txtEmail.setText(email);

        profileLocation = myProfile.LOCATION;
        txtLocation.setText(profileLocation);
        regID = myProfile.REG_ID;

    }

    public void UpdateProfile( View view)
    {
        try {

            if (myProfile.strImage == null || myProfile.strImage == "") {
                txtProfileMessage.setVisibility(View.VISIBLE);
                txtProfileMessage.setText("Please Select Image");
                return;

            }

            String newName = txtName.getText().toString().trim();
            String newLocation = txtLocation.getText().toString().trim();
            String newEmail = txtEmail.getText().toString().trim();

            if (newName.matches(profileName.trim()) && newLocation.matches(profileLocation.trim()) && newEmail.matches(email.trim()))
            {
                txtProfileMessage.setVisibility(View.VISIBLE);
                txtProfileMessage.setText("No Change in Data");
               // Toast.makeText(this, "No Change in Data", Toast.LENGTH_LONG).show();
                return;
            } else {
                profileName= newName;
                profileLocation = newLocation;
                email = newEmail;
                UpdateProfile();
            }
        }
        catch (Exception ex)
        {
            Toast.makeText(this,"Failed to Update Profile", Toast.LENGTH_LONG).show();
        }

    }

   public void UpdatePassword(View v)
   {
       if(oldPassword.getText()==null || newPassword.getText()==null|| cnfPassword.getText()==null)
       {
           txtPasswordMessage.setText("Password can not be empty");
       }

      else if(!newPassword.getText().toString().trim().matches(cnfPassword.getText().toString().trim()))
       {
           txtPasswordMessage.setText("Password does not match");
       }

       else {
           strPassword = oldPassword.getText().toString().trim();
           strNewPassword = newPassword.getText().toString().trim();
           UpdateMyPassword();
       }


   }

    public void EditClick(View v)
    {
        if(v== txtEditProfile)
        {
            if (txtProfile.getVisibility()==View.VISIBLE) {
                txtProfile.setVisibility(View.GONE);
            }
            if(editProfile.getVisibility() == View.GONE)
            {
                editName = (EditText) findViewById(R.id.editName);
                editEmail = (EditText) findViewById(R.id.editEmail);
                editLocation = (EditText) findViewById(R.id.editLocation);
                btnUpdateProfile = (Button) findViewById(R.id.btnUpdateProfile);
                editName.setText(profileName);
                editEmail.setText(email);
                editLocation.setText(profileLocation);
                txtProfileMessage = (TextView) findViewById(R.id.txtProfileMessage);

                editProfile.setVisibility(View.VISIBLE);
            }
            if(editPassword.getVisibility() == View.VISIBLE)
            {
                editPassword.setVisibility(View.GONE);
            }
        }
        else if(v== txtChangePassword)
        {
            if (txtProfile.getVisibility()==View.VISIBLE) {
                txtProfile.setVisibility(View.GONE);
            }
            if(editProfile.getVisibility() == View.VISIBLE)
            {
                editProfile.setVisibility(View.GONE);
            }
            if(editPassword.getVisibility() == View.GONE)
            {
                oldPassword = (EditText) findViewById(R.id.oldPassword);
                newPassword = (EditText) findViewById(R.id.newPassword);
                cnfPassword = (EditText) findViewById(R.id.cnfPassword);
                txtPasswordMessage= (TextView)findViewById(R.id.txtPasswordMessage);
                btnUpdatePassword = (Button) findViewById(R.id.btnUpdatePassword);

                editPassword.setVisibility(View.VISIBLE);
            }
        }
    }

    private void UpdateProfile()
    {
        prgBar.setVisibility(View.VISIBLE);
        String url = APP_CONST.APP_SERVER_URL+ "/api/Profile";
        String reqBody = "{\"MobileNumber\":\""+ MobileNumber + "\",\"userName\":\""+ profileName + "\",\"Email\":\""+ email + "\",\"Location\":\""+ profileLocation + "\"}";
        try {
            JSONObject jsRequest = new JSONObject(reqBody);
            //-------------------------------------------------------------------------------------------------
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

            JsonObjectRequest jsArrayRequest = new JsonObjectRequest(Request.Method.POST, url,jsRequest, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jObj) {

                    Toast.makeText(getApplicationContext(), "Updated Successfully.",
                            Toast.LENGTH_SHORT).show();
                    prgBar.setVisibility(View.GONE);

                    Profile myProfile = new Profile();
                    myProfile.NAME = profileName;
                    myProfile.strImage = strImage;
                    myProfile.MOB_NUMBER = MobileNumber;
                    myProfile.LOCATION = profileLocation;
                    myProfile.E_MAIL = email;
                    myProfile.REG_ID = regID;
                    ImageServer.SaveBitmapImage(newBitmap, myProfile.MOB_NUMBER,getApplicationContext());
                    Session.AddUser(getApplicationContext(), myProfile);
                    Intent mainIntent = new Intent(ProfileActivity.this,MainActivity.class);
                    mainIntent.putExtra("parent","Profile");

                    startActivity(mainIntent);
                    ProfileActivity.this.finish();

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    String message = error.toString();
                    prgBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Post could not be submitted : Try Again",
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
         }

        finally {

        }

    }

    private void UpdateMyPassword()
    {
        prgBar.setVisibility(View.VISIBLE);
        String url = APP_CONST.APP_SERVER_URL+ "/api/ChangePassword";
        String reqBody = "{\"MobileNumber\":\""+ MobileNumber + "\",\"Password\":\""+ strPassword + "\",\"NewPassword\":\""+ strNewPassword + "\"}";
        try {
            JSONObject jsRequest = new JSONObject(reqBody);
            //-------------------------------------------------------------------------------------------------
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

            JsonObjectRequest jsArrayRequest = new JsonObjectRequest(Request.Method.POST, url,jsRequest, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jObj) {
                    try {
                        String response = jObj.getString("Response");
                        if(response.matches("OK"))
                        {
                            txtPasswordMessage.setText("Password Changed");
                        }
                        else if(response.matches("NoUser"))
                        {
                            txtPasswordMessage.setText("Wrong Password");
                        }
                        else
                        {
                            txtPasswordMessage.setText(response);
                        }

                    }
                    catch (JSONException jEx)
                    {

                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    String message = error.toString();
                    prgBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Post could not be submitted : Try Again",
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
        }

        finally {


        }

    }

    public void EditImage(View view)
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
            } else if (requestCode == REQUEST_IMAGE_CROP) {

                if (data != null) {

                    Bundle bundle = data.getExtras();
                    if(bundle!= null) {
                        newBitmap = bundle.getParcelable("data");
                        profileImage.setImageBitmap(newBitmap);
                        btnImageUpload.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        Uri cropUri =  data.getData();
                        InputStream image_stream = getContentResolver().openInputStream(cropUri);
                        newBitmap= BitmapFactory.decodeStream(image_stream);
                        profileImage.setImageBitmap(newBitmap);
                        btnImageUpload.setVisibility(View.VISIBLE);
                    }
                    strImage = ImageServer.getStringFromBitmap(newBitmap);
                    profileImage.invalidate();
                }
            }
        } catch (Exception ex)
        {
            int a=1;
        }
    }

   public void Image_Update(View v)
    {
        btnImageUpload.setVisibility(View.INVISIBLE);

        prgBar.setVisibility(View.VISIBLE);
        String url = APP_CONST.APP_SERVER_URL+ "/api/Image";
        String reqBody = "{\"MobileNumber\":\""+ MobileNumber + "\",\"userName\":\""+ profileName + "\",\"Email\":\""+ email + "\",\"Location\":\""+ profileLocation + "\",\"ImageString\":\""+ strImage + "\"}";
        try {
            JSONObject jsRequest = new JSONObject(reqBody);
            //-------------------------------------------------------------------------------------------------
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

            JsonObjectRequest jsArrayRequest = new JsonObjectRequest(Request.Method.POST, url,jsRequest, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jObj) {
                    try {
                        String Response = jObj.getString("Response");

                        if(Response.matches("OK"))
                        {
                            ImageServer.SaveBitmapImage(newBitmap,MobileNumber,getApplicationContext());

                        }
                        else if(Response.matches("Fail"))
                        {
                            btnImageUpload.setVisibility(View.VISIBLE);
                            Toast.makeText(getApplicationContext(),"Failed to Upload Image", Toast.LENGTH_LONG).show();

                        }
                    }
                    catch (JSONException jex)
                    {}

                    prgBar.setVisibility(View.GONE);


                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    String message = error.toString();
                    btnImageUpload.setVisibility(View.VISIBLE);
                    prgBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Image Upload failed : Try Later", Toast.LENGTH_LONG).show();

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

            RetryPolicy rPolicy = new DefaultRetryPolicy(0,-1,0);
            jsArrayRequest.setRetryPolicy(rPolicy);
            queue.add(jsArrayRequest);

            //*******************************************************************************************************
        }
        catch (JSONException js)
        {
            btnImageUpload.setVisibility(View.VISIBLE);
            prgBar.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(), "Image Upload failed : Try Later", Toast.LENGTH_LONG).show();
        }

        finally {

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
            CropIntent.putExtra("scaleUpIfNeeded", true);
            CropIntent.putExtra("return-data", true);

            startActivityForResult(CropIntent, REQUEST_IMAGE_CROP);

        } catch (Exception e) {

        }
    }

    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_GET && resultCode == RESULT_OK) {
            Uri fullPhotoUri;
            try {
                //Bitmap thumbnail = data.getData("data");
                fullPhotoUri = data.getData();
                InputStream image_stream = getContentResolver().openInputStream(fullPhotoUri);
                Bitmap bitmap= BitmapFactory.decodeStream(image_stream);
                newBitmap = Bitmap.createScaledBitmap(bitmap,200,200,false);
                profileImage.setImageBitmap(newBitmap);
                strImage = ImageServer.getStringFromBitmap(newBitmap);
                profileImage.invalidate();

            }
            catch (Exception ex)
            {

            }
        }
    }
    */

}
