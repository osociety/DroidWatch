package com.developers.paras.droidwatch;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.UUID;
import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class DeviceHomeFragment extends Fragment {

    private int CALL_STATE_RING = 0;
    private int MESSAGE_STATE = 0;
    private String INCOMING_NUMBER = null;
    private String INCOMING_MESSAGE_BODY =null;
    private String INCOMING_MESSAGE_NUMBER =null;

    TelephonyManager tmg=null;
    CallStateListener callstate=null;
    ConnectThread conthread = null;
    String deviceLabel = "Device : ";
    String data_sending = "Data : Sending";
    String data_not_sending = "Data : Not Sending";
    String status_not_connected = "Status : Not Connected";
    String status_connected = "Status : Connected";
    TextView device_data;
    TextView device_status;

    View v;
    Context con=null;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
         con = context;
    }


    public DeviceHomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v=inflater.inflate(R.layout.fragment_device_home, container, false);
        return v;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

         final FragmentManager fm = getFragmentManager();

        SharedPreferences sp = con.getSharedPreferences("device_info",MODE_PRIVATE);
        final String address = sp.getString("hardware_address",null);
        final String name = sp.getString("device_name",null);

        final BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);

        //Trying to create tunnel bluetooth device
        conthread = new ConnectThread(device);


        TelephonyManager  tm = (TelephonyManager) con.getSystemService(Context.TELEPHONY_SERVICE);
           tmg=tm;
        CallStateListener callStateListener = new CallStateListener();
        callstate=callStateListener;
        if (tm != null) {
            tm.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
        else{
            Toast.makeText(con, "Unable to connect to telephone manager", Toast.LENGTH_SHORT).show();
        }

        IntentFilter filter = new IntentFilter();

        filter.addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);

        con.registerReceiver(msgReceiver, filter);



        final Button disconnect =  v.findViewById(R.id.disconnect);
        final TextView device_name =  v.findViewById(R.id.device_name);
        device_status = v.findViewById(R.id.device_status);
        device_data = v.findViewById(R.id.device_data);
        Button reconnect =  v.findViewById(R.id.reconnect);


        //Trying to connect with bluetooth device
            conthread.run();

           String deviceName = deviceLabel+name;
            // Setting the name on the TextView
            device_name.setText(deviceName);

            // Setting the Status on the TextView
            if(!conthread.getStatus())
            {
                device_status.setText(status_not_connected);
                device_data.setText(data_not_sending);
                Log.d("mybt","Status not connected");


            } else if(conthread.getStatus())
            {
                device_status.setText(status_connected);
                device_data.setText(data_sending);
                Log.d("mybt","Status connected");
                Log.d("mybt","lets start");
                sendSignals();
            }

        final FragmentTransaction ft = Objects.requireNonNull(fm).beginTransaction();

        //Closing the Connection on button click
            reconnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(con, "Reconnecting. Please wait a moment.", Toast.LENGTH_SHORT).show();
                    //******SENDING CONTROL BACK TO DEVICE LIST*************

                    ft.replace(R.id.device_list_layout, new DeviceHomeFragment());
                    ft.commit();
                }
            });

            //Closing the Connection on button click
            disconnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    conthread.cancel();
                    conthread.setStatus(false);
                    device_status.setText(status_not_connected);


                    SharedPreferences sp = con.getSharedPreferences("device_info",MODE_PRIVATE);
                    final SharedPreferences.Editor et = sp.edit();
                    et.clear();
                    et.apply();

                    ft.replace(R.id.device_list_layout, new FeedbackDialogFragment());
                    ft.commit();

                }
            });

    }
    public void sendSignals(){
        device_status.setText(status_connected);
        device_data.setText(data_sending);
        Log.d("mybt","inside send signal");
        MyBluetoothService.ConnectedThread connectedThread =
                new MyBluetoothService.ConnectedThread(conthread.getSocket());

        while (true) {
            Log.d("mybt","iterations going on");
            if(CALL_STATE_RING==1)
            {
                Log.d("mybt","Phone is ringing");
            }

            byte[] byte_data ;
            //Retrieving current time
            java.util.Calendar c = java.util.Calendar.getInstance();
            int hh = c.get(java.util.Calendar.HOUR);
            int mm = c.get(java.util.Calendar.MINUTE);
            int ss = c.get(java.util.Calendar.SECOND);
            int pm= c.get(java.util.Calendar.AM_PM);
            if(hh==0&&pm==1)
            {
                hh=12;
            }
            Log.d("mybt","PM = "+pm);
            //Retrieving call information
            String incomingName=getContactName(con,INCOMING_NUMBER);
            //sending data to the watch
            String dataUrl = hh+":"+mm+":"+ss+":"+pm+"\0"+CALL_STATE_RING+"\0"+incomingName+"\0"+INCOMING_NUMBER+"\0"+MESSAGE_STATE+"\0"+INCOMING_MESSAGE_NUMBER+"\0"+INCOMING_MESSAGE_BODY+"\0";
            Log.d("mybt","Call state : "+CALL_STATE_RING+" Name : "+incomingName+" Num : "+INCOMING_NUMBER);
            Log.d("mybt","Message received :"+MESSAGE_STATE+" "+INCOMING_MESSAGE_NUMBER+" "+INCOMING_MESSAGE_BODY);
            byte_data = dataUrl.getBytes();
            connectedThread.write(byte_data);
            if(MESSAGE_STATE==1)
            {
                MESSAGE_STATE=0;
                INCOMING_MESSAGE_NUMBER=null;
                INCOMING_MESSAGE_BODY=null;
            }


            if(!connectedThread.getWriteSocket().isConnected())
            {
                Log.d("mybt","Disconnected during transmission");
                break;
            }

        }
        CALL_STATE_RING = 0;
        MESSAGE_STATE=0;
        INCOMING_NUMBER = null;
        INCOMING_MESSAGE_BODY =null;
        INCOMING_MESSAGE_NUMBER =null;


        device_status.setText(status_not_connected);

        device_data.setText(data_not_sending);
    }


    public static String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = "Unknown";
        if(cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if(!cursor.isClosed()) {
            cursor.close();
        }

        return contactName;
    }

    /**
     * Listener to detect incoming calls.
     */
    private class CallStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    // called when someone is ringing to this phone
                          CALL_STATE_RING=1;
                              INCOMING_NUMBER=incomingNumber;
                    Toast.makeText(con, "Phone is ringing", Toast.LENGTH_SHORT).show();

                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    CALL_STATE_RING =0;
                    break;
            }
        }
    }

    private final BroadcastReceiver msgReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
                for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                    INCOMING_MESSAGE_BODY= smsMessage.getMessageBody();
                    INCOMING_MESSAGE_NUMBER = smsMessage.getOriginatingAddress();
                    MESSAGE_STATE=1;
                    Toast.makeText(context,"Message state "+MESSAGE_STATE, Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        tmg.listen(callstate, PhoneStateListener.LISTEN_NONE);
        con.unregisterReceiver(msgReceiver);

    }
}


