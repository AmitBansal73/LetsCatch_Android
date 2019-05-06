package net.anvisys.letscatch;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Amit Bansal on 04-01-2017.
 */
public class TabsPagerAdapter extends FragmentPagerAdapter {

    private static MapFragment mapFragment;
    private static fragmentFragment fragFragment;

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }


    @Override
    public int getItemPosition(Object object) {
       // return super.getItemPosition(object);
        return POSITION_UNCHANGED;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                if(mapFragment==null)
                {
                    mapFragment = new MapFragment();
                }
                return mapFragment;
            case 1:

                return new fragmentFragment();

            case 2:
                // Movies fragment activity
                return new ContactFragment();

        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position==0)
        {
            return "Map";

        }
        else if (position==1)
        {
            return "Meetings";

        }
        else if (position==2)
        {
            return "Contacts";
        }
        return null;
    }
}
