package com.developers.paras.droidwatch;


import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
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

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import org.w3c.dom.Text;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import static android.content.ContentValues.TAG;
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
    private static final String ADMOB_ID = "ca-app-pub-9074226798924140/8780544128";

    private static final String ADMOB_ID_TEST = "ca-app-pub-3940256099942544/1033173712";

    TelephonyManager tmg=null;
    CallStateListener callstate=null;
    ConnectThread conthread = null;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v=inflater.inflate(R.layout.fragment_device_home, container, false);
        return v;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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
        final TextView device_status =  v.findViewById(R.id.device_status);
        final TextView device_data =  v.findViewById(R.id.device_data);
        Button reconnect =  v.findViewById(R.id.reconnect);


        //Trying to connect with bluetooth device
            conthread.run();


            // Setting the name on the TextView
            device_name.setText("Name : "+name);

            // Setting the Status on the TextView
            if(!conthread.getStatus())
            {
                device_status.setText("Status : Not Connected");
                Log.d("mybt","Status not connected");


            } else if(conthread.getStatus())
            {
                device_status.setText("Status : Connected");
                Log.d("mybt","Status connected");

                sendSignals(device_status,device_data);
            }


            //Closing the Connection on button click
            reconnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(con, "Reconnecting. Please wait a moment.", Toast.LENGTH_SHORT).show();
                    //******SENDING CONTROL BACK TO DEVICE LIST*************
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
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
                    device_status.setText("Status : Not Connected");

                    SharedPreferences sp = con.getSharedPreferences("device_info",MODE_PRIVATE);
                    final SharedPreferences.Editor et = sp.edit();
                    et.clear();
                    et.apply();

                    android.app.FragmentManager fragmentManager = getFragmentManager();
                    final FragmentTransaction ft = fragmentManager.beginTransaction();
                    FeedbackDialogFragment f = new FeedbackDialogFragment();
                    ft.replace(R.id.device_list_layout, f);
                    ft.commit();

                }
            });

    }
    public void sendSignals(TextView device_status, TextView device_data){
        device_status.setText("Status : Connected");
        device_data.setText("Data : Sending");

        MyBluetoothService.ConnectedThread connectedThread =
                new MyBluetoothService.ConnectedThread(conthread.getSocket());

        while (true) {
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

        device_status.setText("Status : Not Connected");
        device_data.setText("Data : Not Sending");
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

    public boolean getStatus() {
        return SUCCESS;
    }
    public void setStatus(boolean value) {
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
        BluetoothSocket tmp = null;
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
            SUCCESS=true;
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            Log.d("mybt","Closing connection due to : "+connectException);
            try {
                mmSocketFallback.close();
            } catch (IOException closeException) {
                Log.d("mybt", "Could not close the client socket due to : "+closeException, closeException);
            }
        }

        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
       

  //      manageMyConnectedSocket(mmSocket);
    }

    // Closes the client socket and causes the thread to finish.
    void cancel() {
        try {
            mmSocketFallback.close();
            SUCCESS=false;
        } catch (IOException e) {
            Log.d("mybt", "Could not close the client socket", e);
        }
    }


}