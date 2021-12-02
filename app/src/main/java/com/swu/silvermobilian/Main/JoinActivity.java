package com.swu.silvermobilian.Main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.swu.silvermobilian.Bean.Main.UserBean;
import com.swu.silvermobilian.Bean.Main.Utils;
import com.swu.silvermobilian.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.util.UUID;
import java.util.regex.Pattern;

public class JoinActivity extends AppCompatActivity {

    //비밀번호 정규식
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^[a-zA-Z0-9!@.#$%^&*?_~]{4,16}$");

    //firebase 인증 객체 생성
    private FirebaseAuth firebaseAuth;

    //onActivityResult 에서 사용하는 구분값
    public static final int REQUEST_IMAGE_CAPTURE= 200;

    private UserBean userBean;

    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private FirebaseDatabase mDatabase;

    private Button btnJoin, btnLogin;
    private EditText edtId, edtPw, edtPw2, edtmyName, edtmyTel, edtmyAdr, edtMemo;
    private EditText edtproName, edtproTel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        //파이어베이스 인증 객체 선언
        firebaseAuth= FirebaseAuth.getInstance();

        mAuth= FirebaseAuth.getInstance();
        mStorage= FirebaseStorage.getInstance();
        mDatabase= FirebaseDatabase.getInstance();

        edtId= findViewById(R.id.edtId);
        edtPw= findViewById(R.id.edtPw);
        edtPw2= findViewById(R.id.edtPw2);
        edtmyName = findViewById(R.id.edtmyName);
        edtmyTel = findViewById(R.id.edtmyTel);
        edtmyAdr = findViewById(R.id.edtmyAdr);
        edtMemo = findViewById(R.id.edtMemo);

        edtproName = findViewById(R.id.edtproName);
        edtproTel = findViewById(R.id.edtproTel);

        btnJoin = findViewById(R.id.btnJoin);
        btnLogin = findViewById(R.id.btnLogin);

        //회원가입
        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id= edtId.getText().toString();
                String pw= edtPw.getText().toString();
                String pw2= edtPw2.getText().toString();

                if(isValidEmail(id)&&isValidPasswd(pw, pw2)){
                    //회원가입 시키겠다. 어디에? Firebase에
                    createUser(id, pw);
                }
                else if(!edtPw.equals(edtPw2)){ //pw와 pw2가 같지 않다.

                }
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i= new Intent(JoinActivity.this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
    } //onCreate()

    // 이메일 유효성 검사
    private boolean isValidEmail(String email) {
        if (email.isEmpty()) {
            // 이메일 공백
            Toast.makeText(JoinActivity.this, "이메일은 필수 입력 사항입니다.", Toast.LENGTH_SHORT).show();
            return false;
        }else if ( !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            // 이메일 형식 불일치
            Toast.makeText(JoinActivity.this, "이메일 형식이 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }//end of isValiEmail


    // 비밀번호 유효성 검사
    private boolean isValidPasswd(String password, String password2) {
        if (password.isEmpty()||password2.isEmpty()) {
            // 비밀번호 공백
            Toast.makeText(JoinActivity.this, "비밀번호는 필수 입력 사항입니다.",Toast.LENGTH_SHORT).show();
            return false;
        }else if(!password.equals(password2)){
            Toast.makeText(JoinActivity.this, "비밀번호 확인이 올바르지 않습니다. 다시 확인해주세요",Toast.LENGTH_SHORT).show();
            return false;
        }else if (!PASSWORD_PATTERN.matcher(password).matches()) {
            // 비밀번호 형식 불일치
            Toast.makeText(JoinActivity.this, "비밀번호 형식이 올바르지 않습니다.",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }//end of isValiPasswd()

    //회원가입 처리
    private void createUser(final String email, final String pw){
        firebaseAuth.createUserWithEmailAndPassword(email, pw).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                System.out.println( email +  ""+ pw);
                if(task.isSuccessful()){
                    //회원가입 성공
                    Toast.makeText(JoinActivity.this, "회원가입을 축하합니다.", Toast.LENGTH_SHORT).show();
                    upload();
                    goLoginActivity();
                }else{
                    //회원가입 실패
                    Toast.makeText(JoinActivity.this, "회원가입을 실패하였습니다. 다시 시도해주세요.",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }//end createUser

    private void goLoginActivity(){
        Intent i= new Intent(JoinActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }//end goLoginActivity

    //Storage Database Upload 관련 Funtuon- Start!
    private void upload(){

        //다이얼 로그 보이기
        Utils.showProgress(JoinActivity.this);

        //사용자가 입력한 내용을 Realtime Database에 업로드한다.
        DatabaseReference firebaseRef= mDatabase.getReference();

        //Database에 저장한다.
        UserBean mBean = new UserBean();
        mBean.setId(mAuth.getCurrentUser().getEmail());
        mBean.setPw(edtPw.getText().toString());
        mBean.setPw2(edtPw2.getText().toString());
        mBean.setMyname(edtmyName.getText().toString());
        mBean.setMytel(edtmyTel.getText().toString());
        mBean.setMyadr(edtmyAdr.getText().toString());
        mBean.setMemo(edtMemo.getText().toString());
        mBean.setProname(edtproName.getText().toString());
        mBean.setProtel(edtproTel.getText().toString());

        //userEmail의 고유번호를 기준으로 사용자 데이터를 쌓기 위해서 고유키를 생성한다.
        String userIdUUID = getUserIdFromUUID(mBean.getId());
        firebaseRef.child("User").child(userIdUUID).setValue(mBean);

        //다이얼 로그 숨기기
        Utils.hideProgress(JoinActivity.this);

        finish();

    }//end upload()

    //아이디의 문자 기준으로 고유번호를 뽑는다.
    public static String getUserIdFromUUID(String userEmail){
        long val= UUID.nameUUIDFromBytes(userEmail.getBytes()).getMostSignificantBits();
        return val+"";
    }//end getUserIdFromUUID
}