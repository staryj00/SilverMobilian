package com.swu.silvermobilian.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.swu.silvermobilian.Bean.Phone.NumItem;
import com.swu.silvermobilian.Bean.Main.UserBean;
import com.swu.silvermobilian.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.UUID;

public class NumAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private ArrayList<NumItem> data;
    private Context context;
    private int layout;
    public Button btnDel;

    ArrayList<NumItem> items = null;


    public NumAdapter(ArrayList<NumItem> datas, int layout, Context context) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.data = datas;
        this.layout = layout;
        this.context=context;
    } //NumAdapter()

    @Override
    public int getCount() { // 리스트 Item 개수 count
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position).getPnum();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.num_item, parent, false);
        }
        items = new ArrayList<>();

        btnDel = convertView.findViewById(R.id.btnDel);
        NumItem numItem = data.get(position);

        // 이름 연동
        TextView pname = convertView.findViewById(R.id.txtName);
        pname.setText(numItem.getPname());

        // 전화번호 연동
        final TextView pnum = convertView.findViewById(R.id.txtNum);
        pnum.setText(numItem.getPnum());

        // 즐겨찾기 삭제 버튼
        btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                removeDialog(position);

            }
        });

        return convertView;

    } //getView()

    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;

    UserBean bean;

    //즐겨찾기 삭제 다이얼
    private void removeDialog(final int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // 1.다이얼 제목 '삭제할 이름'으로
        final String text = data.get(position).getPname();
        builder.setTitle(text);
        builder.setMessage("즐겨찾기에서 삭제하시겠어요?");

        mAuth= FirebaseAuth.getInstance();
        mDatabase= FirebaseDatabase.getInstance();

        builder.setPositiveButton("네", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Toast.makeText(context,"삭제되었습니다!", Toast.LENGTH_SHORT).show();

                //데이터를 Firebase로 부터 가져온다.
                String emailUUID = getUserIdFromUUID(mAuth.getCurrentUser().getEmail());
                Log.i("될 것이다", mAuth.getCurrentUser().getEmail());
                mDatabase.getReference().child("User").child(emailUUID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //실시간으로 서버가 변경된 내용이 있을 경우 호출된다.

                        //리스트를 서버로 부터 온 데이터로 새로 만든다.
                        bean = dataSnapshot.getValue(UserBean.class);
                        mDatabase.getReference().child(bean.getMyname() + " " + "List").orderByChild("name").equalTo(text).addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                dataSnapshot.getRef().removeValue();
                            }

                            @Override
                            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                            }

                            @Override
                            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

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

                data.remove(position);
                notifyDataSetChanged();
            }
        });

        builder.setNegativeButton("아니요",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Toast.makeText(context,"취소되었습니다!",Toast.LENGTH_SHORT).show();
                    }
                });
        builder.show();

    } //removeDialog()

    //이메일의 문자 기준으로 고유번호를 뽑는다.
    public static String getUserIdFromUUID(String userEmail) {
        long val = UUID.nameUUIDFromBytes(userEmail.getBytes()).getMostSignificantBits();
        return val + "";
    }
}
