package com.swu.silvermobilian.Phone;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.swu.silvermobilian.Adapter.NumAdapter;
import com.swu.silvermobilian.Bean.Phone.ListBean;
import com.swu.silvermobilian.Bean.Phone.NumItem;
import com.swu.silvermobilian.Bean.Main.UserBean;
import com.swu.silvermobilian.Main.MainActivity;
import com.swu.silvermobilian.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class CallActivity extends AppCompatActivity {

    // ListView 필요한 코드
    ArrayList<NumItem> items = null;

    private Button btnContact, btnSearch, btnMark, btnRing;
    private ImageButton btnUndo;
    private EditText edtNum, edtName;
    private String mNum, mName;
    private ListView listMark;
    private String number;
    private String sname;

    private TextView listN;

    // firebase 인증 객체 생성
    private FirebaseAuth firebaseAuth;

    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;

    private ListBean listBean;
    private UserBean bean;

    private Animation guide_open, guide_close;
    private Boolean isGdOpen = false;
    private FloatingActionButton guide, gdCall, gdContact, gdMark, gdSearch;

    private Toast m;

    InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        // 키보드 속성(UI 가림방지)
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        // 키보드 내림
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        // ListView 필요한 코드
        listMark = findViewById(R.id.listMark);
        items = new ArrayList<>();

        final NumAdapter adapter = new NumAdapter(items, R.layout.num_item, this);
        listMark.setAdapter(adapter);

        btnRing = findViewById(R.id.btnRing);
        btnContact = findViewById(R.id.btnContact);
        btnMark = findViewById(R.id.btnMark);
        btnSearch = findViewById(R.id.btnSearch);
        btnUndo = findViewById(R.id.btnUndo);

        edtNum = findViewById(R.id.edtNum);
        edtName = findViewById(R.id.edtName);

        //파이어베이스 인증 객체 선언
        firebaseAuth = FirebaseAuth.getInstance();

        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        bean = (UserBean) getIntent().getSerializableExtra(UserBean.class.getName());
        listBean = (ListBean) getIntent().getSerializableExtra(ListBean.class.getName());

        guide_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.guide_open);
        guide_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.guide_close);

        guide = findViewById(R.id.guide);
        gdSearch = findViewById(R.id.gdSearch);
        gdMark = findViewById(R.id.gdMark);
        gdContact = findViewById(R.id.gdContact);
        gdCall = findViewById(R.id.gdCall);

        listN = findViewById(R.id.listN);

        Intent i = getIntent();
        int mode = i.getIntExtra("mode", 0);
        Log.i("mode", "현재 모드= " + mode);

        final Animation guideAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.guide_anim);
        final Handler handler = new Handler();

        if (mode == 0) {
            Toast.makeText(getApplicationContext(), "즐겨찾기 기능이 제한됩니다", Toast.LENGTH_LONG).show();
            btnMark.setAlpha(0.5f);
            listN.setAlpha(0.5f);
            btnMark.setEnabled(false);

            // 안내 버튼
            guide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Click(view);
                }
            });

            // 전화걸기 안내 버튼
            gdCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Click(v);
                    setFalse();
                    edtNum.startAnimation(guideAnimation);
                    m = Toast.makeText(getApplicationContext(), "번호를 입력하세요", Toast.LENGTH_LONG);
                    toastMsg(m);


                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            edtNum.clearAnimation();
                            btnRing.startAnimation(guideAnimation);
                            m = Toast.makeText(getApplicationContext(), "입력한 번호로 전화합니다", Toast.LENGTH_LONG);
                            toastMsg(m);
                        }
                    }, 4000);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            btnRing.clearAnimation();
                            edtName.startAnimation(guideAnimation);
                            m = Toast.makeText(getApplicationContext(), "이름을 입력하세요", Toast.LENGTH_LONG);
                            toastMsg(m);
                        }
                    }, 8000);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            edtName.clearAnimation();
                            btnRing.startAnimation(guideAnimation);
                            m = Toast.makeText(getApplicationContext(), "주소록에 있으면 바로 전화합니다", Toast.LENGTH_LONG);
                            toastMsg(m);
                        }
                    }, 12000);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            btnRing.clearAnimation();
                            setTrue();
                        }
                    }, 16000);
                }
            });

            // 주소록 저장 안내 버튼
            gdContact.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Click(v);
                    setFalse();
                    edtName.startAnimation(guideAnimation);
                    m = Toast.makeText(getApplicationContext(), "이름과", Toast.LENGTH_LONG);
                    toastMsg(m);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            edtName.clearAnimation();
                            edtNum.startAnimation(guideAnimation);
                            m = Toast.makeText(getApplicationContext(), "번호를 입력하고", Toast.LENGTH_LONG);
                            toastMsg(m);
                        }
                    }, 3000);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            edtNum.clearAnimation();
                            btnContact.startAnimation(guideAnimation);
                            m = Toast.makeText(getApplicationContext(), "주소록에 저장 버튼을 누르면 주소록에 저장됩니다", Toast.LENGTH_LONG);
                            toastMsg(m);
                        }
                    }, 6000);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            btnContact.clearAnimation();
                            setTrue();
                        }
                    }, 10000);
                }
            });

            //즐겨찾기 안내 버튼 비활성화
            gdMark.setAlpha(0.5f);
            gdMark.setEnabled(false);

            // 검색 안내 버튼
            gdSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Click(v);
                    setFalse();
                    edtName.startAnimation(guideAnimation);
                    m = Toast.makeText(getApplicationContext(), "이름이나", Toast.LENGTH_LONG);
                    toastMsg(m);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            edtName.clearAnimation();
                            edtNum.startAnimation(guideAnimation);
                            m = Toast.makeText(getApplicationContext(), "번호를 입력하고", Toast.LENGTH_LONG);
                            toastMsg(m);
                        }
                    }, 3000);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            edtNum.clearAnimation();
                            btnSearch.startAnimation(guideAnimation);
                            m = Toast.makeText(getApplicationContext(), "검색 버튼을 눌러보세요", Toast.LENGTH_LONG);
                            toastMsg(m);
                        }
                    }, 6000);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            btnSearch.clearAnimation();
                            edtNum.startAnimation(guideAnimation);
                            edtName.startAnimation(guideAnimation);
                            m = Toast.makeText(getApplicationContext(), "주소록에 저장된 이름 또는 번호를 보여줍니다", Toast.LENGTH_LONG);
                            toastMsg(m);
                        }
                    }, 9000);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            edtNum.clearAnimation();
                            edtName.clearAnimation();
                            btnRing.startAnimation(guideAnimation);
                            m = Toast.makeText(getApplicationContext(), "바로 전화 버튼을 누르면 전화합니다", Toast.LENGTH_LONG);
                            toastMsg(m);
                        }
                    }, 13000);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            btnRing.clearAnimation();
                            btnMark.startAnimation(guideAnimation);
                            m = Toast.makeText(getApplicationContext(), "즐겨찾기 추가도 할 수 있어요", Toast.LENGTH_LONG);
                            toastMsg(m);
                        }
                    }, 17000);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            btnMark.clearAnimation();
                            setTrue();
                        }
                    }, 21000);

                }
            });

            // 전화바로걸기 버튼
            btnRing.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO: 전화 바로 걸기

                    mName = edtName.getText().toString();
                    mNum = edtNum.getText().toString();
                    Log.i("**입력된 이름 =====>", mName);
                    Log.i("**입력된 번호 =====>", mNum);

                    // 전화번호로 전화 걸기
                    if (edtNum.length() != 0 && edtName.length() == 0) {
                        phoneCall(CallActivity.this, mNum);
                        edtNum.setText("");
                        mNum = null;
                        Toast.makeText(getApplicationContext(), "전화 연결 중...", Toast.LENGTH_LONG).show();
                    } else if (edtName.length() != 0 && edtNum.length() == 0) {  // 주소록 번호로 전화 걸기 (주소록에 저장된 이름이라면 바로 전화걸기 / 저장되어있지 않다면 Toast 메시지 띄우기)
                        number = getPhoneNumber(CallActivity.this, mName);

                        // 주소록에 있음
                        if (number.length() != 0) {
                            Log.i("**검색 결과 =====> ", number);
                            phoneCall(CallActivity.this, number);
                            edtName.setText("");
                            mName = null;
                            Toast.makeText(getApplicationContext(), "주소록에 있어 바로 전화 연결 중...", Toast.LENGTH_LONG).show();
                        }
                        // 주소록에 없음
                        if (number.length() == 0) {
                            edtName.setText("");
                            mName = null;
                            Log.i("**검색 결과 =====> ", number);
                            Toast.makeText(getApplicationContext(), "주소록에 없습니다. 번호를 입력하세요!", Toast.LENGTH_LONG).show();
                        }

                    } else if (edtNum.length() != 0 && edtName.length() != 0) { // 주소록 검색 후 전화 걸기
                        phoneCall(CallActivity.this, mNum);
                        edtNum.setText("");
                        edtName.setText("");
                        mNum = null;
                        mName = null;
                        Toast.makeText(getApplicationContext(), "전화 연결 중...", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "이름 또는 번호를 입력해주세요", Toast.LENGTH_LONG).show();
                    }

                }
            });


            // 주소록에 저장 버튼
            btnContact.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO: 전화번호 저장
                    final String cName = edtName.getText().toString();
                    final String cNum = edtNum.getText().toString();
                    if (cName.length() != 0 && cNum.length() != 0) {
                        new Thread() {
                            @Override
                            public void run() {
                                ArrayList<ContentProviderOperation> list = new ArrayList<>();
                                try {
                                    list.add(
                                            ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                                                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                                                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build()
                                    );

                                    list.add(
                                            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                                                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, cName).build()
                                    ); // 이름 저장

                                    list.add(
                                            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                                                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, cNum)
                                                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build()
                                    ); // 전화번호 저장

                                    getApplicationContext().getContentResolver().applyBatch(ContactsContract.AUTHORITY, list); //주소록 추가
                                    list.clear();   //리스트 초기화
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                } catch (OperationApplicationException e) {
                                    e.printStackTrace();
                                }
                            }

                        }.start();
                        Toast.makeText(getApplicationContext(), "저장되었습니다!", Toast.LENGTH_SHORT).show();

                        imm.hideSoftInputFromWindow(btnContact.getWindowToken(), 0);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                edtName.setText("");
                                edtNum.setText("");
                            }
                        }, 1500);
                    } else {
                        Toast.makeText(getApplicationContext(), "이름과 번호를 입력해주세요", Toast.LENGTH_LONG).show();
                    }


                }

            });

            // 검색(주소록) 버튼
            btnSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mName = edtName.getText().toString(); // 사용자가 검색할 이름
                    mNum = edtNum.getText().toString(); // 사용자가 검색할 번호
                    Log.i("**입력된 이름 =====>", mName);
                    Log.i("**입력된 번호 =====>", mNum);

                    // 입력한 이름으로 주소록에서 검색
                    if (mName.length() != 0 && mNum.length() == 0) {
                        number = getPhoneNumber(CallActivity.this, mName);

                        // 주소록에 있음
                        if (number.length() != 0) {
                            imm.hideSoftInputFromWindow(btnSearch.getWindowToken(), 0);
                            Log.i("**검색 결과 =====> ", number);
                            edtNum.setText(number);
                            Toast.makeText(getApplicationContext(), "전화 번호가 검색되었습니다!", Toast.LENGTH_SHORT).show();
                        }

                        // 주소록에 없음
                        if (number.length() == 0) {
                            Log.i("**검색 결과 =====> ", number);
                            Toast.makeText(getApplicationContext(), "주소록에서 찾지 못했습니다ㅠㅠ", Toast.LENGTH_LONG).show();
                        }
                    }

                    // 입력한 번호로 주소록에서 검색
                    if (mNum.length() != 0 && mName.length() == 0) {
                        sname = getPhoneName(CallActivity.this, mNum);

                        // 주소록에 있음
                        if (sname.length() != 0) {
                            imm.hideSoftInputFromWindow(btnSearch.getWindowToken(), 0);
                            Log.i("**검색 결과 =====> ", sname);
                            edtName.setText(sname);
                            Toast.makeText(getApplicationContext(), "전화 번호가 검색되었습니다!", Toast.LENGTH_SHORT).show();
                        }

                        // 주소록에 없음
                        if (sname.length() == 0) {
                            Log.i("**검색 결과 =====> ", sname);
                            Toast.makeText(getApplicationContext(), "주소록에서 찾지 못했습니다ㅠㅠ", Toast.LENGTH_LONG).show();
                        }
                    }

                    // 검색 결과 나온 상태에서 다른 사람 or 번호 검색
                    if (mName.length() != 0 && mNum.length() != 0) {
                        Toast.makeText(getApplicationContext(), "취소 버튼을 누른 다음 다시 검색해주세요ㅠㅠ", Toast.LENGTH_LONG).show();
                    }

                }
            });

            btnUndo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    edtName.setText("");
                    edtNum.setText("");
                    Toast.makeText(getApplicationContext(), "입력하신 모든 내용을 취소합니다!", Toast.LENGTH_SHORT).show();
                }
            });
        } else if (mode == 1) {
            // 안내 버튼
            guide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Click(view);
                }
            });

            // 전화걸기 안내 버튼
            gdCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Click(v);
                    setFalse();
                    edtNum.startAnimation(guideAnimation);
                    m = Toast.makeText(getApplicationContext(), "번호를 입력하세요", Toast.LENGTH_LONG);
                    toastMsg(m);


                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            edtNum.clearAnimation();
                            btnRing.startAnimation(guideAnimation);
                            m = Toast.makeText(getApplicationContext(), "입력한 번호로 전화합니다", Toast.LENGTH_LONG);
                            toastMsg(m);
                        }
                    }, 4000);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            btnRing.clearAnimation();
                            edtName.startAnimation(guideAnimation);
                            m = Toast.makeText(getApplicationContext(), "이름을 입력하세요", Toast.LENGTH_LONG);
                            toastMsg(m);
                        }
                    }, 8000);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            edtName.clearAnimation();
                            btnRing.startAnimation(guideAnimation);
                            m = Toast.makeText(getApplicationContext(), "주소록에 있으면 바로 전화합니다", Toast.LENGTH_LONG);
                            toastMsg(m);
                        }
                    }, 12000);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            btnRing.clearAnimation();
                            setTrue();
                        }
                    }, 16000);
                }
            });

            // 주소록 저장 안내 버튼
            gdContact.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Click(v);
                    setFalse();
                    edtName.startAnimation(guideAnimation);
                    m = Toast.makeText(getApplicationContext(), "이름과", Toast.LENGTH_LONG);
                    toastMsg(m);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            edtName.clearAnimation();
                            edtNum.startAnimation(guideAnimation);
                            m = Toast.makeText(getApplicationContext(), "번호를 입력하고", Toast.LENGTH_LONG);
                            toastMsg(m);
                        }
                    }, 3000);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            edtNum.clearAnimation();
                            btnContact.startAnimation(guideAnimation);
                            m = Toast.makeText(getApplicationContext(), "주소록에 저장 버튼을 누르면 주소록에 저장됩니다", Toast.LENGTH_LONG);
                            toastMsg(m);
                        }
                    }, 6000);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            btnContact.clearAnimation();
                            setTrue();
                        }
                    }, 10000);
                }
            });

            // 즐겨찾기 안내 버튼
            gdMark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Click(v);
                    setFalse();
                    edtName.startAnimation(guideAnimation);
                    m = Toast.makeText(getApplicationContext(), "이름과", Toast.LENGTH_LONG);
                    toastMsg(m);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            edtName.clearAnimation();
                            edtNum.startAnimation(guideAnimation);
                            m = Toast.makeText(getApplicationContext(), "번호를 입력하고", Toast.LENGTH_LONG);
                            toastMsg(m);
                        }
                    }, 3000);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            edtNum.clearAnimation();
                            btnMark.startAnimation(guideAnimation);
                            m = Toast.makeText(getApplicationContext(), "즐겨찾기 추가 버튼을 눌러보세요", Toast.LENGTH_LONG);
                            toastMsg(m);
                        }
                    }, 6000);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            btnMark.clearAnimation();
                            listMark.startAnimation(guideAnimation);
                            m = Toast.makeText(getApplicationContext(), "즐겨찾기 목록에 추가됩니다", Toast.LENGTH_LONG);
                            toastMsg(m);
                        }
                    }, 10000);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            listMark.clearAnimation();
                            adapter.btnDel.startAnimation(guideAnimation);
                            m = Toast.makeText(getApplicationContext(), "즐겨찾기 목록에서 삭제할 수 있습니다", Toast.LENGTH_LONG);
                            toastMsg(m);
                        }
                    }, 14000);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            adapter.btnDel.clearAnimation();
                            listMark.startAnimation(guideAnimation);
                            m = Toast.makeText(getApplicationContext(), "즐겨찾기 목록을 누르면 해당 번호로 전화합니다", Toast.LENGTH_LONG);
                            toastMsg(m);
                        }
                    }, 18000);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            listMark.clearAnimation();
                            setTrue();
                        }
                    }, 22000);

                }
            });

            // 검색 안내 버튼
            gdSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Click(v);
                    setFalse();
                    edtName.startAnimation(guideAnimation);
                    m = Toast.makeText(getApplicationContext(), "이름이나", Toast.LENGTH_LONG);
                    toastMsg(m);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            edtName.clearAnimation();
                            edtNum.startAnimation(guideAnimation);
                            m = Toast.makeText(getApplicationContext(), "번호를 입력하고", Toast.LENGTH_LONG);
                            toastMsg(m);
                        }
                    }, 3000);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            edtNum.clearAnimation();
                            btnSearch.startAnimation(guideAnimation);
                            m = Toast.makeText(getApplicationContext(), "검색 버튼을 눌러보세요", Toast.LENGTH_LONG);
                            toastMsg(m);
                        }
                    }, 6000);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            btnSearch.clearAnimation();
                            edtNum.startAnimation(guideAnimation);
                            edtName.startAnimation(guideAnimation);
                            m = Toast.makeText(getApplicationContext(), "주소록에 저장된 이름 또는 번호를 보여줍니다", Toast.LENGTH_LONG);
                            toastMsg(m);
                        }
                    }, 9000);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            edtNum.clearAnimation();
                            edtName.clearAnimation();
                            btnRing.startAnimation(guideAnimation);
                            m = Toast.makeText(getApplicationContext(), "바로 전화 버튼을 누르면 전화합니다", Toast.LENGTH_LONG);
                            toastMsg(m);
                        }
                    }, 13000);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            btnRing.clearAnimation();
                            btnMark.startAnimation(guideAnimation);
                            m = Toast.makeText(getApplicationContext(), "즐겨찾기 추가도 할 수 있어요", Toast.LENGTH_LONG);
                            toastMsg(m);
                        }
                    }, 17000);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            btnMark.clearAnimation();
                            setTrue();
                        }
                    }, 21000);

                }
            });

            // 전화바로걸기 버튼
            btnRing.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO: 전화 바로 걸기

                    mName = edtName.getText().toString();
                    mNum = edtNum.getText().toString();
                    Log.i("**입력된 이름 =====>", mName);
                    Log.i("**입력된 번호 =====>", mNum);

                    // 전화번호로 전화 걸기
                    if (edtNum.length() != 0 && edtName.length() == 0) {
                        phoneCall(CallActivity.this, mNum);
                        edtNum.setText("");
                        mNum = null;
                        Toast.makeText(getApplicationContext(), "전화 연결 중...", Toast.LENGTH_LONG).show();
                    } else if (edtName.length() != 0 && edtNum.length() == 0) {  // 주소록 번호로 전화 걸기 (주소록에 저장된 이름이라면 바로 전화걸기 / 저장되어있지 않다면 Toast 메시지 띄우기)
                        number = getPhoneNumber(CallActivity.this, mName);

                        // 주소록에 있음
                        if (number.length() != 0) {
                            Log.i("**검색 결과 =====> ", number);
                            phoneCall(CallActivity.this, number);
                            edtName.setText("");
                            mName = null;
                            Toast.makeText(getApplicationContext(), "주소록에 있어 바로 전화 연결 중...", Toast.LENGTH_LONG).show();
                        }
                        // 주소록에 없음
                        if (number.length() == 0) {
                            edtName.setText("");
                            mName = null;
                            Log.i("**검색 결과 =====> ", number);
                            Toast.makeText(getApplicationContext(), "주소록에 없습니다. 번호를 입력하세요!", Toast.LENGTH_LONG).show();
                        }

                    } else if (edtNum.length() != 0 && edtName.length() != 0) { // 주소록 검색 후 전화 걸기
                        phoneCall(CallActivity.this, mNum);
                        edtNum.setText("");
                        edtName.setText("");
                        mNum = null;
                        mName = null;
                        Toast.makeText(getApplicationContext(), "전화 연결 중...", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "이름 또는 번호를 입력해주세요", Toast.LENGTH_LONG).show();
                    }

                }
            });


            // 주소록에 저장 버튼
            btnContact.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO: 전화번호 저장
                    final String cName = edtName.getText().toString();
                    final String cNum = edtNum.getText().toString();
                    if (cName.length() != 0 && cNum.length() != 0) {
                        new Thread() {
                            @Override
                            public void run() {
                                ArrayList<ContentProviderOperation> list = new ArrayList<>();
                                try {
                                    list.add(
                                            ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                                                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                                                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build()
                                    );

                                    list.add(
                                            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                                                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, cName).build()
                                    ); // 이름 저장

                                    list.add(
                                            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                                                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, cNum)
                                                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build()
                                    ); // 전화번호 저장

                                    getApplicationContext().getContentResolver().applyBatch(ContactsContract.AUTHORITY, list); //주소록 추가
                                    list.clear();   //리스트 초기화
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                } catch (OperationApplicationException e) {
                                    e.printStackTrace();
                                }
                            }

                        }.start();
                        Toast.makeText(getApplicationContext(), "저장되었습니다!", Toast.LENGTH_SHORT).show();

                        imm.hideSoftInputFromWindow(btnContact.getWindowToken(), 0);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                edtName.setText("");
                                edtNum.setText("");
                            }
                        }, 1500);
                    } else {
                        Toast.makeText(getApplicationContext(), "이름과 번호를 입력해주세요", Toast.LENGTH_LONG).show();
                    }


                }

            });

            mDatabase = FirebaseDatabase.getInstance();
            mReference = mDatabase.getReference();
            // 즐겨찾기 추가 버튼
            btnMark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO: 즐겨찾기 추가
                    final String mname = edtName.getText().toString();
                    final String mnum = edtNum.getText().toString();

                    if (mname.length() != 0 && mnum.length() != 0) {
                        String emailUUID = MainActivity.getUserIdFromUUID(mAuth.getCurrentUser().getEmail());
                        mDatabase.getReference().child("User").child(emailUUID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                //실시간으로 서버가 변경된 내용이 있을 경우 호출된다.

                                //리스트를 서버로 부터 온 데이터로 새로 만든다.
                                bean = dataSnapshot.getValue(UserBean.class);
                                listBean = new ListBean(mname, mnum);

                                mReference.child(bean.getMyname() + " " + "List").push().setValue(listBean);

                                edtName.setText("");
                                edtNum.setText("");
                                imm.hideSoftInputFromWindow(btnMark.getWindowToken(), 0);
                                // 즐겨찾기 추가 완료 토스트
                                Toast.makeText(getApplicationContext(), "즐겨찾기 목록에 추가되었습니다!", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    } else {
                        Toast.makeText(getApplicationContext(), "이름과 번호를 입력해주세요", Toast.LENGTH_LONG).show();
                    }


                }

            });

            String emailUUID = MainActivity.getUserIdFromUUID(mAuth.getCurrentUser().getEmail());
            if (emailUUID.length() != 0) {
                mDatabase.getReference().child("User").child(emailUUID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //실시간으로 서버가 변경된 내용이 있을 경우 호출된다.

                        //리스트를 서버로 부터 온 데이터로 새로 만든다.
                        bean = dataSnapshot.getValue(UserBean.class);

                        mDatabase.getReference().child(bean.getMyname() + " " + "List").addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                listBean = dataSnapshot.getValue(ListBean.class);
                                NumItem numitems = new NumItem(listBean.getName(), listBean.getNum());
                                items.add(numitems);
                                adapter.notifyDataSetChanged();
                            }


                            @Override
                            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                            }

                            @Override
                            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                                listBean = dataSnapshot.getValue(ListBean.class);
                                NumItem numitems = new NumItem(listBean.getName(), listBean.getNum());
                                items.remove(numitems);
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            } else {

            }
            // 즐겨찾기 리스트 클릭 이벤트
            listMark.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // 클릭한 이벤트 position 겟
                    String itNum = items.get(position).getPnum();

                    // 전화 바로 걸기
                    phoneCall(CallActivity.this, itNum);
                    Toast.makeText(getApplicationContext(), "전화 연결 중...", Toast.LENGTH_SHORT).show();
                }
            });

            // 검색(주소록) 버튼
            btnSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mName = edtName.getText().toString(); // 사용자가 검색할 이름
                    mNum = edtNum.getText().toString(); // 사용자가 검색할 번호
                    Log.i("**입력된 이름 =====>", mName);
                    Log.i("**입력된 번호 =====>", mNum);

                    // 입력한 이름으로 주소록에서 검색
                    if (mName.length() != 0 && mNum.length() == 0) {
                        number = getPhoneNumber(CallActivity.this, mName);

                        // 주소록에 있음
                        if (number.length() != 0) {
                            imm.hideSoftInputFromWindow(btnSearch.getWindowToken(), 0);
                            Log.i("**검색 결과 =====> ", number);
                            edtNum.setText(number);
                            Toast.makeText(getApplicationContext(), "전화 번호가 검색되었습니다!", Toast.LENGTH_SHORT).show();
                        }

                        // 주소록에 없음
                        if (number.length() == 0) {
                            Log.i("**검색 결과 =====> ", number);
                            Toast.makeText(getApplicationContext(), "주소록에서 찾지 못했습니다ㅠㅠ", Toast.LENGTH_LONG).show();
                        }
                    }

                    // 입력한 번호로 주소록에서 검색
                    if (mNum.length() != 0 && mName.length() == 0) {
                        sname = getPhoneName(CallActivity.this, mNum);

                        // 주소록에 있음
                        if (sname.length() != 0) {
                            imm.hideSoftInputFromWindow(btnSearch.getWindowToken(), 0);
                            Log.i("**검색 결과 =====> ", sname);
                            edtName.setText(sname);
                            Toast.makeText(getApplicationContext(), "전화 번호가 검색되었습니다!", Toast.LENGTH_SHORT).show();
                        }

                        // 주소록에 없음
                        if (sname.length() == 0) {
                            Log.i("**검색 결과 =====> ", sname);
                            Toast.makeText(getApplicationContext(), "주소록에서 찾지 못했습니다ㅠㅠ", Toast.LENGTH_LONG).show();
                        }
                    }

                    // 검색 결과 나온 상태에서 다른 사람 or 번호 검색
                    if (mName.length() != 0 && mNum.length() != 0) {
                        Toast.makeText(getApplicationContext(), "취소 버튼을 누른 다음 다시 검색해주세요ㅠㅠ", Toast.LENGTH_LONG).show();
                    }

                }
            });

            btnUndo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    edtName.setText("");
                    edtNum.setText("");
                    Toast.makeText(getApplicationContext(), "입력하신 모든 내용을 취소합니다!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    } //onCreate()


    public void Click(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.guide:
                anim();
                break;
            case R.id.gdSearch:
                anim();
                break;
            case R.id.gdMark:
                anim();
                break;
            case R.id.gdContact:
                anim();
                break;
            case R.id.gdCall:
                anim();
                break;
        }
    } //Click()

    public void anim() {
        if (isGdOpen) {
            gdSearch.startAnimation(guide_close);
            gdMark.startAnimation(guide_close);
            gdContact.startAnimation(guide_close);
            gdCall.startAnimation(guide_close);
            gdSearch.setClickable(false);
            gdMark.setClickable(false);
            gdContact.setClickable(false);
            gdCall.setClickable(false);
            isGdOpen = false;

        } else {
            gdSearch.startAnimation(guide_open);
            gdMark.startAnimation(guide_open);
            gdContact.startAnimation(guide_open);
            gdCall.startAnimation(guide_open);
            gdSearch.setClickable(true);
            gdMark.setClickable(true);
            gdContact.setClickable(true);
            gdCall.setClickable(true);
            isGdOpen = true;
        }
    } //anim()

    // 이름 정보를 통해 주소록에서 전화번호 획득하는 함수
    private String getPhoneNumber(Context context, String strName) {

        Cursor phoneCursor = null;
        String strReturn = "";
        try {
            // 주소록이 저장된 URI
            Uri uContactsUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

            // 주소록의 이름과 전화번호의 이름
            String strProjection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;

            // 주소록을 얻기 위한 쿼리문을 날리고 커서를 리턴 (이름으로 정렬해서 가져옴)
            phoneCursor = context.getContentResolver().query(uContactsUri, null, null, null, strProjection);
            phoneCursor.moveToFirst();
            Log.d("TAG", "AddressMgr address count = " + phoneCursor.getCount());

            String name;
            String number;
            int nameColumn = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int numberColumn = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            int NumberTypeColumn = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);

            // 정보 찾기
            while (!phoneCursor.isAfterLast() && strReturn.equals("")) {
                name = phoneCursor.getString(nameColumn);
                number = phoneCursor.getString(numberColumn);
                int numberType = Integer.valueOf(phoneCursor.getString(NumberTypeColumn));

                // 이름에서 특수문자 제거
                if (name != null && number != null) {
                    String name2 = name.replaceAll("[^\uAC00-\uD7A3xfe0-9a-zA-Z\\s]", "");
                    String name3 = name2.replace(" ", "");

                    // 번호에서 '-' 제거
                    String number2 = number.replace("-", "");

                    Log.d("AddressMgr", "AddressMgr name:" + name3 + " number:" + number2/*+ " email:" + email*/);


                    // 이름과 일치하는 정보 찾으면 번호만 return 하고 loop 종료
                    if (name3.equals(strName)) {
                        strReturn = number2;
                    }
                }

                phoneCursor.moveToNext();

            } //while()
        } catch (Exception e) {
            Log.e("[Get] getContactData", e.toString());
        } finally {
            if (phoneCursor != null) {
                phoneCursor.close();
            }
        }

        return strReturn;

    } //getPhoneNumber()

    // 번호 정보를 통해 주소록에서 이름을 획득하는 함수 (많이 보던 번호인데, 누구였는지 알고 싶을 때)
    private String getPhoneName(Context context, String strNum) {
        Cursor phoneCursor = null;
        String numReturn = "";
        try {

            // 주소록이 저장된 URI
            Uri uContactsUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

            // 주소록의 이름과 전화번호의 이름
            String strProjection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;

            // 주소록을 얻기 위한 쿼리문을 날리고 커서를 리턴 (이름으로 정렬해서 가져옴)
            phoneCursor = context.getContentResolver().query(uContactsUri, null, null, null, strProjection);
            phoneCursor.moveToFirst();
            Log.d("TAG", "AddressMgr address count = " + phoneCursor.getCount());

            String name;
            String number;
            int nameColumn = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int numberColumn = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            int NumberTypeColumn = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);

            // 정보 찾기
            while (!phoneCursor.isAfterLast() && numReturn.equals("")) {
                name = phoneCursor.getString(nameColumn);
                number = phoneCursor.getString(numberColumn);
                int numberType = Integer.valueOf(phoneCursor.getString(NumberTypeColumn));

                // 전화번호에서 '-' 제거
                if (name != null && number != null) {
                    String number2 = number.replace("-", "");

                    //이름에서 특수문자 제거
                    String name2 = name.replaceAll("[^\uAC00-\uD7A3xfe0-9a-zA-Z\\s]", "");

                    Log.d("AddressMgr", "AddressMgr name:" + name2 + " number:" + number2/*+ " email:" + email*/);

                    // 번호와 일치하는 정보 찾으면 이름만 return 하고 loop 종료
                    if (number2.equals(strNum)) {
                        numReturn = name2;
                    }
                }

                phoneCursor.moveToNext();

            } //while()
        } catch (Exception e) {
            Log.e("[Get] getContactData", e.toString());
        } finally {
            if (phoneCursor != null) {
                phoneCursor.close();
            }
        }

        return numReturn;

    } //getPhoneName()

    // 전화걸기 함수
    public boolean phoneCall(Context context, String number) {
        Intent iCall = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
        iCall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(iCall);
        return true;

    } //phoneCall()

    public boolean toastMsg(Toast msg) {
        msg.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        msg.show();
        return true;
    } //toastMsg()

    public boolean setFalse() {
        edtName.setEnabled(false);
        edtNum.setEnabled(false);
        btnSearch.setEnabled(false);
        btnRing.setEnabled(false);
        btnContact.setEnabled(false);
        btnMark.setEnabled(false);
        listMark.setEnabled(false);

        return true;
    } //setFalse()

    public boolean setTrue() {
        edtName.setEnabled(true);
        edtNum.setEnabled(true);
        btnSearch.setEnabled(true);
        btnRing.setEnabled(true);
        btnContact.setEnabled(true);
        btnMark.setEnabled(true);
        listMark.setEnabled(true);

        return true;
    } //setTrue()


}
