package com.example.t5gservice;

import android.util.Log;

import com.opencsv.CSVWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public class TaskProfiler {


    public static boolean isLastValueEqualTo(String[] array, String targetValue) {
        if (array != null && array.length > 0) {
            String lastValue = array[array.length - 1];
            return lastValue.equals(targetValue);
        }
        return false;
    }

    public static String[][] executegfxShellCommand(Process p, String pkg_name) {
        StringBuilder profileData = new StringBuilder();
//        Process process = p;
        boolean isInsideProfileData = false;

        String[][] resultArray = new String[0][];
        try {
            // ProcessBuilder를 사용하여 명령어 실행
            ProcessBuilder processBuilder = new ProcessBuilder("su", "-c", "dumpsys", "gfxinfo", pkg_name, "framestats");
            p = processBuilder.start();

            // 명령어 실행 후 출력을 읽어오기 위한 BufferedReader
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line;
            int lineIndex = 0;
            resultArray = new String[2][];

            while ((line = reader.readLine()) != null) {
                if (isInsideProfileData == true && line.startsWith("---PROFILEDATA---") == false && line.startsWith("Flags") == false) {
                    resultArray[lineIndex] = line.split(",");
                    lineIndex++;
//                    profileData.append(line).append("\n");
//                    Log.i("tftf", "line : "+line);
                }


                if (line.startsWith("---PROFILEDATA---") && isInsideProfileData == false) {
                    isInsideProfileData = true;

                } else if (isInsideProfileData) {
//                    profileData.append(line).append("\n");
                    if (line.startsWith("---PROFILEDATA---")) {
                        isInsideProfileData = false;
                        break;

                    }

//                    profileData.append(line).append("\n");
                }
//                Log.i("tftf", "line : "+line);
            }

            // 프로세스 종료 대기
            p.waitFor();

            // BufferedReader와 프로세스 리소스 정리
            reader.close();
//            process.destroy();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        if (resultArray == null) {
            resultArray[0][0] = "Not running";
            return resultArray;
        }
        return resultArray;
    }

    public static String[] getprocessinfo(Process p, String pkg_name) {
        StringBuilder profileData = new StringBuilder();



        String[] result=new String[0];
        try {
            // ProcessBuilder를 사용하여 명령어 실행
            ProcessBuilder processBuilder = new ProcessBuilder("su", "-c", "cat", "/proc/sched_debug", "|grep",pkg_name);
            p = processBuilder.start();

            // 명령어 실행 후 출력을 읽어오기 위한 BufferedReader
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line;
            int lineIndex = 0;


            while ((line = reader.readLine()) != null) {
//                    Log.i("tftf", line);
                    result = line.trim().split("\\s+");
//                    Log.i("tftf", result[8]);
//                    Log.i("tftf", result[9]);
                }


            // 프로세스 종료 대기
            p.waitFor();

            // BufferedReader와 프로세스 리소스 정리
            reader.close();
//            process.destroy();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        if (result == null) {
            result[0] = "Not running";
            return result;
        }
        return result;
    }


    public static String[] getTID(Process p, String pid) {
        StringBuilder profileData = new StringBuilder();
        ArrayList<String> tid_Arraylists = new ArrayList<>();

        String command = "/proc/" + pid + "/task/";

        try {
            // ProcessBuilder를 사용하여 명령어 실행
            ProcessBuilder processBuilder = new ProcessBuilder("su", "-c", "ls", command);

            p = processBuilder.start();

            // 명령어 실행 후 출력을 읽어오기 위한 BufferedReader
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line;
            int lineIndex = 0;


            while ((line = reader.readLine()) != null) {

                tid_Arraylists.add(line);
//                result = line.split("\\s+");
            }


            // 프로세스 종료 대기
            p.waitFor();

            // BufferedReader와 프로세스 리소스 정리
            reader.close();
//            process.destroy();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        String[] result=tid_Arraylists.toArray(new String[0]);

        if (result == null) {
            result[0] = "Not running";
            return result;
        }
        return result;
    }


    public static String readCommFile(Process p, String pid, String tid) {
        String command = "adb shell cat /proc/" + pid + "/task/" + tid + "/comm";
        StringBuilder commContent = new StringBuilder();

        String result=null;
        try {
            // ProcessBuilder를 사용하여 명령어 실행
            ProcessBuilder processBuilder = new ProcessBuilder("su", "-c", "cat", command);

            p = processBuilder.start();

            // 명령어 실행 후 출력을 읽어오기 위한 BufferedReader
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line;
            int lineIndex = 0;


            while ((line = reader.readLine()) != null) {
                result = line;
//                result = line.split("\\s+");
            }

            // 프로세스 종료 대기
            p.waitFor();
            // BufferedReader와 프로세스 리소스 정리
            reader.close();
//            process.destroy();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        if (result == null) {
            result = "Not running";
            return result;
        }
        return result;
    }

    public static String[] getThreadinfo(Process p, String pid, String[] tid) {
        String[] result=new String[tid.length];
        int idx = 0;
        for (String tid_tmp : tid) {
            String commContent = readCommFile(p, pid, tid_tmp);
            result[idx]=(commContent);
            idx = idx+1;
        }


        if (result == null) {
            result[0] = "Not running";
            return result;
        }
        return result;
    }

    public static String[][] executeTopShellCommand(Process p, String pid) {
        StringBuilder profileData = new StringBuilder();
//        Process process = p;
        boolean isInsideThreadData = false;
//        String command = "top -H -n 1 -q -b -o TID,%CPU -p "+ pid;
        String command = "top -H -n 1 -q -b -p "+ pid;

        String[][] resultArray = new String[20][14];
        try {
            // ProcessBuilder를 사용하여 명령어 실행
            ProcessBuilder processBuilder = new ProcessBuilder("su", "-c", command);
            p = processBuilder.start();

            // 명령어 실행 후 출력을 읽어오기 위한 BufferedReader
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line;
            int lineIndex = 0;


            while ((line = reader.readLine()) != null && lineIndex<20) {
                resultArray[lineIndex] = line.trim().split("\\s+");
                if (!resultArray[lineIndex][8].startsWith("-")){
                    if (Float.parseFloat(resultArray[lineIndex][8])>0.1) {
    //                    Log.i("tftf", resultArray[lineIndex][8]);
                        lineIndex++;
                }
                }
            }

            // 프로세스 종료 대기
            p.waitFor();

            // BufferedReader와 프로세스 리소스 정리
            reader.close();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        if (resultArray == null) {
            resultArray[0][0] = "Not running";
            return resultArray;
        }


            return resultArray;
    }


    static String[] tid_arr = new String[50];
    static String[] cpu_arr = new String[50];
    static String[] tname_arr = new String[50];
    public void tid_writeCSV(String[][] data, File filename) {

        boolean found = false;
        int i =0;
        int count = 0;

        if (tid_arr[tid_arr.length-1]==null){

            for (String[] threadinfo : data) {
                i = 0;
                found = false;
                for (String tidData : tid_arr) {
                    if (tid_arr[i] == null) {
                        found = true;
                        tid_arr[i] = threadinfo[0];
                        cpu_arr[i] = threadinfo[8];
                        tname_arr[i] = threadinfo[11];
                        i = i + 1;
                        break;
                    } else if (tidData.equals(threadinfo[0])) {
                        found = true;
                        cpu_arr[i] = threadinfo[8];
//                        tname_arr[i] = threadinfo[11];
                        i = i + 1;
                        break;
                    }
                    i = i + 1;
                }
            }

            try (CSVWriter writer = new CSVWriter(new FileWriter(filename, true))) {
                writer.writeNext(cpu_arr);
                Log.i("tftf", "["+count+"] "+ Arrays.toString(tname_arr));
                count = count+1;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void tid_header_writeCSV(File filename) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filename, true))) {
            writer.writeNext(tname_arr);
            writer.close();
            Log.i("tftf", "csv saved: "+filename);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }




    public void writeCSV(String[][] data, File filename, String[] header) {
        boolean fileExists = filename.exists();
//        Log.i("tftf", String.valueOf(Paths.get(String.valueOf(filename))));
//        File csvfile = new File(getFilesDir(), filename);
        try (CSVWriter writer = new CSVWriter(new FileWriter(filename, true))) {
            if (!fileExists) {
                writer.writeNext(header); // 파일이 처음 생성되는 경우에만 헤더 쓰기

            }

            for (String[] row : data) {
                if (row != null) {
                    writer.writeNext(row);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
