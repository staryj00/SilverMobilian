package com.swu.silvermobilian.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.swu.silvermobilian.Bean.Message.SendBean;
import com.swu.silvermobilian.Bean.Message.Util;
import com.swu.silvermobilian.R;
import com.google.gson.Gson;

import java.util.List;

public class ChatAdapter extends BaseAdapter {

    /** 인텐트에 컨텐츠 텍스트를 실어보내기 위한 키 값**/
    public static  final String KEY_FOUND_CONTENTS = "keyFoundContents";
    /** 인텐트에 ListData 클래스를 통째로 실어보내기 위한 키값**/
    public static final String KEY_FOUND_DATA_CLASS = "keyFoundDataClass";

    private Context mContext;
    private List<SendBean> chatList;
    private int mSendBeanIdx;
    private LayoutInflater mInflater;

    public ChatAdapter(Context context, int sendBeanIdx, List<SendBean> list) {
        mContext = context;
        chatList = list;
        mSendBeanIdx = sendBeanIdx;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return chatList.size();
    }

    @Override
    public Object getItem(int position) {
        return chatList.get(position);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public View getView(int position, View convertView, ViewGroup parent){

        convertView = mInflater.inflate(R.layout.chat_view, null);

        final SendBean bean = chatList.get(position);

        TextView txtWrite = convertView.findViewById(R.id.txtContent);
        txtWrite.setText(bean.getComment());
        TextView txtDate = convertView.findViewById(R.id.txtDate);
        txtDate.setText(bean.getRegDate());

        Button btnDel = convertView.findViewById(R.id.btnDel);
        btnDel.setTag(position);
        btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Gson gson = new Gson();
                String jsonStr = Util.openFile(mContext, SendBean.class.getName());
                SendBean sendBean = gson.fromJson(jsonStr, SendBean.class);

                Integer index = (Integer) v.getTag();

                if(index != null) {
                    chatList.remove(index.intValue());

                    //저장
                    sendBean.getSBList().get(mSendBeanIdx).getCBList().remove(index.intValue());
                    String jsonStr2 = gson.toJson(sendBean);
                    Util.saveFile(mContext, SendBean.class.getName(),jsonStr2);

                    ChatAdapter.this.notifyDataSetInvalidated(); //어댑터 리프레쉬
                }
            }
        });

        return convertView;
    }
}
