package com.developers.paras.droidwatch;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.internal.SnackbarContentLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {


    View v;
    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v= inflater.inflate(R.layout.fragment_settings, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Switch s1 = (Switch) v.findViewById(R.id.noti);
        s1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

               if(s1.isChecked())
               {
                   Snackbar.make(s1,"You have turned On notifications", Snackbar.LENGTH_LONG).setAction("Action",null).show();
                   s1.setChecked(true);
               }
               else
               {

                   s1.setChecked(false);
                   Snackbar.make(s1,"You have turned Off notifications", Snackbar.LENGTH_LONG).setAction("Action",null).show();
               }
            }
        });

    }
}
