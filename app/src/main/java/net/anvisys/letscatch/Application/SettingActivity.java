package net.anvisys.letscatch.Application;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import net.anvisys.letscatch.Object.APP_SETTINGS;
import net.anvisys.letscatch.ContactActivity;
import net.anvisys.letscatch.Object.Contact;
import net.anvisys.letscatch.Common.Session;
import net.anvisys.letscatch.R;

public class SettingActivity extends AppCompatActivity {

    Switch switchAlarm;
    Switch panicAlarm;
    Switch enableRouting;
    Switch enableLocationSound;
    Boolean isAlarm;
    Boolean isPanicAlarm;
    int PRIM_CONTACT_PICKER_REQUEST =1;
    int SEC_CONTACT_PICKER_REQUEST =2;
    TextView PrimaryContact;
    TextView SecondaryContact;
    private Toolbar myToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);

        setSupportActionBar(myToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Settings");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.show();

        switchAlarm = (Switch)findViewById(R.id.switchAlarm);
        panicAlarm = (Switch)findViewById(R.id.panicAlarm);
        PrimaryContact = (TextView)findViewById(R.id.Contact1);
        SecondaryContact = (TextView)findViewById(R.id.Contact2);
        enableRouting = (Switch)findViewById(R.id.Routing);
        enableLocationSound = (Switch)findViewById(R.id.LocationSound);



        if (APP_SETTINGS.MEETING_ALARM == true)
        {
            switchAlarm.setChecked(true);
        }
        else
        {
            switchAlarm.setChecked(false);
        }

        if (APP_SETTINGS.PANIC_BUTTON == true)
        {
            panicAlarm.setChecked(true);
            PrimaryContact.setText(APP_SETTINGS.PRIMARY_CONTACT);
            if(!APP_SETTINGS.PRIMARY_CONTACT.matches(""))
            {
                PrimaryContact.setText(APP_SETTINGS.PRIMARY_CONTACT);
            }
            if(!APP_SETTINGS.SECONDARY_CONTACT.matches(""))
            {
                SecondaryContact.setText(APP_SETTINGS.SECONDARY_CONTACT);
            }
            PrimaryContact.setOnClickListener(new  Button_Click());
            SecondaryContact.setOnClickListener(new Button_Click());
        }
        else
        {
            panicAlarm.setChecked(false);
        }

        enableLocationSound.setChecked(APP_SETTINGS.LOCATION_NOTIFICATION_SOUND);

        enableLocationSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                APP_SETTINGS.LOCATION_NOTIFICATION_SOUND = isChecked;
            }
        });



        enableRouting.setChecked(APP_SETTINGS.ENABLE_ROUTING);

        enableRouting.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                APP_SETTINGS.ENABLE_ROUTING = isChecked;

            }
        });

        switchAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
               @Override
               public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                   APP_SETTINGS.MEETING_ALARM = isChecked;
               }
           }
        );



        panicAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    APP_SETTINGS.PANIC_BUTTON = true;
                    PrimaryContact.setTextColor(Color.BLACK);
                    PrimaryContact.setOnClickListener(new  Button_Click());
                } else {
                    APP_SETTINGS.PANIC_BUTTON = false;
                    PrimaryContact.setEnabled(false);
                }

            }
        });
    }

    private class Button_Click implements View.OnClickListener
    {

        @Override
        public void onClick(View view) {

            if(view == PrimaryContact)
            {
                Intent contactIntent = new Intent(getApplicationContext(), ContactActivity.class);
                contactIntent.putExtra("PARENT_ACTIVITY", "MainActivty");
                startActivityForResult(contactIntent, PRIM_CONTACT_PICKER_REQUEST);
            }

            if(view == SecondaryContact)
            {
                if(APP_SETTINGS.PRIMARY_CONTACT.matches(""))
                {
                    Toast.makeText(SettingActivity.this, "First Select the primary contact",Toast.LENGTH_LONG).show();
                    return;
                }
                Intent contactIntent = new Intent(getApplicationContext(), ContactActivity.class);
                contactIntent.putExtra("PARENT_ACTIVITY", "MainActivty");
                startActivityForResult(contactIntent, SEC_CONTACT_PICKER_REQUEST);
            }

        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {

                if (resultCode == RESULT_OK) {

                    Contact tcontact = new Contact();

                        if (requestCode == PRIM_CONTACT_PICKER_REQUEST)
                        {
                        APP_SETTINGS.PRIMARY_CONTACT = tcontact.userName;
                            if(APP_SETTINGS.PRIMARY_CONTACT.matches(APP_SETTINGS.SECONDARY_CONTACT))
                            {
                                Toast.makeText(SettingActivity.this, "Select Diff Contact or only one contact", Toast.LENGTH_LONG).show();
                                return;
                            }
                        PrimaryContact.setText(tcontact.userName);
                        PrimaryContact.setTextColor(Color.BLACK);
                        SecondaryContact.setOnClickListener(new Button_Click());
                        }
                        else if (requestCode == SEC_CONTACT_PICKER_REQUEST)
                        {
                            APP_SETTINGS.SECONDARY_CONTACT=tcontact.userName;
                            if(APP_SETTINGS.PRIMARY_CONTACT.matches(APP_SETTINGS.SECONDARY_CONTACT))
                            {
                                Toast.makeText(SettingActivity.this, "Select Diff Contact or only one contact", Toast.LENGTH_LONG).show();
                                return;
                            }
                          SecondaryContact.setText(tcontact.userName);
                        }

                    return;
                }

        }
        catch (Exception ex)
        {
            Toast.makeText(this,"Error in OnActivityResult", Toast.LENGTH_LONG).show();
        }
    }

    public void SaveSettings(View v)
    {
        try {


            Session.SaveSetting(getApplicationContext());

            SettingActivity.this.finish();
        }
        catch (Exception ex)
        {

        }

    }

/*    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
            if(id==android.R.id.home)
            {
                SettingActivity.this.finish();
            }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }   */

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Session.SetSetting(getApplicationContext());
    }
}
