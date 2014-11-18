package com.thirtysparks.apple.bot;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp.OkHttpUrlLoader;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.*;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private static final String APPLE_ID = "ENTER_APPLIE_ID";
    private static final String PASSWORD = "ENTER_PASSWORD";
    private static final String PHONE_NUMBER = "ENTER_PHONE_NUMBER";
    private static final String GOV_ID = "ENTER_GOV_ID";
    private static final String GOV_ID_TYPE = "idHongkong";

    private static final String STORE_IFC = "R428";
    private static final String COLOR_GOLD = "Gold";
    private static final String MODEL_IPHONE6_PLUS_GROUP = "MG4E2ZP/A,MG492ZP/A,MG4J2ZP/A";
    public static final String MODEL_IPHONE6_PLUS_16GB = "MGAA2ZP/A";
    public static final String MODEL_IPHONE6_PLUS_NAME = "iPhone 6 Plus";

    public static final String BROADCAST_SEND_SMS = "com.thirtysparks.apple.bot.sms.send";
    public static final String BROADCAST_RECEIVE_SMS = "com.thirtysparks.apple.bot.sms.receive";
    public static final String KEY_SEND_SMS_RESULT = "com.thirtysparks.apple.bot.sms.send.result";
    public static final String KEY_RECEIVE_SMS_RESULT = "com.thirtysparks.apple.bot.sms.receive.result";

    ReserveWorker reserveWorker;
    String firstName, lastName;

    BroadcastReceiver localBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent != null){
                if(BROADCAST_SEND_SMS.equals(intent.getAction())){
                    boolean result = intent.getBooleanExtra(KEY_SEND_SMS_RESULT, false);
                    if(result){
                        addLog("Send SMS successfully");
                    }
                    else{
                        addLog("Failed to send SMS");
                    }
                }
                else if(BROADCAST_RECEIVE_SMS.equals(intent.getAction())){
                    String smsCode = intent.getStringExtra(KEY_RECEIVE_SMS_RESULT);
                    if(smsCode != null){
                        addLog("got reservation code: " + smsCode);
                        // submit reservation code
                        submitSmsReservationCode(smsCode);
                    }
                }
            }
        }
    };

    TextView tvMsg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivty_main);

        registerReceiver();

        tvMsg = (TextView)findViewById(R.id.tv_msg);

        reserveWorker = new ReserveWorker();

        findViewById(R.id.btn_captcha).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goLoginCaptcha();
            }
        });

        findViewById(R.id.btn_send_sms).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSms(((EditText)findViewById(R.id.et_sms_code)).getText().toString());
            }
        });

        goFrontPage();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(localBroadcastReceiver);
    }

    public void addLog(String s){
        tvMsg.setText(s + "\n" + tvMsg.getText());
    }

    private void registerReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_SEND_SMS);

        LocalBroadcastManager.getInstance(this).registerReceiver(localBroadcastReceiver, intentFilter);
    }

    private void goFrontPage(){
        addLog("Visiting front page");
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                String resultUrl = null;
                try {
                    resultUrl = reserveWorker.visitFirstPage();
                }
                catch(Exception e){
                    e.printStackTrace();
                }

                return resultUrl;
            }

            @Override
            protected void onPostExecute(String resultString) {
                //update the UI

                Log.d(TAG, "Result url is " + resultString);
                boolean isLogin = (resultString.startsWith("https://signin.apple.com/IDMSWebAuth/"));
                if(isLogin){
                    addLog("Success. Redirected to login page");
                }
                else{
                    addLog("Failed");
                }

                goGetCaptcha();

            }
        }.execute();
    }


    private void goGetCaptcha() {
        addLog("Getting captcha");
        ImageView captchaImageView = (ImageView)findViewById(R.id.iv_captcha);
        String imageUrl = "https://signin.apple.com/IDMSWebAuth/imageCaptcha/942#" + GregorianCalendar.getInstance().getTime().getTime();
        Glide.get(MainActivity.this).register(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(reserveWorker.getOkHttpClient()));
        Glide.with(MainActivity.this).load(imageUrl).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).listener(new RequestListener<String, GlideDrawable>() {
            @Override
            public boolean onException(Exception e, String s, Target<GlideDrawable> glideDrawableTarget, boolean b) {
                addLog("Error in loading captcha");
                addLog("Error in loading captcha");
                if(e != null){
                    addLog("Exception is " + e.getClass().getSimpleName() + ": " + e.getMessage());
                }
                else{
                    addLog("Exception is null");
                }
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable glideDrawable, String s, Target<GlideDrawable> glideDrawableTarget, boolean b, boolean b2) {
                addLog("Got Captcha, please enter the string. ");
                return false;
            }
        }).into(captchaImageView);
    }



    private void goLoginCaptcha() {
        addLog("Submitting captcha");

        final String captchaInput = ((EditText) findViewById(R.id.et_captcha)).getText().toString();
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params)  {
                String string = null;
                try {
                    string = reserveWorker.loginWithCaptcha(captchaInput, APPLE_ID, PASSWORD);
                }
                catch(Exception e){
                    e.printStackTrace();
                }

                return string;
            }

            @Override
            protected void onPostExecute(String s) {
                if ("https://reserve-hk.apple.com/HK/en_HK/reserve/iPhone?execution=e1s2".equals(s)) {
                    addLog("Apple ID Login successfully");

                    getSmsCode();
                } else {
                    addLog("Error: Apple ID Login failed");
                }
            }
        }.execute();
    }

    private void getSmsCode() {
        addLog("Getting SMS request code");
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String smsCode = null;
                try {
                    smsCode = reserveWorker.retrieveSmsCodePage();
                }
                catch(Exception e){
                    e.printStackTrace();
                }

                return smsCode;
            }

            @Override
            protected void onPostExecute(String smsCode) {
                if (smsCode != null) {
                    addLog("SMS Request code returned");

                    String[] splitString = smsCode.split(",");
                    byte[] decodedString = Base64.decode(splitString[1], Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    ((ImageView) findViewById(R.id.iv_sms_code)).setImageBitmap(decodedByte);
                }
            }
        }.execute();
    }

    private void sendSms(String code) {
        Intent intent = new Intent(BROADCAST_SEND_SMS);
        PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        SmsManager.getDefault().sendTextMessage("64500366", null, code, sentIntent, null);
        addLog("Sending SMS: " + code);
    }

    private void submitSmsReservationCode(final String smsReservationCode){
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params)  {
                String result = null;
                try{
                    result = reserveWorker.submitSmsCode(PHONE_NUMBER, smsReservationCode);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
                return result;
            }

            @Override
            protected void onPostExecute(String s) {
                //check submission result
                getSubmitResult();
            }
        }.execute();
    }

    private void getSubmitResult(){
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params)  {
                String result = null;
                try{
                    result = reserveWorker.getCommonAjax();
                }
                catch(Exception e){
                    e.printStackTrace();
                }
                return result;
            }

            @Override
            protected void onPostExecute(String jsonStr) {
                //parse the JSON

                try {
                    JSONObject jsonObject = new JSONObject(jsonStr);
                    JSONArray errors = jsonObject.getJSONArray("errors");
                    if(errors.length() > 0){
                        for(int i=0; i < errors.length(); i++){
                            addLog("Errors: " + errors.getString(i) );
                        }
                    }
                    else{
                        //we have reached page 3!
                    }
                } catch (JSONException jsonException) {
                    //NO ERROR, should be proceed
                } catch (NullPointerException e) {
                    addLog("Null pointer.  Please start again");
                }
            }
        }.execute();
    }

    private void doFinalStep(){
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params)  {
                List<String[]> timeSlotList = getTimeSlot(STORE_IFC);
                Map<String, Boolean> stockList = getStock(STORE_IFC, MODEL_IPHONE6_PLUS_GROUP);

                if(timeSlotList != null && stockList != null){
                    //do ordering
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void param) {
            }
        }.execute();
    }

    private List<String[]> getTimeSlot(String storeNumber){
        List<String[]> timeSlotList = null;
        try{
            String jsonStr = reserveWorker.getTimeSlots(storeNumber);

            timeSlotList = new ArrayList<String[]>();
            try {
                JSONObject json = new JSONObject(jsonStr);
                JSONArray timeSlotsJsonArray= json.getJSONArray("timeslots");
                for(int i=0; i < timeSlotsJsonArray.length(); i++){
                    JSONObject timeSlotJson = timeSlotsJsonArray.getJSONObject(i);
                    String timeSlotId = timeSlotJson.getString("timeSlotId");
                    String timeSlotTime = timeSlotJson.getString("formattedTime");
                    timeSlotList.add(new String[]{timeSlotId, timeSlotTime});
                }

                //we have the time slot now, check the stock;
            } catch (JSONException jsonException) {
                //NO ERROR, should be proceed
            } catch (NullPointerException e) {
                addLog("Null pointer.  Please start again");
            }

        }
        catch(Exception e){
            e.printStackTrace();
        }

        return timeSlotList;
    }

    private Map<String, Boolean> getStock(String storeNumber, String groupPartNumber){
        Map<String, Boolean> list = null;
        try{
            String jsonStr = reserveWorker.getAvailability(storeNumber, groupPartNumber);
            list = new HashMap<String, Boolean>();
            try {
                JSONObject json = new JSONObject(jsonStr);
                JSONArray timeSlotsJsonArray= json.getJSONArray("inventories");
                for(int i=0; i < timeSlotsJsonArray.length(); i++){
                    JSONObject timeSlotJson = timeSlotsJsonArray.getJSONObject(i);
                    String partNumber = timeSlotJson.getString("partNumber");
                    boolean available = timeSlotJson.getBoolean("available");
                    list.put(partNumber, available);
                }
                //we have the time slot now, check the stock;
            } catch (JSONException jsonException) {
                //NO ERROR, should be proceed
            } catch (NullPointerException e) {
                addLog("Null pointer.  Please start again");
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return list;
    }

    private String order(String storeNumber, String timeSlotId){
        String jsonStr = null;
        try {
            jsonStr = reserveWorker.submitOrder(COLOR_GOLD, APPLE_ID, firstName, lastName, GOV_ID, GOV_ID_TYPE, MODEL_IPHONE6_PLUS_NAME, MODEL_IPHONE6_PLUS_GROUP, storeNumber, timeSlotId);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return jsonStr;
    }
}
