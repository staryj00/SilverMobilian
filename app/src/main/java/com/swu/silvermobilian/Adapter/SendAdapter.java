package com.swu.silvermobilian.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.swu.silvermobilian.Bean.Message.SendBean;
import com.swu.silvermobilian.Bean.Message.Util;
import com.swu.silvermobilian.Message.ChatActivity;
import com.swu.silvermobilian.R;
import com.google.gson.Gson;

import java.util.List;

public class SendAdapter extends BaseAdapter {

    private Context mContext;
    private List<SendBean> mList;
    private LayoutInflater mInflater;


    /** 인텐트에 컨텐츠 텍스트를 실어보내기 위한 키 값**/
    public static  final String KEY_FOUND_CONTENTS = "keyFoundContents";
    /** 인텐트에 ListData 클래스를 통째로 실어보내기 위한 키값**/
    public static final String KEY_FOUND_DATA_CLASS = "keyFoundDataClass";

    Integer selIdx = 0;

    public SendAdapter(Context context, List<SendBean> list) {
        mContext = context;
        mList = list;
        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        // 리스트 갯수
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        //고유의 데이터가 넘어감
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // 인플레이터로 뷰 가져옴
        convertView = mInflater.inflate(R.layout.list_view, null);

        final SendBean bean = mList.get(position);

//        bean.setSelIdx(position);

        TextView txtTitle = convertView.findViewById(R.id.txtTitle);
        TextView txtContent = convertView.findViewById(R.id.txtContent);

        txtTitle.setText(bean.getPhone());
        txtContent.setText(bean.getContent());

        TextView txtDate = convertView.findViewById(R.id.txtDate);
        txtDate.setText(bean.getRegDate());


        Button btnDel = convertView.findViewById(R.id.btnDel);


        btnDel.setTag(position);



        // 삭제 버튼
        btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 선택된 삭제 버튼의 메모된 인덱스 번호를 취득
                selIdx = (Integer) v.getTag();

                SendBean sendBean = new SendBean();
                Gson gson = new Gson();

                String jsonStr = Util.openFile(mContext, SendBean.class.getName());
                SendBean sendBean1 = gson.fromJson(jsonStr, SendBean.class);

                if(selIdx != null) {
                    SendBean bean = mList.get(selIdx.intValue());
//                    bean.setSelIdx(selIdx.intValue());

                    mList.remove(selIdx.intValue());
                    sendBean1.getSBList().remove(selIdx.intValue());
                    String jsonStr2 = gson.toJson(sendBean1);
                    Util.saveFile(mContext, SendBean.class.getName(), jsonStr2);

                    SendAdapter.this.notifyDataSetInvalidated(); //어댑터 리프레쉬
                }

            }
        });

        // 한 row에 클릭 이벤트 적용
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(mContext, ChatActivity.class);
                i.putExtra(KEY_FOUND_CONTENTS, bean.getContent());
                i.putExtra(KEY_FOUND_DATA_CLASS, bean);

                mContext.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

            }
        });

        return convertView;
    }
}
