package com.developers.paras.droidwatch;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import io.fabric.sdk.android.Fabric;

import static android.app.Activity.RESULT_OK;

public class NavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_ENABLE_BT =2 ;
    private static final int REQUEST_PERMISSIONS = 10;
    private static final String appURL = "https://play.google.com/store/apps/details?id=com.developers.paras.droidwatch";
    InterstitialAd mInterstitialAd;
    private AdView mAdview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
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
        // activate the interstitial ad
        String admob_interstitial = getResources().getString(R.string.admob_interstitial);
        MobileAds.initialize(this, admob_interstitial);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(admob_interstitial);
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        // activate the banner ad
        String admobBanner = getResources().getString(R.string.admob_banner);
        MobileAds.initialize(this, admobBanner);
        mAdview = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdview.loadAd(adRequest);
        mAdview.setAdListener(new AdListener() {
            @Override
            public void onAdClicked() {
                super.onAdClicked();
                Toast.makeText(getApplicationContext(), "You clicked on ad", Toast.LENGTH_SHORT).show();
            }
        });

        android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
        DeviceListFragment f = new DeviceListFragment();
        ft.add(R.id.device_list_layout, f);
        ft.commit();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) + ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) + ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) + ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) + ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) + ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) + ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) + ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) + ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_SMS) || shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CONTACTS) || shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH) || shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_ADMIN) || shouldShowRequestPermissionRationale(Manifest.permission.RECEIVE_SMS) || shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) || shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) || shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE) || shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
                    Toast.makeText(this, "you need to check permission", Toast.LENGTH_SHORT).show();
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_SMS, Manifest.permission.WRITE_CONTACTS, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS}, REQUEST_PERMISSIONS);
            }
        } else {

        }


        // Requesting Bluetooth permission to open it
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }


        // ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();
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
    public void onPause() {
        super.onPause();
        mAdview.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdview.resume();
    }


    @Override
    protected void onStart() {
        super.onStart();



    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Log.d("Admob interstitial", "The interstitial wasn't loaded yet.");
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


        }
        else if (id == R.id.nav_about) {

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.device_list_layout, new AboutAppFragment());
            ft.addToBackStack("stack");
            ft.commit();


        } else if (id == R.id.nav_share) {
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = "Download Droid Watch app from play store "+appURL;
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


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {


        if(requestCode==REQUEST_PERMISSIONS){
            if((grantResults.length>0)&&(grantResults[0]+grantResults[2]+grantResults[3]+grantResults[4]+grantResults[5]+grantResults[6]+grantResults[7]+grantResults[8])==PackageManager.PERMISSION_GRANTED){

                SharedPreferences sp = getSharedPreferences("permissions",MODE_PRIVATE);
                boolean grant = sp.getBoolean("grantResult",false);
                if(!grant){
                    Toast.makeText(this, "Permissions successfully granted.", Toast.LENGTH_SHORT).show();
                    SharedPreferences.Editor et = sp.edit();
                    et.putBoolean("grantResult",true);
                    et.apply();
                }

            }else {

                Toast.makeText(this, "permission was not granted", Toast.LENGTH_LONG).show();
            }
        }
        else
        {
            super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        }
    }
}


