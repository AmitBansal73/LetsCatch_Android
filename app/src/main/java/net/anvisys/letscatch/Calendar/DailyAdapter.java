package net.anvisys.letscatch.Calendar;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.anvisys.letscatch.Object.MEETING_STATUS;
import net.anvisys.letscatch.Object.Schedule;
import net.anvisys.letscatch.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Amit Bansal on 09-01-2017.
 */
public class DailyAdapter extends BaseAdapter {
    private Context mContext;
    LayoutInflater inflater;
    HashMap<Integer,Schedule> hm = new HashMap<>();
    public List<Schedule> meetingList = new ArrayList<Schedule>();

    public DailyAdapter(Context mContext, List<Schedule> list) {
        try {
            this.mContext = mContext;
            inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            meetingList = list;
            for (Schedule meet:meetingList
                    ) {
                String[] arr =  meet.GET_SCHEDULE_TIME().split(":");
                int key =  Integer.parseInt(arr[0]);
                hm.put(key,meet);
            }
        }
        catch (Exception ex)
        {

        }

    }

    @Override
    public int getCount() {
        return 24;
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
        TextView timeHeader = (TextView) view.findViewById(R.id.timeHeading);
        TextView content = (TextView) view.findViewById(R.id.content);
        timeHeader.setText(Integer.toString(i + 1) + ":00");
        try {
            if (hm.size() > 0) {
                Schedule tempMeeting = hm.get(i+1);

                content.setText(tempMeeting.GET_MEETING_NAME());

                if (tempMeeting.MEETING_STATUS == MEETING_STATUS.SCHEDULED_FUTURE)
                {
                    content.setTextColor(Color.argb(250, 37, 50, 117));
                }
                else if(tempMeeting.MEETING_STATUS == MEETING_STATUS.SCHEDULED_IGNORE)
                {
                    content.setTextColor(Color.RED);
                }
                else if(tempMeeting.MEETING_STATUS == MEETING_STATUS.SCHEDULED_DELAYED)
                {
                    content.setTextColor(Color.RED);
                }
                else if(tempMeeting.MEETING_STATUS == MEETING_STATUS.SCHEDULED_LEFT)
                {
                    content.setTextColor(Color.RED);
                }
                else if(tempMeeting.MEETING_STATUS == MEETING_STATUS.SCHEDULED_COMPLETED)
                {
                    content.setTextColor(Color.argb(200, 84, 133, 89));
                }
            }
        }
        catch (Exception ex)
        {
            content.setText("");
        }

        return view;
    }
}
