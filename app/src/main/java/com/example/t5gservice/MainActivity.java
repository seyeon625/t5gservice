package com.example.t5gservice;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;
import android.annotation.SuppressLint;
public class MainActivity extends AppCompatActivity {
    Intent serviceIntent;
    Profiler profiler = new Profiler();
    CpuThrottleManager cpuThrottleManager = new CpuThrottleManager(profiler);
    NetworkThrottleManager networkThrottleManager = new NetworkThrottleManager(profiler);
    boolean enableThrottle = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button startBtn = findViewById(R.id.startService) ;
        Button stopBtn = findViewById(R.id.stopService) ;
        serviceIntent = new Intent(this, ProfilingService.class);




        // Start service
        startBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                startService();
                Toast.makeText(getApplicationContext(), "Start service", Toast.LENGTH_SHORT).show();
            }
        });

        // Stop service
        stopBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                stopService();
                Toast.makeText(getApplicationContext(), "Stop service", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void startService(){

        serviceIntent = new Intent(this, ProfilingService.class);
        startService(serviceIntent);
    }

    public void stopService(){
        serviceIntent = new Intent(this, ProfilingService.class);
        stopService(serviceIntent);

    }



    public void onClick(View v) {

        Log.i("button", "Button: Clicked");
        int id = v.getId();
        if (id == R.id.set_little) {
            Log.i("cputhrottle", "Button: Enable little core throttling");
            List<Integer> cpuFrequencies = profiler.AVAIL_LITTLE_FREQ;
            CharSequence[] freqs = new CharSequence[cpuFrequencies.size()];
            for (int i = 0; i < cpuFrequencies.size(); i++) {
                freqs[i] = String.valueOf(cpuFrequencies.get(i));
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle("Choose a frequency for the little core")
                    .setSingleChoiceItems(freqs, -1, null)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                            if (selectedPosition != -1) {
                                // Perform action when an item is selected
                                int selectedFreq = cpuFrequencies.get(selectedPosition);
                                // Set the CPU frequency for the little core
                                cpuThrottleManager.setCPUFrequency(0, selectedFreq);
                                Log.i("cputhrottle", "button ok");
                            }
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
//                break;
        } else if (id == R.id.set_big1) {
            Log.i("cputhrottle", "Button: Enable big1 core throttling");
            List<Integer> cpuFrequencies = profiler.AVAIL_BIG1_FREQ;
            CharSequence[] freqs = new CharSequence[cpuFrequencies.size()];
            for (int i = 0; i < cpuFrequencies.size(); i++) {
                freqs[i] = String.valueOf(cpuFrequencies.get(i));
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle("Choose a frequency for the big1 core")
                    .setSingleChoiceItems(freqs, -1, null)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                            if (selectedPosition != -1) {
                                // Perform action when an item is selected
                                int selectedFreq = cpuFrequencies.get(selectedPosition);
                                // Set the CPU frequency for the little core
                                cpuThrottleManager.setCPUFrequency(1, selectedFreq);
                            }
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
//                break;
        } else if (id == R.id.set_big2) {
            Log.i("cputhrottle", "Button: Enable big2 core throttling");
            List<Integer> cpuFrequencies = profiler.AVAIL_BIG2_FREQ;
            CharSequence[] freqs = new CharSequence[cpuFrequencies.size()];
            for (int i = 0; i < cpuFrequencies.size(); i++) {
                freqs[i] = String.valueOf(cpuFrequencies.get(i));
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle("Choose a frequency for the big2 core")
                    .setSingleChoiceItems(freqs, -1, null)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                            if (selectedPosition != -1) {
                                // Perform action when an item is selected
                                int selectedFreq = cpuFrequencies.get(selectedPosition);
                                // Set the CPU frequency for the little core
                                cpuThrottleManager.setCPUFrequency(2, selectedFreq);
                            }
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
//                break;
        } else if (id == R.id.resetcpu) {
            cpuThrottleManager.resetCpuSetting();
            Log.i("cputhrottle", "Button: Disabled throttling");
        } else if (id == R.id.runthrottle) {
            EditText netCommand = findViewById(R.id.net_command);
            String userInput = netCommand.getText().toString();
            int userInteger;
            try {
                userInteger = Integer.parseInt(userInput);
                enableThrottle = true;

                // Alert Dialog for Uplink or Downlink
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Select Throttling Direction")
                        .setItems(new String[]{"Uplink", "Downlink"}, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // The 'which' argument contains the index position
                                // of the selected item
                                switch (which) {
                                    case 0: // Uplink
                                        networkThrottleManager.manualOperation(userInput, false);
                                        break;
                                    case 1: // Downlink
                                        networkThrottleManager.manualOperation(userInput, true);
                                        break;
                                }
                                networkThrottleManager.throttlingManagement();
                                Log.i ("netthrottle", "Button: Enabled throttling");
                            }
                        });
                builder.create().show();
                Log.i ("netthrottle", "Button: Enabled throttling");
            } catch (NumberFormatException e) {
                // Handle the exception here
                Toast.makeText(getApplicationContext(), "Only Integer Value!", Toast.LENGTH_LONG).show();
            }

        } else if(id == R.id.resetnet) {
            enableThrottle = false;
            networkThrottleManager.cancelDownlinkThrottling();
            networkThrottleManager.cancelUplinkThrottling();
            Log.i ("netthrottle", "Button: Disabled throttling");

        }

    }
}