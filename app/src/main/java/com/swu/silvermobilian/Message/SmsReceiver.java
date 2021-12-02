package com.swu.silvermobilian.Message;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsMessage;
import android.util.Log;

import static android.content.ContentValues.TAG;

public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        SmsMessage[] messages = parseSmsMessage(bundle);
        if(messages != null && messages.length>0) {
            // 나에게 메세지를 보낸 번호 catch
            String sender = messages[0].getOriginatingAddress();

            // 나에게 메시지를 보낸 번호가 주소록에 있는지 검색
            String name = getPhoneName(context,sender);
            Log.i("**검색 결과 =====> ", name);

            Log.i(TAG, "SMS sender(수신 번호): " + sender);
            String contents = messages[0].getMessageBody();

            Log.i(TAG, "SMS contents(내용): " + contents);

            // 주소록에 있는 번호
            if (name.length() != 0 && sender.length() != 0){
                sendToList(context, name, contents); //받은 메시지 리스트에 추가
                sendToPush(context, name, contents); //받은 메시지 Push 알림
            }
            // 주소록에 없는 번호
            else if(name.length() == 0 && sender.length() != 0){
                sendToList(context, sender, contents); //받은 메시지 리스트에 추가
                sendToPush(context, sender, contents); //받은 메시지 Push 알림
            }

        }
    } //onReceive()

    /** SMS 메세지를 확인할 수 있도록 API에서 설정해둔 메소드**/
    private SmsMessage[] parseSmsMessage(Bundle bundle) {
        Object[] objs = (Object[]) bundle.get("pdus");
        SmsMessage[] messages = new SmsMessage[objs.length];
        int smsCount = objs.length;
        for (int i = 0; i < smsCount; i++) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                String format = bundle.getString("format");
                messages[i] = SmsMessage.createFromPdu((byte[]) objs[i], format);
            } else {
//                messages[i] = SmsMessage.createFromPdu((byte[]) objs[i]);
            }
        }
        return messages;
    } //parseSmsMessage()

    /** SmsActivity로 인텐트 보내기 **/
    // Push 창에 보내기
    private void sendToPush(Context context, String sender, String contents) {
        Intent myIntent = new Intent(context, PushActivity.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        myIntent.putExtra("sender", sender);
        myIntent.putExtra("contents", contents);
        context.startActivity(myIntent);
    } //sendToPush()

    // 문자 수신 리스트에 보내기
    private void sendToList(Context context, String sender2, String contents2) {
        Intent myIntent = new Intent(context, SmslistActivity.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        myIntent.putExtra("sender2", sender2);
        myIntent.putExtra("contents2", contents2);
        context.startActivity(myIntent);
    } //sendToList()

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
//            int NumberTypeColumn = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);

            // 정보 찾기
            while (!phoneCursor.isAfterLast() && numReturn.equals(""))
            {
                name = phoneCursor.getString(nameColumn);
                number = phoneCursor.getString(numberColumn);
//                int numberType = Integer.valueOf(phoneCursor.getString(NumberTypeColumn));

                // 전화번호에서 '-' 제거
                if(name != null && number != null) {
                    String number2 = number.replace("-", "");

                    //이름에서 특수문자 제거
                    String name2 = name.replaceAll("[^\uAC00-\uD7A3xfe0-9a-zA-Z\\s]","");

                    Log.d("AddressMgr", "AddressMgr name:" + name2 + " number:" + number2/*+ " email:" + email*/);

                    // 번호와 일치하는 정보 찾으면 이름만 return 하고 loop 종료
                    if (number2.equals(strNum)) {
                        numReturn = name2;
                    }
                }

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
            }
        }

        return numReturn;

    } //getPhoneName()
}
