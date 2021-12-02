package com.swu.silvermobilian.Message;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.swu.silvermobilian.R;

public class PushActivity extends AppCompatActivity {

    private TextView phoneNum;
    private TextView content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push);

        phoneNum = findViewById(R.id.phoneNum);
        content = findViewById(R.id.content);

        //브로드캐스트로 온 인텐트는 SMS 메세지임
        Intent passedIntent = getIntent();  //인텐트 수신 함수
        processIntent(passedIntent);

        // 이 부분이 바로 화면을 깨우는 부분
        // 화면이 잠겨있을 때 보여주기
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                // 키잠금 해제하기
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                // 화면 켜기
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); //투명배경


        /* 확인버튼클릭시 sms로 **/
        Button ib = findViewById(R.id.btnOK);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(getApplicationContext(), SmslistActivity.class);
//                startActivity(intent);
                finish();
            }
            public boolean onTouchEvent(MotionEvent event) {
                //바깥레이어 클릭시 안닫히게
                return event.getAction() != MotionEvent.ACTION_OUTSIDE;

            }
        });

        /* 취소시 꺼짐 **/
        Button cb = findViewById(R.id.btnCancel);
        cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 앱 종료
                finishAffinity();
                // push 창 종료
                finish();
            }
        });

    }//onCreate

    /** SmsActivity가 이미 켜져있는 상태에서도 SMS 수신하도록 **/
    @Override
    protected void onNewIntent(Intent intent) {
        processIntent(intent);
        super.onNewIntent(intent);
    }  //onNewIntent()

    private void processIntent(Intent intent) {
        if (intent != null) {
            String sender = intent.getStringExtra("sender");    //인텐트에서 해당 데이터명의 값을 가져옴
            String contents = intent.getStringExtra("contents");
            phoneNum.setText(sender);
            content.setText(contents);
        }
    } //processIntent()

}
