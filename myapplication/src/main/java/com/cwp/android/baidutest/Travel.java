package com.cwp.android.baidutest;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.overlayutil.OverlayManager;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;

import utils.MyLog;

/**
 * Created by Adminis on 2016/5/29.
 */
public class Travel implements  OnGetRoutePlanResultListener {

    private OverlayManager routeOverlay = null;
    //路线的父类，包括多种路线，具体用时动态生成
    public RouteLine route = null;
    private BaiduMap mBaiduMap;
    //路径规划搜索接口 ,定义成static 为了在MainActivity中destroy掉
    private static RoutePlanSearch mSearch = null;


    public Travel(BaiduMap mBaiduMap) {

        this.mBaiduMap = mBaiduMap;
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(this);
    }

    public void search(PlanNode stNode,PlanNode enNode ) {

        //重置浏览节点的路线数据
        mBaiduMap.clear();
        route = null;

        //成功发起检索返回true , 失败返回false
        mSearch.drivingSearch((new DrivingRoutePlanOption())
                .from(stNode)
                .to(enNode));

    }

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {

    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult result) {

        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {

            MainActivity.t.speak("规划路线失败！");
//            Toast.makeText()
        }

        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            //起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            //result.getSuggestAddrInfo()
            return;
        }

        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            // nodeIndex = -1;

            MyLocationConfiguration configuration = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.FOLLOWING
                    , true, BitmapDescriptorFactory.fromResource(R.drawable.icon_car));

            mBaiduMap.setMyLocationConfigeration(configuration);

            //获取第一个路线
            MainActivity.t.speak("规划路线成功！");
            route = result.getRouteLines().get(0);

            MyLog.LogE("Travel", "***onGetDrivingRouteResult***");

            DrivingRouteOverlay overlay = new MyDrivingRouteOverlay(mBaiduMap);
            routeOverlay = overlay;
            mBaiduMap.setOnMarkerClickListener(overlay);
            overlay.setData(result.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
        }
    }

    @Override
    public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

    }


    //乘车路线覆盖物
    private class MyDrivingRouteOverlay extends DrivingRouteOverlay {

        /**
         * 构造函数
         *
         * @param baiduMap 该DrivingRouteOvelray引用的 BaiduMap
         */
        public MyDrivingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }


    }

}
