package com.swu.silvermobilian.Main;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.swu.silvermobilian.Bean.Main.UserBean;
import com.swu.silvermobilian.Bean.Main.Utils;
import com.swu.silvermobilian.Message.SmslistActivity;
import com.swu.silvermobilian.Phone.CallActivity;
import com.swu.silvermobilian.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    //비밀번호 정규식
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^[a-zA-Z0-9!@.#$%^&*?_~]{4,16}$");

    //보이스피싱
    private static final int MY_PERMISSIONS_REQUEST_READ_CALL_LOG = 0;

    //firebase 인증 객체 생성
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;

    //TODO:SOS 문자 전송을 위한 변수
    String PhoneNum;
    String text;


    EditText edtId, edtPw;
    TextView txtName, txtHi;
    Button btnJoin, btnLogin;
    private CheckBox chkAuto;

    private UserBean bean;

    private static int log = 1004;

    //권한 리스트
    private String[] permissions = {
            Manifest.permission.CALL_PHONE, // 전화걸고 관리 권한
            Manifest.permission.READ_CONTACTS, // 주소록 액세스 권한
            Manifest.permission.WRITE_CONTACTS, // 주소록에 액세스 권한
            Manifest.permission.ACCESS_FINE_LOCATION, // 위치 액세스 권한
            Manifest.permission.ACCESS_COARSE_LOCATION, // 위치 액세스 권한
            Manifest.permission.READ_SMS, // 문자 액세스 권한
            Manifest.permission.SEND_SMS, //문자 전송 권한
            Manifest.permission.RECEIVE_SMS, //문자 액세스 권한
            Manifest.permission.READ_CALL_LOG //전화 기록 액세스 권한
    };
    private static final int MULTIPLE_PERMISSIONS = 101;

    /**
     * 위치 정보를 가져오기 위해 GpsTracker 객체 사용
     **/

    private GpsTracker gpsTracker;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private FloatingActionButton fab, fabCall, fabMsg, fabSos, fabUser;
    private Animation fab_open, fab_close;
    private Boolean isFabOpen = false;

    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    int mode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 로그아웃 여부 확인 코드
        Intent i = getIntent();
        mode = i.getIntExtra("mode", 0);
        Log.i("mode","현재모드 = "+mode);

        //안드로이드 6.0 이상일 경우 퍼미션 체크
        if (Build.VERSION.SDK_INT >= 23) {
            checkPermissions();
        }

        //파이어베이스 인증 객체 선언
        firebaseAuth = FirebaseAuth.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mReference = mDatabase.getReference();

        edtId = findViewById(R.id.edtId);
        edtPw = findViewById(R.id.edtPw);
        txtName = findViewById(R.id.txtName);
        txtName.setVisibility(View.INVISIBLE);
        txtHi = findViewById(R.id.txtHi);
        txtHi.setVisibility(View.INVISIBLE);
        btnLogin = findViewById(R.id.btnLogin);
        btnJoin = findViewById(R.id.btnJoin);
        chkAuto = findViewById(R.id.chkAuto);

        //회원가입 -> JoinActivity로 이동
        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, JoinActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = edtId.getText().toString();
                String pw = edtPw.getText().toString();

                if (!isValidEmail(id) || !isValidPasswd(pw)) {
                } else {
                    //로그인 하겠다.
                    loginUser(id, pw);
                }
            }
        });

        /** 위치 접근 권한과 GPS 사용 가능 여부 체크 **/
        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        } else {
            checkRunTimePermission();
        }

        //안내 코드
        final Animation mAnimation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.guide_anim);
        final Handler handler = new Handler();

        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.guide_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.guide_close);

        fab = findViewById(R.id.fab);
        fabCall = findViewById(R.id.fabCall);
        fabMsg = findViewById(R.id.fabMsg);
        fabSos = findViewById(R.id.fabSos);
        fabUser = findViewById(R.id.fabUser);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Click(view);
            }
        });

        final Button btnCall = findViewById(R.id.btnCall);
        final ImageButton btnSOS = findViewById(R.id.btnSOS);
        final Button btnMsg = findViewById(R.id.btnMsg);
        final ImageButton btnUser = findViewById(R.id.btnUser);


        fabCall.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Click(v);

                btnCall.startAnimation(mAnimation);
                Toast.makeText(getApplicationContext(), "전화입니다. \n전화를 할 수 있고, 주소록과 즐겨찾기 기능을 이용할 수 있습니다.",Toast.LENGTH_LONG).show();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        btnCall.clearAnimation();

                    }
                },3000);


            }
        });

        fabMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Click(v);

                btnMsg.startAnimation(mAnimation);
                Toast.makeText(getApplicationContext(), "메시지입니다.\n 문자를 보낼 수 있고, 주고 받은 내역을 확인 할 수 있습니다.",Toast.LENGTH_LONG).show();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        btnMsg.clearAnimation();

                    }
                },3000);


            }
        });

        fabSos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Click(v);

                btnSOS.startAnimation(mAnimation);
                Toast.makeText(getApplicationContext(), "긴급호출입니다.\n 저장되어있던 보호자에게로 현재 위치가 전송되고 전화를 겁니다.",Toast.LENGTH_LONG).show();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        btnSOS.clearAnimation();

                    }
                },3000);


            }
        });

        fabUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Click(v);

                btnUser.startAnimation(mAnimation);
                Toast.makeText(getApplicationContext(), "마이페이지입니다.\n 로그인을 통해 내 정보를 관리할 수 있습니다.",Toast.LENGTH_LONG).show();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        btnUser.clearAnimation();
                    }
                },3000);


            }
        });


        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mode == 0) {
                    Intent i = new Intent(getApplicationContext(),CallActivity.class);
                    i.putExtra("mode", mode);
                    startActivity(i);
                }
                else {
                    Intent i = new Intent(getApplicationContext(),CallActivity.class);
                    i.putExtra("mode", mode);
                    startActivity(i);
                }
            }
        });

        btnMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),SmslistActivity.class);
                startActivity(intent);
            }
        });

        btnSOS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuth.getCurrentUser() == null) { //현재 사용자가 없다면 정보 보이지 않기
                    //Intent i = new Intent(MyPageActivity.this , MainActivity.class);
                    finish();
                    //startActivity(i);
                } else if (mAuth.getCurrentUser() != null) {
                    //TODO: 보호자 연락 + 현재 위치 전송
                    //현재 위치 확인
                    gpsTracker = new GpsTracker(MainActivity.this);

                    double latitude = gpsTracker.getLatitude();
                    double longitude = gpsTracker.getLongitude();

                    //함수를 통해서 위도/경도를 위치로 변환
                    String address = getCurrentAddress(latitude, longitude);
                    Log.i("현재 위도", Double.toString(latitude));
                    Log.i("현재 경도", Double.toString(longitude));
                    Log.i("현재 위치", address);
                    text = address;

                    //사용자가 입력한 보호자 번호로 전화가 걸린다.
                    final UserBean userBean = new UserBean();
                    DatabaseReference firebaseRef = mDatabase.getReference();
                    final FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    String emailUUID = MainActivity.getUserIdFromUUID(mAuth.getCurrentUser().getEmail());
                    firebaseRef.child("User").child(emailUUID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            //리스트를 서버로 부터 온 데이터로 새로 만든다.
                            bean = dataSnapshot.getValue(UserBean.class);

                            if (bean != null) {
                                PhoneNum = bean.getProtel();
                                sendSmsMessage();
                                phoneCall(MainActivity.this, bean.getProtel());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

                //TODO : 입력한 번호로 사용자의 위치 정보가 문자로 발송됨
                //sendSmsMessage();
                //Log.d(">>>>>>>>>>>.", PhoneNum);
            }
        }); //btnSOS


        btnUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: 마이페이지로 이동
                if (mode == 0) {
                    Toast.makeText(getApplicationContext(),"로그인 후 이용해주세요",Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(getApplicationContext(), MyPageActivity.class);
                    startActivity(intent);
                }
            }
        });

        //보이스피싱
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

        telephonyManager.listen(new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, final String incomingNumber) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_CALL_LOG)) {

                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CALL_LOG}, MY_PERMISSIONS_REQUEST_READ_CALL_LOG);
                    }
                } else {
                    switch (state) {
                        case TelephonyManager.CALL_STATE_RINGING:
                            mDatabase.getReference().child("Voice_Fishing").child("사칭 번호").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    System.out.println(dataSnapshot);
                                    if (incomingNumber.equals(dataSnapshot.getValue())) {
                                        Log.d(TAG, "전화번호가 존재");
                                        Toast.makeText(MainActivity.this, "보이스피싱 위험 번호" + incomingNumber, Toast.LENGTH_LONG).show();
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });


                    }
                }

            }
        }, PhoneStateListener.LISTEN_CALL_STATE);

    } //onCreate

    public void Click(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.fab:
                anim();
                break;
            case R.id.fabCall:
                anim();
                break;
            case R.id.fabMsg:
                anim();
                break;
            case R.id.fabSos:
                anim();
                break;
            case R.id.fabUser:
                anim();
                break;
        }


    }//onClick

    private void anim() {

        if (isFabOpen) {
            fabCall.startAnimation(fab_close);
            fabMsg.startAnimation(fab_close);
            fabSos.startAnimation(fab_close);
            fabUser.startAnimation(fab_close);
            fabCall.setClickable(false);
            fabMsg.setClickable(false);
            fabSos.setClickable(false);
            fabUser.setClickable(false);
            isFabOpen = false;
        } else {
            fabCall.startAnimation(fab_open);
            fabMsg.startAnimation(fab_open);
            fabSos.startAnimation(fab_open);
            fabUser.startAnimation(fab_open);
            fabCall.setClickable(true);
            fabMsg.setClickable(true);
            fabSos.setClickable(true);
            fabUser.setClickable(true);
            isFabOpen = true;
        }
    }//on anim


    private boolean checkPermissions() {
        int result;
        List<String> permissionList = new ArrayList<>();
        for (String pm : permissions) {
            result = ContextCompat.checkSelfPermission(this, pm);
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(pm);
            }
        }
        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    } //checkPermissions()

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++) {
                        if (permissions[i].equals(this.permissions[i])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showToast_PermissionDeny();
                            }
                        }
                    }
                } else {
                    showToast_PermissionDeny();
                }
                return;
            }
        }

    } //onRequestPermissionsResult()


    private void showToast_PermissionDeny() {
        Toast.makeText(this, "권한 요청에 동의 해주셔야 이용 가능합니다. 설정에서 권한 허용 하시기 바랍니다.", Toast.LENGTH_SHORT).show();
        finish();
    } //showToast_PermissionDeny()

    @Override
    protected void onResume() {
        super.onResume();

        System.out.println( "hi" );

        //자동로그인이 체크되어 있는지 확인한다.
        boolean isAutoLoginChecked = Utils.getDataBoolean(MainActivity.this,"auto");

        if(isAutoLoginChecked) {
            //자동로그인 체크로 인해서 바로 MainActivity로 이동
            if(firebaseAuth.getCurrentUser() != null) {
                goActivity(true);
                //finish();
            } else {
                String email = Utils.getDataString(this, "email");
                String pw = Utils.getDataString(this, "pw");
                loginUser(email, pw);
            }
        }
    }

    // 이메일 유효성 검사
    private boolean isValidEmail(String email) {
        if (email.isEmpty()) {
            // 이메일 공백
            Toast.makeText(MainActivity.this, "이메일은 필수 입력 사항입니다.",Toast.LENGTH_SHORT).show();
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            // 이메일 형식 불일치
            Toast.makeText(MainActivity.this, "이메일 형식이 올바르지 않습니다.",Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }//end of isValiEmail

    // 비밀번호 유효성 검사
    private boolean isValidPasswd(String password) {
        if (password.isEmpty()) {
            // 비밀번호 공백
            Toast.makeText(MainActivity.this, "비밀번호는 필수 입력 사항입니다.",Toast.LENGTH_SHORT).show();
            return false;
        } else if (!PASSWORD_PATTERN.matcher(password).matches()) {
            // 비밀번호 형식 불일치
            Toast.makeText(MainActivity.this, "비밀번호 형식이 올바르지 않습니다.",Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }//end of isValiPasswd()

    private void loginUser(final String email, final String pw){
        //다이얼 로그 보이기
        Utils.showProgress(MainActivity.this);

        firebaseAuth.signInWithEmailAndPassword(email, pw)
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()&&chkAuto.isChecked()){
                            //자동로그인일 경우 저장한다.
                            Utils.setData(MainActivity.this, "auto", true);
                            Utils.setData(MainActivity.this, "email", email);
                            Utils.setData(MainActivity.this, "pw", pw);
                            goActivity(true);

                            mode = 1;
                        }else if(task.isSuccessful()){
                            goActivity(false);

                            mode = 1;
                        }else{
                            Toast.makeText(MainActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();
                        }
                        //다이얼 로그 보이기
                        Utils.hideProgress(MainActivity.this);
                    }
                });
    }//loginUser

    private void goActivity(final boolean autoType){

        FirebaseAuth mAuth= FirebaseAuth.getInstance();
        //로그인한 id가 현재 사용자임을 넣어준다.
        final String emailUUID= MainActivity.getUserIdFromUUID(mAuth.getCurrentUser().getEmail());

        mDatabase.getReference().child("User").child(emailUUID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserBean bean = dataSnapshot.getValue(UserBean.class);
                goActivityProc(autoType, emailUUID, bean);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }//end goActivity

    private void goActivityProc(final boolean autoType, final String emailUUID, final UserBean userBean) {
        //데이터 목록을 Firebase로 부터 가져온다.
        mDatabase.getReference().child("User").child(emailUUID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    if (log == 1004) {
                        //userBean.setToken( Utils.getDataString(MainActivity.this, "token") );
                        //update
                        mDatabase.getReference().child("User").child(emailUUID).setValue(userBean);

                        if (autoType == false) {
                            Toast.makeText(MainActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();
                        } else if (autoType == true) {
                            Toast.makeText(MainActivity.this, mAuth.getCurrentUser().getEmail() + "님 자동로그인 입니다.", Toast.LENGTH_SHORT).show();
                        }
                        //로그인창 없애고 사용자 이름 보이기
                        edtId.setVisibility(View.INVISIBLE);
                        edtPw.setVisibility(View.INVISIBLE);
                        btnLogin.setVisibility(View.INVISIBLE);
                        btnJoin.setVisibility(View.INVISIBLE);
                        chkAuto.setVisibility(View.INVISIBLE);
                        txtName.setVisibility(View.VISIBLE);
                        txtHi.setVisibility(View.VISIBLE);

                        //현재 로그인한 사용자 이름 가져오기
                        UserBean bean = dataSnapshot.getValue(UserBean.class);
                        txtName.setText(bean.getMyname());

                        log = 0;
                    } else if(log == 0) {
                        //update
                        mDatabase.getReference().child("User").child(emailUUID).setValue(userBean);

                        //로그인창 없애고 사용자 이름 보이기
                        edtId.setVisibility(View.INVISIBLE);
                        edtPw.setVisibility(View.INVISIBLE);
                        btnLogin.setVisibility(View.INVISIBLE);
                        btnJoin.setVisibility(View.INVISIBLE);
                        chkAuto.setVisibility(View.INVISIBLE);
                        txtName.setVisibility(View.VISIBLE);
                        txtHi.setVisibility(View.VISIBLE);

                        //현재 로그인한 사용자 이름 가져오기
                        UserBean bean = dataSnapshot.getValue(UserBean.class);
                        txtName.setText(bean.getMyname());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    //이메일의 문자 기준으로 고유번호를 뽑는다.
    public static String getUserIdFromUUID(String userEmail){
        long val= UUID.nameUUIDFromBytes(userEmail.getBytes()).getMostSignificantBits();
        return val+"";
    }//end getUserIdFromUUID

    void checkRunTimePermission(){

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


            // 3.  위치 값을 가져올 수 있음



        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(MainActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }


    public String getCurrentAddress( double latitude, double longitude) {

        //지오코더: GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.KOREAN);

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }



        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";

    }


    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }

                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    // 전화걸기 함수
    public boolean phoneCall(Context context, String number)
    {
        Intent iCall = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
        iCall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(iCall);
        return true;

    } //phoneCall()

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    //전화번호 받아오기
    public class myPhoneStateChangeListener extends PhoneStateListener {
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                String phoneNumber = incomingNumber;
            }
        }
    } //myPhoneStateChangeListener()


    //TODO : 메시지 전송을 위한 함수
    public void sendSmsMessage(){
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
        smsManager.sendTextMessage(PhoneNum, null, text, null, null);
        Toast.makeText(getApplicationContext(), "메시지 전송 및 전화중...", Toast.LENGTH_LONG).show();

    } //sendSmsMessage()


}