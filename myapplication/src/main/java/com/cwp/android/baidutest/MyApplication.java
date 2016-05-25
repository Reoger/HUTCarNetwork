package com.cwp.android.baidutest;


import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.baidu.mapapi.SDKInitializer;
import java.util.concurrent.Semaphore;

import cn.bmob.v3.Bmob;
import just.services.InfoSyncToCloudService;

/**
 * Created by Just on 2016/5/4.
 */
public class MyApplication extends Application {
    public static final String NOT_LANDING="NOT_LANDING";
    public static final String NULL_NAME="NULL_NAME";
    public static final String FILE_NAME="LoginInfo";

    public static final String USERNAME_INFO="username";
    public static final String NAME_INFO="name";

    private static Context mContext;
    public static Semaphore mSyncSemaphore;
    private static String USERNAME;
    private static String NAME;


    @Override
    public void onCreate() {
        super.onCreate();
        mContext=getApplicationContext();
        init();
        autoLogin();
    }

    public static void init() {
        Bmob.initialize(mContext,"11c50a59fafd8add5a2c19107b769f9d"); //Bmob相关初始化
        SDKInitializer.initialize(mContext);//百度地图初始化
    }

//    //当用账号登陆的时候，需要开启一个服务从云端同步数据至本地
//    public static void startSyncFromCloudService() {
//
//    }

    //当记住了账号的时候，直接开启将本地数据同步至云端的服务
    public static void startSyncToCloudService() {
        mSyncSemaphore=new Semaphore(1);
        Intent intentService=new Intent(mContext,InfoSyncToCloudService.class);
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

    public static String getName() {
        return NAME;
    }

    public static boolean isLanded() {
        return !USERNAME.equals(NOT_LANDING);
    }

    public static void logoutCurrentAccount() {
        USERNAME=NOT_LANDING;
        NAME=NULL_NAME;
        SharedPreferences.Editor editor = mContext.getSharedPreferences(FILE_NAME,
                MODE_PRIVATE).edit();
        editor.putString(USERNAME_INFO, NOT_LANDING);
        editor.putString(NAME_INFO, NULL_NAME);
        editor.apply();
    }

    public static void saveLoginInfo(String username,String name) {
        SharedPreferences.Editor editor = mContext.getSharedPreferences(MyApplication.FILE_NAME,
                MODE_PRIVATE).edit();
        editor.putString(MyApplication.USERNAME_INFO, username);
        editor.putString(MyApplication.NAME_INFO, name);
        editor.commit();
        USERNAME=username;
        NAME=name;
    }

    private static void autoLogin() {
        SharedPreferences pref = mContext.getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        String username = pref.getString(USERNAME_INFO, NOT_LANDING);
        String name = pref.getString(NAME_INFO, NULL_NAME);
        MyApplication.setUsername(username);
        MyApplication.setName(name);
    }
}
