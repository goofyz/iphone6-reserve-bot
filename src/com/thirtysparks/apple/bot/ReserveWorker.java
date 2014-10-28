package com.thirtysparks.apple.bot;

import android.util.Log;
import com.squareup.okhttp.*;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URLDecoder;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

public class ReserveWorker {
    private static final String TAG = "AppleReserveWorker";

    OkHttpClient okHttpClient;
    Map<String, String> loginPageQueryString;

    public ReserveWorker() {
        okHttpClient = new OkHttpClient();
        okHttpClient.setFollowSslRedirects(true);

        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        okHttpClient.setCookieHandler(cookieManager);

        loginPageQueryString = new HashMap<String, String>();
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public static Map<String, String> extractQueryString(String url) {
        String param = url.substring(url.indexOf("?") + 1);
        if (param.indexOf("#") > -1) {
            param = param.substring(0, param.indexOf("#"));
        }
        String paramsStr[] = param.split("&");
        Map<String, String> params = new HashMap<String, String>();
        for (String str : paramsStr) {
            String keyVal[] = str.split("=");
            if (keyVal.length == 2 && keyVal[0].length() > 0 && keyVal[1].length() > 0) {
                params.put(keyVal[0], keyVal[1]);
            }
        }

        return params;
    }

    public String visitFirstPage() throws Exception {
        Request request = new Request.Builder()
                .url("https://reserve-hk.apple.com/HK/en_HK/reserve/iPhone")
                .build();
        Response response = okHttpClient.newCall(request).execute();

        String resultUrl = response.request().url().toString();


        loginPageQueryString = extractQueryString(resultUrl);
        Log.d(TAG, "Path is " + loginPageQueryString.get("path"));

        return resultUrl;
    }


    public synchronized String loginWithCaptcha(String captchaInput, String appleId, String password) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("openiForgotInNewWindow", "true");
        params.put("fdcBrowserData", "{\"U\":\"Mozilla/5.0 (Windows NT 6.3; WOW64; rv:32.0) Gecko/20100101 Firefox/32.0\",\"L\":\"en-US\",\"Z\":\"GMT+08:00\",\"V\":\"1.1\",\"F\":\"TF1;016;;;;;;;;;;;;;;;;;;;;;;Mozilla;Netscape;5.0%20%28Windows%29;20100101;undefined;true;Windows%20NT%206.3%3B%20WOW64;true;Win32;undefined;Mozilla/5.0%20%28Windows%20NT%206.3%3B%20WOW64%3B%20rv%3A32.0%29%20Gecko/20100101%20Firefox/32.0;en-US;undefined;signin.apple.com;undefined;undefined;undefined;undefined;false;false;" + GregorianCalendar.getInstance().getTime().getTime() + ";8;6/7/2005%2C%209%3A33%3A44%20PM;1920;1080;;12.0;;;;2013;12;-480;-480;9/22/2014%2C%209%3A13%3A52%20AM;24;1920;1040;0;0;Adobe%20Acrobat%7CAdobe%20PDF%20Plug-In%20For%20Firefox%20and%20Netscape%2011.0.06;;;;;Shockwave%20Flash%7CShockwave%20Flash%2012.0%20r0;;;;;;;;;;;;;18;;;;;;;\"}");

        params.put("appleId", appleId);
        params.put("accountPassword", password);
        params.put("captchaInput", captchaInput);
        params.put("captchaAudioInput", "");
        params.put("appIdKey", "db0114b11bdc2a139e5adff448a1d7325febef288258f0dc131d6ee9afe63df3");
        params.put("language", "HK-EN");
        params.put("path", URLDecoder.decode(loginPageQueryString.get("path")));
        params.put("rv", "3");
        params.put("sslEnabled", "true");
        params.put("Env", "PROD");
        params.put("captchaType", "image");
        params.put("captchaToken", "");

        FormEncodingBuilder builder = new FormEncodingBuilder();
        for (String key : params.keySet()) {
            builder.add(key, params.get(key));
        }
        RequestBody formBody = builder.build();
        Request request = new Request.Builder()
                .url("https://signin.apple.com/IDMSWebAuth/authenticate")
                .post(formBody)
                .build();
        Response response =  okHttpClient.newCall(request).execute();


        String returnResponse = response.request().url().toString();
        return returnResponse;
    }
}
