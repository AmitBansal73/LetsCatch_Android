package net.anvisys.letscatch.Application;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import net.anvisys.letscatch.Common.DataAccess;
import net.anvisys.letscatch.Common.UTILITY;
import net.anvisys.letscatch.Object.Log;
import net.anvisys.letscatch.R;

import java.util.ArrayList;
import java.util.List;

public class LogActivity extends AppCompatActivity {

    List<Log> logList = new ArrayList<Log>();

    ListView logListView;
    LogAdapter logAdapter;
    private Toolbar myToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        //region Initiate View
        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);

        setSupportActionBar(myToolbar);
        ActionBar actionBar = getSupportActionBar();
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Your Log");
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.show();


        logListView = (ListView)findViewById(R.id.logListView);
        logAdapter = new LogAdapter(this);
        DataAccess da = new DataAccess(this);
        da.open();
        logList = da.getAllLog();
        da.close();
        logAdapter.notifyDataSetChanged();
        logListView.setAdapter(logAdapter);
        registerForContextMenu(logListView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       // getMenuInflater().inflate(R.menu.menu_log, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
            if(id== android.R.id.home)
            {
            LogActivity.this.finish();
            }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_context_log, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int id =  item.getItemId();

        Log log = (Log)logListView.getItemAtPosition(info.position);
        DataAccess da = new DataAccess(this);
        switch (item.getItemId()) {
            case R.id.action_delete:
                da.open();
                da.deleteLog(log.ID);
                da.InsertLog("Log File Cleared");
                da.close();
              //  editNote(info.id);
                return true;
            case R.id.action_deleteAll:
                da.open();
                da.deleteAllLog();
                da.InsertLog("Log File Cleared");
                da.close();
              //  deleteNote(info.id);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public class LogAdapter extends BaseAdapter
    {

        LayoutInflater inflater;

        public LogAdapter(Activity activity) {
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return logList.size();
        }

        @Override
        public Log getItem(int position) {
            return logList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if (view == null) {
                view = inflater.inflate(R.layout.loglist_row_item, null);
            }

            TextView txtDateTime = (TextView) view.findViewById(R.id.logDate);
            TextView txtMessage = (TextView) view.findViewById(R.id.logMessage);


            Log log = logList.get(position);
            txtDateTime.setText(UTILITY.ChangeFormat(log.DateTime));
            txtMessage.setText(log.text);

            return view;
        }
    }

}
