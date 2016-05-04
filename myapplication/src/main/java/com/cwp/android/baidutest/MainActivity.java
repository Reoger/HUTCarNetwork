package com.cwp.android.baidutest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.overlayutil.OverlayManager;
import com.baidu.mapapi.overlayutil.PoiOverlay;
import com.baidu.mapapi.overlayutil.TransitRouteOverlay;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiBoundSearchOption;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;

import java.util.Locale;


public class MainActivity extends Activity implements BaiduMap.OnMapClickListener,
        OnGetRoutePlanResultListener,View.OnClickListener {

    //地图相关，使用继承MapView的MyRouteMapView目的是重写touch事件实现泡泡处理
    //如果不处理touch事件，则无需继承，直接使用MapView即可
    // 地图View

    MapView mMapView;
    BaiduMap mBaiduMap;

    LocationClient mLocationClient;
    MyLocationListener mLocationListener;

    PoiSearch mPoiSearch;
    OnGetPoiSearchResultListener poiListener;

    //定位用的按钮
    Button btn_myPosition;

    //加油站搜索按钮
    Button btn_search;

    //经度
    private double mLatitue;
    //纬度
    private double mLongLatitue;

    //是否第一次进入，是就先定位到当前位置
    boolean isFirstIn = true;

    //***************
    //上一个节点
    Button mBtnPre = null;
    //下一个节点
    Button mBtnNext = null;
    //节点索引,供浏览节点时使用

    int nodeIndex = -1;

    //路线的父类，包括多种路线，具体用时动态生成
    RouteLine route = null;

    /**
     * 该类提供一个能够显示和管理多个Overlay的基类
     */
    OverlayManager routeOverlay = null;

    //默认不使用系统起点、终点图标

    boolean useDefaultIcon = false;
    //泡泡view
    private TextView popupText = null;

    // 搜索模块，也可去掉地图模块独立使用
    RoutePlanSearch mSearch = null;


    //语音TTS
    TextToSpeech tts;

    String editSearchKeyEt, editCityEt;

    //加油站信息布局
    LinearLayout mView;

    private LinearLayout mTabMusic;//音乐Tab
    private LinearLayout mTabNav;//导航Tab
    private LinearLayout mTabMy;//我的Tab

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();

        initLocation();

        editCityEt = "株洲";
        editSearchKeyEt = "加油站";


    }


    private void initLocation() {

        mLocationClient = new LocationClient(this);
        mLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(mLocationListener);

        LocationClientOption option = new LocationClientOption();

        option.setCoorType("bd09ll");
        option.setIsNeedAddress(true);
        option.setOpenGps(true);
        option.setScanSpan(1000);

        mLocationClient.setLocOption(option);


    }

    void init() {
        mTabMusic= (LinearLayout) findViewById(R.id.id_tab_music);
        mTabNav= (LinearLayout) findViewById(R.id.id_tab_nav);
        mTabMy= (LinearLayout) findViewById(R.id.id_tab_my);
        mTabMusic.setOnClickListener(this);
        mTabNav.setOnClickListener(this);
        mTabMy.setOnClickListener(this);

        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.main_layout);

        mView = (LinearLayout)LayoutInflater.from(this).inflate(R.layout.myview,null);

        mMapView = (MapView) findViewById(R.id.map);
        mBaiduMap = mMapView.getMap();

        //缩放地图，让地图更加美观
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15.0f);
        mBaiduMap.setMapStatus(msu);

        //***************
        //导航功能初始化
        mBtnPre = (Button) findViewById(R.id.pre);
        mBtnNext = (Button) findViewById(R.id.next);
        mBtnPre.setVisibility(View.INVISIBLE);
        mBtnNext.setVisibility(View.INVISIBLE);

        mBaiduMap.setOnMapClickListener(this);

        mSearch = RoutePlanSearch.newInstance();

        mSearch.setOnGetRoutePlanResultListener(this);

        //***************


        //初始化检索
        mPoiSearch = PoiSearch.newInstance();

        btn_myPosition = (Button) findViewById(R.id.button);

        btn_search = (Button) findViewById(R.id.search);

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                nearbySearch(0);
//                citySearch(4);
//                显示页为第0页的结果


                boundSearch(0);
            }
        });


        btn_myPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //定位功能,以自己为中点定位
                centerToMyLocation(mLatitue, mLongLatitue);

            }
        });


        //****************************检索功能***********************
        //

        poiListener = new OnGetPoiSearchResultListener() {
            public void onGetPoiResult(PoiResult result) {

                if (result == null
                        || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {// 没有找到检索结果
                    Toast.makeText(MainActivity.this, "未找到结果",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                if (result.error == SearchResult.ERRORNO.NO_ERROR) {// 检索结果正常返回

                    mBaiduMap.clear();
                    MyPoiOverlay poiOverlay = new MyPoiOverlay(mBaiduMap);
                    poiOverlay.setData(result);// 设置POI数据

                    mBaiduMap.setOnMarkerClickListener(poiOverlay);

                    poiOverlay.addToMap();// 将所有的overlay添加到地图上
                    poiOverlay.zoomToSpan();

                    int totalPage = result.getTotalPageNum();// 获取总分页数

                    Toast.makeText(
                            MainActivity.this,
                            "总共查到" + result.getTotalPoiNum() + "个兴趣点, 分为"
                                    + totalPage + "页", Toast.LENGTH_SHORT).show();

                }

            }

            public void onGetPoiDetailResult(PoiDetailResult result) {

                if (result.error != SearchResult.ERRORNO.NO_ERROR) {

                    Toast.makeText(MainActivity.this, "抱歉，未找到结果",
                            Toast.LENGTH_SHORT).show();

                } else {// 正常返回结果的时候，此处可以获得很多相关信息


//        popupText.setBackgroundResource(R.drawable.popup);

                    LatLng nodeLocation = result.getLocation();

                    TextView t1 ,t2,t3,t4;

                    t1 = (TextView) mView.findViewById(R.id.textView1);
                    t2 = (TextView) mView.findViewById(R.id.textView2);
                    t3 = (TextView) mView.findViewById(R.id.textView3);
                    t4 = (TextView) mView.findViewById(R.id.textView4);

                    t1.setText(result.getName());
                    t2.setText(result.getAddress());
                    t3.setText("5.0");
                    t4.setText("未知");
Log.i("sdsdsdsdsd",result.getDetailUrl());
                    mBaiduMap.showInfoWindow(new InfoWindow(mView, nodeLocation, 0));


//                    Toast.makeText(
//                            MainActivity.this,
//                            result.getName() + ": "
//                                    + result.getAddress(),
//                            Toast.LENGTH_LONG).show();
                }

            }


        };

        mPoiSearch.setOnGetPoiSearchResultListener(poiListener);

        //***************语音合成系统************************
        tts = new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {

                if(status == TextToSpeech.SUCCESS){
                    int result = tts.setLanguage(Locale.CHINA);

                    if(result != TextToSpeech.LANG_MISSING_DATA &&
                            result!= TextToSpeech.LANG_NOT_SUPPORTED){

                        Toast.makeText(MainActivity.this, "error",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        //***************语音合成系统************************

        //在公交线路规划回调方法中添加TransitRouteOverlay用于展示换乘信息
    }

    /**
     * 城市内搜索
     */
    private void citySearch(int page) {
        // 设置检索参数
        PoiCitySearchOption citySearchOption = new PoiCitySearchOption();

        citySearchOption.city(editCityEt);// 城市
        citySearchOption.keyword(editSearchKeyEt);// 关键字

        citySearchOption.pageCapacity(15);// 默认每页10条
        citySearchOption.pageNum(page);// 分页编号

        // 发起检索请求
        mPoiSearch.searchInCity(citySearchOption);
    }

    /**
     * 范围检索
     */
    private void boundSearch(int page) {

        PoiBoundSearchOption boundSearchOption = new PoiBoundSearchOption();

        LatLng southwest = new LatLng(mLatitue - 0.01, mLongLatitue - 0.012);// 西南
        LatLng northeast = new LatLng(mLatitue + 0.01, mLongLatitue + 0.012);// 东北

        LatLngBounds bounds = new LatLngBounds.Builder().include(southwest)
                .include(northeast).build();// 得到一个地理范围对象

        boundSearchOption.bound(bounds);// 设置poi检索范围
        boundSearchOption.keyword(editSearchKeyEt);// 检索关键字
        boundSearchOption.pageNum(page);

        mPoiSearch.searchInBound(boundSearchOption);// 发起poi范围检索请求
    }

    /**
     * 附近检索
     */
    private void nearbySearch(int page) {
        PoiNearbySearchOption nearbySearchOption = new PoiNearbySearchOption();

        nearbySearchOption.location(new LatLng(mLatitue, mLongLatitue));
        nearbySearchOption.keyword(editSearchKeyEt);
        nearbySearchOption.radius(1000);// 检索半径，单位是米
        nearbySearchOption.pageNum(page);

        mPoiSearch.searchNearby(nearbySearchOption);// 发起附近检索请求
    }

    @Override
    public void onClick(View v) {
        Class<?> cls=null;
        switch (v.getId()) {
            case R.id.id_tab_music:break;
            case R.id.id_tab_nav:break;
            case R.id.id_tab_my:break;
            default:break;
        }
        if(cls!=null) {
            Intent intent=new Intent(this,cls);
            startActivity(intent);
        }
    }


    class MyPoiOverlay extends PoiOverlay {
        public MyPoiOverlay(BaiduMap arg0) {
            super(arg0);
        }

        @Override
        public boolean onPoiClick(int arg0) {
            super.onPoiClick(arg0);
            PoiInfo poiInfo = getPoiResult().getAllPoi().get(arg0);
            // 检索poi详细信息
            mPoiSearch.searchPoiDetail(new PoiDetailSearchOption()
                    .poiUid(poiInfo.uid));
            return true;
        }
    }



//****************************检索功能***********************


    private class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {

            MyLocationData data = new MyLocationData.Builder()
                    .accuracy(location.getRadius())//
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude())
                    .build();
            mBaiduMap.setMyLocationData(data);

//           MyLocationConfiguration config = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.COMPASS,arg1,arg2);

//           时时获取经纬度
            mLatitue = location.getLatitude();
            mLongLatitue = location.getLongitude();
            Log.v("$$$$$$$$$$$$$", "**********************");
            Log.v("mLatitue", mLatitue + "");
            Log.v("mLongLatitue", mLongLatitue + "");


            if (isFirstIn) {

                centerToMyLocation(location.getLatitude(), location.getLongitude());

            }
        }
    }


    private void centerToMyLocation(double mLatitue, double mLongLatitue) {

        //定位功能
        LatLng latLng = new LatLng(mLatitue, mLongLatitue);
        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);

        mBaiduMap.animateMapStatus(msu);
        isFirstIn = false;
    }


    //******************以下为导航信息*********************
    //路线规划相关类及方法

    /**
     * 驾车搜索，公交搜索，步行搜索按钮点击事件
     * <p>
     * 发起路线规划搜索示例
     *
     * @param v 视图
     */


    public void SearchButtonProcess(View v) {

        //重置浏览节点的路线数据
        route = null;
        mBtnPre.setVisibility(View.INVISIBLE);
        mBtnNext.setVisibility(View.INVISIBLE);
        mBaiduMap.clear();

        // 处理搜索按钮响应
        EditText editSt = (EditText) findViewById(R.id.start);
        EditText editEn = (EditText) findViewById(R.id.end);

        //设置起终点信息，对于tranist search 来说，城市名无意义

        /**
         * PlanNode:
         * 路径规划中的出行节点信息,出行节点包括：起点，终点，途经点
         * 出行节点信息可以通过两种方式确定：
         *
         * 1： 给定出行节点经纬度坐标
         * 2： 给定出行节点地名和城市名
         *
         * public static PlanNode withCityNameAndPlaceName(java.lang.String city,
         * java.lang.String placeName)
         * 通过地名和城市名确定出行节点信息
         * @param placeName - 地点名; city - 城市名
         * @return 出行节点对象
         * */

        PlanNode stNode = PlanNode.withCityNameAndPlaceName("株洲", editSt.getText().toString());
        PlanNode enNode = PlanNode.withCityNameAndPlaceName("株洲", editEn.getText().toString());

        // 实际使用中请对起点终点城市进行正确的设定

        /**
         * 驾车搜索
         * */

        if (v.getId() == R.id.drive) {

            /**
             * public boolean drivingSearch(DrivingRoutePlanOption option)
             * 发起驾车路线规划
             * @param option - 请求参数
             * @return 成功发起检索返回true , 失败返回false
             *
             * DrivingRoutePlanOption:驾车路线规划参数
             * public DrivingRoutePlanOption from(PlanNode from)
             * 设置起点
             * @param from - 起点
             * @return 该 DrivingRoutePlanOption 选项对象
             *
             * public DrivingRoutePlanOption to(PlanNode to)
             * 设置终点
             * @param to - 终点
             * @return 该 DrivingRoutePlanOption 选项对象
             * */

            mSearch.drivingSearch((new DrivingRoutePlanOption())
                    .from(stNode)
                    .to(enNode));

            /**
             * 公交搜索
             * */

        } else if (v.getId() == R.id.transit) {

            /**
             * public boolean transitSearch(TransitRoutePlanOption option)
             * 发起换乘路线规划
             * @param option - 请求参数
             * @return 成功发起检索返回true , 失败返回false
             *
             * TransitRoutePlanOption:换乘路线规划参数
             * public TransitRoutePlanOption from(PlanNode from)
             * 设置起点
             * @param from - 起点
             * @return 该换乘路线规划参数对象
             *
             * public TransitRoutePlanOption city(java.lang.String city)
             * 设置换乘路线规划城市，起终点中的城市将会被忽略
             * @param city - 城市
             * @return 该换乘路线规划参数对象
             *
             * public TransitRoutePlanOption to(PlanNode to)
             * 设置终点
             * @return 该换乘路线规划参数对象
             * */

            mSearch.transitSearch((new TransitRoutePlanOption())
                    .from(stNode)
                    .city("北京")
                    .to(enNode));
            /**
             * 步行搜索
             * */

        } else if (v.getId() == R.id.walk) {

            /**
             * public boolean walkingSearch(WalkingRoutePlanOption option)
             * 发起步行路线规划
             * @param option - 请求参数
             * @return 成功发起检索返回true , 失败返回false
             *
             * WalkingRoutePlanOption:步行路线规划参数
             * public WalkingRoutePlanOption from(PlanNode from)
             * 设置起点
             * @param from - 起点
             * @return 该步行路线规划参数对象
             *
             * public WalkingRoutePlanOption to(PlanNode to)
             * 设置终点
             * @param to - 终点
             * @return 该步行路线规划参数对象
             * */

            mSearch.walkingSearch((new WalkingRoutePlanOption())
                    .from(stNode)
                    .to(enNode));
        }
    }


    /**
     * 上一个或下一个节点点击事件
     * <p>
     * 节点浏览示例
     *
     * @param v
     */

    public void nodeClick(View v) {

        /**
         * public java.util.List<T> getAllStep()
         * 获取路线中的所有路段
         * 返回:路线中的所有路段
         * */

        if (route == null || route.getAllStep() == null) {
            return;
        }

        if (nodeIndex == -1 && v.getId() == R.id.pre) {
            return;
        }

        //设置节点索引
        if (v.getId() == R.id.next) {
            if (nodeIndex < route.getAllStep().size() - 1) {
                nodeIndex++;
            } else {
                return;
            }

        } else if (v.getId() == R.id.pre) {
            if (nodeIndex > 0) {
                nodeIndex--;
            } else {
                return;
            }
        }

        //获取节结果信息
        LatLng nodeLocation = null;
        String nodeTitle = null;
        Object step = route.getAllStep().get(nodeIndex);

        /**
         * public static class DrivingRouteLine.DrivingStep extends RouteStep
         * 表示一个驾车路段
         *
         * public RouteNode getEntrace()
         * 路段入口信息
         * 返回:路段入口信息
         *
         * public LatLng getLocation()
         * 获取位置
         * 返回:位置
         *
         * public java.lang.String getInstructions()
         * 路段总体指示信息
         * 返回: 路段总体指示信息
         * */

        if (step instanceof DrivingRouteLine.DrivingStep) {

            nodeLocation = ((DrivingRouteLine.DrivingStep) step).getEntrance().getLocation();
            nodeTitle = ((DrivingRouteLine.DrivingStep) step).getInstructions();

            /**
             * public static class WalkingRouteLine.WalkingStep extends RouteStep
             * 描述一个步行路段
             * */

        } else if (step instanceof WalkingRouteLine.WalkingStep) {

            nodeLocation = ((WalkingRouteLine.WalkingStep) step).getEntrance().getLocation();
            nodeTitle = ((WalkingRouteLine.WalkingStep) step).getInstructions();

            /**
             * public static class TransitRouteLine.TransitStep extends RouteStep
             * 表示一个换乘路段
             * */

        } else if (step instanceof TransitRouteLine.TransitStep) {

            nodeLocation = ((TransitRouteLine.TransitStep) step).getEntrance().getLocation();
            nodeTitle = ((TransitRouteLine.TransitStep) step).getInstructions();
        }

        if (nodeLocation == null || nodeTitle == null) {
            return;
        }

        //移动节点至中心
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(nodeLocation));
        //显示弹出窗口
        popupText = new TextView(MainActivity.this);
//        popupText.setBackgroundResource(R.drawable.popup);

        popupText.setTextColor(0xFF000000);
        popupText.setText(nodeTitle);

        tts.speak(nodeTitle, TextToSpeech.QUEUE_ADD, null);
        /**
         * public void showInfoWindow(InfoWindow infoWindow)
         * 显示 InfoWindow
         * 参数:infoWindow - 要显示的 InfoWindow 对象
         *
         * InfoWindow:在地图中显示一个信息窗口，可以设置一个View作为该窗口的内容，
         *               也可以设置一个 BitmapDescriptor作为该窗口的内容。
         *
         * public InfoWindow(View view,LatLng position,int yOffset)
         *
         * 通过传入的 view 构造一个 InfoWindow, 此时只是利用该view生成一个Bitmap绘制在地图中。
         * 参数:
         * view - InfoWindow 展示的 view
         * position - InfoWindow 显示的地理位置
         * yOffset - InfoWindow Y 轴偏移量
         * listener - InfoWindow 点击监听者
         * */

        mBaiduMap.showInfoWindow(new InfoWindow(popupText, nodeLocation, 0));


    }


    /**
     * 自定义起终点图标点击事件
     * <p>
     * 切换路线图标，刷新地图使其生效
     * 注意： 起终点图标使用中心对齐.
     */

    public void changeRouteIcon(View v) {
        if (routeOverlay == null) {
            return;
        }
        if (useDefaultIcon) {
            ((Button) v).setText("自定义起终点图标");
            Toast.makeText(this, "将使用系统起终点图标", Toast.LENGTH_SHORT).show();

        } else {
            ((Button) v).setText("系统起终点图标");
            Toast.makeText(this, "将使用自定义起终点图标", Toast.LENGTH_SHORT).show();
        }
        useDefaultIcon = !useDefaultIcon;
        /**
         * * public final void removeFromMap()
         * 将所有Overlay 从 地图上消除
         * */

        routeOverlay.removeFromMap();

        /**
         * public final void addToMap()
         * 将所有Overlay 添加到地图上
         * */

        routeOverlay.addToMap();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult result) {

        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(MainActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            //起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            //result.getSuggestAddrInfo()
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            nodeIndex = -1;
            mBtnPre.setVisibility(View.VISIBLE);
            mBtnNext.setVisibility(View.VISIBLE);

            /**
             * public java.util.List<WalkingRouteLine> getRouteLines()
             * 获取所有步行规划路线
             * 返回:所有步行规划路线
             * */

            route = result.getRouteLines().get(0);

            WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mBaiduMap);

            /**
             * 设置地图 Marker 覆盖物点击事件监听者
             * 需要实现的方法：     onMarkerClick(Marker marker)
             * */

            mBaiduMap.setOnMarkerClickListener(overlay);
            routeOverlay = overlay;
            /**
             * public void setData(WalkingRouteLine line)设置路线数据。
             * 参数:line - 路线数据
             * */

            overlay.setData(result.getRouteLines().get(0));

            /**
             * public final void addToMap()将所有Overlay 添加到地图上
             * */

            overlay.addToMap();
            /**
             * public void zoomToSpan()
             * 缩放地图，使所有Overlay都在合适的视野内
             * 注： 该方法只对Marker类型的overlay有效
             * */

            overlay.zoomToSpan();
        }
    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult result) {

        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(MainActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            //起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            //result.getSuggestAddrInfo()
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            nodeIndex = -1;
            mBtnPre.setVisibility(View.VISIBLE);
            mBtnNext.setVisibility(View.VISIBLE);

            route = result.getRouteLines().get(0);
            TransitRouteOverlay overlay = new MyTransitRouteOverlay(mBaiduMap);
            mBaiduMap.setOnMarkerClickListener(overlay);

            routeOverlay = overlay;
            overlay.setData(result.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
        }
    }


    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(MainActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            //起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            //result.getSuggestAddrInfo()
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            nodeIndex = -1;
            mBtnPre.setVisibility(View.VISIBLE);
            mBtnNext.setVisibility(View.VISIBLE);

            route = result.getRouteLines().get(0);
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

        //自行车，暂时未实现
    }

    //乘车路线覆盖物
    private class MyDrivingRouteOverlay extends DrivingRouteOverlay {

        public MyDrivingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }
    }

    /**
     * WalkingRouteOverlay已经实现了BaiduMap.OnMarkerClickListener接口
     */

    //步行路线覆盖图
    private class MyWalkingRouteOverlay extends WalkingRouteOverlay {

        public MyWalkingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        /**
         * public BitmapDescriptor getStartMarker()
         * 覆写此方法以改变默认起点图标
         * 返回:起点图标
         */
        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        /**
         * public BitmapDescriptor getTerminalMarker()
         * 覆写此方法以改变默认终点图标
         * 返回:终点图标
         */
        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }
    }

    /**
     * TransitRouteOverlay已经实现了BaiduMap.OnMarkerClickListener接口
     */
    private class MyTransitRouteOverlay extends TransitRouteOverlay {

        public MyTransitRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }
    }

    ////////////BaiduMap.OnMapClickListener/////////////

    @Override
    public void onMapClick(LatLng point) {
        /**
         * 隐藏当前 InfoWindow
         * */
        mBaiduMap.hideInfoWindow();
    }

    /**
     * 地图内 Poi 单击事件回调函数
     */
    @Override
    public boolean onMapPoiClick(MapPoi poi) {
        return false;
    }

    //******************************
    //以下为生命周期及菜单选项

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        Log.v("MenuCreate", "$$$$$$$$$$$$$$$$$$$");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.map_common:

                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                break;

            case R.id.map_site:

                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                break;

            case R.id.map_mylocation:

                centerToMyLocation(mLatitue, mLongLatitue);
                break;


            case R.id.map_traffic:
                if (mBaiduMap.isTrafficEnabled()) {

                    mBaiduMap.setTrafficEnabled(false);
                    item.setTitle("实施交通(off)");
                } else {

                    mBaiduMap.setTrafficEnabled(true);
                    item.setTitle("实施交通(on)");
                }
                break;

            default:
                break;

        }
        Log.v("MenuItSlecteCreate", "$$$$$$$$$$$$$$$$$$$");
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        mPoiSearch.destroy();
        mSearch.destroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //开启定位
        mBaiduMap.setMyLocationEnabled(true);
        if (!mLocationClient.isStarted()) {
            mLocationClient.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //关闭定位
        mBaiduMap.setMyLocationEnabled(false);
        mLocationClient.stop();

    }
}