class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    private BluetoothSocket mmSocketFallback;
    private static boolean SUCCESS = false;

    boolean getStatus() {
        return SUCCESS;
    }
    void setStatus(boolean value) {
        SUCCESS=value;
    }

    BluetoothSocket getSocket() {
        return mmSocketFallback;
    }


    ConnectThread(BluetoothDevice device) {
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        BluetoothSocket tmp = null;

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            // careful for attacks before connection is insecure
            tmp = device.createRfcommSocketToServiceRecord(UUID.randomUUID());


        } catch (IOException e) {
            Log.d("mybt", "Socket's create() method failed", e);
        }
        mmSocket = tmp;
        mmSocketFallback = tmp;
    }

    public void run() {
        // Cancel discovery because it otherwise slows down the connection.
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.cancelDiscovery();
        Log.d("mybt","discovery cancelled");
        BluetoothSocket tmp;
        tmp=mmSocket;
        try
        {
            Class<?> clazz = tmp.getRemoteDevice().getClass();
            Class<?>[] paramTypes = new Class<?>[] {Integer.TYPE};
            Method m = clazz.getMethod("createRfcommSocket", paramTypes);
            Object[] params = new Object[] {1};

            mmSocketFallback = (BluetoothSocket) m.invoke(tmp.getRemoteDevice(), params);
        }
        catch (Exception e)
        {
           Log.d("mybt","Fall failed due to : "+ e.getMessage());
        }
        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mmSocketFallback.connect();
            setStatus(true);
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            Log.d("mybt","Closing connection due to : "+connectException);
            try {
                mmSocketFallback.close();
            } catch (IOException closeException) {
                Log.d("mybt", "Could not close the client socket due to : "+closeException, closeException);
            }
        }
    }

    void cancel() {
        try {
            mmSocketFallback.close();
            SUCCESS=false;
        } catch (IOException e) {
            Log.d("mybt", "Could not close the client socket", e);
        }
    }


}