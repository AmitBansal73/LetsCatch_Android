package net.anvisys.letscatch.Application;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;

import net.anvisys.letscatch.R;

public class HelpActivity extends AppCompatActivity {

    Toolbar myToolbar;
    TextView helpText;
    WebView HelpFileView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
     //   myToolbar.setNavigationIcon(R.drawable.user_icon);
        setSupportActionBar(myToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Help");
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.show();
      //  HelpFileView = (WebView)findViewById(R.id.webViewHelp);
      helpText = (TextView)findViewById(R.id.txthelp);

      /*  String htmlMessage = "<b> Meet Us </b> <br> merge the power of Google Map with Organiser" +
                            " <br> Trach your friedns using Mobile Number<br> "+
                            "Organise meeting with location<br>"+
                             "Track a Group";

        Spanned result = Html.fromHtml(htmlMessage);
        helpText.setText(result);
        */

        try {
            Spanned result = Html.fromHtml(GetHTMLText());
            helpText.setText(result);
            } catch (Exception IOEx) {

        }
    }


    private String GetHTMLText()
    {
     String html=  " <h3> Guide to use LetsCatch Application:- </h3><p>"
        +"    This Application is designed to track a friend using their mobile location. Your Contact can be tracked only when " +
             "agrees to share his/her mobile location. </br> The Application can also be used for SOS call in case of Emergency.</p></br></br>"

       +"  <h4> Instructions to use Application:-</h4> <ol><li>"


             +"<li> <h4>Find & Locate a Friend:- </h4> When it is difficult to locate a person by Address, Mobile Tracking help to meet with your friends."
             +"<h5> Go To Contact Tab --> Select a Contact you want track. </h5></br>"
             +" As your contact accept your invitation. Both of you can see each other location and route"
             +"</br> In Active Meeting Tab you can see details of ongoing track.</br></br>"
             +"</br> You can also tracking multiple contacts by adding them one by one.</br></br>"

             +" <h4>Panic Alarm:-</h4> In case of any Emergency situation your location is sent to Configured contacts with a predefined message."
             +"<h5> Go To Setting--> Activate Panic Button --> Select two Contacts </h5>"

             ;

     return html;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == android.R.id.home)
        {
            HelpActivity.this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
