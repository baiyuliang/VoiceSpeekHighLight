package com.byl.voicetest;

import android.app.Application;

import com.iflytek.cloud.SpeechUtility;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SpeechUtility.createUtility(this, "appid=5b20bb1c");
    }
}
