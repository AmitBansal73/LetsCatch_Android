package net.anvisys.letscatch;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import net.anvisys.letscatch.Object.ActiveMeeting;


/**
 * A simple {@link Fragment} subclass.
 */
public class fragmentFragment extends Fragment {

   static Fragment CurrentFragment;


    public fragmentFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fragment, container, false);

        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        CurrentFragment = new ActiveFragment();
        ft.add(R.id.fragment, CurrentFragment);
        // ft.add(R.id.FragDescription,new FragmentDescription());
        // alternatively add it with a tag
        // trx.add(R.id.your_placehodler, new YourFragment(), "detail");
        ft.commit();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        ((MainActivity)getActivity()).CurrentFragment = fragmentFragment.this;
    }

    public void ShowDetails(ActiveMeeting selectedMeeting)
    {
        FragmentManager fm  = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        CurrentFragment = new ChatFragment();
        ft.replace(R.id.fragment, CurrentFragment);
        // ft.add(R.id.FragDescription,new FragmentDescription());
        // alternatively add it with a tag
        // trx.add(R.id.your_placehodler, new YourFragment(), "detail");
        ft.commit();

    }

}
