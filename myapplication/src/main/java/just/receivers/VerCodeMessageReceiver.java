package just.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class VerCodeMessageReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        Object[] pdus = (Object[]) bundle.get("pdus"); // 提取短信消息
        SmsMessage[] messages = new SmsMessage[pdus.length];
        for (int i = 0; i < messages.length; i++) {
            messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
        }
        String address = messages[0].getOriginatingAddress(); // 获取发送方号码
        if(address.equals("10690563168910245")) {
            StringBuilder fullMessage = new StringBuilder();
            for (SmsMessage message : messages) {
                fullMessage.append(message.getMessageBody()); // 获取短信内容
            }
            fullMessage.substring(12,19);
        }
    }
}
