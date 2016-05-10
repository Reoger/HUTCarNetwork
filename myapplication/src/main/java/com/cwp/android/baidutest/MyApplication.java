package com.cwp.android.baidutest;

import android.app.Application;
import android.content.Intent;

import com.baidu.mapapi.SDKInitializer;
import java.util.concurrent.Semaphore;

import cn.bmob.v3.Bmob;
import just.services.AutoInfoSyncService;

/**
 * Created by Just on 2016/5/4.
 */
public class MyApplication extends Application {
    public static Semaphore mSyncSemaphore;
    public static String USERNAME="13874939742";

    @Override
    public void onCreate() {
        super.onCreate();
        SDKInitializer.initialize(getApplicationContext());
        Bmob.initialize(getApplicationContext(),"f6344bf02fe34ae0c0dca856e9563a30");
        mSyncSemaphore=new Semaphore(1);
        Intent intentService=new Intent(this,AutoInfoSyncService.class);
        startService(intentService);
    }

    public void setUsername(String username) {
    }

    public static String getUsername() {
        return USERNAME;
    }
}
