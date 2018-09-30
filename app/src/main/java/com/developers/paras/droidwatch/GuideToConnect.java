package com.developers.paras.droidwatch;


import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * A simple {@link Fragment} subclass.
 */
public class GuideToConnect extends Fragment {


    public GuideToConnect() {
        // Required empty public constructor
    }

    View v;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v =inflater.inflate(R.layout.fragment_guide_to_connect, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Button back = v.findViewById(R.id.backtodevicelist1);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.app.FragmentManager fragmentManager = getFragmentManager();
                final FragmentTransaction ft = fragmentManager.beginTransaction();
                DeviceListFragment f = new DeviceListFragment();
                ft.replace(R.id.device_list_layout, f);
                ft.commit();
            }
        });

    }
}
