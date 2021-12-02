package com.swu.silvermobilian.Message;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.swu.silvermobilian.Adapter.SendAdapter;
import com.swu.silvermobilian.Bean.Message.SendBean;
import com.swu.silvermobilian.Bean.Message.Util;
import com.swu.silvermobilian.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class SmslistActivity extends AppCompatActivity {

    /** 안내 floating 추가 **/
    private Animation guide_open, guide_close;
    private Boolean isGuideOpen = false;

    private ListView lstFind;
    private SendAdapter sendAdapter;
    private EditText edtSearch;
    private Button btnSearch;
    private SendBean sendBean;

    int currentMode = 0;

    /** 메시지 안내 기능 추가**/

    /**안내 floating **/
    private FloatingActionButton btnGuide, btnGNew, btnGRe, btnGSearch;


    private Button btnPlus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smslist);

        lstFind = findViewById(R.id.lstFindMember);
        btnSearch = findViewById(R.id.btnSearch);
        edtSearch = findViewById(R.id.edtSearch);
        btnPlus = findViewById(R.id.btnPlus);

        // 안내 코드
        final Animation mAnimation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.guide_anim);
        final Handler handler = new Handler();

        /** 안내 floating **/
        guide_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.guide_open);
        guide_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.guide_close);
        btnGuide = findViewById(R.id.btnGuide);
        btnGNew = findViewById(R.id.btnGNew);
        btnGRe = findViewById(R.id.btnGRe);
        btnGSearch = findViewById(R.id.btnGSearch);

        btnGuide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Click(v);
            }
        });

        btnGNew = findViewById(R.id.btnGNew);
        btnGNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Click(v);
                btnPlus.startAnimation(mAnimation);
                Toast.makeText(getApplicationContext(), "+ 버튼을 누르세요.", Toast.LENGTH_LONG).show();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        btnPlus.clearAnimation();
                    }
                },3000);

                currentMode = 55;
                Log.i("current mode","mode==>"+currentMode);

            }
        });

        btnGRe = findViewById(R.id.btnGRe);
        btnGRe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Click(v);
                Toast.makeText(getApplicationContext(), "답장할 메시지를 클릭하세요.", Toast.LENGTH_LONG).show();
                lstFind.startAnimation(mAnimation);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        lstFind.clearAnimation();
                        Toast.makeText(getApplicationContext(), "답장을 보낼 수 있습니다.", Toast.LENGTH_LONG).show();
                    }
                },3000);

                currentMode = 56;
            }
        });

        btnGSearch = findViewById(R.id.btnGSearch);
        btnGSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Click(view);
                edtSearch.startAnimation(mAnimation);
                Toast.makeText(getApplicationContext(),"검색할 번호를 입력하세요.", Toast.LENGTH_LONG).show();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        edtSearch.clearAnimation();
                        btnSearch.startAnimation(mAnimation);
                        Toast.makeText(getApplicationContext(), "검색 버튼을 누르세요.", Toast.LENGTH_LONG).show();
                    }
                },3000);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        btnSearch.clearAnimation();
                    }
                },6000);

                currentMode = 56;

            }
        });

        // 새 메시지 작성 버튼
        btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), SendWriteActivity.class);
                if (currentMode == 55){
                    i.putExtra("Current Mode", currentMode);
                    currentMode = 0;
                    startActivity(i);
                } else if (currentMode != 55){
                    i.putExtra("Current Mode", 0);
                    startActivity(i);
                }
            }});

        // 검색 창
        edtSearch.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //Enter key Action
                if((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER))
                {
                    searchList();
                    return true;
                    // 리턴이 true일 경우에는 사용자가 입력한 엔터키의 이벤트를 os 까지 보내지 않겠다는 뜻
                    // 즉 사용자가 입력한 엔터키가 소멸됨.
                }
                return false;
            }
        });

        // 리스트 내 검색
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchList();
            }
        });

        //브로드캐스트로 온 인텐트는 SMS 메세지임
        Intent passedIntent = getIntent();  //인텐트 수신 함수
        processIntent(passedIntent);


    } //oncreate

    public void searchList() {
        String search = edtSearch.getText().toString();

        List<SendBean> tempList = new ArrayList<>();
        List<SendBean> allList = sendBean.getSBList();

        for(int i=0; i<allList.size(); i++) {
            SendBean sendBean1 = allList.get(i);

            if(sendBean1.getPhone().indexOf(search) > -1) {
                tempList.add(sendBean1);
            } else if(sendBean1.getContent().indexOf(search) > -1) {
                tempList.add(sendBean1);
            }
        }//end for

        if(search != null && search.length() > 0 && tempList.size() > 0) {
            sendAdapter = new SendAdapter(getApplicationContext(), tempList);
        }
        else if (search != null && search.length() > 0 && tempList.size() == 0) {
            sendAdapter = new SendAdapter(getApplicationContext(), tempList);
        }
        else if(search == null || search.length() == 0) {
            sendAdapter = new SendAdapter(getApplicationContext(), allList);
        }
        lstFind.setAdapter(sendAdapter);
    } //searchList()

    public void onResume() {
        super.onResume();

        String jsonStr = Util.openFile(getApplicationContext(), SendBean.class.getName());
        Gson gson = new Gson();
        sendBean = gson.fromJson(jsonStr, SendBean.class);

        if (sendBean != null){
            sendAdapter = new SendAdapter(getApplicationContext(), sendBean.getSBList());
            lstFind.setAdapter(sendAdapter);
        }

    } //onResume()



    /** SmsActivity가 이미 켜져있는 상태에서도 SMS 수신하도록 **/
    @Override
    protected void onNewIntent(Intent intent) {
        processIntent(intent);
        super.onNewIntent(intent);
    } //onNewIntent()

    private void processIntent(Intent intent) {
        if (intent != null) {
            String sender = intent.getStringExtra("sender2");    //인텐트에서 해당 데이터명의 값을 가져옴
            String contents = intent.getStringExtra("contents2");

            if (sender != null && contents != null){
                SendBean ReceivedBean = new SendBean();
                ReceivedBean.setPhone(sender);
                ReceivedBean.setContent(contents);

                /*String title = mEdtTitle.getText().toString();

                SendBean sendBean = new SendBean();
                sendBean.setTitle(mEdtTitle.getText().toString());
                sendBean.setContent(mEdtContent.getText().toString());*/

                Gson gson = new Gson();
                String jsonStr = Util.openFile(this, SendBean.class.getName());
                SendBean sendBean1 = null;


                if(jsonStr == null || jsonStr.length() == 0) {
                    sendBean1 = new SendBean();
                } else {
                    sendBean1 = gson.fromJson(jsonStr, SendBean.class);
                }

                List<SendBean> list = sendBean1.getSBList();
                list.add(ReceivedBean);

                String jsonStr2 = gson.toJson(sendBean1);
                Util.saveFile(SmslistActivity.this, SendBean.class.getName(), jsonStr2);
            }
            else if(sender == null && contents == null){

            }

        } // check is intent null

    } //processIntent()


    public void Click(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btnGuide:
                anim();
                break;
            case R.id.btnGNew:
                anim();
                break;
            case R.id.btnGRe:
                anim();
                break;
            case R.id.btnGSearch:
                anim();
                break;
        }
    } //Click()

    public void anim() {
        if (isGuideOpen) {
            btnGNew.startAnimation(guide_close);
            btnGRe.startAnimation(guide_close);
            btnGSearch.startAnimation(guide_close);
            btnGNew.setClickable(false);
            btnGRe.setClickable(false);
            btnGSearch.setClickable(false);
            isGuideOpen = false;
        } else {
            btnGNew.startAnimation(guide_open);
            btnGRe.startAnimation(guide_open);
            btnGSearch.startAnimation(guide_open);
            btnGNew.setClickable(true);
            btnGRe.setClickable(true);
            btnGSearch.setClickable(true);
            isGuideOpen = true;
        }
    }   //anim()


}