package com.example.t5gservice;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class CpuThrottleManager {

    public Profiler profiler;
    public String LITTLE_PATH;
    public String BIG1_PATH;
    public String BIG2_PATH;

    public CpuThrottleManager(Profiler profiler) {
        LITTLE_PATH = profiler.CLOCK_LITTLE.replace("cpuinfo_cur_freq", "");
        BIG1_PATH = profiler.CLOCK_BIG1.replace("cpuinfo_cur_freq", "");
        BIG2_PATH = profiler.CLOCK_BIG2.replace("cpuinfo_cur_freq", "");

        //initialCpuSetting();
    }


    public void periodicOperation (Profiler profiler) {
        //throttlingManagement();
    }

    public void manualOperation (Profiler profiler, int cpu_id, int frequency) {
        setCPUFrequency(cpu_id, frequency);
    }

    public void resetCpuSetting () {

        String freqPath = "";
        for (int i = 0; i < 3; i++) {
            if (i == 0)
                freqPath = LITTLE_PATH;
            else if (i == 1)
                freqPath = BIG1_PATH;
            else if (i == 2)
                freqPath = BIG2_PATH;

            try {
                Process process = Runtime.getRuntime().exec("su -c cat " +  freqPath + "/scaling_available_frequencies");
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String output = reader.readLine();

                if (output != null) {
                    String[] valuesStr = output.split(" ");

                    String minFreq = valuesStr[0];
                    String maxFreq = valuesStr[valuesStr.length - 1];


                    String minFreqPath = freqPath +"scaling_min_freq";
                    String maxFreqPath = freqPath +"scaling_max_freq";
                    String min666Command = "su -c chmod 666 "+minFreqPath+"\n";
                    String max666Command = "su -c chmod 666 "+maxFreqPath+"\n";
                    String minFreqCommand = "echo " + minFreq + " > " + minFreqPath + "\n";
                    String maxFreqCommand = "echo " + maxFreq + " > " + maxFreqPath + "\nexit\n";

                    Process suProcess = Runtime.getRuntime().exec("su");
                    DataOutputStream out = new DataOutputStream(suProcess.getOutputStream());


                    out.writeBytes(min666Command);
                    out.writeBytes(max666Command);
                    out.writeBytes(minFreqCommand);
                    out.writeBytes(maxFreqCommand);
                    out.writeBytes(minFreqCommand);
                    out.flush();
                }

            } catch (Exception e) {
                // Handle exceptions here, e.g., print error message
                e.printStackTrace();
            }
        }

    }

    public void UnlockCpuSetting() {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("su -c chmod 666 /sys/devices/system/cpu/cpufreq_limit/cpufreq_max_limit\n");
            os.writeBytes("su -c echo 2995200 > /sys/devices/system/cpu/cpufreq_limit/cpufreq_max_limit\n");
            os.writeBytes("su -c chmod 444 /sys/devices/system/cpu/cpufreq_limit/cpufreq_max_limit\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();

            Log.i("CpuThrottleManager", "unlock setting done");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setCPUFrequency(int cpu_id, int frequency) {

//        UnlockCpuSetting();

        String freqPath = "";
        if (cpu_id == 0) {
            freqPath = LITTLE_PATH;
        }
        else if (cpu_id == 1) {
            freqPath = BIG1_PATH;
        }
        else if (cpu_id == 2) {
            freqPath = BIG2_PATH;
        }

        Process suProcess = null;
        try {
            suProcess = Runtime.getRuntime().exec("su");
            DataOutputStream out = new DataOutputStream(suProcess.getOutputStream());

            String minFreqPath = freqPath +"scaling_min_freq";
            String maxFreqPath = freqPath +"scaling_max_freq";

            Log.i("CpuThrottleManager", minFreqPath + " " + frequency);
            Log.i("CpuThrottleManager", maxFreqPath + " " + frequency);

            String minFreqCommand = "echo " + frequency + " > " + minFreqPath + "\n";
            String maxFreqCommand = "echo " + frequency + " > " + maxFreqPath + "\n";
            String min666Command = "su -c chmod 666 "+minFreqPath+"\n";
            String min444Command = "su -c chmod 444 "+minFreqPath+"\n";
            String max666Command = "su -c chmod 666 "+maxFreqPath+"\n";
            String max444Command = "su -c chmod 444 "+maxFreqPath+"\n";

            out.writeBytes(min666Command);
            out.writeBytes(minFreqCommand);
            out.writeBytes(min444Command);

            out.writeBytes(max666Command);
            out.writeBytes(maxFreqCommand);
            out.writeBytes(max444Command);
            out.writeBytes("exit\n");

            out.flush();
            Log.i("CpuThrottleManager", "set");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (suProcess != null) {
                try {
                    suProcess.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
