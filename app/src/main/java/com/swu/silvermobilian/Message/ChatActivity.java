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
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.swu.silvermobilian.Adapter.ChatAdapter;
import com.swu.silvermobilian.Adapter.SendAdapter;
import com.swu.silvermobilian.Bean.Message.SendBean;
import com.swu.silvermobilian.Bean.Message.Util;
import com.swu.silvermobilian.R;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatActivity extends AppCompatActivity {

    InputMethodManager imm;

    private String phone;
    private String content;
    private ListView lstChat;
    private ChatAdapter chatAdapter;
    private Button btnWrite;
    private EditText edtWrite;
    private SendBean msendBean;
//    {// TODO : 메시지 안내 기능 추가 }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // 키보드 속성(UI 가림방지)
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        // 키보드 내림
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        msendBean = (SendBean) getIntent().getSerializableExtra(SendAdapter.KEY_FOUND_DATA_CLASS);

        btnWrite = findViewById(R.id.btnUndo);
        edtWrite = findViewById(R.id.edtWrite);
        lstChat = findViewById(R.id.lstChat);
        TextView txtContent = findViewById(R.id.txtContent);
        TextView txtTitle = findViewById(R.id.txtTitle);

        btnWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendSmsMessage();
                saveComment();
                imm.hideSoftInputFromWindow(btnWrite.getWindowToken(),0);
            }
        });

        if(msendBean != null) {
            txtTitle.setText(msendBean.getPhone());  //TODO:추가 (XML 고치기)
            txtContent.setText(msendBean.getContent());
        }
    } //onCreate

    public void sendSmsMessage(){

        //TODO : 추가
        phone = msendBean.getPhone();
        content = edtWrite.getText().toString();
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
        smsManager.sendTextMessage(phone, null, content, null, null);
        Toast.makeText(getApplicationContext(), "메시지 전송 완료", Toast.LENGTH_LONG).show();
    }

    private void saveComment() {
        SendBean chatBean = new SendBean();
        chatBean.setComment(edtWrite.getText().toString());

        //날짜
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        chatBean.setRegDate(timeStamp);

        Gson gson = new Gson();

        String jsonStr = Util.openFile(this, SendBean.class.getName());
        SendBean sendBean1 = gson.fromJson(jsonStr, SendBean.class);

        SendBean sendBean2 = sendBean1.getSBList().get(msendBean.getSelIdx());
        sendBean2.getCBList().add(chatBean);

        //저장
        String jsonStr2 = gson.toJson(sendBean1);
        Util.saveFile(ChatActivity.this, SendBean.class.getName(), jsonStr2);

        chatAdapter = new ChatAdapter(ChatActivity.this, msendBean.getSelIdx(), sendBean2.getCBList());

        lstChat.setAdapter(chatAdapter);

        new Handler().postDelayed(new Runnable() { //TODO : 추가
            @Override
            public void run() {
                edtWrite.setText("");
                edtWrite.requestFocus();
            }
        }, 400);
    } //saveComment()

    protected void onResume() {
        super.onResume();

        //adapter 생성 (리스트에 데이터 넘기기)
        chatAdapter = new ChatAdapter(ChatActivity.this, msendBean.getSelIdx(), msendBean.getCBList());
        //adapter에 리스트뷰 세팅하기
        lstChat.setAdapter(chatAdapter);

    } //onResume()

}