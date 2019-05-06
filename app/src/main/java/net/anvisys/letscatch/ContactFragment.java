package net.anvisys.letscatch;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.ContactsContract;
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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.anvisys.letscatch.Common.DataAccess;
import net.anvisys.letscatch.Common.ImageServer;
import net.anvisys.letscatch.Object.APP_CONST;
import net.anvisys.letscatch.Object.APP_VARIABLES;

import net.anvisys.letscatch.Object.ActiveMeetingGroup;
import net.anvisys.letscatch.Object.Contact;

import java.util.ArrayList;
import java.util.List;

import net.anvisys.letscatch.Shape.OvalImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContactFragment extends Fragment {

   static List<Contact> contactlist = new ArrayList<>();
   static ContactListAdapter myContAdapter;
    static ListView ContList;
    static TextView noDataView;
   Contact selectedContact;
   private long prevTime = 0;

    public ContactFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_contact, container, false);
        noDataView = (TextView)view.findViewById(R.id.noDataView);
        ContList = (ListView) view.findViewById(R.id.contactList);
        try {

            DataAccess da = new DataAccess(getContext());
            da.open();
            contactlist = da.getAllContact();
            //contactlist = new ArrayList<>();
            da.close();
            if (contactlist.size() == 0) {
                noDataView.setVisibility(View.VISIBLE);
                ContList.setVisibility(View.GONE);
                String text = "<p>Wait for few minutes till we sync.<br> If none of your contact is on LetsCatch,<br> Please invite your friends using Invite friend</p>";
                Spanned result = Html.fromHtml(text);
                noDataView.setText(result);

            } else
            {
                noDataView.setVisibility(View.GONE);
                ContList.setVisibility(View.VISIBLE);
            }

                myContAdapter = new ContactListAdapter(getActivity());
                myContAdapter.notifyDataSetChanged();
                ContList.setAdapter(myContAdapter);

                ContList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        try {

                            if (APP_VARIABLES.NETWORK_STATUS == false) {
                                ((MainActivity) getActivity()).ShowSnackBar("Network unavailable!");
                                return;
                            }
                            selectedContact = (Contact) ContList.getItemAtPosition(position);

                            if(ActiveMeetingGroup.GetInstance(getContext()).RunningMeetings.containsKey(selectedContact.MobileNumber))
                            {
                                String status = ActiveMeetingGroup.GetInstance(getContext()).RunningMeetings.get(selectedContact.MobileNumber).MEETING_STATUS;

                                if(status.matches(APP_CONST.MEETING_STATUS_SENDING_LOCATION)|| status.matches(APP_CONST.MEETING_STATUS_SHARING_LOCATION))
                                {
                                    Toast.makeText(getContext(), "Already Tracking", Toast.LENGTH_LONG).show();
                                }
                                else
                                {
                                    ShareLocationDialog();
                                }
                            }
                            else
                            {
                                ShareLocationDialog();
                            }

                        } catch (Exception ex) {
                            Toast.makeText(getContext(), "Error starting Activity", Toast.LENGTH_LONG).show();
                        }


                    }
                });

                setHasOptionsMenu(true);

        }
            catch(Exception ex)
            {
                Toast.makeText(getContext(), "Error in creating Contact List", Toast.LENGTH_LONG).show();
            }

                return view;

    }

    public void NewContact(Context context)
    {
        try {

            DataAccess da = new DataAccess(context);
            da.open();
            contactlist =  da.getAllContact();
            da.close();
            if (contactlist.size() == 0) {
                noDataView.setVisibility(View.VISIBLE);
                ContList.setVisibility(View.GONE);

            } else
            {
                noDataView.setVisibility(View.GONE);
                ContList.setVisibility(View.VISIBLE);
                myContAdapter.notifyDataSetChanged();
            }
        }
        catch (Exception ex)
        {

        }
    }

    public void Refresh()
    {
        try {
            myContAdapter.notifyDataSetChanged();
        }
        catch (Exception ex)
        {

        }
    }

    private void ShareLocationDialog()
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("Share Location With:");
        dialog.setMessage(selectedContact.userName);
        dialog.setCancelable(false);
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog.setPositiveButton("Share Location", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (IsFirstClick()) {

                    ActiveMeetingGroup.GetInstance(getContext()).InitiateMeetingByContact(selectedContact, getContext());
                }

            }
        });
        AlertDialog alert = dialog.create();
        alert.show();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            int id = item.getItemId();
            if (id == R.id.action_New) {
                if(IsFirstClick()) {
                    Intent intent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
                    startActivity(intent);
                }
            }

            if (id == R.id.Sync_Contact) {
                if(IsFirstClick()) {
                    Intent SyncIntent = new Intent(getContext(), ContactSyncService.class);
                    getContext().startService(SyncIntent);
                }
            }
            if (id == R.id.Clear_Contact) {
                if(IsFirstClick()) {
                    ClearAllDialog();
                }
            }


        }
        catch (Exception ex)
        {

        }
        return true;
    }

    private class ContactListAdapter extends BaseAdapter {

        LayoutInflater inflater;

        public ContactListAdapter(Activity activity) {
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return contactlist.size();
        }

        @Override
        public Object getItem(int position) {
            return contactlist.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = convertView;
            try {


                if (view == null) {
                    view = inflater.inflate(R.layout.contact_row_item, null);
                }
                OvalImageView img = (OvalImageView) view.findViewById(R.id.userImage);
                TextView txtName = (TextView) view.findViewById(R.id.userName);
                TextView txtLocation = (TextView) view.findViewById(R.id.userLocation);
                TextView txtMobile = (TextView) view.findViewById(R.id.editMobile);
                TextView txtStatus = (TextView) view.findViewById(R.id.contStatus);
                final Contact cont = contactlist.get(position);
                String Status;
                if(ActiveMeetingGroup.GetInstance(getContext()).RunningMeetings.containsKey(cont.MobileNumber))
                {
                    Status=  ActiveMeetingGroup.GetInstance(getContext()).RunningMeetings.get(cont.MobileNumber).MEETING_STATUS;
                    view.setBackgroundColor(Color.argb(60,150,150,150));
                }
                else
                {
                    Status = cont.Status;
                    view.setBackgroundColor(Color.argb(100,224,224,224));
                }

                txtStatus.setText(Status);
                txtName.setText(cont.userName);
                txtMobile.setText(cont.MobileNumber);
                txtLocation.setText(cont.location);
                if(!cont.strImage.matches("")) {
                    Bitmap bitmap = ImageServer.getBitmapFromString(cont.strImage,getContext());
                    img.setImageBitmap(bitmap);
                }

            }
            catch (Exception ex)
            {
                Toast .makeText(getContext(), "error in ContactListAdapter.view", Toast.LENGTH_LONG).show();
            }
            return view;

        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        ((MainActivity)getActivity()).CurrentFragment = ContactFragment.this;
        menu.clear();
        inflater.inflate(R.menu.menu_contact_fragment, menu);
    }

    private void ClearAllDialog()
    {

        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("Clear All");
        dialog.setMessage("Remove All Contact");
        dialog.setCancelable(false);
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog.setPositiveButton("Clear", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                    if(IsFirstClick()) {
                        try {
                            DataAccess da = new DataAccess(getContext());
                            da.open();
                            da.deleteAllContact();
                            da.close();
                            contactlist.clear();
                            myContAdapter.notifyDataSetChanged();
                        } catch (Exception ex) {
                            Toast.makeText(getContext(), "Error in Pause", Toast.LENGTH_LONG).show();
                        }
                    }

            }
        });
        AlertDialog alert = dialog.create();
        alert.show();
    }

  private boolean  IsFirstClick()
    {

        long time = SystemClock.currentThreadTimeMillis();

        if (prevTime == 0) {
            prevTime = SystemClock.currentThreadTimeMillis();
            return true;
        }
        else {

            if (time - prevTime > 2000) {
                prevTime = time;
                return true;
            } else
            {
                prevTime = time;
                return false;
            }
        }
    }
}
