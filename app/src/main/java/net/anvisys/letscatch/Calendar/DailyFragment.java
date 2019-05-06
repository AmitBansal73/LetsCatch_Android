package net.anvisys.letscatch.Calendar;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import net.anvisys.letscatch.Common.DataAccess;
import net.anvisys.letscatch.Common.UTILITY;
import net.anvisys.letscatch.Object.APP_CONST;
import net.anvisys.letscatch.Object.MEETING_STATUS;
import net.anvisys.letscatch.Object.Schedule;
import net.anvisys.letscatch.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class DailyFragment extends Fragment implements PopupMenu.OnMenuItemClickListener {

    Spinner viewType;
    ImageButton previousDate;
    ImageButton nextDate;
    TextView txtDate;
    DateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
    String selectedViewType;
    public String strCurrDate="";

    List<Schedule> meetingList = new ArrayList<Schedule>();

    DailyAdapter myDailyAdapter;
    GridView gridview;
    HashMap<Integer,Schedule> hm = new HashMap<>();

    Schedule selectedMeeting;


    public DailyFragment() {
        // Required empty public constructor
    }
    int Position;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_daily, container, false);
        try {
            // Inflate the layout for this fragment

            previousDate = (ImageButton) view.findViewById(R.id.previousDate);
            nextDate = (ImageButton) view.findViewById(R.id.nextDate);
            txtDate = (TextView) view.findViewById(R.id.Date);
            if (strCurrDate.isEmpty() || strCurrDate == null || strCurrDate.matches("")) {
                strCurrDate = UTILITY.CurrentLocalDate();
            }
            Bundle bundle = getArguments();
            Position = bundle.getInt("ARG_PAGE");
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, Position - 50); //minus number would decrement the days
            Date date = cal.getTime();
            String newDate = dateFormatter.format(date);

            txtDate.setText(newDate);
            nextDate.setOnClickListener(new ChangeDate());
            previousDate.setOnClickListener(new ChangeDate());
            meetingList = GetMeetingForDate(newDate);

            for (Schedule meet : meetingList
                    ) {
                String[] arr = meet.GET_SCHEDULE_TIME().split(":");
                int key = Integer.parseInt(arr[0]);
                hm.put(key, meet);
            }

            gridview = (GridView) view.findViewById(R.id.gridview);
            gridview.setAdapter(new DailyAdapter(getActivity(), meetingList));

            gridview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    try {
                        if (hm.containsKey(i)) {
                            selectedMeeting = hm.get(i);
                            if (selectedMeeting.GET_SCHEDULE_TYPE() == APP_CONST.MEETING_TYPE_SCHEDULED_SELF && selectedMeeting.MEETING_STATUS != MEETING_STATUS.SCHEDULED_LEFT) {
                                ShowPopUp2(view);
                            } else if (selectedMeeting.GET_SCHEDULE_TYPE() ==APP_CONST.MEETING_TYPE_SCHEDULE && selectedMeeting.MEETING_STATUS != MEETING_STATUS.SCHEDULED_LEFT) {
                                ShowPopUp(view);
                            }
                        }
                        return true;
                    } catch (Exception ex) {
                        return true;
                    }
                }
            });

        }
        catch (Exception ex)
        {

        }
        return view;

    }

    private class ChangeDate implements View.OnClickListener
    {
        @Override
        public void onClick(View view) {
            if (view ==previousDate)
            {
                try {
                    String currentDate = txtDate.getText().toString();
                    Date currDate = dateFormatter.parse(currentDate);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(currDate);
                    cal.add(Calendar.DATE, -1); //minus number would decrement the days
                    Date date = cal.getTime();
                    String newDate = dateFormatter.format(date);
                    txtDate.setText(newDate);
                    meetingList = GetMeetingForDate(newDate);

                    myDailyAdapter = new DailyAdapter(getActivity(),meetingList);
                    gridview.setAdapter(myDailyAdapter);
                    myDailyAdapter.notifyDataSetChanged();


                }
                catch (ParseException pex)
                {

                }

            }
            else if (view ==nextDate)
            {
                try {
                    String currentDate = txtDate.getText().toString();
                    Date currDate = dateFormatter.parse(currentDate);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(currDate);
                    cal.add(Calendar.DATE, 1); //minus number would decrement the days
                    Date date = cal.getTime();
                    String newDate = dateFormatter.format(date);
                    txtDate.setText(newDate);
                    meetingList = GetMeetingForDate(newDate);
                    myDailyAdapter = new DailyAdapter(getActivity(),meetingList);
                    gridview.setAdapter(myDailyAdapter);
                    myDailyAdapter.notifyDataSetChanged();
                }
                catch (ParseException pex)
                {

                }
            }
        }
    }

    private List<Schedule> GetMeetingForDate(String date)
    {
        try {
            List<Schedule> todayMeeting;
            DataAccess da = new DataAccess(getActivity());
            da.open();
            todayMeeting = da.getMeetingForDate(date);
            return todayMeeting;
        }
        catch (Exception ex)
        {
            Toast.makeText(getActivity(), "Error in GetTodayMeeting", Toast.LENGTH_LONG).show();
            return null;
        }
    }

    private void ShowPopUp(View v)
    {
        PopupMenu popup = new PopupMenu(getActivity(), v);
        popup.setOnMenuItemClickListener(this);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_context_schedule, popup.getMenu());
        popup.show();
    }
    private void ShowPopUp2(View v)
    {
        PopupMenu popup = new PopupMenu(getActivity(), v);
        popup.setOnMenuItemClickListener(this);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_context_schedule, popup.getMenu());
        popup.show();
    }
    @Override
    public boolean onMenuItemClick(MenuItem item) {

        return true;
    }

}
