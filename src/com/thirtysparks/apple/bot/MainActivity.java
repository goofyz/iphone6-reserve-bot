package com.thirtysparks.apple.bot;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
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

import java.io.InputStream;
import java.util.GregorianCalendar;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private static final String APPLE_ID = "ENTER_APPLIE_ID";
    private static final String PASSWORD = "ENTER_PASSWORD";

    ReserveWorker reserveWorker;

    TextView tvMsg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivty_main);

        tvMsg = (TextView)findViewById(R.id.tv_msg);

        reserveWorker = new ReserveWorker();

        findViewById(R.id.btn_captcha).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goLoginCaptcha();
            }
        });

        goFrontPage();
    }

    private void addLog(String s){
        tvMsg.setText(s + "\n" + tvMsg.getText());
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
}
