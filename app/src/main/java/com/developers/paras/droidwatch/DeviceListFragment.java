package com.developers.paras.droidwatch;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Set;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class DeviceListFragment extends Fragment {


    private final static int BOND_NONE = 10;
    private final static int BOND_BONDED = 12;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       v= inflater.inflate(R.layout.fragment_device_list, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);



        final BluetoothAdapter badapter = BluetoothAdapter.getDefaultAdapter();

        FloatingActionButton fab = (FloatingActionButton) v.findViewById(R.id.fab);
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

       /* Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
*/
        // Showing Paired devices
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

                showListPaired();

            }
        }

    }

    public void showListPaired()
    {

        ListView lv = (ListView) v.findViewById(R.id.device_list_paired);

        ArrayAdapter<String> ad = new ArrayAdapter<String>(con,R.layout.simple_list_item,arr_paired);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i1, long l) {

                if(arr_paired[i1]!=" ")
                {
                    SharedPreferences sp = con.getSharedPreferences("device_info",MODE_PRIVATE);
                    SharedPreferences.Editor et = sp.edit();
                    et.putString("device_name",data_paired[i1][0]);
                    et.putString("hardware_address",data_paired[i1][1]);
                    et.commit();

                    BackgroundTask task = new BackgroundTask((NavigationActivity) getActivity());
                    task.execute();
                }

            }
        });

        lv.setAdapter(ad);
    }

    public void showList()
    {

        ListView lv = (ListView) v.findViewById(R.id.device_list);

        ArrayAdapter<String> ad = new ArrayAdapter<String>(con,R.layout.simple_list_item,arr);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i1, long l) {

                if(arr[i1]!=" ")
                {

                    SharedPreferences sp = con.getSharedPreferences("device_info",MODE_PRIVATE);
                    SharedPreferences.Editor et = sp.edit();
                    et.putString("device_name",data[i1][0]);
                    et.putString("hardware_address",data[i1][1]);
                    et.commit();


                    BackgroundTask task = new BackgroundTask((NavigationActivity) getActivity());
                    task.execute();
                }
                }
        });

        lv.setAdapter(ad);

    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                //discovery starts, we can show progress dialog or perform other tasks

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //discovery finishes, dismis progress dialog

            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {

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
    private class BackgroundTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog dialog;

        public BackgroundTask(NavigationActivity activity) {
            dialog = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Connecting...., please wait.");
            dialog.show();
        }

        @Override
        protected void onPostExecute(Void result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(2000);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.device_list_layout, new DeviceHomeFragment());
                ft.commit();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }

    }

}


