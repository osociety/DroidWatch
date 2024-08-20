package com.developers.paras.droidwatch;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import android.widget.TextView;


public class ReadLogDemo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_log_demo);

        try {
            Process process = Runtime.getRuntime().exec("logcat -d");
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            StringBuilder log=new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                line = line + "\n";
                log.append(line);
            }
            TextView tv = findViewById(R.id.textView1);
            tv.setText(log.toString());
        } catch (IOException e) {
            // Handle Exception
        }
    }

}