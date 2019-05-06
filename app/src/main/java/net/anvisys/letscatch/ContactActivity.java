package net.anvisys.letscatch;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import net.anvisys.letscatch.Common.DataAccess;
import net.anvisys.letscatch.Object.Contact;

import java.util.ArrayList;
import java.util.List;

public class ContactActivity extends AppCompatActivity {

    private Toolbar myToolbar;
    List<Contact> contactlist = new ArrayList<>();
    ContactListAdapter myContAdapter;
    ListView ContList;
    int PLACE_PICKER_REQUEST = 1;
    int RESULT_OK = -1;
    ProgressBar prgBar;

    Contact selectedContact;
    String [] ActiveContacts;
    ArrayList<Contact> selectedList = new ArrayList<>();

    ArrayList<String> selectedNameList = new ArrayList<>();
    ArrayList<String> selectedMobileList = new ArrayList<>();

    boolean selectionOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);

        setSupportActionBar(myToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(" LetsCatch ");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.show();


        DataAccess da = new DataAccess(this);
        contactlist =  da.GetDummyContactList();

        ContList = (ListView)findViewById(R.id.contactList);
        myContAdapter = new ContactListAdapter(ContactActivity.this);
        myContAdapter.notifyDataSetChanged();
        ContList.setAdapter(myContAdapter);


        ContList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                  Contact cont =  (Contact) ContList.getItemAtPosition(i);
                    ImageView imageChecked = (ImageView) view.findViewWithTag("Check");
                    ContactHolder Holder = (ContactHolder) view.getTag();
                    String name = Holder.txtName.getText().toString();
                    String Mobile = Holder.txtMobile.getText().toString();
                    if (imageChecked.getVisibility() == View.VISIBLE) {
                        imageChecked.setVisibility(View.INVISIBLE);
                        if (selectedNameList.contains(name)) {
                            selectedNameList.remove(name);
                            selectedMobileList.remove(Mobile);
                            selectedList.remove(cont);
                        }

                    } else {
                        imageChecked.setVisibility(View.VISIBLE);
                        if (!selectedNameList.contains(name)) {
                            selectedNameList.add(name);
                            selectedMobileList.add(Mobile);
                            selectedList.add(cont);
                        }
                    }
                    invalidateOptionsMenu();
                } catch (Exception ex) {

                }
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        if(selectedNameList.size()>0) {
            getMenuInflater().inflate(R.menu.menu_contact, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }


    private class ContactHolder
    {
        ImageView img;
        ImageView imageChecked;
        TextView txtName;
        TextView txtLocation;
        TextView txtMobile;
    }

    private class ContactListAdapter extends BaseAdapter {
        ContactHolder holder;
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


            try {


                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.contact_row_item, null);
                    ContactHolder holder = new ContactHolder();
                    holder.img = (ImageView) convertView.findViewById(R.id.userImage);
                    holder.txtName = (TextView) convertView.findViewById(R.id.userName);
                    holder.txtLocation = (TextView) convertView.findViewById(R.id.userLocation);
                    holder.txtMobile = (TextView) convertView.findViewById(R.id.editMobile);
                    holder.imageChecked = (ImageView)convertView.findViewById(R.id.isChecked);
                    holder.imageChecked.setTag("Check");
                    convertView.setTag(holder);
                }
                holder = (ContactHolder) convertView.getTag();
                final Contact cont = contactlist.get(position);
                holder.imageChecked.setVisibility(View.INVISIBLE);
                holder.txtName.setText(cont.userName);
                holder.txtMobile.setText(cont.MobileNumber);
                holder.txtLocation.setText(cont.location);

            }
            catch (Exception ex)
            {
                Toast .makeText(getApplicationContext(), "error in ContactListAdapter.view", Toast.LENGTH_LONG).show();
            }
            return convertView;

        }
    }

}
