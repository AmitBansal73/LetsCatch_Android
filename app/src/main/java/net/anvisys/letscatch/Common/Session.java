package net.anvisys.letscatch.Common;

import android.content.Context;
import android.content.SharedPreferences;

import net.anvisys.letscatch.Object.APP_CONST;
import net.anvisys.letscatch.Object.APP_SETTINGS;
import net.anvisys.letscatch.Object.ActiveMeeting;
import net.anvisys.letscatch.Object.Profile;

import java.util.HashMap;

/**
 * Created by Amit Bansal on 06-01-2017.
 */
public class Session {


    public static boolean GetApplicationStatus(Context context)
    {
        SharedPreferences prefs = context.getSharedPreferences(APP_CONST.SESSION_NAME, Context.MODE_PRIVATE);
        if (prefs != null) {
            return prefs.getBoolean("ApplicationRunning", false);
        }
        else
            return false;
    }

    public static void SetApplicationStatus(Context context, boolean status)
    {
        SharedPreferences prefs = context.getSharedPreferences(APP_CONST.SESSION_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("ApplicationRunning", status);
        editor.commit();
    }


    public static boolean setRegistrationID(Context context, String RegID)
    {
        String registrationId = "";
        try {
            SharedPreferences prefs = context.getSharedPreferences(APP_CONST.SESSION_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("regID",RegID);
            editor.commit();
            return  true;
        }
        catch (Exception ex)
        {
            return false;
        }

    }

    public static String getRegistrationID(Context context)
    {
        String registrationId = "";
        try {
            SharedPreferences prefs = context.getSharedPreferences(APP_CONST.SESSION_NAME, Context.MODE_PRIVATE);
            registrationId = prefs.getString("regID", "");
        }
        catch (Exception ex)
        {}
        return registrationId;
    }

    public static boolean AddUser(Context context, Profile myProfile)
    {
        try {
            SharedPreferences prefs = context.getSharedPreferences(APP_CONST.SESSION_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("regID", myProfile.REG_ID);
            editor.putString("MobileNo",myProfile.MOB_NUMBER);
            editor.putString("Location",myProfile.LOCATION);
            editor.putString("Name",myProfile.NAME);
            editor.putString("Email",myProfile.E_MAIL);
            //  editor.putString("imgSrc",myProfile.IMAGE_SRC);
            editor.putString("ImageString",myProfile.strImage);
            editor.commit();
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    public static Profile GetUser(Context context)
    {
        Profile mProfile = new Profile();
        try {
            SharedPreferences prefs = context.getSharedPreferences(APP_CONST.SESSION_NAME, Context.MODE_PRIVATE);
            mProfile.MOB_NUMBER =  prefs.getString("MobileNo","9999999999");
            mProfile.NAME =  prefs.getString("Name","User Name");
            mProfile.E_MAIL =  prefs.getString("Email","user@xyz.com");
            mProfile.LOCATION =  prefs.getString("Location","abc");
            mProfile.strImage =  prefs.getString("ImageString","");
            mProfile.REG_ID =  prefs.getString("regID","");
            return mProfile;
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    public static boolean RemoveUser(Context context)
    {
        try {
            SharedPreferences prefs = context.getSharedPreferences(APP_CONST.SESSION_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("regID", "");
            editor.putString("MobileNo","");
            editor.putString("Location","");
            editor.putString("Name","");
            editor.putString("imgSrc","");
            editor.putString("Contact_Refresh_Time","");
            editor.putString("Contact_Refresh_MilliSecond","");
            editor.putString("INSTANT_MEETING","");
            editor.commit();
            return true;

        }
        catch (Exception ex)
        {
            return false;
        }
    }

    public static boolean LogOff(Context context)
    {
        try {
            SharedPreferences prefs = context.getSharedPreferences(APP_CONST.SESSION_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.commit();
            return true;

        }
        catch (Exception ex)
        {
            return false;
        }
    }

    public static  HashMap<String,ActiveMeeting> InitiateMeetingsFromSession(Context context)
    {
        HashMap<String,ActiveMeeting> RunningMeetings = new HashMap<>();

        try {
            SharedPreferences prefs = context.getSharedPreferences(APP_CONST.SESSION_NAME, Context.MODE_PRIVATE);

            String INSTANT_MEETING = prefs.getString("INSTANT_MEETING", "");
            String START_TIME = prefs.getString("START_TIME", "");
            if (!INSTANT_MEETING.matches(""))
            {

                String[] ContStatus = INSTANT_MEETING.split("#");
                for (String str:ContStatus
                        ) {
                    ActiveMeeting tempMeeting = new ActiveMeeting(str);
                    RunningMeetings.put(tempMeeting.DESTINATION_MOBILE_NO, tempMeeting);
                }

            }
            return RunningMeetings;
        } catch (Exception ex) {
            return RunningMeetings;
        }
    }


    public static boolean UpdateSessionMeeting(Context context)
    {
/*
        try {
            SharedPreferences prefs = context.getSharedPreferences(APP_CONST.SESSION_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            String InstantMeeting = "";
            if (ActiveMeetingGroup.GetInstance(context).RunningMeetings.size()>0) {
                for (ActiveMeeting meet : ActiveMeetingGroup.GetInstance(context).RunningMeetings.values()
                        ) {
                    InstantMeeting = InstantMeeting + meet.GetSessionString() + "#";
                }
                InstantMeeting = InstantMeeting.substring(0, InstantMeeting.length() - 1);

                editor.putString("INSTANT_MEETING", InstantMeeting);
            } else
            {
                editor.putString("MEETING_GROUP_NAME","");
                editor.putString("INSTANT_MEETING", "");
                editor.putString("START_TIME","");
            }


            editor.commit();
            return true;
        }

        catch (Exception ex)
        {
            return false;
        }
        finally {
            //   meetingGroup.ClearMeeting();
        }
        */
        return false;
    }

    public static void SaveSetting(Context context)
    {
        SharedPreferences prefs = context.getSharedPreferences(APP_CONST.SESSION_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("IS_ALARM", APP_SETTINGS.MEETING_ALARM);
        editor.putBoolean("IS_ROUTING", APP_SETTINGS.ENABLE_ROUTING);
        editor.putBoolean("IS_PANIC", APP_SETTINGS.PANIC_BUTTON);
        editor.putString("PRIMARY_CONTACT", APP_SETTINGS.PRIMARY_CONTACT);
        editor.putString("SECONDARY_CONTACT", APP_SETTINGS.SECONDARY_CONTACT);
        editor.commit();
    }

    public static void SetSetting(Context context)
    {
        SharedPreferences prefs = context.getSharedPreferences(APP_CONST.SESSION_NAME, Context.MODE_PRIVATE);
        APP_SETTINGS.MEETING_ALARM =  prefs.getBoolean("IS_ALARM", true);
        APP_SETTINGS.ENABLE_ROUTING =  prefs.getBoolean("IS_ROUTING", true);
        APP_SETTINGS.PANIC_BUTTON =  prefs.getBoolean("IS_PANIC", true);
        APP_SETTINGS.PRIMARY_CONTACT =  prefs.getString("PRIMARY_CONTACT", "");
        APP_SETTINGS.SECONDARY_CONTACT =  prefs.getString("SECONDARY_CONTACT", "");


    }

    public static boolean SetLocalContactSyncLocalMilliSecond(Context context)
    {
        try {

            String strTime =  UTILITY.GetLocalMillisecond();
            SharedPreferences prefs = context.getSharedPreferences(APP_CONST.SESSION_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            editor.putString("Contact_Refresh_MilliSecond", strTime);
            editor.commit();
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    public static String GetLocalContactSyncLocalMilliSecond(Context context)
    {
        String vendorRefreshTime ="01/01/2000 12:00:00";
        try {
            SharedPreferences prefs = context.getSharedPreferences(APP_CONST.SESSION_NAME, Context.MODE_PRIVATE);
            vendorRefreshTime =  prefs.getString("Contact_Refresh_MilliSecond", "0");
            return vendorRefreshTime;
        }
        catch (Exception ex)
        {
            return "01/01/2000 12:00:00";
        }
    }

    public static boolean SetLocalContactSyncUTCMilliSecond(Context context)
    {
        try {

            String strTime =  UTILITY.GetUTCMillisecond();
            SharedPreferences prefs = context.getSharedPreferences(APP_CONST.SESSION_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            editor.putString("Contact_Refresh_UTC_MilliSecond", strTime);
            editor.commit();
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    public static String GetLocalContactSyncUTCMilliSecond(Context context)
    {
        String vendorRefreshTime ="01/01/2000 12:00:00";
        try {
            SharedPreferences prefs = context.getSharedPreferences(APP_CONST.SESSION_NAME, Context.MODE_PRIVATE);
            vendorRefreshTime =  prefs.getString("Contact_Refresh_UTC_MilliSecond", "0");
            return vendorRefreshTime;
        }
        catch (Exception ex)
        {
            return "01/01/2000 12:00:00";
        }
    }

    public static boolean SetServerContactSyncUTCDateTime(Context context)
    {
        try {
            SharedPreferences prefs = context.getSharedPreferences(APP_CONST.SESSION_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            String time = UTILITY.GetUTCDateTime();
            editor.putString("Contact_Refresh_Time", time);
            editor.commit();
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
    }


    public static String GetServerContactSyncUTCDateTime(Context context)
    {
        String vendorRefreshTime ="01/01/2000 12:00:00";
        try {
            SharedPreferences prefs = context.getSharedPreferences(APP_CONST.SESSION_NAME, Context.MODE_PRIVATE);
            vendorRefreshTime =  prefs.getString("Contact_Refresh_Time", "0");
            return vendorRefreshTime;
        }
        catch (Exception ex)
        {
            return "01/01/2000 12:00:00";
        }
    }

    public static boolean IsAlarmSet(Context context)
    {
        try {
            SharedPreferences prefs = context.getSharedPreferences(APP_CONST.SESSION_NAME, Context.MODE_PRIVATE);
            return prefs.getBoolean("AlarmSet", false);
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    public static boolean SetAlarm(Context context)
    {
        try {
            SharedPreferences prefs = context.getSharedPreferences(APP_CONST.SESSION_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("AlarmSet", true);
            editor.commit();
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }

    }
    public static boolean CancelAlarm(Context context)
    {
        try {
            SharedPreferences prefs = context.getSharedPreferences(APP_CONST.SESSION_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("AlarmSet", false);
            editor.commit();
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }

    }
}
