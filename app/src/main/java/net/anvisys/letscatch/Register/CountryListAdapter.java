package net.anvisys.letscatch.Register;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import net.anvisys.letscatch.Object.Country;
import net.anvisys.letscatch.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Amit Bansal on 05-03-2017.
 */
public class CountryListAdapter extends BaseAdapter {

    Context mContext;
    List<Country> countryList = new ArrayList<>();
    LayoutInflater inflater;

    public CountryListAdapter(Context context) {
        this.mContext = context;
        FillCountryData();
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return countryList.size();
    }

    @Override
    public Object getItem(int position) {
        return countryList.get(position);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        View view = convertView;
        try {


            if (view == null) {
                view =  inflater.inflate(R.layout.country_row_item, null);
            }

            TextView txtName = (TextView) view.findViewById(R.id.CountryName);
            TextView txtCode = (TextView) view.findViewById(R.id.Code);

            final Country cont = countryList.get(position);

            txtCode.setText(cont.Code);
            txtName.setText(cont.Name);


        }
        catch (Exception ex)
        {
            Toast.makeText(mContext, "error in ContactListAdapter.view", Toast.LENGTH_LONG).show();
        }
        return view;
    }



    private void FillCountryData()
    {
        Country tempCountry = new Country();
        tempCountry.Name = "India(IN)";
        tempCountry.Code = "+91";
        countryList.add(tempCountry);

        tempCountry = new Country();
        tempCountry.Name = "Australia";
        tempCountry.Code = "+61";
        countryList.add(tempCountry);

        tempCountry = new Country();
        tempCountry.Name = "USA";
        tempCountry.Code = "+1";
        countryList.add(tempCountry);

        tempCountry = new Country();
        tempCountry.Name = "UAE";
        tempCountry.Code = "+971";
        countryList.add(tempCountry);

        tempCountry = new Country();
        tempCountry.Name = "Singapore";
        tempCountry.Code = "+65";
        countryList.add(tempCountry);
    }

}
