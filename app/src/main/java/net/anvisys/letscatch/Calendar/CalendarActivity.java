package net.anvisys.letscatch.Calendar;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import net.anvisys.letscatch.Common.UTILITY;
import net.anvisys.letscatch.R;
import net.anvisys.letscatch.ScheduleActivity;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CalendarActivity extends AppCompatActivity implements MonthFragment.OnDateSelectedListener{

    Spinner viewType;
    String selectedViewType;
    String strCurrDate;
    private Toolbar myToolbar;

    private ViewPager viewPager;
    private FragmentStatePagerAdapter mAdapter;

    int diffFromCurrentDate =0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        viewType = (Spinner)findViewById(R.id.viewType);
        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.show();


        viewPager = (ViewPager)findViewById(R.id.viewPager);
        ArrayAdapter<CharSequence> adapterCycle = ArrayAdapter.createFromResource(this,
                R.array.viewType, android.R.layout.simple_spinner_item);
        // adapterCycle the layout to use when the list of choices appears
        adapterCycle.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        viewType.setAdapter(adapterCycle);
        viewType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedViewType = viewType.getSelectedItem().toString();
                if (selectedViewType.matches("Daily")) {
                    mAdapter = new DayPagerAdapter(getSupportFragmentManager());
                    viewPager.setAdapter(mAdapter);
                    viewPager.setCurrentItem(50-diffFromCurrentDate, true);
                }

                if (selectedViewType.matches("Monthly")) {
                    mAdapter = new MonthPagerAdapter(getSupportFragmentManager());
                    viewPager.setAdapter(mAdapter);
                    viewPager.setCurrentItem(50, true);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

       // GCMListenerService.setGCMNotificationListener(this);
        //  viewType.setSelection(0, true);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_calendar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_schedule) {

            Intent scheduleIntent = new Intent(CalendarActivity.this, ScheduleActivity.class);
            startActivity(scheduleIntent);

        }
        return true;
    }

    @Override
    public void onDateSelected(String date) {
        try {
            strCurrDate = date;
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            Date selectedDate = formatter.parse(date);

            Calendar cal = Calendar.getInstance();
            Date CurrDate = cal.getTime();
            diffFromCurrentDate = UTILITY.DayDiff(CurrDate, selectedDate);
            viewType.setSelection(0,false);
           /*
            mAdapter = new DayPagerAdapter(getSupportFragmentManager());
            viewPager.setAdapter(mAdapter);
            viewPager.setCurrentItem(50 - diff, true);
            */


        }
        catch (ParseException pEx)
        {

        }
        catch (Exception ex)
        {

        }


    }

    public void ButtonClick(View v)
    {


    }



    public  class MonthPagerAdapter extends FragmentStatePagerAdapter {

        public MonthPagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            MonthFragment fragment = new MonthFragment();
            Bundle args = new Bundle();
            args.putInt("ARG_PAGE", position);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void finishUpdate(ViewGroup container) {
            super.finishUpdate(container);
            int a =5;
        }

        @Override
        public void startUpdate(ViewGroup container) {
            super.startUpdate(container);
            int b=10;
        }

        @Override
        public int getItemPosition(Object object) {
            int c=10;
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return 100;
        }
    }

    public  class DayPagerAdapter extends FragmentStatePagerAdapter {

        public DayPagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            DailyFragment fragment = new DailyFragment();
            Bundle args = new Bundle();
            args.putInt("ARG_PAGE", position);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void finishUpdate(ViewGroup container) {
            super.finishUpdate(container);
            int a =5;
        }

        @Override
        public void startUpdate(ViewGroup container) {
            super.startUpdate(container);
            int b=10;
        }

        @Override
        public int getItemPosition(Object object) {
            int c=10;
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return 100;
        }
    }
}
