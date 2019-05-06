package net.anvisys.letscatch;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import net.anvisys.letscatch.Common.DataAccess;
import net.anvisys.letscatch.Common.UTILITY;
import net.anvisys.letscatch.Object.APP_CONST;
import net.anvisys.letscatch.Object.Contact;
import net.anvisys.letscatch.Object.MEETING_STATUS;
import net.anvisys.letscatch.Object.Schedule;
import net.anvisys.letscatch.Services.FetchAddressService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class ScheduleActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks  {



    Toolbar myToolbar;
    EditText txtTopic;
    TextView txtContact, txtAddress,txtLocation, txtSchedule,txtDate,StartDate, EndDate;
    ImageButton btnContact,btnLocation, btnSchedule, btnDate;

    Button btnDone;

    int CONTACT_PICKER_REQUEST = 1;
    int PLACE_PICKER_REQUEST = 2;
    int SCHEDULE_REQUEST_CODE = 3;
    int RESULT_OK = -1;

    String strTargetAddress = "", ScheduleDate ="",ScheduleTime= "", strTargetName="", strMeetingName="";
    int ScheduleType = APP_CONST.MEETING_TYPE_SCHEDULE;
    LatLng targetLatLong;
    int selectedDay;

    String strSelectedName ="";
    String strSelectedMobile ="TARGET";
    String strTargetLatLong="";

    String selectedContID="";
    byte[] contImageByte;
    private android.app.DatePickerDialog DatePickerDialog;
    private SimpleDateFormat dateFormatter;

    public static final String PACKAGE_NAME = "net.anvisys.meetus";

    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = "LOCATION_DATA_EXTRA";
    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;

    protected Location mLastLocation;
    private AddressResultReceiver mResultReceiver;

    private GoogleApiClient mGoogleAPIClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.show();

        txtTopic = (EditText)findViewById(R.id.txtTopic);
        txtContact = (TextView)findViewById(R.id.editContact);
        txtAddress = (TextView)findViewById(R.id.txtAddress);
        txtSchedule = (TextView)findViewById(R.id.txtSchedule);
        txtDate = (TextView)findViewById(R.id.txtDate);

        btnContact = (ImageButton)findViewById(R.id.btnContact);
        btnLocation = (ImageButton)findViewById(R.id.btnLocation);
        btnSchedule = (ImageButton)findViewById(R.id.btnSchedule);
        btnDate = (ImageButton)findViewById(R.id.btnDate);

        btnDone = (Button)findViewById(R.id.btnDone);
        dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.US);


        ScheduleTime = Integer.toString(UTILITY.CurrentHour() + 2)+":00";
        txtSchedule.setText(ScheduleTime);

        ScheduleDate = UTILITY.CurrentLocalDate();
        txtDate.setText(ScheduleDate);

        strTargetAddress = "Select Location";

        txtAddress.setText(strTargetAddress);



        Intent intent=  getIntent();
        mLastLocation = intent.getParcelableExtra("LOCATION_DATA_EXTRA");
        if(mLastLocation != null)
        {
            targetLatLong = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()) ;
            startIntentService();
        }
    }


    protected void startIntentService() {

        mGoogleAPIClient = new GoogleApiClient
                .Builder(getApplicationContext())
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleAPIClient.connect();

    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mResultReceiver = new AddressResultReceiver(new Handler());
        Intent intent = new Intent(this, FetchAddressService.class);
        intent.putExtra("RECEIVER", mResultReceiver);
        intent.putExtra("LOCATION", mLastLocation);
        startService(intent);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }



    public void ButtonClick( View view)
    {
        if (view == btnContact)
        {
            Intent contactIntent = new Intent(this,ContactActivity.class);
            contactIntent.putExtra("PARENT_ACTIVITY","ScheduleActivity");
            startActivityForResult(contactIntent, CONTACT_PICKER_REQUEST);
        }

        if (view == btnLocation)
        {
            try {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                Activity act = ScheduleActivity.this;
                act.setTheme(R.style.AppTheme);
                startActivityForResult(builder.build(act), PLACE_PICKER_REQUEST);
            }
            catch (GooglePlayServicesRepairableException ex)
            {

            }
            catch (GooglePlayServicesNotAvailableException ex)
            {

            }
        }

        if (view == btnSchedule)
        {
            PickTime();
            // Intent daytimeIntent = new Intent(PlanActivity.this,DateTimeActivity.class);
            // startActivityForResult(daytimeIntent, SCHEDULE_REQUEST_CODE);
        }
        if (view == btnDate)
        {
            DatePicker();
            // Intent daytimeIntent = new Intent(PlanActivity.this,DateTimeActivity.class);
            // startActivityForResult(daytimeIntent, SCHEDULE_REQUEST_CODE);
        }
        if (view == btnDone)
        {
           SaveMeeting();
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CONTACT_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                String text="";
                ArrayList<Contact>   selectedList = data.getParcelableArrayListExtra("CONTACT");

                if (selectedList.size()>0)
                {
                    for (Contact cont: selectedList
                            ) {

                        strSelectedName = strSelectedName +"<p>"+ cont.userName +"</p></br>";
                        selectedContID = selectedContID + cont.ID+ ",";
                    }
                }
                text =   strSelectedName.substring(0,strSelectedName.length()-6);
                selectedContID = selectedContID.substring(0,selectedContID.length()-1);
                Spanned result = Html.fromHtml(text);
                txtContact.setText(result);

                return;
            }
        }

        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                strTargetName = place.getName().toString();
                strTargetAddress = place.getAddress().toString();
                targetLatLong = place.getLatLng();
                if((strTargetAddress.isEmpty() || strTargetAddress =="")&&(targetLatLong != null))
                {
                    strTargetLatLong = Double.toString(targetLatLong.latitude).substring(0,9) + "," +  Double.toString(targetLatLong.longitude).substring(0,9);
                    strTargetAddress = strTargetLatLong;
                    txtAddress.setText(strTargetLatLong);
                }
                else {
                    txtAddress.setText(strTargetName + ":" +strTargetAddress);
                }

                return;
            }
        }

        if (requestCode == SCHEDULE_REQUEST_CODE) {
            if (resultCode==3) {
                ScheduleType = APP_CONST.MEETING_TYPE_SCHEDULE;
                ScheduleDate = data.getStringExtra(APP_CONST.SCHEDULE_DATE).trim();
                ScheduleTime = data.getStringExtra(APP_CONST.SCHEDULE_TIME).trim();
                txtSchedule.setText(ScheduleDate + "  at  " + ScheduleTime);
                return;
            }
        }
    }


    private void SaveMeeting() {
        try {
            if (strSelectedName == null || strSelectedName.isEmpty() || strSelectedName == "") {
                Toast.makeText(this, "Select Contact", Toast.LENGTH_LONG).show();
                return;
            }

            if (strTargetAddress == "" && strTargetLatLong == "") {
                Toast.makeText(this, "Select Location", Toast.LENGTH_LONG).show();
                return;
            }

            if (ScheduleDate == "" || ScheduleTime == "") {
                Toast.makeText(this, "Select Time", Toast.LENGTH_LONG).show();
                return;
            }

            strMeetingName = txtTopic.getText().toString();
            if (strMeetingName == ""|| strMeetingName.matches("")) {
                Toast.makeText(this, "Give Name", Toast.LENGTH_LONG).show();
                return;
            }

            if (strSelectedName.matches("Self Reminder")) {
                SharedPreferences prefs = this.getSharedPreferences("MeetUsSession", Context.MODE_PRIVATE);
                String img = prefs.getString("ImageString", "");
              //  contImageByte = ImageSaver.getByteFromString(img);
            }

            Schedule newMeet = new Schedule(strMeetingName, strSelectedMobile, selectedContID, MEETING_STATUS.SCHEDULED_FUTURE, ScheduleType, ScheduleDate, ScheduleTime);

            newMeet.TARGET_NAME = selectedContID;
            //newMeet.TARGET_ADDRESS = strTargetName + ":" + strTargetAddress;
            newMeet.TARGET_ADDRESS = strTargetAddress;
            newMeet.MEETING_STATUS = MEETING_STATUS.SCHEDULED_FUTURE;
            newMeet.TARGET_LATLONG = Double.toString(targetLatLong.latitude).substring(0, 9) + "," + Double.toString(targetLatLong.longitude).substring(0, 9);

            DataAccess da = new DataAccess(this);
            da.open();
            da.insertNewSchedule(newMeet);
            da.close();
            Intent mainIntent = new Intent(this, MainActivity.class);
            mainIntent.putExtra(APP_CONST.SCHEDULE_TYPE, "SCHEDULE");
            startActivity(mainIntent);
        }
        catch (Exception ex)
        {
            Toast.makeText(this,"Error in adding Meeting", Toast.LENGTH_LONG).show();
        }
    }

    private void DatePicker()
    {
        Calendar newCalendar = Calendar.getInstance();

        DatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                txtDate.setText(dateFormatter.format(newDate.getTime()));
                ScheduleDate = dateFormatter.format(newDate.getTime());
                selectedDay = dayOfMonth;

            }
        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        DatePickerDialog.show();

    }

    private void PickTime()
    {
        try {
            int nextHour = UTILITY.CurrentHour()+2;
            TimePickerDialog dialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int i, int i1) {
                    txtSchedule.setText(Integer.toString(i) + ":" + Integer.toString(i1));
                    ScheduleTime = Integer.toString(i) + ":" + Integer.toString(i1);
                }
            },nextHour, 0, true);
            dialog.show();
        }
        catch (Exception ex)
        {

        }
    }




    @SuppressLint("ParcelCreator")
    class AddressResultReceiver extends ResultReceiver
    {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }


        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {


            if (resultCode == SUCCESS_RESULT) {
                strTargetAddress = resultData.getString(RESULT_DATA_KEY);

                txtAddress.setText(strTargetAddress);
            }

            else if(resultCode == FAILURE_RESULT)
            {
                txtAddress.setText("");
            }

        }
    }
}
