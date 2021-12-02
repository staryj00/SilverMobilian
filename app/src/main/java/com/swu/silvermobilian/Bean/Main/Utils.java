package com.swu.silvermobilian.Bean.Main;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;

import com.swu.silvermobilian.R;

public class Utils {

    private static Dialog mProgDlg;

    //화면에 프로그래스를 보여준다.
    public static void showProgress(Context context){
        if(mProgDlg!=null&& mProgDlg.isShowing()){
            mProgDlg.hide();
        }

        mProgDlg= new Dialog(context);

        mProgDlg.setContentView(R.layout.view_progress);
        mProgDlg.setCancelable(false);
        mProgDlg.show();
    }//end setProgress

    public static void hideProgress(Context context){
        if(mProgDlg!=null){
            //mProgDlg.hide();
            mProgDlg.dismiss();
        }
    }//end hideProgress

    //계정 정보 저장
    public static void setData(Context context, String key, String value){     //key값은 항상 String이여야 함
        SharedPreferences pref = context.getSharedPreferences(key, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key,value);
        editor.commit();
    }

    //계정 정보 가져오기
    public static String getDataString(Context context, String key) {
        SharedPreferences pref = context.getSharedPreferences(key, Activity.MODE_PRIVATE);
        return pref.getString(key, "");
    }

    public static void setData(Context context, String key, Boolean value){     //key값은 항상 String이여야 함
        SharedPreferences pref = context.getSharedPreferences(key, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(key,value);
        editor.commit();
    }

    public static Boolean getDataBoolean(Context context, String key) {
        SharedPreferences pref = context.getSharedPreferences(key, Activity.MODE_PRIVATE);
        return pref.getBoolean(key, false);
    }
}
