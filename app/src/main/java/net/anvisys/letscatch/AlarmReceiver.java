package net.anvisys.letscatch;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.widget.Toast;

import net.anvisys.letscatch.Common.Session;
import net.anvisys.letscatch.Object.APP_SETTINGS;

/**
 * Created by Amit Bansal on 13-02-2017.
 */
public class AlarmReceiver extends WakefulBroadcastReceiver {
    private static AlarmManager alarmMgr;
    private static PendingIntent alarmIntent;
    static AlarmListener alarmListener;
    Context mContext;

    public AlarmReceiver() {
    }

    public interface AlarmListener
    {
        public void AlarmReceived(String message);
    }

    // Assign the listener implementing events interface that will receive the events
    public void setCustomObjectListener(AlarmListener listener) {
        alarmListener = listener;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
       // SyncContact sync = new SyncContact(mContext);
     //   sync.StartSyncingContact();

        Intent SyncIntent = new Intent(context, ContactSyncService.class);
        context.startService(SyncIntent);

    }

    public static void setAlarm(Context context)
    {
        try {
              alarmMgr = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
                Intent intent = new Intent(context, AlarmReceiver.class);
                alarmIntent = PendingIntent.getBroadcast(context, 1234, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), APP_SETTINGS.ALARM_INTERVAL_MILLISECONDS, alarmIntent);
                Session.SetAlarm(context);

        }
        catch (Exception ex)
        {
            Toast.makeText(context, "Error occurred in SetAlarm", Toast.LENGTH_SHORT).show();
        }

    }

    public static void cancelAlarm(Context context)
    {
        try {
            alarmMgr = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
            Intent intent = new Intent(context, AlarmReceiver.class);
            alarmIntent = PendingIntent.getBroadcast(context, 1234, intent, PendingIntent.FLAG_UPDATE_CURRENT);
           // alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 300000, alarmIntent);
            alarmMgr.cancel(alarmIntent);
            alarmIntent.cancel();
            Session.CancelAlarm(context);

        }
        catch (Exception ex)
        {}

    }

    public static boolean isRegistered(Context context)
    {
        try {

            Intent intent = new Intent(context, AlarmReceiver.class);//the same as up

            alarmIntent = (PendingIntent.getBroadcast(context, 1234, intent, PendingIntent.FLAG_UPDATE_CURRENT));//just changed the flag
            if(alarmIntent == null)
            {
                return false;
            }
            else
            {
                return true;
            }
        }
        catch (Exception ex)
        {
            return false;
        }

    }
}
