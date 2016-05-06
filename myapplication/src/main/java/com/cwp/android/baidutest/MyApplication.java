package com.cwp.android.baidutest;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;
import java.util.concurrent.Semaphore;

import cn.bmob.v3.Bmob;

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
        Bmob.initialize(getApplicationContext(),"f6344bf02fe34ae0c0dca856e9563a30");
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
