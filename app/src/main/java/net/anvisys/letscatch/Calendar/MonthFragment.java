package net.anvisys.letscatch.Calendar;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import net.anvisys.letscatch.Common.DataAccess;
import net.anvisys.letscatch.Object.Schedule;
import net.anvisys.letscatch.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class MonthFragment extends Fragment {

    ImageButton previousMonth;
    ImageButton nextMonth;
    TextView txtMonth;
    DateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
    String selectedViewType;
    GridView gridview;
    MonthAdapter myAdapter;

    String strCurrMonth;
    int intCurrMonth;
    int intCurrYear;
    OnDateSelectedListener mListener;
    List<Schedule> meetingList = new ArrayList<Schedule>();
    int Position;

    public MonthFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_month, container, false);

        previousMonth = (ImageButton)view.findViewById(R.id.previousMonth);
        nextMonth = (ImageButton) view.findViewById(R.id.nextMonth);
        txtMonth = (TextView) view.findViewById(R.id.txtMonth);
        Bundle bundle =  getArguments();
        Position =   bundle.getInt("ARG_PAGE");
        Calendar cal = Calendar.getInstance(Locale.US);
        cal.add(Calendar.MONTH,Position-50);
        strCurrMonth =  cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US);
        intCurrMonth = cal.get(Calendar.MONTH);
        intCurrYear =  cal.get(Calendar.YEAR);

        meetingList = GetMeetingForMonth(intCurrMonth+1,intCurrYear);

        txtMonth.setText(strCurrMonth +"," + Integer.toString(intCurrYear));
        nextMonth.setOnClickListener(new ChangeMonth());
        previousMonth.setOnClickListener(new ChangeMonth());


        gridview = (GridView) view.findViewById(R.id.gridview);
        myAdapter = new MonthAdapter(getActivity(),intCurrMonth,intCurrYear,meetingList);
        gridview.setAdapter(myAdapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                TextView date = (TextView) v.findViewById(R.id.timeHeading);

                String day = date.getText().toString();
                Toast.makeText(getActivity(), "" + day,
                        Toast.LENGTH_SHORT).show();
                String selectedDate = day + "/" + Integer.toString(intCurrMonth+1)  + "/" + Integer.toString(intCurrYear);
                mListener.onDateSelected(selectedDate);
            }
        });
        mListener = (OnDateSelectedListener) getActivity();
        return view;
    }


    class ChangeMonth implements View.OnClickListener
    {

        @Override
        public void onClick(View view) {
            if (view == previousMonth) {
                try {
                    Calendar cal = Calendar.getInstance();
                    cal.set(intCurrYear,intCurrMonth,1);
                    cal.add(Calendar.MONTH, -1);
                    strCurrMonth =  cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US);
                    intCurrMonth = cal.get(Calendar.MONTH);
                    intCurrYear =  cal.get(Calendar.YEAR);

                    txtMonth.setText(strCurrMonth + "," + Integer.toString(intCurrYear));

                    meetingList = GetMeetingForMonth(intCurrMonth,intCurrYear);
                    myAdapter = new MonthAdapter(getActivity(),intCurrMonth,intCurrYear,meetingList);
                    gridview.setAdapter(myAdapter);
                    myAdapter.notifyDataSetChanged();
                } catch (Exception pex) {

                }

            } else if (view == nextMonth) {
                try {
                    Calendar cal = Calendar.getInstance();
                    cal.set(intCurrYear,intCurrMonth,1);
                    cal.add(Calendar.MONTH, 1);
                    strCurrMonth =  cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US);
                    intCurrMonth = cal.get(Calendar.MONTH);
                    intCurrYear =  cal.get(Calendar.YEAR);

                    txtMonth.setText(strCurrMonth + "," + Integer.toString(intCurrYear));

                    meetingList = GetMeetingForMonth(intCurrMonth,intCurrYear);
                    myAdapter = new MonthAdapter(getActivity(),intCurrMonth,intCurrYear,meetingList);
                    gridview.setAdapter(myAdapter);
                    myAdapter.notifyDataSetChanged();
                }
                catch (Exception pex) {
                    Toast.makeText(getActivity(), "" + pex.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public interface OnDateSelectedListener {
        public void onDateSelected(String date);
    }


    private List<Schedule> GetMeetingForMonth(int month, int Year)
    {
        try {
            List<Schedule> monthMeeting;
            DataAccess da = new DataAccess(getActivity());
            da.open();
            monthMeeting = da.getMeetingForMonth(month, Year);
            return monthMeeting;
        }
        catch (Exception ex)
        {
            Toast.makeText(getActivity(), "Error in GetMonthMeeting", Toast.LENGTH_LONG).show();
            return null;
        }
    }


}
