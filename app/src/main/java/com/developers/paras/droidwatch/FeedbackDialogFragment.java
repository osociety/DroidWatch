package com.developers.paras.droidwatch;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
public class FeedbackDialogFragment extends Fragment {
    View v;
    Context con;
    AdView mAdview;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        con = context;

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.feedback_dialog, container,false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final FragmentManager fm = getFragmentManager();

        // activate the banner ad
        mAdview = v.findViewById(R.id.feedbackAdView);
        AdRequest adRequest = new AdRequest.Builder()
//                .addTestDevice("038E382011FDA83824D4A2F832132730")
                .build();
        mAdview.loadAd(adRequest);


        Button backToList = v.findViewById(R.id.backtodevicelist);
        backToList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(fm!=null){
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.replace(R.id.device_list_layout, new DeviceListFragment());
                    ft.commit();
                }
            }
        });

        final SeekBar seekBar = v.findViewById(R.id.seekbar);

        Button successfulYes = v.findViewById(R.id.successful_yes);
        successfulYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String appPackageName = con.getPackageName(); // getPackageName() from Context or Activity object
                if(seekBar.getProgress()>4){
                    try {
                        Toast.makeText(con, "Please rate my app if you loved working with this app and project.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                    }
                }
                else{
                    Toast.makeText(con, "If you still face issue then send feedback to me from Navigation Drawer", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Button successfulNo = v.findViewById(R.id.successful_no);
        successfulNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fm!=null){
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.replace(R.id.device_list_layout, new GuideToConnect());
                    ft.commit();
                }
            }
        });
    }

}
