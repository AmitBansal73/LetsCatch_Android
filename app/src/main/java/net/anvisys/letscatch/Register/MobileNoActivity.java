package net.anvisys.letscatch.Register;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import net.anvisys.letscatch.Object.APP_CONST;
import net.anvisys.letscatch.Object.Country;
import net.anvisys.letscatch.R;

import org.json.JSONException;
import org.json.JSONObject;

public class MobileNoActivity extends AppCompatActivity {

    AutoCompleteTextView MobileNo;
    Button btnNext;
    Toolbar myToolbar;
    ProgressBar prgBar;

    TextView ccSpinner;
    ImageView ccImage;
    ListView CountryListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_no);

        myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(" LetsCatch ");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.show();
        prgBar=(ProgressBar)findViewById(R.id.progressBar);
        prgBar.setVisibility(View.GONE);
        ccSpinner = (TextView)findViewById(R.id.ccSpinner);
        ccImage = (ImageView)findViewById(R.id.ccImage);

        MobileNo = (AutoCompleteTextView)findViewById(R.id.mobile);
        btnNext = (Button)findViewById(R.id.btnAction);


        ccImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (CountryListView.getVisibility() == View.GONE) {
                    CountryListView.setVisibility(View.VISIBLE);
                } else {
                    CountryListView.setVisibility(View.GONE);
                }
            }
        });

        CountryListView = (ListView)findViewById(R.id.countryList);

        CountryListAdapter cAdapter = new CountryListAdapter(this);
        cAdapter.notifyDataSetChanged();
        CountryListView.setAdapter(cAdapter);
       Country count=  (Country)CountryListView.getItemAtPosition(0);
        ccSpinner.setText(count.Code);

        ccSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (CountryListView.getVisibility() == View.GONE) {
                    CountryListView.setVisibility(View.VISIBLE);
                } else {
                    CountryListView.setVisibility(View.GONE);
                }
            }
        });

        CountryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    Country selectedCountry = (Country) CountryListView.getItemAtPosition(i);
                    ccSpinner.setText(selectedCountry.Code);
                    CountryListView.setVisibility(View.GONE);
                }
                catch (Exception ex)
                {

                }
            }
        });
    }

    public void btnClick(View view)
    {
        String CCCode = ccSpinner.getText().toString();
        String Mobile = CCCode + MobileNo.getText().toString();

        Mobile=Mobile.replaceAll("\\s+","");

        String regexStr = "^[+]?[0-9]{8,15}$";
        if (Mobile.matches(regexStr)) {

            if (Mobile.startsWith("+")) {
                Mobile = Mobile.substring(1, Mobile.length());
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Enter only numbers",Toast.LENGTH_LONG).show();
        }

        GetUser(Mobile);
    }

    private void GetUser(final String MobileNumber)
    {
        prgBar.setVisibility(View.VISIBLE);
        btnNext.setEnabled(false);
        String url = APP_CONST.APP_SERVER_URL+ "/api/cuuser/" + MobileNumber;
        //  String reqBody = "{\"MobileNumber\":\""+ MobileNumber + "\"}";
        try {
            //  JSONObject jsRequest = new JSONObject(reqBody);
            //-------------------------------------------------------------------------------------------------
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

            JsonObjectRequest jsArrayRequest = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jObj) {
                    try {

                        // JSONObject jO = jArray.getJSONObject(0);
                        btnNext.setEnabled(true);
                        prgBar.setVisibility(View.GONE);
                        String name = jObj.getString("Name");
                        if(jObj.getString("Name") == "null")
                        {
                            Intent loginIntent = new Intent(MobileNoActivity.this, RegisterActivity.class);
                            loginIntent.putExtra("Mobile","+"+MobileNumber);
                            startActivity(loginIntent);
                        }
                        else {
                            Intent loginIntent = new Intent(MobileNoActivity.this, LoginActivity.class);
                            loginIntent.putExtra("Mobile",jObj.getString("MobileNumber"));
                            loginIntent.putExtra("Name",jObj.getString("Name"));
                            loginIntent.putExtra("Image",jObj.getString("Image"));
                            loginIntent.putExtra("Password",jObj.getString("Password"));
                            loginIntent.putExtra("GCMCode",jObj.getString("GCMCode"));
                            loginIntent.putExtra("Location",jObj.getString("Location"));
                            loginIntent.putExtra("Email",jObj.getString("Email"));
                            startActivity(loginIntent);
                        }

                    }
                    catch (JSONException jex)
                    {
                        prgBar.setVisibility(View.GONE);
                        btnNext.setEnabled(true);
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    String message = error.toString();
                    prgBar.setVisibility(View.GONE);
                    btnNext.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "Network Error : Try later",Toast.LENGTH_LONG).show();

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
            prgBar.setVisibility(View.GONE);
            btnNext.setEnabled(true);
        }

    }
}
