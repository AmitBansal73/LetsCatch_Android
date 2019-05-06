package net.anvisys.letscatch.Object;

import android.content.Context;

import android.widget.Toast;


import net.anvisys.letscatch.Common.DataAccess;
import net.anvisys.letscatch.Common.ImageServer;
import net.anvisys.letscatch.Common.Session;
import net.anvisys.letscatch.Common.UTILITY;


import java.util.HashMap;

/**
 * Created by Amit Bansal on 09-01-2017.
 */
public  class ActiveMeetingGroup  {

    public static ActiveMeetingGroup meetingGroup;
    public HashMap<String, ActiveMeeting> RunningMeetings = new HashMap<String, ActiveMeeting>();
    Context mContext;
    public String StartTime = "";


    private static MeetingGroupListener meetingListener;

    ActiveMeetingGroup(Context mContext) {
        this.mContext = mContext;
        StartTime = UTILITY.CurrentLocalTimeString();
    }

    public static ActiveMeetingGroup GetInstance(Context mContext)
    {
        if (meetingGroup==null)
        {
            meetingGroup = new ActiveMeetingGroup(mContext);

        }

        return meetingGroup;
    }
    public void InitiateMeetingByContact(Contact cont, Context context)
    {
        try {
            ActiveMeeting tempMeeting;
            if(ActiveMeetingGroup.GetInstance(context).RunningMeetings.containsKey(cont.MobileNumber))
            {
                tempMeeting = ActiveMeetingGroup.GetInstance(context).RunningMeetings.get(cont.MobileNumber);
                tempMeeting.MEETING_STATUS = APP_CONST.MEETING_STATUS_SHARING_LOCATION;
            }
             else {
               tempMeeting = new ActiveMeeting(APP_CONST.MEETING_TYPE_INSTANT, "I Am", APP_VARIABLES.MY_MOBILE_NUMBER, APP_VARIABLES.MY_LOCATION_STRING, cont.MobileNumber, cont.userName, cont.LatLong, UTILITY.CurrentLocalDateTimeString());
                tempMeeting.MEETING_STATUS = APP_CONST.MEETING_STATUS_SENDING_LOCATION;
                ImageServer.SaveBitmapImage(ImageServer.getBitmapFromString(cont.strImage, mContext), cont.MobileNumber, mContext);
                ActiveMeetingGroup.GetInstance(context).RunningMeetings.put(cont.MobileNumber, tempMeeting);
                // Session.UpdateSessionMeeting(context);
                Log.AddLog("Meeting Invited with " + cont.userName, mContext);
            }

            DataAccess da = new DataAccess(context);
            da.open();
            da.UpdateAllActiveMeetings();
            da.close();
            tempMeeting.sendNotification(context,APP_VARIABLES.MY_LOCATION_STRING,Message.SEND_LOCATION);

        }
        catch (Exception ex)
        {
            Toast.makeText(context, "Error while adding contact", Toast.LENGTH_LONG).show();
        }
    }


    public void aAcceptMeeting(String Name, String MobileNumber, String Location)
    {
        try {
            ActiveMeeting tempMeeting;
            if (!RunningMeetings.containsKey(MobileNumber)) {

                DataAccess da = new DataAccess(mContext);
                da.open();
                Contact cont = da.getContactByMobile(MobileNumber);
                da.close();
                if(cont!= null)
                {
                    Name = cont.userName;
                    ImageServer.SaveBitmapImage(ImageServer.getBitmapFromString(cont.strImage,mContext), MobileNumber, mContext);
                }
                tempMeeting = new ActiveMeeting(APP_CONST.MEETING_TYPE_INSTANT,"I Am", APP_VARIABLES.MY_MOBILE_NUMBER , APP_VARIABLES.MY_LOCATION_STRING,MobileNumber,Name, Location,UTILITY.CurrentLocalDateTimeString());
                //tempMeeting.MEETING_STATUS = APP_CONST.MEETING_STATUS_TRACKING;
                RunningMeetings.put(MobileNumber, tempMeeting);
             }
            else
            {
                tempMeeting = RunningMeetings.get(MobileNumber);
            }

            //tempMeeting.sendNotification(mContext, MobileNumber,Message.INVITE_ACCEPTED);
        }
        catch (Exception ex)
        {
            Toast.makeText(mContext,"Error while adding contact to trace",Toast.LENGTH_LONG).show();
        }
    }

