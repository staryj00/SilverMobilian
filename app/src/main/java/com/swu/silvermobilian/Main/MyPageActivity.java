package com.swu.silvermobilian.Main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.swu.silvermobilian.Bean.Main.UserBean;
import com.swu.silvermobilian.Bean.Main.Utils;
import com.swu.silvermobilian.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;

import static android.content.ContentValues.TAG;

public class MyPageActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;

    private Button btnModify, btnLogout;
    private TextView txtId;
    private EditText edtPw, edtPw2, edtmyName, edtmyTel, edtmyAdr, edtMemo;
    private EditText edtproName, edtproTel;

    private UserBean userBean, bean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        btnModify = findViewById(R.id.btnModify);
        btnLogout = findViewById(R.id.btnLogout);
        txtId = findViewById(R.id.txtId);
        edtPw = findViewById(R.id.edtPw);
        edtPw2 = findViewById(R.id.edtPw2);
        edtmyName = findViewById(R.id.edtmyName);
        edtmyTel = findViewById(R.id.edtmyTel);
        edtmyAdr = findViewById(R.id.edtmyAdr);
        edtMemo = findViewById(R.id.edtMemo);

        edtproName = findViewById(R.id.edtproName);
        edtproTel = findViewById(R.id.edtproTel);

        userBean = (UserBean)getIntent().getSerializableExtra(UserBean.class.getName());

        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() == null) { //현재 사용자가 없다면 정보 보이지 않기
            //Intent i = new Intent(MyPageActivity.this , MainActivity.class);
            finish();
            //startActivity(i);

        } else if (mAuth.getCurrentUser() != null) {
            //사용자 ID 표시
            txtId.setText(mAuth.getCurrentUser().getEmail());

            //데이터를 Firebase로 부터 가져온다.
            String emailUUID = MainActivity.getUserIdFromUUID(mAuth.getCurrentUser().getEmail());
            mDatabase.getReference().child("User").child(emailUUID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //실시간으로 서버가 변경된 내용이 있을 경우 호출된다.

                    //리스트를 서버로 부터 온 데이터로 새로 만든다.
                    bean = dataSnapshot.getValue(UserBean.class);

                    if (bean != null) {
                        edtPw.setHint(bean.getPw());
                        edtPw2.setHint(bean.getPw());
                        edtmyName.setHint(bean.getMyname());
                        edtmyTel.setHint(bean.getMytel());
                        edtmyAdr.setHint(bean.getMyadr());
                        edtMemo.setHint(bean.getMemo());
                        edtproName.setHint(bean.getProname());
                        edtproTel.setHint(bean.getProtel());
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        btnModify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                update(bean);
            }
        });


        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //로그아웃 버튼 누를 시 mAuth=null, 아이디 및 패스워드 반환
                mAuth.signOut();
                Utils.setData(MyPageActivity.this, "auto", false);
                Utils.setData(MyPageActivity.this, "email", "");
                Utils.setData(MyPageActivity.this, "pw","");

                Intent i = new Intent(MyPageActivity.this , MainActivity.class);
                finish();
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                i.putExtra("mode",0);
                startActivity(i);
            }
        });

    } //onCreate

    //수정처리
    private void update(UserBean userBean) {
        //비밀번호 수정
        String pw = edtPw.getText().toString();
        String pw2 = edtPw2.getText().toString();
        //비밀번호 입력, 확인란이 모두 빈 경우
        if(edtPw.getText().length()==0 && edtPw2.getText().length()==0) {
            userBean.setPw(edtPw.getHint().toString());
            userBean.setPw2(edtPw2.getHint().toString());
        }
        //정상적으로 수정
        else if(pw.equals(pw2)) {
            userBean.setPw(edtPw.getText().toString());
            userBean.setPw2(edtPw2.getText().toString());
        }

        //사용자 이름 수정
        if (edtmyName.getText().length()==0) {
            userBean.setMyname(edtmyName.getHint().toString());
        } else {
            userBean.setMyname(edtmyName.getText().toString());
        }

        //사용자 전화번호 수정
        if (edtmyTel.getText().length()==0) {
            userBean.setMytel(edtmyTel.getHint().toString());
        } else {
            userBean.setMytel(edtmyTel.getText().toString());
        }

        //사용자 주소 수정
        if (edtmyAdr.getText().length()==0) {
            userBean.setMytel(edtmyAdr.getHint().toString());
        } else {
            userBean.setMytel(edtmyAdr.getText().toString());
        }

        //사용자 메모 수정
        if (edtMemo.getText().length()==0) {
            userBean.setMytel(edtMemo.getHint().toString());
        } else {
            userBean.setMytel(edtMemo.getText().toString());
        }

        //비밀번호 입력 또는 확인란 중 하나 기입 안함.
        if((edtPw.getText().length()!=0&&edtPw2.getText().length()==0)||(edtPw.getText().length()==0&&edtPw2.getText().length()!=0)) {
            Toast.makeText(this, "비밀번호 입력란과 확인란 모두 기입해주세요.", Toast.LENGTH_LONG).show();
        }
        //입력란과 확인란이 다름.
        else if (!pw.equals(pw2)){
            Toast.makeText(this, "비밀번호를 다시 확인해주세요.", Toast.LENGTH_LONG).show();
        }
        //이름만 수정한 경우
        else if (edtmyName.getText().length()!=0) {
            String emailUUID = getUserIdFromUUID(userBean.getId());

            //Intent i = new Intent(this, MainActivity.class);
            //i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            //서버 수정처리
            mDatabase.getReference().child("User").child(emailUUID).setValue(userBean);

            //startActivity(i);
            finish();
        }
//        else if (edtproTel.getText().length()!=0) {
//            String emailUUID = getUserIdFromUUID(userBean.getId());
//
//            //Intent i = new Intent(this, MainActivity.class);
//            //i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//
//            //서버 수정처리
//            mDatabase.getReference().child("User").child(emailUUID).setValue(userBean);
//
//            //startActivity(i);
//            finish();
//        }
        else {
            String emailUUID = getUserIdFromUUID(userBean.getId());

            //Intent i = new Intent(this, MainActivity.class);
            //i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            //서버 수정처리
            mDatabase.getReference().child("User").child(emailUUID).setValue(userBean);

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            //String newPassword = memberBean.getPw(edtPw.getText().toString());

            user.updatePassword(pw).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User password updated.");
                    }
                }
            });
            Toast.makeText(this, "회원정보가 수정되었습니다.", Toast.LENGTH_LONG).show();
            //startActivity(i);
            finish();
        }


    }//end update()

    //이메일의 문자 기준으로 고유번호를 뽑는다.
    public static String getUserIdFromUUID(String userEmail) {
        long val = UUID.nameUUIDFromBytes(userEmail.getBytes()).getMostSignificantBits();
        return val + "";
    }
}
