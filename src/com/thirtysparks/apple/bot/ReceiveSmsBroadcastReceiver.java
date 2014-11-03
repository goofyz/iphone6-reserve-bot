package com.thirtysparks.apple.bot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsMessage;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Kelly Lau on 30/10/2014.
 */
public class ReceiveSmsBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "ReceiveSmsBroadcastReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {

        if (null != intent) {
            Bundle bundle = intent.getExtras();
            Log.d(TAG, "Received SMS intent");

            if (null != bundle) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                SmsMessage[] smsMessage = new SmsMessage[pdus.length];
                String [] allMessageContent = new String[pdus.length];

                for (int i = 0; i < pdus.length; i++) {
                    smsMessage[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    allMessageContent[i] = smsMessage[i].getMessageBody();
                    for(String message:allMessageContent){
                        if(message != null) {
                            String smsPattern = "你的註冊代碼為 (Your registration code is) ";
                            int idx = message.indexOf(smsPattern);
                            if(idx > -1){
                                String smsCode = message.substring(idx + smsPattern.length());
                                Log.d(TAG, "Matched SMS code: " + smsCode);
                            }
                        }
                    }
                }
            }
        }
    }

    private void broadcastMessageToActivity(Context context, String msg) {
        Intent in = new Intent(MainActivity.BROADCAST_RECEIVE_SMS);
        in.putExtra(MainActivity.KEY_RECEIVE_SMS_RESULT, msg);
        LocalBroadcastManager.getInstance(context).sendBroadcast(in);
    }
}
