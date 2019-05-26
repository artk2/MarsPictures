package com.artk.gallery.data;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * a class responsible for performing read/write and other operations with files
 */
public class FileUtils {

    public static String readJsonFromFile(Context context, String fileName){
        String json = "";
        try{
            FileInputStream in = context.openFileInput(fileName);
            InputStreamReader is = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(is);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null)
                sb.append(line);
            json = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static void writeFile(Context context, String fileName, String content){
        FileOutputStream outputStream;
        try {
            outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(content.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
