package com.swu.silvermobilian.Message;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.swu.silvermobilian.Bean.Message.SendBean;
import com.swu.silvermobilian.Bean.Message.Util;
import com.swu.silvermobilian.R;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SendWriteActivity extends AppCompatActivity {

    private EditText edtPhone;
    private EditText edtContent;
    private Button btnSave;
    private Button btnBook;
    private String number;
    private String name;
    private String phone;
    private String content;

    /** 메시지 안내 기능 추가**/
    final Animation mAnimation = new AlphaAnimation(1,0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_write);

        edtPhone = findViewById(R.id.edtTitle);
        edtContent = findViewById(R.id.edtContent);
        btnSave = findViewById(R.id.btnSave);

        /** 메시지 안내 기능 **/
        Intent i = getIntent();
        int currentMode = i.getIntExtra("Current Mode", 0);
        Log.i("current mode","mode==>"+currentMode);

        mAnimation.setDuration(600);
        mAnimation.setInterpolator(new LinearInterpolator());
        mAnimation.setRepeatCount(Animation.INFINITE);
        mAnimation.setRepeatMode(Animation.REVERSE);
        Handler handler = new Handler();

        if (currentMode == 55) {

            edtPhone.startAnimation(mAnimation);
            Toast.makeText(getApplicationContext(), "상대방 전화번호를 입력하세요.", Toast.LENGTH_LONG).show();

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    edtPhone.clearAnimation();
                    edtContent.startAnimation(mAnimation);
                    Toast.makeText(getApplicationContext(), "문자 메시지를 입력하세요.", Toast.LENGTH_LONG).show();
                }
            },3000);
            edtContent = findViewById(R.id.edtContent);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    edtContent.clearAnimation();
                    btnSave.startAnimation(mAnimation);
                    Toast.makeText(getApplicationContext(), "보내기 버튼을 누르세요.", Toast.LENGTH_LONG).show();
                }
            },6000);

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    btnSave.clearAnimation();
                }
            },9000);
        } // 안내기능

        // 주소록에서 가져온 번호
        number = getIntent().getStringExtra("number");
        Log.i("number","받아온 번호==>"+number);
        // 주소록에서 가져온 이름
        name  = getIntent().getStringExtra("name");
        Log.i("name","번호 해당 이름==>"+name);

        if (number != null){
            edtPhone.setText(name); // 주소록 검색 후 번호 있으면 가져옴
        }

        // 보내기 버튼
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(name != null){
                    sendSmsMessage2();
                    saveMemo2();
                    name = "";
                }
                else if(name == null){
                    sendSmsMessage();
                    saveMemo();
                }

            }
        });

        // 주소록 버튼
        btnBook = findViewById(R.id.btnBook);
        btnBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                startActivity(intent);
                finish();
            }
        });

    } //onCreate

    public void sendSmsMessage(){
        phone = edtPhone.getText().toString(); // 번호
        content = edtContent.getText().toString(); // 내용
        PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_SENT_ACTION"), 0);
        PendingIntent deliveredIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_DELIVERED_ACTION"), 0);

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch(getResultCode()){
                    case Activity.RESULT_OK:
                        // 전송 성공
                        Toast.makeText(getApplicationContext(), "전송 완료", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        // 전송 실패
                        Toast.makeText(getApplicationContext(), "전송 실패", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        // 서비스 지역 아님
                        Toast.makeText(getApplicationContext(), "서비스 지역이 아닙니다", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        // 무선 꺼짐
                        Toast.makeText(getApplicationContext(), "무선(Radio)가 꺼져있습니다", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        // PDU 실패
                        Toast.makeText(getApplicationContext(), "PDU Null", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter("SMS_SENT_ACTION"));

        SmsManager smsManager = SmsManager.getDefault();
        if(phone.length() != 0 && content.length() != 0) {
            smsManager.sendTextMessage(phone, null, content, null, null);
            Toast.makeText(getApplicationContext(), "보내졌어요!!!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "안보내졌어요!!!", Toast.LENGTH_LONG).show();
        }

    } //sendSmsMessage()

    public void sendSmsMessage2(){
        phone = number; // 번호
        content = edtContent.getText().toString(); // 내용
        PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_SENT_ACTION"), 0);
        PendingIntent deliveredIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_DELIVERED_ACTION"), 0);

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch(getResultCode()){
                    case Activity.RESULT_OK:
                        // 전송 성공
                        Toast.makeText(getApplicationContext(), "전송 완료", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        // 전송 실패
                        Toast.makeText(getApplicationContext(), "전송 실패", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        // 서비스 지역 아님
                        Toast.makeText(getApplicationContext(), "서비스 지역이 아닙니다", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        // 무선 꺼짐
                        Toast.makeText(getApplicationContext(), "무선(Radio)가 꺼져있습니다", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        // PDU 실패
                        Toast.makeText(getApplicationContext(), "PDU Null", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter("SMS_SENT_ACTION"));

        SmsManager smsManager = SmsManager.getDefault();
        if(phone.length() != 0 && content.length() != 0) {
            smsManager.sendTextMessage(phone, null, content, null, null);
            Toast.makeText(getApplicationContext(), "보내졌어요", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "안보내졌어요", Toast.LENGTH_LONG).show();
        }

    } //sendSmsMessage2()

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // 주소록에 없는 번호 (새로운 번호)
    private void saveMemo() {

        String phone = edtPhone.getText().toString();

        SendBean sendBean = new SendBean();
        sendBean.setPhone(edtPhone.getText().toString());
        sendBean.setContent(edtContent.getText().toString());


        // 날짜
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        sendBean.setRegDate(timeStamp);

        Gson gson = new Gson();
        String jsonStr = Util.openFile(this, SendBean.class.getName());
        SendBean sendBean1;


        if(jsonStr == null || jsonStr.length() == 0) {
            sendBean1 = new SendBean();
        } else {
            sendBean1 = gson.fromJson(jsonStr, SendBean.class);
        }

        List<SendBean> tempList = new ArrayList<SendBean>();
        List<SendBean> allList = sendBean1.getSBList();


        //멤버 리스트를 돌면서 하나씩 비교 검색함

        for(int i=0; i<allList.size(); i++) {
            SendBean sendBean2 = allList.get(i);

            // 찾고자 하는 것이 있다면 찾은 문자열의 인덱스를 반환함
            if(sendBean2.getPhone().indexOf(phone) > -1) {
                tempList.add(sendBean2); // 찾은 객체를 add함
            }
        }  //end for



        // 입력한 번호가 있고, 찾은 리스트가 있다.
        if(phone != null && phone.length() > 0 && tempList.size() > 0){
            // mAdapter = new SendAdapter(getApplicationContext(), tempList);
            List<SendBean> list = sendBean1.getSBList();
            list.add(sendBean);
        }

        // 입력한 번호가 있고, 찾은 리스트가 없다.
        else if(phone != null && phone.length() > 0 && tempList.size() == 0) {
            List<SendBean> list = sendBean1.getSBList();
            list.add(sendBean);
        }

        //Toast.makeText(getApplicationContext(), "저장됨", Toast.LENGTH_LONG).show();
        //List<SendBean> list = saveBean.getSendBeanList();
        //list.add(sendBean);


        String jsonStr2 = gson.toJson(sendBean1);
        Util.saveFile(SendWriteActivity.this, SendBean.class.getName(), jsonStr2);

        finish();

    } //saveMemo()

    // 주소록에 있는 번호
    private void saveMemo2() {

        String phone = name;

        SendBean sendBean = new SendBean();
        sendBean.setPhone(name);
        sendBean.setContent(edtContent.getText().toString());


        // 날짜
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        sendBean.setRegDate(timeStamp);

        Gson gson = new Gson();
        String jsonStr = Util.openFile(this, SendBean.class.getName());
        SendBean sendBean1 = null;


        if(jsonStr == null || jsonStr.length() == 0) {
            sendBean1 = new SendBean();
        } else {
            sendBean1 = gson.fromJson(jsonStr, SendBean.class);
        }

        List<SendBean> tempList = new ArrayList<SendBean>();
        List<SendBean> allList = sendBean1.getSBList();


        //멤버 리스트를 돌면서 하나씩 비교 검색함

        for(int i=0; i<allList.size(); i++){
            SendBean sendBean2 = allList.get(i);

            // 찾고자 하는 것이 있다면 찾은 문자열의 인덱스를 반환함
            if(sendBean2.getPhone().indexOf(phone) > -1) {
                tempList.add(sendBean2); // 찾은 객체를 add함
            }
        }  //end for



        // 입력한 번호가 있고, 찾은 리스트가 있다.
        if(phone != null && phone.length() > 0 && tempList.size() > 0){
            // mAdapter = new SendAdapter(getApplicationContext(), tempList);
            List<SendBean> list = sendBean1.getSBList();
            list.add(sendBean);
        }

        // 입력한 번호가 있고, 찾은 리스트가 없다.
        else if(phone != null && phone.length() > 0 && tempList.size() == 0) {
            List<SendBean> list = sendBean1.getSBList();
            list.add(sendBean);
        }

        //Toast.makeText(getApplicationContext(), "저장됨", Toast.LENGTH_LONG).show();
        //List<SendBean> list = saveBean.getSendBeanList();
        //list.add(sendBean);


        String jsonStr2 = gson.toJson(sendBean1);
        Util.saveFile(SendWriteActivity.this, SendBean.class.getName(), jsonStr2);

        finish();

    } //saveMemo2()




}
