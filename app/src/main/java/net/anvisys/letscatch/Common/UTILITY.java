package net.anvisys.letscatch.Common;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by Amit Bansal on 06-01-2017.
 */
public class UTILITY {

    private static AlarmManager alarmMgr;
    private static PendingIntent alarmIntent;
    static final String DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
    static final String DATE_FORMAT = "dd/MM/yyyy";

    public static String CurrentLocalDate()
    {
        DateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        String currentDate = sdf.format(new Date());
        return currentDate;
    }



    public static String CurrentLocalDateTimeString()
    {
        DateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT);
        sdf.format(new Date());
        String dateTime = sdf.format(new Date());;
        return dateTime;
    }

    public static String CurrentLocalTimeString()
    {

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        String min = Integer.toString(calendar.get(calendar.MINUTE));
        String sec = Integer.toString(calendar.get(calendar.SECOND));

       if(min.length()==1)
       {
           min = "0"+min;
       }
       String dateTime = Integer.toString(calendar.get(calendar.HOUR_OF_DAY)) + "_" + min+ "_" + sec;
       return dateTime;
    }

    public static String GetUTCDateTime()
    {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String utcTime = sdf.format(new Date());
        return utcTime;
    }

    public static String GetLocalMillisecond()
    {
        try {
            Calendar cal = Calendar.getInstance();
            Long time = cal.getTimeInMillis();
            String strTime = Long.toString(time);
            return strTime;
        }
        catch (Exception ex)
        {
            throw new RuntimeException();
        }

    }

    public static String GetUTCMillisecond()
    {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTimeZone(TimeZone.getTimeZone("UTC"));
            Long time = cal.getTimeInMillis();
            String strTime = Long.toString(time);
            return strTime;
        }
        catch (Exception ex)
        {
            throw new RuntimeException();
        }

    }


   public static int CurrentHour()
    {
        return 2;
    }

   public static int DayDiff(Date CurrDate,Date selectedDate)
    {
        return 0;
    }

    public static int MinutesDifference(String strCurrentDateTime,String strDateTime )
    {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT);
            Date lastDate= sdf.parse(strDateTime);

            Date currentDate= sdf.parse(strCurrentDateTime);

            long duration  = currentDate.getTime() - lastDate.getTime();
            long diffInmin = TimeUnit.MILLISECONDS.toMinutes(duration);
            return (int)diffInmin;
        }
        catch (Exception ex)
        {
            return 0;
        }

    }



    public static String ChangeFormat(String inDate)
    {
        String OutDate = "";
        try {
            SimpleDateFormat idf = new SimpleDateFormat(DATE_TIME_FORMAT);
            Date dateTime = idf.parse(inDate);


            Calendar c = Calendar.getInstance(Locale.getDefault());
            int CurrentYear = c.get(Calendar.YEAR);
            int CurrentDay = c.get(Calendar.DAY_OF_YEAR);

            c.setTime(dateTime);
            int day =  c.get(Calendar.DAY_OF_MONTH);
            String Month = c.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH);

            int year = c.get(Calendar.YEAR);
            int dayOfYear = c.get(Calendar.DAY_OF_YEAR);

            String min = Integer.toString(c.get(c.MINUTE));
            if(min.length()==1)
            {
                min = "0"+ min;
            }

            String time =  c.get(c.HOUR_OF_DAY) +":" + min;

            if(CurrentDay == dayOfYear)
            {
                return time;
            }
            else if (year == CurrentYear)
            {
                return Integer.toString(day) + "," + Month + " at " + time;
            }
            else
            {
                return Integer.toString(day)  + Month +"," + year  + " at " + time;
            }
        }
        catch (Exception ex)
        {
            int a =5;
            return "1 Jan, 2000";
        }

    }

    public static void HandleException(Context context, String Method, String ex)
    {
        //Toast.makeText(context, Method + ":" + ex, Toast.LENGTH_LONG).show();

    }
}
