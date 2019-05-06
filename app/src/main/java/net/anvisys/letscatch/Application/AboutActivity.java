package net.anvisys.letscatch.Application;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import net.anvisys.letscatch.R;

public class AboutActivity extends AppCompatActivity {

    Toolbar myToolbar;
    TextView txtVersion;
    TextView txtAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
      //  myToolbar.setNavigationIcon(R.drawable.user_icon);
        setSupportActionBar(myToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("About LetsCatch");
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.show();


        txtVersion = (TextView)findViewById(R.id.txtVersion);
        txtAbout = (TextView)findViewById(R.id.txtAbout);
        txtVersion.setText("Lets Catch version 1.2");
        txtAbout.setText("Developed By Anvisys Technologies Pvt. Ltd.");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
        {
            AboutActivity.this.finish();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
