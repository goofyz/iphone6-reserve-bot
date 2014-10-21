package com.thirtysparks.apple.bot;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;

import java.util.GregorianCalendar;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    ReserveWorker reserveWorker;

    TextView tvMsg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivty_main);

        tvMsg = (TextView)findViewById(R.id.tv_msg);

        reserveWorker = new ReserveWorker();
        goFrontPage();
    }

    private void goFrontPage(){
        tvMsg.setText("Visiting front page");
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
                    tvMsg.setText("Success. Redirected to login page");
                }
                else{
                    tvMsg.setText("Failed");
                }

                getCaptcha();

            }
        }.execute();
    }


    private void getCaptcha() {
        tvMsg.setText("Getting captcha");
        ImageView captchaImageView = (ImageView)findViewById(R.id.iv_captcha);
        String imageUrl = "https://signin.apple.com/IDMSWebAuth/imageCaptcha/942#" + GregorianCalendar.getInstance().getTime().getTime();
        Glide.with(MainActivity.this).load(imageUrl).into(captchaImageView);
    }
}
