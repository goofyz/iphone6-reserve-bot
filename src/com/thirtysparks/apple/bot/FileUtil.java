package com.thirtysparks.apple.bot;

import android.os.Environment;

import java.io.*;

public class FileUtil {
    public static void outputToFile(String message){
        try {
            File logFile = new File(Environment.getExternalStorageDirectory(), "output.txt");
            FileWriter fileWriter = new FileWriter(logFile, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(message + "\n");
            bufferedWriter.close();
            fileWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            // handle exception
        } catch (IOException e) {
            // handle exception
            e.printStackTrace();
        }
    }
}