    public void aRejectMeeting(String MobileNumber)
    {
        try {
            ActiveMeeting tempMeeting;
            if(RunningMeetings.containsKey(MobileNumber)) {
                tempMeeting = RunningMeetings.get(MobileNumber);
            }
            else
            {
                tempMeeting = new ActiveMeeting(APP_CONST.MEETING_TYPE_INSTANT,"I Am", APP_VARIABLES.MY_MOBILE_NUMBER , APP_VARIABLES.MY_LOCATION_STRING,MobileNumber,"", "",UTILITY.CurrentLocalDateTimeString());
                RunningMeetings.put(MobileNumber,tempMeeting);
            }
            //tempMeeting.sendNotification(mContext, MobileNumber, Message.INVITE_REJECTED);
        }
        catch (Exception ex)
        {
            Toast.makeText(mContext,"Error while adding contact to trace",Toast.LENGTH_LONG).show();
        }
    }
/*
    @Override
    public void OnInviteSent(ActiveMeeting meeting) {
        try {

            if (meetingListener != null) {
                meetingListener.OnMeetingInitiate(meeting.DESTINATION_MOBILE_NO);
            }
        }
        catch (Exception ex)
        {
            Toast.makeText(mContext,"Error On Invite Sent", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void OnStopSent(ActiveMeeting meeting) {
        try {

            if (meetingListener != null) {
                meetingListener.OnMeetingComplete(meeting.DESTINATION_MOBILE_NO);
            }
        }
        catch (Exception ex)
        {
            Toast.makeText(mContext,"Error Stopping", Toast.LENGTH_LONG).show();
        }

    }
    */

    public void StopMeetingByMobile(String mobile, Context context)
    {
        try {
           ActiveMeeting tempMeeting = RunningMeetings.get(mobile);
            Session.UpdateSessionMeeting(context);
            Log.AddLog("Meeting stopped with " + mobile, context);
            tempMeeting.StopSending(context);
            //tempMeeting.sendNotification(context, mobile, Message.STOP_TRACKING);
        }
        catch (Exception ex)
        {
            Toast.makeText(context, "Error while stopping meeting", Toast.LENGTH_LONG).show();
        }
    }

    public  void PauseMeetingByMobile(String mobile, Context context)
    {
        RunningMeetings.get(mobile).MEETING_STATUS=APP_CONST.MEETING_STATUS_PAUSE;
        Session.UpdateSessionMeeting(context);
        meetingListener.OnMeetingPause(mobile);
    }

    public  void ResumeMeetingByMobile(String mobile, Context context)
    {
        RunningMeetings.get(mobile).MEETING_STATUS=APP_CONST.MEETING_STATUS_SHARING_LOCATION;
        Session.UpdateSessionMeeting(context);
        meetingListener.OnMeetingResume(mobile);
    }

    public void aRemindMeetingByMobile(String mobile, Context context)
    {
        ActiveMeeting meeting = RunningMeetings.get(mobile);
       // meeting.sendNotification(context, mobile, Message.SEND_INVITE);
    }


    public  ActiveMeeting GetMeetingByName(String Name)
    {
        ActiveMeeting ret=null;

        for (ActiveMeeting meeting : RunningMeetings.values()
                ) {

            if (meeting.DESTINATION_NAME.matches(Name)) {
                ret = meeting;
                break;
            }
        }

        return ret;
    }

    public  void StopAllRunningMeetings( Context context)
    {

        for (ActiveMeeting meeting : RunningMeetings.values()
                ) {
            //meeting.RegisterMeetingStatusListener(this);
            if(meeting.MEETING_STATUS.matches(APP_CONST.MEETING_STATUS_SHARING_LOCATION) || meeting.MEETING_STATUS.matches(APP_CONST.MEETING_STATUS_SENDING_LOCATION))
            {
                meeting.sendNotification(context, meeting.DESTINATION_MOBILE_NO, Message.STOP_TRACKING);
            }
        }
    }

    public  void RegisterMeetingListener(MeetingGroupListener listener) {
        meetingListener = listener;
    }

    public void RemoveMeetingListener() {
        this.meetingListener = null;
    }


    public interface MeetingGroupListener {
        void OnMeetingComplete(String MobileNumber);
        void OnStopAllMeetings();
        void OnMeetingInitiate(String MobileNumber);
        void OnMeetingPause(String MobileNumber);
        void OnMeetingResume(String MobileNumber);
    }



}