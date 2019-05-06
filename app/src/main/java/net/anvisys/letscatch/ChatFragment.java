package net.anvisys.letscatch;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import net.anvisys.letscatch.Common.DataAccess;
import net.anvisys.letscatch.Common.ImageServer;
import net.anvisys.letscatch.Common.UTILITY;
import net.anvisys.letscatch.Object.APP_CONST;
import net.anvisys.letscatch.Object.APP_VARIABLES;
import net.anvisys.letscatch.Object.ActiveMeeting;
import net.anvisys.letscatch.Object.ActiveMeetingGroup;
import net.anvisys.letscatch.Object.ChatMessage;
import net.anvisys.letscatch.Object.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment implements ActiveMeeting.ChatUpdateListener{

    TextView ActiveList;

    ImageView Image;
    ImageView imageChecked;
    TextView txtName,txtStatus,txtDistance,txtTime;
    TextView txtStartTime;
    TextView txtUpdateTime;
    ListView ChatListView;
    static ChatListAdapter chatAdapter;
    EditText myMessage;
    ImageView send;
    String MobileNumber;
    ChatMessage newChat;
   // List<ChatMessage> msgList = new ArrayList<>();
    LinkedHashMap<Integer,ChatMessage> msgList = new LinkedHashMap<>();
    private String CurrentMeetingStatus;
    private String Name;
    private int REQUEST_IMAGE_GET=1;
    ActiveMeeting meeting;
    static int  MSG_ID = 1;
    int IMAGE_SEND_REQUEST = 2;
    int REQUEST_SEND_IMAGE=2;
    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        Image = (ImageView)view.findViewById(R.id.markerImage);
        txtName = (TextView) view.findViewById(R.id.userName);
        txtStatus = (TextView) view.findViewById(R.id.userStatus);
        txtDistance = (TextView) view.findViewById(R.id.userDistance);
        txtTime = (TextView) view.findViewById(R.id.userTime);
        txtStartTime = (TextView) view.findViewById(R.id.startTime);
        txtUpdateTime = (TextView) view.findViewById(R.id.updateTime);
        ChatListView = (ListView)view.findViewById(R.id.chatList);
        myMessage = (EditText)view.findViewById(R.id.myMessage);
        send = (ImageView)view.findViewById(R.id.sendImage);


        Bundle args =   getArguments();
        try {
            meeting = (ActiveMeeting) args.getParcelable("Meeting");
            meeting.newMessage=0;
            Bitmap bmp = ImageServer.GetImageBitmap(meeting.DESTINATION_MOBILE_NO, getContext());
            Image.setImageBitmap(bmp);
            Name = meeting.DESTINATION_NAME;
            txtName.setText(meeting.DESTINATION_NAME);
            CurrentMeetingStatus = meeting.MEETING_STATUS;
            txtStatus.setText(meeting.MEETING_STATUS);

            txtDistance.setText(" Dist: "+ meeting.DIST_To_GO);
            txtTime.setText(" Expected time :"+meeting.TIME_TO_GO);
            txtStartTime.setText("Started At: "+ UTILITY.ChangeFormat(meeting.START_TIME));
            txtUpdateTime.setText("Last Updated At: "+UTILITY.ChangeFormat(meeting.UPDATE_TIME));

            MobileNumber = meeting.DESTINATION_MOBILE_NO;
            // UpdateMessageList();
            DataAccess da = new DataAccess(getContext());
            da.open();
            msgList = da.GetMessage(MobileNumber) ;
            da.close();
            chatAdapter= new ChatListAdapter(getActivity());
            ChatListView.setAdapter(chatAdapter);
            ChatListView.setDivider(null);

        }
        catch (Exception ex)
        {
            int a =1;
        }

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = myMessage.getText().toString();
                if(!msg.matches(""))
                {
                    MSG_ID++;
                    newChat = new ChatMessage();
                    newChat.ID = MSG_ID;
                    newChat.Dest_Mobile = MobileNumber;
                    newChat.Mobile = "ME";
                    newChat.Message = msg;
                    newChat.Type = Message.TEXT_MESSAGE;
                    newChat.dateTime = UTILITY.CurrentLocalDateTimeString();
                    myMessage.setText("");
                    newChat.Delivery_Status = 0;
                    msgList.put(MSG_ID,newChat);
                    chatAdapter.notifyDataSetChanged();
                    sendMessage(getContext(),newChat,Message.TEXT_MESSAGE);
                }
            }
        });


        setHasOptionsMenu(true);
        return view;
    }

    public void OnStatusChanged(String status)
    {
        txtStatus.setText(status);
    }

    public void OnRouteCalculated(ActiveMeeting meet)
    {
        txtDistance.setText(" Dist: "+ meet.DIST_To_GO);
        txtTime.setText(" Expected time :"+meet.TIME_TO_GO);
        txtStartTime.setText("Started At: "+ UTILITY.ChangeFormat(meet.START_TIME));
        txtUpdateTime.setText("Last Updated At: "+UTILITY.ChangeFormat(meet.UPDATE_TIME));
    }

    @Override
    public void OnTextSent(String MeetingMobile, String MsgMobile, String message) {
        try {
            MSG_ID++;
            newChat = new ChatMessage();
            newChat.Dest_Mobile = MeetingMobile;
            newChat.Type = Message.TEXT_MESSAGE;
            newChat.Mobile = MsgMobile;
            newChat.Message = message;
            newChat.dateTime = UTILITY.CurrentLocalDateTimeString();
            newChat.Delivery_Status = 1;
            msgList.put(MSG_ID,newChat);
            chatAdapter.notifyDataSetChanged();
        }
        catch (Exception ex)
        {
UTILITY.HandleException(getContext(),"OnTextSent", ex.toString());
        }
    }

    public void OnImageReceived(String MeetingMobile, String MsgMobile, String message)
    {
        try {
            MSG_ID++;
            newChat = new ChatMessage();
            newChat.Dest_Mobile = MeetingMobile;
            newChat.Mobile = MsgMobile;
            newChat.Type = Message.IMAGE_MESSAGE;
            newChat.ImageName = message;
            newChat.Message = message;
            newChat.dateTime = UTILITY.CurrentLocalDateTimeString();
            newChat.DownloadStatus = 1;
            newChat.Delivery_Status = 1;
            msgList.put(MSG_ID,newChat);
            chatAdapter.notifyDataSetChanged();
           // GetImage(MSG_ID,message);
        }
        catch (Exception ex)
        {
            UTILITY.HandleException(getContext(),"OnImageReceived", ex.toString());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            int id = item.getItemId();
            if (id == R.id.action_Share) {
                if(APP_VARIABLES.NETWORK_STATUS==false)
                {
                    ((MainActivity)getActivity()).ShowSnackBar("Network unavailable!");

                }
                else {
                    ShareLocationDialog();
                }
            }
            else if (id == R.id.action_Image) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivityForResult(intent, REQUEST_IMAGE_GET);
                }
            }

            else if (id == R.id.action_Stop) {
                StopSelectedMeetings();
            }

            else if (id == R.id.action_Pause) {

                PauseSelectedMeeting();
            }
            else if (id == R.id.action_Resume) {
                ResumeSelectedMeeting();
            }

            return true;
        }
        catch (Exception ex)
        {
            Toast.makeText(getContext(),"Error in completing operation",Toast.LENGTH_LONG).show();
            return false;
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        ((fragmentFragment)getParentFragment()).CurrentFragment = ChatFragment.this;
        //((MainActivity)getActivity()).CurrentFragment = ChatFragment.this;
        menu.clear();

            if(CurrentMeetingStatus.matches(APP_CONST.MEETING_STATUS_PAUSE))
            {
                inflater.inflate(R.menu.menu_pause_meeting, menu);
            }
            else if (CurrentMeetingStatus.matches(APP_CONST.MEETING_STATUS_SHARING_LOCATION)) {
                inflater.inflate(R.menu.menu_sending_location, menu);
            }
            else if (CurrentMeetingStatus.matches(APP_CONST.MEETING_STATUS_RECEIVING_LOCATION)) {
                inflater.inflate(R.menu.menu_receiving_location, menu);
            }

            else if (CurrentMeetingStatus.matches(APP_CONST.MEETING_STATUS_SENDING_LOCATION)) {
                inflater.inflate(R.menu.menu_sending_location, menu);
            }
            else
            {
                inflater.inflate(R.menu.menu_meeting_no_select, menu);
            }


    }

    private class ChatListAdapter extends BaseAdapter {

        LayoutInflater inflater;


        public ChatListAdapter(Activity activity) {
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return msgList.size();
        }

        @Override
        public Object getItem(int position) {
            return (ChatMessage) msgList.values().toArray()[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            try {

                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.chat_row_item, null);
                }
                View inView = convertView.findViewById(R.id.incomingView);
                View outView = convertView.findViewById(R.id.outgoingView);

                TextView inMessage = (TextView)convertView.findViewById(R.id.incomingMessage);
                TextView inTime = (TextView)convertView.findViewById(R.id.incomingTime);
                ImageView incomingImage = (ImageView)convertView.findViewById(R.id.incomingImage);
                TextView outMessage = (TextView)convertView.findViewById(R.id.outgoingMessage);
                ImageView outgoingImage = (ImageView)convertView.findViewById(R.id.outgoingImage);
                TextView outTime = (TextView)convertView.findViewById(R.id.outgoingTime);
                ImageView checkStatus = (ImageView)convertView.findViewById(R.id.checkStatus);

                ProgressBar prgBar = (ProgressBar)convertView.findViewById(R.id.progressBar);

                ChatMessage chat = (ChatMessage)msgList.values().toArray()[position];

                if(chat.Delivery_Status == 0)
                {
                    checkStatus.setVisibility(View.GONE);
                    prgBar.setVisibility(View.VISIBLE);
                }
                if(chat.Delivery_Status == 1)
                {
                    checkStatus.setVisibility(View.VISIBLE);
                    checkStatus.setImageResource(R.drawable.correct);
                    prgBar.setVisibility(View.GONE);
                }
                if(chat.Delivery_Status == 2)
                {
                    checkStatus.setVisibility(View.VISIBLE);
                    checkStatus.setImageResource(R.drawable.fail);
                    prgBar.setVisibility(View.GONE);
                }

                if(chat.Mobile.matches("ME"))
                {
                    outView.setVisibility(View.GONE);
                    inView.setVisibility(View.VISIBLE);
                    if(chat.Type.equalsIgnoreCase(Message.IMAGE_MESSAGE))
                    {
                        incomingImage.setVisibility(View.VISIBLE);
                        inMessage.setVisibility(View.GONE);
                        Bitmap bmp = ImageServer.GetImageBitmap(chat.ImageName, getContext());
                        incomingImage.setImageBitmap(bmp);
                        incomingImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    }
                    else
                    {
                        incomingImage.setVisibility(View.GONE);
                        inMessage.setVisibility(View.VISIBLE);
                        inMessage.setText(chat.Message);
                    }
                    inTime.setText(UTILITY.ChangeFormat(chat.dateTime));
                }
                else
                {
                    outView.setVisibility(View.VISIBLE);
                    inView.setVisibility(View.GONE);

                    if(chat.Type.equalsIgnoreCase(Message.IMAGE_MESSAGE))
                    {
                        outgoingImage.setVisibility(View.VISIBLE);
                        outMessage.setVisibility(View.GONE);
                        if(chat.DownloadStatus == 0)
                        {
                            outgoingImage.setImageResource(R.drawable.img_downloading);
                            GetImage(chat.ID, chat.Message);
                        }
                        else {
                            Bitmap bmp = ImageServer.GetImageBitmapFromExternal(chat.ImageName, getContext());
                            if(bmp != null)
                            {
                                outgoingImage.setImageBitmap(bmp);
                            }
                            else
                            {
                                outgoingImage.setImageResource(R.drawable.img_deleted);
                            }

                        }
                        outgoingImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    }
                    else {
                        outgoingImage.setVisibility(View.GONE);
                        outMessage.setVisibility(View.VISIBLE);
                        outMessage.setText(chat.Message);
                        outTime.setText(UTILITY.ChangeFormat(chat.dateTime));
                    }
                }

            }
            catch (Exception ex)
            {
                Toast.makeText(getContext(), "error in ActiveListAdapter.view", Toast.LENGTH_LONG).show();
            }
            return convertView;

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == REQUEST_IMAGE_GET) {

                if (data != null) {
                    Uri uri = data.getData();
                    Intent imageIntent = new Intent(getActivity(), SendImageActivity.class);
                    imageIntent.putExtra("image_uri", uri.toString());
                    startActivityForResult(imageIntent, IMAGE_SEND_REQUEST);

                }
            }
            else if (requestCode == IMAGE_SEND_REQUEST) {

                String Name = data.getStringExtra("ImageName");
                Bitmap bmp = ImageServer.GetImageBitmap(Name, getContext());
                int count = bmp.getByteCount();
                        String imgString = ImageServer.getStringFromBitmap(bmp);
                newChat = new ChatMessage();
                newChat.Dest_Mobile = MobileNumber;
                newChat.Mobile = "ME";
                newChat.Type = Message.IMAGE_MESSAGE;
                newChat.Message = imgString;
                newChat.ImageName = Name;
                newChat.dateTime = UTILITY.CurrentLocalDateTimeString();
                newChat.Delivery_Status = 0;
                msgList.put(MSG_ID, newChat);
                chatAdapter.notifyDataSetChanged();
                sendMessage(getContext(), newChat, Message.IMAGE_MESSAGE);

            }
        }
        catch (Exception ex)
        {
            UTILITY.HandleException(getContext(),"ChatActivityResult", ex.toString());
        }
    }

    private void PauseSelectedMeeting()
    {
        String strName ="<p>Will Not send location and Route</p>" + Name;
        Spanned result = Html.fromHtml(strName);
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("PAUSE");
        dialog.setMessage(result);
        dialog.setCancelable(false);
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog.setPositiveButton("Pause", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ActiveMeetingGroup.GetInstance(getContext()).PauseMeetingByMobile(MobileNumber, getContext());
                txtStatus.setText(APP_CONST.MEETING_STATUS_PAUSE);
                DataAccess da = new DataAccess(getContext());
                da.open();
                da.UpdateAllActiveMeetings();
                da.close();
                getActivity().invalidateOptionsMenu();

            }
        });
        AlertDialog alert = dialog.create();
        alert.show();
    }

    private void ResumeSelectedMeeting()
    {
        String strName ="<p>Following Meetings will be Resumed</p></br> "+Name;
        Spanned result = Html.fromHtml(strName);
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("RESUME");
        dialog.setMessage(result);
        dialog.setCancelable(false);
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog.setPositiveButton("Resume", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        try {

                            ActiveMeetingGroup.GetInstance(getContext()).ResumeMeetingByMobile(MobileNumber, getContext());
                            txtStatus.setText(CurrentMeetingStatus);
                            getActivity().invalidateOptionsMenu();
                            DataAccess da = new DataAccess(getContext());
                            da.open();
                            da.UpdateAllActiveMeetings();
                            da.close();

                        } catch (Exception ex) {
                            Toast.makeText(getContext(), "Error in Resume", Toast.LENGTH_LONG).show();
                        }

                    }
                }

        );
        AlertDialog alert = dialog.create();
        alert.show();
    }


    private void StopSelectedMeetings()
    {
        String strName ="<p>Stop Sharing Location with</p></br>" + Name;

        Spanned result = Html.fromHtml(strName);
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("STOP");
        dialog.setMessage(result);
        dialog.setCancelable(false);
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getActivity().invalidateOptionsMenu();
            }
        });
        dialog.setPositiveButton("Stop", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(meeting.MEETING_STATUS.matches(APP_CONST.MEETING_STATUS_SHARING_LOCATION))
                {
                    meeting.MEETING_STATUS = APP_CONST.MEETING_STATUS_RECEIVING_LOCATION;
                    txtStatus.setText(APP_CONST.MEETING_STATUS_RECEIVING_LOCATION);
                    chatAdapter.notifyDataSetChanged();
                }
                else if (meeting.MEETING_STATUS.matches(APP_CONST.MEETING_STATUS_SENDING_LOCATION))
                {
                    txtStatus.setText("Stopped");
                    chatAdapter.notifyDataSetChanged();
                }
                DataAccess da = new DataAccess(getContext());
                da.open();
                da.UpdateAllActiveMeetings();
                da.close();
                meeting.StopSending(getContext());
                //meeting.sendNotification(getContext(), MobileNumber, Message.STOP_TRACKING);
                //ActiveMeetingGroup.GetInstance(getContext()).StopMeetingByMobile(meeting.DESTINATION_MOBILE_NO, getContext());
                getActivity().invalidateOptionsMenu();

            }
        });
        AlertDialog alert = dialog.create();
        alert.show();
    }

    private void ShareLocationDialog()
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("Share Location With:");
        dialog.setMessage(Name);
        dialog.setCancelable(false);
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog.setPositiveButton("Share Location", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                meeting.MEETING_STATUS = APP_CONST.MEETING_STATUS_SHARING_LOCATION;
                ((MainActivity)getActivity()).OnMeetingStatusChanged();
                meeting.sendNotification(getContext(), APP_VARIABLES.MY_LOCATION_STRING, Message.SEND_LOCATION);

            }
        });
        AlertDialog alert = dialog.create();
        alert.show();
    }


    public void sendMessage( final Context context, final ChatMessage chat, final  String msg) {

        try {
        int RequestID=1;
        String reqBody = "{\"hostMobile\":\"" + APP_VARIABLES.MY_MOBILE_NUMBER + "\",\"hostName\":\"" + APP_VARIABLES.MY_NAME + "\",\"trackerID\":" + RequestID + ",\"Type\":\"" + msg + "\",\"hostLocation\":\"" + chat.Message + "\",\"inviteeMobile\":\"" + MobileNumber + "\",\"inviteeLocation\":\"" + APP_VARIABLES.MY_LOCATION_STRING + "\"}";

        String url = APP_CONST.APP_SERVER_URL + "api/Tracker";

            JSONObject jsRequest = new JSONObject(reqBody);
            //-------------------------------------------------------------------------------------------------
            RequestQueue queue = Volley.newRequestQueue(context);

            JsonObjectRequest jsArrayRequest = new JsonObjectRequest(Request.Method.POST, url, jsRequest, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jObj) {
                    try {
                        String Response = jObj.getString("Response");

                        if(msg.matches(Message.TEXT_MESSAGE))
                        {
                            if (Response.matches("OK")) {
                                DataAccess da = new DataAccess(context);
                                da.open();
                                da.insertNewMessage(chat.Dest_Mobile,"ME",Message.TEXT_MESSAGE, chat.Message);
                                da.close();
                                chat.Delivery_Status=1;

                            } else if (Response.matches("Fail")) {
                                chat.Delivery_Status=2;
                                Toast.makeText(context, "Error in Sending Notification", Toast.LENGTH_SHORT).show();
                            }
                            chatAdapter.notifyDataSetChanged();
                        }
                        else if(msg.matches(Message.IMAGE_MESSAGE))
                        {
                            if (Response.matches("OK")) {
                                DataAccess da = new DataAccess(context);
                                da.open();
                                da.insertNewMessage(chat.Dest_Mobile, "ME",Message.IMAGE_MESSAGE, chat.ImageName);
                                da.close();
                                chat.Delivery_Status=1;
                            } else if (Response.matches("Fail")) {
                                chat.Delivery_Status=2;
                                Toast.makeText(context, "Error in Sending Notification", Toast.LENGTH_SHORT).show();
                            }
                            chatAdapter.notifyDataSetChanged();
                        }

                    } catch (JSONException jEx) {

                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    chat.Delivery_Status=2;
                    chatAdapter.notifyDataSetChanged();

                }
            });


            RetryPolicy rPolicy = new DefaultRetryPolicy(0,-1,0);
            jsArrayRequest.setRetryPolicy(rPolicy);
            queue.add(jsArrayRequest);


            //*******************************************************************************************************
        } catch (JSONException js) {
            // prgBar.setVisibility(View.GONE);
            chat.Delivery_Status=2;
            //  Toast.makeText(getApplicationContext(), "Post could not be submitted : Try Again",Toast.LENGTH_LONG).show();
        } finally {

        }
    }

    public void GetImage(final int ID,final String file)
    {
        try {
            String escapedFilepath = file.replace("\\","\\\\");
            String reqBody = "{\"FilePath\":\"" + escapedFilepath + "\"}";
            String url = APP_CONST.APP_SERVER_URL + "api/ImageMessage";
            JSONObject jsRequest = new JSONObject(reqBody);

        //-------------------------------------------------------------------------------------------------
        RequestQueue queue = Volley.newRequestQueue(getContext());
        JsonObjectRequest jsArrayRequest = new JsonObjectRequest(Request.Method.POST, url,jsRequest, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jObject) {

                try{
                    String strImage =  jObject.getString("ImageString");
                    Bitmap bmp = ImageServer.getBitmapFromString(strImage, getContext());
                    ChatMessage chat = msgList.get(ID);
                    chat.DownloadStatus=1;
                    ImageServer.SaveBitmapToExternal(bmp,chat.ImageName,getContext());

                    DataAccess da = new DataAccess(getContext());
                    da.open();
                    da.insertNewMessage(chat.Dest_Mobile,chat.Dest_Mobile,Message.IMAGE_MESSAGE, chat.ImageName);
                    da.close();
                    chatAdapter.notifyDataSetChanged();
                }
                catch (JSONException e)
                {
                    // HideSnackBar();
                }

                catch (Exception ex)
                {
                    int b =8;
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //   ShowSnackBar("Could not refresh data", "Retry");
            }
        });
        RetryPolicy policy = new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 10000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 3;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        };

        jsArrayRequest.setRetryPolicy(policy);
        queue.add(jsArrayRequest);
        }
        catch (JSONException e)
        {
            // HideSnackBar();
        }
    }

}
