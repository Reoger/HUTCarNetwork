package com.com.reoger.music.Utils;

import android.util.Log;

import com.com.reoger.music.constant.Constant;


/**
 * Created by 24540 on 2016/4/20.
 */
public class LogUtils {

    public static void d(String a,String b){
        if(Constant.mIsLog){
            Log.d(a, b);
        }
    }
    public static void i(String a,String b){
        if(Constant.mIsLog){
            Log.i(a, b);
        }
    }

    public static void w(String a,String b){
        if(Constant.mIsLog){
            Log.w(a, b);
        }
    }

    public static void e(String a,String b){
        if(Constant.mIsLog){
            Log.e(a, b);
        }
    }


}
