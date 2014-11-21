package com.thirtysparks.apple.bot;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class SendSmsBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "SendSmsBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (null != intent) {
            Log.d(TAG, "Got Sent intent");
            boolean success = false;
            if(getResultCode() == Activity.RESULT_OK) {
                success = true;
            }

            Log.d(TAG, "Sent result: " + success);
            broadcastToMainActivity(context, success);
        }
    }

    private void broadcastToMainActivity(Context context, boolean success) {
        Intent in = new Intent(MainActivity.BROADCAST_SEND_SMS);
        in.putExtra(MainActivity.KEY_SEND_SMS_RESULT, success);
        LocalBroadcastManager.getInstance(context).sendBroadcast(in);
    }
}
