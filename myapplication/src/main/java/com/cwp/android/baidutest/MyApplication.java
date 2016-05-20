package com.cwp.android.baidutest;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.baidu.mapapi.SDKInitializer;
import java.util.concurrent.Semaphore;

import cn.bmob.v3.Bmob;
import just.services.AutoAndMaInfoSyncService;

/**
 * Created by Just on 2016/5/4.
 */
public class MyApplication extends Application {
    private static Context mContext;
    public static Semaphore mSyncSemaphore;
    public static String USERNAME;
    public static String NAME;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext=getApplicationContext();

        init();
    }

    public static void init() {
        //        Bmob.initialize(mContext,"f6344bf02fe34ae0c0dca856e9563a30"); //Just
        Bmob.initialize(mContext,"ad3064090c6b457cd256c20d62639243"); //杰哥
        SDKInitializer.initialize(mContext);
        mSyncSemaphore=new Semaphore(1);
        Intent intentService=new Intent(mContext,AutoAndMaInfoSyncService.class);
        mContext.startService(intentService);
    }

    public static void setUsername(String username) {
        USERNAME=username;
    }

    public static String getUsername() {
        return USERNAME;
    }

    public static void setName(String name) {
        NAME=name;
    }

    public static String getNAME() {
        return NAME;
    }
}
