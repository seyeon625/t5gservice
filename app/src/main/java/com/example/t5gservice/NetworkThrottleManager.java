package com.example.t5gservice;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class NetworkThrottleManager {

    Profiler profiler;
    public String NET_INTERFACE = "";

    public NetworkThrottleManager(Profiler profiler) {
        profiler = profiler;
        NET_INTERFACE = profiler.NET_INTERFACE;
    }

    public void periodicOperation (Profiler profiler) {
        throttlingManagement();

    }

    // The unit of command --> mbits/s
    public void manualOperation (String command, boolean isDownlink) {
        if (isDownlink) {
            executeDownlinkThrottling(command);
        }
        else {
            executeUplinkThrottling (command);
        }
    }

    // TODO
    public void throttlingManagement () {

    }

    public void executeUplinkThrottling (String rate) {
        try {
            // Create a list to hold all commands
            List<String> commands = new ArrayList<>();

            commands.add("tc qdisc del dev " + NET_INTERFACE +" root");
            commands.add("tc qdisc add dev " + NET_INTERFACE +" root handle 1: htb default 6");
            commands.add("tc class add dev " + NET_INTERFACE +" parent 1: classid 1:6 htb rate " + rate + "mbit burst 1m");

            // Write each command to the output stream
            for (String command : commands) {
                executeCommand (command);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cancelUplinkThrottling () {
        try {
            // Create a list to hold all commands
            List<String> commands = new ArrayList<>();

            commands.add("tc qdisc del dev " + NET_INTERFACE +" root");

            // Write each command to the output stream
            for (String command : commands) {
                executeCommand (command);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void executeDownlinkThrottling (String rate) {
        try {

            // Create a list to hold all commands
            List<String> commands = new ArrayList<>();

            commands.add("ifconfig ifb0 up");
            commands.add("tc qdisc del dev " + NET_INTERFACE +" clsact");
            commands.add("tc qdisc del dev " + NET_INTERFACE +" ingress");
            commands.add("tc qdisc add dev " + NET_INTERFACE +" handle ffff: ingress");
            commands.add("tc filter add dev " + NET_INTERFACE +" parent ffff: u32 match u32 0 0 action mirred egress redirect dev ifb0");
            commands.add("tc qdisc add dev ifb0 root handle 1: htb default 6");
            commands.add("tc class add dev ifb0 parent 1: classid 1:6 htb rate " + rate + "mbit burst 1m");
            commands.add("tc class change dev ifb0 parent 1: classid 1:6 htb rate " + rate + "mbit burst 1m");

            // Write each command to the output stream
            for (String command : commands) {
                executeCommand (command);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cancelDownlinkThrottling () {
        try {
            // Create a list to hold all commands
            List<String> commands = new ArrayList<>();

            commands.add("ifconfig ifb0 up");
            commands.add("tc qdisc del dev " + NET_INTERFACE +" clsact");
            commands.add("tc qdisc del dev " + NET_INTERFACE +" ingress");

            // Write each command to the output stream
            for (String command : commands) {
                executeCommand (command);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean executeCommand(String command) {
        Process process = null;
        try {
            // Create a new process builder
            ProcessBuilder processBuilder = new ProcessBuilder();

            // Start a shell with superuser privileges
            processBuilder.command("su");

            // Start the process
            process = processBuilder.start();

            // Get the output stream of the process
            OutputStream outputStream = process.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));

            // Write the command to the output stream
            writer.write(command);
            writer.write("\n");
            writer.flush();

            // Close the output stream
            writer.close();

            // Wait for the process to finish and check the exit value
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return true;
            } else {
                // Read the standard error
                BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String error;
                while ((error = stdError.readLine()) != null) {
                    System.out.println(error);
                }
                return false;
            }

        } catch (Exception e) {
            System.out.println("Exception occurred: " + e.getMessage());
            return false;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }
}
