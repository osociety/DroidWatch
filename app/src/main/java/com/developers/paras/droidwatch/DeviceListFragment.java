package com.developers.paras.droidwatch;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Objects;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;

public class DeviceListFragment extends Fragment {

    View v;
    Context con;
    String[] data[]= {{" "," "},{" "," "},{" ", " "},{" "," "}, {" "," "},{" "," "},{" "," "},{" "," "}};
    String[] data_paired[]= {{" "," "},{" "," "},{" ", " "},{" "," "}, {" "," "},{" "," "},{" "," "},{" "," "}};
    String[] arr={" "," "," "," ", " "," "," "," "};
    String[] arr_paired={" "," "," "," ", " "," "," "," "};
    int i=0;
    int j=0;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        con=context;
    }

    public DeviceListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       v= inflater.inflate(R.layout.fragment_device_list, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        final BluetoothAdapter badapter = BluetoothAdapter.getDefaultAdapter();

        FloatingActionButton fab =  v.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                i=0;
                j=0;
                for(int k=0;k<8;k++)
                {
                    arr_paired[k]=" ";
                    data_paired[k][0]=" ";
                    data_paired[k][1]=" ";

                    arr[k]=" ";
                    data[k][0]=" ";
                    data[k][1]=" ";

                }

                badapter.startDiscovery();
                getPairedDevices(badapter);
                Snackbar.make(view, "Refreshing the list Please wait", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        con.registerReceiver(mReceiver, filter);

        getPairedDevices(badapter);


    }
    public void getPairedDevices(BluetoothAdapter badapter){
        Set<BluetoothDevice> pairedDevices = badapter.getBondedDevices();


        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address

                if(i>=8)
                {
                    i=0;
                }

                data_paired[i][0]=deviceName;
                data_paired[i][1]=deviceHardwareAddress;

                arr_paired[i++]=deviceName+"\n"+deviceHardwareAddress;
            }
        }
        showListPaired();

    }

    public void showListPaired()
    {

        final FragmentManager fm = getFragmentManager();
        final FragmentTransaction ft = Objects.requireNonNull(fm).beginTransaction();


        ListView lv = v.findViewById(R.id.device_list_paired);

        ArrayAdapter<String> ad = new ArrayAdapter<>(con,R.layout.simple_list_item,arr_paired);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i1, long l) {

                if(!arr_paired[i1].equals(" "))
                {
                    SharedPreferences sp = con.getSharedPreferences("device_info",MODE_PRIVATE);
                    SharedPreferences.Editor et = sp.edit();
                    et.putString("device_name",data_paired[i1][0]);
                    et.putString("hardware_address",data_paired[i1][1]);
                    et.apply();

                        ft.replace(R.id.device_list_layout,new DeviceHomeFragment());
                        ft.commit();

                }

            }
        });

        lv.setAdapter(ad);
    }

    public void showList()
    {

        final FragmentManager fm = getFragmentManager();
        final FragmentTransaction ft = Objects.requireNonNull(fm).beginTransaction();

        ListView lv =  v.findViewById(R.id.device_list);

        ArrayAdapter<String> ad = new ArrayAdapter<>(con,R.layout.simple_list_item,arr);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i1, long l) {


                if(!arr[i1].equals(" "))
                {

                    SharedPreferences sp = con.getSharedPreferences("device_info",MODE_PRIVATE);
                    SharedPreferences.Editor et = sp.edit();
                    et.putString("device_name",data[i1][0]);
                    et.putString("hardware_address",data[i1][1]);
                    et.apply();

                        ft.replace(R.id.device_list_layout,new DeviceHomeFragment());
                        ft.commit();

                }
                }
        });

        lv.setAdapter(ad);

    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                if(j>=8)
                {
                    j=0;
                }
                    data[j][0]=deviceName;
                    data[j][1]=deviceHardwareAddress;

                arr[j++]=deviceName+"\n"+deviceHardwareAddress;

                showList();

            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

       con.unregisterReceiver(mReceiver);

    }

}


