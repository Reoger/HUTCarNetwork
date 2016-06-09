package com.cwp.android.baidutest;

import android.content.Context;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import utils.MyLog;

/**
 * Created by Adminis on 2016/5/28.
 */
public class Locate {

    /*
    *   mLatitue经度 ,mLongLatitue纬度
    *
    * */
    private BaiduMap mBaiduMap;
    private Context context;
    private double mLatitue;
    private double mLongLatitue;

    private String myAddress;
    boolean isFirstIn = true;

    //MainActivity中用到过,mLocationClient,用来打开和关闭mLocationClient
    LocationClient mLocationClient;
    MyLocationListener mLocationListener;


    public Locate(Context context, BaiduMap mBaiduMap) {

        this.context = context;
        this.mBaiduMap = mBaiduMap;
        MyLog.LogE("Locate", "Locate");
    }

         /*
           * LocationClient类是定位SDK的核心类，
           * 必须在主线程中声明。
           * 需要传入一个Context类型的参数，
           * 推荐使用getApplicationContext()来获取全局进程有效的context。
           * */

    public void initLocation() {
        MyLog.LogE("Locate", "initLocation");

        mLocationClient = new LocationClient(context);
        mLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(mLocationListener);

        LocationClientOption option = new LocationClientOption();
        //  坐标类型分为三种：国测局经纬度坐标系(gcj02)，百度墨卡托坐标系(bd09)，百度经纬度坐标系(bd09ll)。
        option.setCoorType("bd09ll");//设置坐标类型
        option.setIsNeedAddress(true);//默认不需要地址，返回的定位结果包含地址信息
        option.setOpenGps(true);//打开GPS
        option.setScanSpan(1000);//扫描时间,小于1000时，定位无效
        option.setNeedDeviceDirect(true);// 设置返回结果包含手机的方向

        mLocationClient.setLocOption(option);

    }

    private class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {

            MyLog.LogE("Locate", "onReceiveLocation");

            MyLocationData data = new MyLocationData.Builder()
                    .accuracy(location.getRadius())//
                    .direction(location.getDirection())
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude())
                    .build();
            mBaiduMap.setMyLocationData(data);

            /*
            *    用来显示状态和标志
            *
            * */

            MyLocationConfiguration configuration = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.COMPASS
                    , true, BitmapDescriptorFactory.fromResource(R.drawable.icon_car));

            mBaiduMap.setMyLocationConfigeration(configuration);

            myAddress = location.getAddrStr();
            MyLog.LogE("mLatitue", mLatitue + "location.getAddress();");

            //时时获取经纬度
            mLatitue = location.getLatitude();
            mLongLatitue = location.getLongitude();

            MyLog.LogE("mLatitue", mLatitue + "");
            MyLog.LogE("mLongLatitue", mLongLatitue + "");

            if (isFirstIn) {

                centerToMyLocation();
            }

        }
    }


    public void centerToMyLocation() {

        //定位功能
        LatLng latLng = new LatLng(mLatitue, mLongLatitue);
        //地图状态将要发生的变化

        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
        //设置地图中心点
        mBaiduMap.animateMapStatus(msu);

        isFirstIn = false;
        if (myAddress != null) {
            Toast.makeText(context, myAddress, Toast.LENGTH_SHORT).show();
            MainActivity.t.speak("当前位置"+myAddress);
        }

    }

    public double getmLatitue() {
        return mLatitue;
    }

    public double getmLongLatitue() {
        return mLongLatitue;
    }
}
