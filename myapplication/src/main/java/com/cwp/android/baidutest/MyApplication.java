package com.cwp.android.baidutest;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.baidu.mapapi.SDKInitializer;
import java.util.concurrent.Semaphore;

/**
 * Created by Just on 2016/5/4.
 */
public class MyApplication extends Application {
    public static Semaphore mSyncSemaphore;
    public static String USERNAME;

    @Override
    public void onCreate() {
        super.onCreate();
        SDKInitializer.initialize(getApplicationContext());
        mSyncSemaphore=new Semaphore(1);
    }

    public void setUsername(String username) {
//        USERNAME=username;
        USERNAME="13874939742";
    }

    public static String getUsername() {
        return USERNAME;
    }
}
