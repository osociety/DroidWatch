package com.developers.paras.droidwatch;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static android.app.Activity.RESULT_OK;

public class NavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_ENABLE_BT =2 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.device_list_layout, new DeviceListFragment());
        ft.commit();

        // Requesting Bluetooth permission to open it
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!mBluetoothAdapter.isEnabled())
        {

            AlertDialog.Builder ad = new AlertDialog.Builder(this);
            ad.setTitle("Unable to open Bluetooth");
            ad.setMessage("You can't run this app when bluetooth is off, Turn it on first on the next screen");
            ad.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    if (!mBluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);
                    }

                }
            });
            ad.show();
        }
        else
        {
            Toast t = Toast.makeText(this, "Click on the refresh button to load the device list", Toast.LENGTH_LONG);
            t.setGravity(Gravity.CENTER,0,0);
            t.show();
        }
    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.device_list_layout, new SettingsFragment());
            ft.addToBackStack("stack");
            ft.commit();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_log) {
            startActivity(new Intent(this, ReadLogDemo.class));

        } else if (id == R.id.nav_bug_report) {


            StringBuilder log=new StringBuilder();
            try {
                Process process = Runtime.getRuntime().exec("logcat -d");
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));

                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    line = line + "\n";
                    log.append(line);
                }
            } catch (IOException e) {
                // Handle Exception
            }

            Intent email = new Intent(Intent.ACTION_SEND);
            String s ="khandelwalparas8@gmail.com";
            email.putExtra(Intent.EXTRA_EMAIL,s);

            email.putExtra(Intent.EXTRA_SUBJECT,"Bug Report for DroidWatch App");
            email.putExtra(Intent.EXTRA_TEXT,"Automated generated Bug report"+log);
            email.setType("message/rfc822");
            startActivity(email);

        }
        else if(id==R.id.nav_feedback) {
            Intent gmail = new Intent(android.content.Intent.ACTION_SEND);
            gmail.putExtra(Intent.EXTRA_EMAIL, new String[] { "khandelwalparas8@gmail.com" });
            gmail.setData(Uri.parse("khandelwalparas8@gmail.com"));
            gmail.putExtra(Intent.EXTRA_SUBJECT, "Feedback for DroidWatch");
            gmail.putExtra(Intent.EXTRA_TEXT, "write feedback here");
            gmail.setType("message/rfc822");

            startActivity(gmail);
        }
        else if (id == R.id.nav_arduino) {


            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.device_list_layout, new ArduinoFragment());
            ft.addToBackStack("stack");
            ft.commit();


        } else if (id == R.id.nav_about) {

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.device_list_layout, new AboutAppFragment());
            ft.addToBackStack("stack");
            ft.commit();


        } else if (id == R.id.nav_share) {
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = "Download Droid Watch app from play store";
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Droid Watch app by Paras khandelwal");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, "Share via"));

        } else if (id == R.id.nav_developer) {

            Intent intent =  new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/parasthekoder"));
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}


