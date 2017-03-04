package com.cwp.android.baidutest;


import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.baidu.mapapi.SDKInitializer;

import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.Semaphore;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobRealTimeData;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.ValueEventListener;
import just.beans.AutoInfo;
import just.beans.MaInfo;
import just.constants.AutoInfoConstants;
import just.operations.AutoInfoLocalDBOperation;
import just.operations.MaInfoLocalDBOperation;
import just.services.InfoSyncToCloudService;
import just.services.InfoSyncToLocalService;

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
    public static Semaphore mSyncSemaphore=new Semaphore(1);
    private static String USERNAME;
    private static String NAME;

    private static Handler mHandler=new Handler(Looper.getMainLooper()) {
        String result="";
        String auto="";

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    result += "汽油低于20%,请及时加油\n";
                    Log.d("测试->MyApplication","onDataChange->汽油++++");
                    break;
                case 2:
                    result += "里程数超过"+((int)msg.obj*15000)+"km,需要进行维护\n";
                    Log.d("测试->MyApplication","onDataChange->里程数++++");
                    break;
                case 3:
                    result += "发动机存在异常\n";
                    Log.d("测试->MyApplication","onDataChange->发动机++++");
                    break;
                case 4:
                    result += "车灯存在异常\n";
                    Log.d("测试->MyApplication","onDataChange->车灯++++");
                    break;
                case 5:
                    result += "变速器存在异常\n";
                    Log.d("测试->MyApplication","onDataChange->变速器++++");
                    break;
                //结束检测,发送通知
                case 0:
                    Log.d("测试->MyApplication","result="+result);
                    //先要判断是否符合发送条件
                    if (!result.equals("")) {
                        Log.d("测试->MyApplication","onDataChange->开始发送通知");
                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
                        mBuilder.setWhen(System.currentTimeMillis())// 通知产生的时间，会在通知信息里显示
                                .setTicker("汽车维护提醒")
                                .setContentTitle(auto)
                                .setContentText(result)
                                .setSmallIcon(R.drawable.ic_logo);
                        Notification notify = mBuilder.build();
                        notify.flags = Notification.FLAG_AUTO_CANCEL;
                        NotificationManager manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                        manager.notify(3, notify);
                        result="";
                    }
                    Log.d("测试->MyApplication","onDataChange->通知发送完毕");
                    auto="";
                    break;
                //先获取汽车信息
                case -1:
                    Log.d("测试->MyApplication","onDataChange->获取相关汽车信息");
                    auto += msg.obj;
                    break;
                default:break;
            }
        }
    };

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

    //当记住了账号的时候，直接开启将两端的数据同步
    public static void startSyncService() {
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

    public static void startMaInfoMonitoring() {

            //首先开启对MaInfo表的实时同步
            BmobRealTimeData rtd = new BmobRealTimeData();

            //start应该在主线程中启动？
            //会影响到维护信息等的添加
            rtd.start(mContext, new ValueEventListener() {
                @Override
                public void onConnectCompleted() {
                    //子线程
                    Log.d("测试->MyApplication", "BmobRealTimeData->currentThread=" + Thread.currentThread());
                    Log.d("测试->MyApplication", "BmobRealTimeData->连接成功:" + rtd.isConnected());

                    //如果连接成功
                    if (rtd.isConnected()) {
                        rtd.subTableUpdate("MaInfo");
                    }
                }

                @Override
                public void onDataChange(JSONObject jsonObject) {
                    //主线程

                    new Thread(() -> {
                        Log.d("测试->MyApplication", "onDataChange->currentThread=" + Thread.currentThread());
                        Log.d("测试->MyApplication", "onDataChange->(" + jsonObject.optString("action") + ")\n数据" + jsonObject);

                        if (BmobRealTimeData.ACTION_UPDATETABLE.equals(jsonObject.optString("action"))) {
                            JSONObject data = jsonObject.optJSONObject("data");

                            Log.d("测试->MyApplication", "onDataChange->开启新线程");
                            String username = data.optString("username");
                            String vin = data.optString("vin");

                            try {
                                mSyncSemaphore.acquire();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            //先查询是否是属于自己的汽车维护信息的变更（可以考虑用云端代码判断以及检测数据是否符合发送推送的要求）
                            List<AutoInfo> list = AutoInfoLocalDBOperation.queryBy(mContext,
                                    AutoInfoConstants.COLUMN_USERNAME + " = ? and " + AutoInfoConstants.COLUMN_VIN + " = ?", new String[]{username, vin});
                            if (list.size() != 0) {
                                Log.d("测试->MyApplication", "onDataChange->属于自己的维护信息");
                                //先传递汽车信息
                                AutoInfo autoInfo = list.get(0);
                                String brand = autoInfo.getBrand();
                                String model = autoInfo.getModel();
                                String plateNum = autoInfo.getLicensePlateNum();
                                Message message = Message.obtain();
                                message.what = -1;
                                message.obj = brand + " " + model + " " + plateNum;
                                //如果是属于自己的汽车维护信息，则检测数据是否发到发送推送
                                if (data.optInt("gasolineVolume") < 20) {//汽油量<20%
                                    //通知告诉汽车车主该去加油
                                    mHandler.sendEmptyMessage(1);
                                }

                                //怎么判断每超过15000公里倍数？
                                //查询上一次的是否超过1500的倍数，如果上次(需要考虑该次是第一次，即没有上一次)超过（就表明上次推送过了），此次就不需要再推送了

                                //发送通知告诉汽车车主需要进行维护

                                //因无车载设备把汽车数据传给服务器，因此功能需求5.E.a-c,可手动更改数据库的值进行功能测试。（进入决赛后，此功能必需可测）
                                //所以这里的意思是说在服务器中已有的数据基础上进行手动修改
                                Semaphore semaphore = new Semaphore(1);
                                double mileage = data.optDouble("mileage");
                                //如果mileage小于15000，则表明还不需要进行维护
                                if (mileage >= 15000) {
                                    try {
                                        semaphore.acquire();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    String scanTime = data.optString("scanTime");
                                    BmobQuery<MaInfo> query = new BmobQuery<MaInfo>();
                                    query.addWhereEqualTo("username", username);
                                    query.addWhereEqualTo("vin", vin);
                                    query.addWhereLessThan("scanTime", scanTime);
                                    query.addQueryKeys("scanTime");
                                    query.order("-scanTime");
                                    final Semaphore finalSemaphore = semaphore;
                                    query.findObjects(mContext, new FindListener<MaInfo>() {
                                        @Override
                                        public void onSuccess(List<MaInfo> list) {
                                            int current = (int) mileage / 15000;
                                            Log.d("测试","current="+current+",mileage="+mileage);
                                            if(list.size()==0) {
                                                message.obj = current;
                                                message.what = 2;
                                                mHandler.sendMessage(message);
                                            }
                                            else {
                                                double lastMileage = list.get(0).getMileage();
                                                int last = (int) lastMileage / 15000;
                                                if (current > last) {
                                                    Message message = Message.obtain();
                                                    message.obj = current;
                                                    message.what = 2;
                                                    mHandler.sendMessage(message);
                                                }
                                            }
                                            finalSemaphore.release();
                                        }

                                        @Override
                                        public void onError(int i, String s) {
                                            finalSemaphore.release();
                                        }
                                    });
                                }

                                try {
                                    semaphore.acquire();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                //当服务器端的数据库里记录的发动机出现异常、变速器出现异常或车灯有坏的时候，给手机发送通知告诉汽车车主需要进行维修
                                if (data.optString("enginePerfor").equals("异常")) {//发动机性能异常
                                    mHandler.sendEmptyMessage(3);
                                }
                                if (data.optString("lamp").equals("异常")) {//车灯异常
                                    mHandler.sendEmptyMessage(4);
                                }
                                if (data.optString("transmissionPerfor").equals("异常")) {//变速器性能异常
                                    mHandler.sendEmptyMessage(5);
                                }
                                semaphore.release();

                                //如果有异常，就进行推送（只需要在本地进行推送）
                                mHandler.sendEmptyMessage(0);
                                mSyncSemaphore.release();
                                Log.d("测试++++++++++++++++++++", "++++++++++++++++");
                            }
                            else {
                                mSyncSemaphore.release();
                            }
                        }
                    }).start();
                }
            });
    }
}
