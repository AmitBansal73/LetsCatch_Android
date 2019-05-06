package net.anvisys.letscatch.Calendar;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.anvisys.letscatch.Object.Schedule;
import net.anvisys.letscatch.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Amit Bansal on 09-01-2017.
 */
public class MonthAdapter extends BaseAdapter {

    Context mContext;
    LayoutInflater inflater;
    int month;
    int Year;
    int max;
    int day;
    List<Schedule> meetingList = new ArrayList<Schedule>();
    HashMap<Integer,String> hm = new HashMap<>();

    public MonthAdapter(Context mContext, int month, int year,List<Schedule> list ) {
        this.mContext = mContext;
        this.month = month;
        this.Year = year;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR,year);
        cal.set(Calendar.MONTH,month);
        max = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int min =  cal.getActualMinimum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DATE,1);
        day =  cal.get(Calendar.DAY_OF_WEEK);
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        meetingList = list;
        try {
        for (Schedule meet:meetingList
                ) {
            String[] arr =  meet.GET_SCHEDULE_DATE().split("/");
            int day =  Integer.parseInt(arr[0]);

            String msg = "";


                if (hm.get(day)!=null)
                {
                    msg = hm.get(day);
                    msg = msg + "-" + meet.GET_YOUR_NAME();
                    hm.put(day,msg);
                }
                else
                {
                    msg =  meet.GET_YOUR_NAME();
                    hm.put(day,msg);
                }
            }

        }
        catch (Exception ex)
        {

        }
    }

    @Override
    public int getCount() {
        return 49;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = inflater.inflate(R.layout.calendar_grid_item, null);
        }

        TextView txtDate = (TextView)view.findViewById(R.id.timeHeading);
        TextView txtDay = (TextView)view.findViewById(R.id.content);
        if (i>=0 && i<=6)
        {
            String weekday;
            switch (i)
            {
                case 0:
                    weekday = "Su";
                    break;
                case 1:
                    weekday = "Mo";
                    break;
                case 2:
                    weekday = "Tu";
                    break;
                case 3:
                    weekday = "We";
                    break;
                case 4:
                    weekday = "Th";
                    break;
                case 5:
                    weekday = "Fr";
                    break;
                case 6:
                    weekday = "Sa";
                    break;
                default:
                    weekday = "";
            }
            txtDate.setText(weekday);
            txtDay.setVisibility(View.GONE);
            view.setBackgroundColor(Color.LTGRAY);
        }
        else if ((i-6) >= day && (i-6-day) < max)
        {

            int date = i-6-day+1;

            txtDate.setText(Integer.toString(date));

            String meetText = "";

            if(hm.size() >0)
            {
                if(hm.get(date)!= null) {
                    Spanned result = Html.fromHtml(hm.get(date));
                    txtDay.setText(result);
                }
                else
                {
                    txtDay.setText("");
                }

            }

        }
        else
        {
            txtDate.setText("");
            txtDate.setTextColor(Color.GRAY);
        }


        return view;
    }
}
