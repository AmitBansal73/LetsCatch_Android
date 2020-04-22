package net.anvisys.letscatch;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import net.anvisys.letscatch.Common.ImageServer;
import net.anvisys.letscatch.Common.UTILITY;
import net.anvisys.letscatch.Object.APP_CONST;
import net.anvisys.letscatch.Object.APP_VARIABLES;
import net.anvisys.letscatch.Object.ActiveMeeting;
import net.anvisys.letscatch.Object.ActiveMeetingGroup;
import net.anvisys.letscatch.Object.Message;
import net.anvisys.letscatch.Shape.OvalImageView;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class ActiveFragment extends Fragment {

    static ListView ActiveList;
    static TextView noDataView;
    static ActiveListAdapter activeMeetingAdapter;
    private Menu menu;
    ArrayList<String> aselectedNameList = new ArrayList<>();
    ArrayList<String> aselectedMobileList = new ArrayList<>();
    ArrayList<ActiveMeeting> selectedMeetingList;

    public ActiveFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_active, container, false);

        try {
            ActiveList = (ListView) view.findViewById(R.id.activeList);
            noDataView = (TextView) view.findViewById(R.id.noDataView);
            if (ActiveMeetingGroup.GetInstance(getContext()).RunningMeetings.size() == 0) {
                noDataView.setVisibility(View.VISIBLE);
                ActiveList.setVisibility(View.GONE);
                String text = "<p>No Active Tracking.<br> Select a contact from Contact Tab to start tracking</p>";
                Spanned result = Html.fromHtml(text);
                noDataView.setText(result);
            }
            else {
                noDataView.setVisibility(View.GONE);
                ActiveList.setVisibility(View.VISIBLE);
            }

            activeMeetingAdapter = new ActiveListAdapter(getActivity());
            activeMeetingAdapter.notifyDataSetChanged();
            ActiveList.setAdapter(activeMeetingAdapter);
            setHasOptionsMenu(true);

            selectedMeetingList = new ArrayList<>();

            ActiveList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    ActiveMeeting selectedMeeting = (ActiveMeeting) ActiveMeetingGroup.GetInstance(getContext()).RunningMeetings.values().toArray()[i];
                    Bundle args = new Bundle();
                    args.putParcelable("Meeting", selectedMeeting);
                    ChatFragment chat = new ChatFragment();
                    chat.setArguments(args);

                    FragmentTransaction ft=   getActivity().getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.fragment,chat);
                    ft.addToBackStack(null);
                    ft.commit();
                }
            });
        }
        catch (Exception ex)
        {
            Toast.makeText(getContext(), "Error in creating Active Meeting View",Toast.LENGTH_LONG).show();
        }



        return view;
    }

    private void SetListView()
    {

    }

    private class ActiveListHolder
    {
        OvalImageView Image;
        ImageView imageChecked;
        TextView txtName;
        TextView txtStatus;
        TextView txtDistance;
        TextView txtTime;
        TextView txtStartTime;
        TextView txtUpdateTime;
        TextView msgCount;
        String Mobile="";
    }


    private class ActiveListAdapter extends BaseAdapter {

        LayoutInflater inflater;
        ActiveListHolder listHolder;

        public ActiveListAdapter(Activity activity) {
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return ActiveMeetingGroup.GetInstance(getContext()).RunningMeetings.size();
        }

        @Override
        public Object getItem(int position) {
            return (ActiveMeeting) ActiveMeetingGroup.GetInstance(getContext()).RunningMeetings.values().toArray()[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            try {


                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.active_row_item, null);

               listHolder = new ActiveListHolder();

                listHolder.Image = (OvalImageView)convertView.findViewById(R.id.markerImage);
                listHolder.imageChecked = (ImageView)convertView.findViewById(R.id.isChecked);
                listHolder.imageChecked.setTag("Check");
                listHolder.txtName = (TextView) convertView.findViewById(R.id.userName);
                listHolder.txtStatus = (TextView) convertView.findViewById(R.id.userStatus);
                listHolder.txtDistance = (TextView) convertView.findViewById(R.id.userDistance);
                listHolder.txtTime = (TextView) convertView.findViewById(R.id.userTime);
                listHolder.txtStartTime = (TextView) convertView.findViewById(R.id.startTime);
                listHolder.txtUpdateTime = (TextView) convertView.findViewById(R.id.updateTime);
                listHolder.msgCount = (TextView) convertView.findViewById(R.id.msgCount);
                    convertView.setTag(listHolder);
                }
                ActiveMeeting meeting = (ActiveMeeting) ActiveMeetingGroup.GetInstance(getContext()).RunningMeetings.values().toArray()[position];

                listHolder = (ActiveListHolder) convertView.getTag();
              /*  Bitmap bmp = ImageServer.GetImageBitmap(meeting.DESTINATION_MOBILE_NO, getContext());
                if(bmp!=null) {
                    listHolder.Image.setImageBitmap(bmp);
                }
                */

                String url1 = APP_CONST.IMAGE_URL + meeting.DESTINATION_USER_ID +".png";
                Picasso.with(getContext()).load(url1).error(R.drawable.user_image).into(listHolder.Image);

                listHolder.imageChecked.setVisibility(View.INVISIBLE);
                listHolder.txtName.setText(meeting.DESTINATION_NAME);
                listHolder.txtStatus.setText(meeting.MEETING_STATUS);
                listHolder.txtDistance.setText(" Dist: "+ meeting.DIST_To_GO);
                listHolder.txtTime.setText(" Expected time :"+meeting.TIME_TO_GO);
                listHolder.txtStartTime.setText("Started At: "+ UTILITY.ChangeFormat(meeting.START_TIME));
                listHolder.txtUpdateTime.setText("Last Updated At: "+UTILITY.ChangeFormat(meeting.UPDATE_TIME));
                listHolder.Mobile = meeting.DESTINATION_MOBILE_NO;
                if(meeting.newMessage>0)
                {
                    listHolder.msgCount.setVisibility(View.VISIBLE);
                    listHolder.msgCount.setText(Integer.toString(meeting.newMessage));
                }
                else
                {
                    listHolder.msgCount.setVisibility(View.GONE);
                }
            }
            catch (Exception ex)
            {
                Toast .makeText(getContext(), "error in ActiveListAdapter.view", Toast.LENGTH_LONG).show();
            }
            return convertView;

        }
    }


    public void DataChanged()
    {
        try {
            if (ActiveMeetingGroup.GetInstance(getContext()).RunningMeetings.size() == 0) {
                noDataView.setVisibility(View.VISIBLE);
                ActiveList.setVisibility(View.GONE);
            } else {
                noDataView.setVisibility(View.GONE);
                ActiveList.setVisibility(View.VISIBLE);
                activeMeetingAdapter.notifyDataSetChanged();
            }
        }
        catch (Exception ex)
        {

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
                    //RemindSelectedMeeting();
                }
            }

            else if (id == R.id.action_Stop) {
                StopSelectedMeetings();
            }

            else if (id == R.id.action_StopAll) {
                if(APP_VARIABLES.NETWORK_STATUS==false)
                {
                    ((MainActivity)getActivity()).ShowSnackBar("Network unavailable!");

                }
                else {
                    StopAllMeetings();
                }

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
        try {
            ((fragmentFragment)getParentFragment()).CurrentFragment = ActiveFragment.this;
            menu.clear();
            if (selectedMeetingList.size()==0)
            {
                inflater.inflate(R.menu.menu_meeting_no_select, menu);
            }

        }
        catch (Exception ex)
        {
            Toast.makeText(getContext(),"Error changing Menu",Toast.LENGTH_LONG).show();
        }

    }


    private void StopSelectedMeetings()
    {
        String strName ="<p>Following Meetings will be Stopped</p></br>";
        for (ActiveMeeting meeting : selectedMeetingList
                ) {
            strName = strName +"<p>"+ meeting.DESTINATION_NAME + "</p></br>";
        }
        strName = strName.substring(0, strName.length()-6);
        Spanned result = Html.fromHtml(strName);
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("STOP");
        dialog.setMessage(result);
        dialog.setCancelable(false);
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedMeetingList.clear();
                getActivity().invalidateOptionsMenu();
            }
        });
        dialog.setPositiveButton("Stop", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (ActiveMeeting meeting : selectedMeetingList
                        ) {
                    meeting.sendNotification(getContext(), meeting.DESTINATION_MOBILE_NO, Message.STOP_TRACKING);
                    //ActiveMeetingGroup.GetInstance(getContext()).StopMeetingByMobile(meeting.DESTINATION_MOBILE_NO, getContext());
                }

                selectedMeetingList.clear();
                getActivity().invalidateOptionsMenu();

            }
        });
        AlertDialog alert = dialog.create();
        alert.show();
    }

    private void StopAllMeetings()
    {
        String strName ="<p>All Running Meetings will be Stopped</p></br>";

        Spanned result = Html.fromHtml(strName);
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("STOP ALL");
        dialog.setMessage(result);
        dialog.setCancelable(false);
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(selectedMeetingList.size()>0) {
                    selectedMeetingList.clear();
                    getActivity().invalidateOptionsMenu();
                }
            }
        });
        dialog.setPositiveButton("Stop", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(selectedMeetingList.size()>0) {
                    selectedMeetingList.clear();
                    getActivity().invalidateOptionsMenu();
                }
                activeMeetingAdapter.notifyDataSetChanged();
                ActiveMeetingGroup.GetInstance(getContext()).StopAllRunningMeetings(getContext());


            }
        });
        AlertDialog alert = dialog.create();
        alert.show();
    }
    private void PauseSelectedMeeting()
    {
        String strName ="<p>Will Not send location and Route</p></br>";

        for (ActiveMeeting meeting : selectedMeetingList
                ) {
            strName = strName +"<p>"+ meeting.DESTINATION_NAME + "</p></br>";
        }

        strName = strName.substring(0, strName.length()-6);
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

                for (ActiveMeeting meeting : selectedMeetingList
                        ) {
                         ActiveMeetingGroup.GetInstance(getContext()).PauseMeetingByMobile(meeting.DESTINATION_MOBILE_NO, getContext());
                }

                selectedMeetingList.clear();
                activeMeetingAdapter.notifyDataSetChanged();
                getActivity().invalidateOptionsMenu();

            }
        });
        AlertDialog alert = dialog.create();
        alert.show();
    }

    private void ResumeSelectedMeeting()
    {
        String strName ="<p>Following Meetings will be Resumed</p></br>";
        for (ActiveMeeting meeting : selectedMeetingList
                ) {
            strName = strName +"<p>"+ meeting.DESTINATION_NAME + "</p></br>";
        }

        strName = strName.substring(0, strName.length()-6);
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
                            for (ActiveMeeting meeting : selectedMeetingList
                                    ) {
                                ActiveMeetingGroup.GetInstance(getContext()).ResumeMeetingByMobile(meeting.DESTINATION_MOBILE_NO, getContext());
                            }
                            selectedMeetingList.clear();
                            activeMeetingAdapter.notifyDataSetChanged();
                            getActivity().invalidateOptionsMenu();

                        } catch (Exception ex) {
                            Toast.makeText(getContext(), "Error in Pause", Toast.LENGTH_LONG).show();
                        }

                    }
                }

        );
            AlertDialog alert = dialog.create();
        alert.show();
        }


    private void RemindSelectedMeeting()
    {
        String strName ="<p>Following Meetings will be Resumed</p></br>";
        for (ActiveMeeting meeting : selectedMeetingList
                ) {
            strName = strName +"<p>"+ meeting.DESTINATION_NAME + "</p></br>";
        }

        strName = strName.substring(0, strName.length()-6);
        Spanned result = Html.fromHtml(strName);
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("Reminder");
        dialog.setMessage(result);
        dialog.setCancelable(false);
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog.setPositiveButton("Remind", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                try {


                    for (ActiveMeeting meeting : selectedMeetingList
                            ) {
                        meeting.sendNotification(getContext(), meeting.DESTINATION_MOBILE_NO, Message.SEND_LOCATION);

                    }
                    selectedMeetingList.clear();
                    activeMeetingAdapter.notifyDataSetChanged();
                    getActivity().invalidateOptionsMenu();



                } catch (Exception ex) {
                    Toast.makeText(getContext(), "Error in Pause", Toast.LENGTH_LONG).show();
                }

            }
        });
        AlertDialog alert = dialog.create();
        alert.show();
    }
    }
