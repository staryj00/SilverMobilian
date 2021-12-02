package com.swu.silvermobilian.Message;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.swu.silvermobilian.R;

public class SearchActivity extends AppCompatActivity {

    private EditText etxt;
    private Button btnSearch;
    private Button btnUndo;
    private String name;
    private String number;

    InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        etxt = findViewById(R.id.etxt);
        btnSearch = findViewById(R.id.btnSearch);
        btnUndo = findViewById(R.id.btnUndo);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = etxt.getText().toString(); // 사용자가 입력한 이름

                // 입력한 이름으로 주소록에서 검색
                if (name.length() != 0) {
                    number = getPhoneNumber(SearchActivity.this, name); // 이름으로 검색한 번호

                    // 주소록에 있음
                    if (number.length() != 0) {
                        imm.hideSoftInputFromWindow(btnSearch.getWindowToken(),0);

                        Intent i = new Intent(getApplicationContext(), SendWriteActivity.class);
                        i.putExtra("number",number);
                        i.putExtra("name",name);
                        Toast.makeText(getApplicationContext(), "전화 번호가 검색되었습니다!", Toast.LENGTH_SHORT).show();
                        Log.i("**검색 결과 =====> ", number);
                        Log.i("**검색 결과 =====> ", name);
                        startActivity(i);
                        finish();
                    }

                    // 주소록에 없음
                    if (number.length() == 0) {
                        Log.i("**검색 결과 =====> ", number);
                        Toast.makeText(getApplicationContext(), "주소록에 없습니다!", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
                else if(name.length() == 0) {
                    Toast.makeText(getApplicationContext(), "이름을 입력해주세요!", Toast.LENGTH_LONG).show();
                }


            }
        });

         btnUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    } //onCreate()

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

            String name = "";
            String number = "";
            int nameColumn = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int numberColumn = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            int NumberTypeColumn = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);

            // 정보 찾기
            while (!phoneCursor.isAfterLast() && strReturn.equals(""))
            {
                name = phoneCursor.getString(nameColumn);
                number = phoneCursor.getString(numberColumn);
                int numberType = Integer.valueOf(phoneCursor.getString(NumberTypeColumn));

                // 이름에서 특수문자 제거
                if(name != null && number != null) {
                    String name2 = name.replaceAll("[^\uAC00-\uD7A3xfe0-9a-zA-Z\\s]", "");
                    String name3 = name2.replace(" ","");

                    // 번호에서 '-' 제거
                    String number2 = number.replace("-", "");

                    Log.d("AddressMgr", "AddressMgr name:" + name3 + " number:" + number2/*+ " email:" + email*/);


                    // 이름과 일치하는 정보 찾으면 번호만 return 하고 loop 종료
                    if (name3.equals(strName)) {
                        strReturn = number2;
                    }
                }

                name = "";
                number = "";
                phoneCursor.moveToNext();

            } //while()
        }
        catch (Exception e)
        {
            Log.e("[Get] getContactData", e.toString());
        }
        finally {
            if (phoneCursor != null)
            {
                phoneCursor.close();
                phoneCursor = null;
            }
        }

        return strReturn;

    } //getPhoneNumber()

}
