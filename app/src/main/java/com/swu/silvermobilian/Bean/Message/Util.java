package com.swu.silvermobilian.Bean.Message;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

public class Util {

    public static void saveFile(Context context, String fileName, String contents){
        try {
            FileOutputStream outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(contents.getBytes());
            outputStream.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    // 내부앱에서 파일을 읽어 들인다.
    public static String openFile(Context context, String fileName){
        // 파일에서 읽은 데이터를 저장하기 위해서 만든 변수
        StringBuffer data = new StringBuffer();
        try {
            FileInputStream fis = context.openFileInput(fileName); // 파일명
            BufferedReader buffer = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = buffer.readLine()) != null){
                data.append(line);
            }
            buffer.close();
        } catch (Exception e){
            e.printStackTrace();
        }
        return data.toString();
    }
}
