package com.cwp.android.baidutest;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.PlanNode;

import just.activities.ActivityCollector;
import just.activities.LoginActivity;
import just.activities.MyInfoActivity;
import utils.MyLog;
import utils.ShapeLoadingDialog;
import utils.TTS;

public class MainActivity extends AppCompatActivity implements BaiduMap.OnMapClickListener {

    // 地图View
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    //上一个节点、下一个节点
    Button mBtnPre = null;
    Button mBtnNext = null;

    private boolean POI_true_folse;
    private Marker mMarker;

    //节点索引,供浏览节点时使用
    private int nodeIndex = -1;
    //泡泡view
    private TextView popupText = null;

    public static Handler myhandler;
    //用于更新加油站数据
    private Bundle bundle = null;

    String editSearchKeyEt, editCityEt;
    //加油站信息布局
    private LinearLayout mView;

    private boolean Search_true_folse;
    private boolean Gas_Show;
    private boolean isOpen;

    private long exitTime;//记录按下返回键的系统时间
    private ListView mListView;

    //卫星菜单的角度
    private double angel[] = {Math.toRadians(270), Math.toRadians(90), Math.toRadians(0)};
    private int[] ivIds = {R.id.iv_b, R.id.iv_c, R.id.iv_d, R.id.iv_e, R.id.iv_a};
    private ImageView[] imageViews = new ImageView[ivIds.length];

    private int[] ls = new int[]{
            R.drawable.map_travel, R.drawable.map_gas, R.drawable.map_localtion,
            R.drawable.map_rout_to, R.drawable.map_sign, R.drawable.map_music,
            R.drawable.map_information, R.drawable.map_back, R.drawable.map_com};
    //改版后***********
    private Locate myLocate;
    private TTS t;
    private Travel travel;
    private POISearch poiSearch;
    public static ShapeLoadingDialog shapeLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_layout);
        ActivityCollector.addActivity(this);

        init();

        initSateliteMenu();

        initLoadDialong();

        myLocate = new Locate(MainActivity.this, mBaiduMap);

        myLocate.initLocation();

        poiSearch = new POISearch(mBaiduMap);

        initlistview();
        mListView.setSelection(Integer.MAX_VALUE / 2);

        editCityEt = "株洲";
        editSearchKeyEt = "加油站";

        //注意初始化顺序
        t = new TTS(MainActivity.this);

        travel = new Travel(mBaiduMap);
    }
    public void init() {

        mView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.myview, null);

        mMapView = (MapView) findViewById(R.id.map);
        mBaiduMap = mMapView.getMap();
        //缩放地图，让地图更加美观
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15.0f);
        mBaiduMap.setMapStatus(msu);
        //导航功能初始化
        mBtnPre = (Button) findViewById(R.id.pre);
        mBtnNext = (Button) findViewById(R.id.next);
        mBtnPre.setVisibility(View.INVISIBLE);
        mBtnNext.setVisibility(View.INVISIBLE);

        mBaiduMap.setOnMapClickListener(this);


        myhandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                switch (msg.what) {
                    case 0x12:
                        MyLog.LogE("MainActivity", "handlemessage 0x12");
                        bundle = msg.getData();
                        updata(bundle);
                        shapeLoadingDialog.dismiss();
                        break;
                    case 0x13:
                        MyLog.LogE("MainActivity", "handlemessage 0x13");
                        Bundle bundle1 = new Bundle();
                        bundle1.putString("NAME", "error");
                        updata(bundle1);
                        shapeLoadingDialog.dismiss();
                    default:
                        break;
                }
            }
        };

    }

    public void initLoadDialong() {
        shapeLoadingDialog = new ShapeLoadingDialog(this);
        shapeLoadingDialog.setLoadingText("加载中...");

    }

    public void initlistview() {

        mListView = (ListView) findViewById(R.id.listview);
        ImageAdapter adapter = new ImageAdapter(this, ls);

        mListView.setAdapter(adapter);

        mListView.setOnItemClickListener((parent, view, position, id) -> {
            switch (position % 8) {
                case 0:
                    travelTo();
                    break;
                case 1:
                    gas();
                    break;
                case 2:
                    location();
                    break;
                case 3:
                    route_to();
                    break;
                case 4:
                    poi();
                    break;
                case 5:
                    music();
                    break;
                case 6:
                    myInfo();
                    break;
                case 7:
                    changeTravelto();
                    break;

                default:
                    Toast.makeText(MainActivity.this, "Click default ", Toast.LENGTH_SHORT).show();
                    break;
            }
        });

    }

    public void initSateliteMenu() {

        for (int i = 0; i < ivIds.length; i++) {
            imageViews[i] = (ImageView) findViewById(ivIds[i]);
        }
        imageViews[0].setOnClickListener(v -> {

//            travel.search();
            Intent intent = new Intent(MainActivity.this, com.com.reoger.music.View.MainActivity.class);
            startActivity(intent);
        });

        imageViews[1].setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MyInfoActivity.class);
            startActivity(intent);
        });
        imageViews[2].setOnClickListener(v -> {
            if (mListView.getVisibility() == View.GONE) {
                mListView.setVisibility(View.VISIBLE);
            } else {
                mListView.setVisibility(View.GONE);
            }
        });

        imageViews[4].setOnClickListener(v -> {
            switch (v.getId()) {
                case R.id.iv_a:
                    executeAnim(isOpen);
                    isOpen = !isOpen;
                    break;
                default:
                    Toast.makeText(MainActivity.this, "Clicked", Toast.LENGTH_SHORT).show();
                    break;
            }
            //卫星菜单是否打开
            isOpen = true;
        });
    }

    //当后台数据完成后显示加油站的信息
    public void updata(Bundle bundle) {

        TextView t1, t2, t3, t4;
        /*
        ** t1是加油站名称  t2是加油站地址  t3是油价 t4也是 价格
         */
        Button btn_ording = (Button) mView.findViewById(R.id.ording);

        t1 = (TextView) mView.findViewById(R.id.textView1);
        t2 = (TextView) mView.findViewById(R.id.textView2);
        t3 = (TextView) mView.findViewById(R.id.textView3);
        t4 = (TextView) mView.findViewById(R.id.textView4);


        String name = (String) bundle.get("NAME");
        String address = (String) bundle.get("ADDRESS");
        String price = (String) bundle.get("price1");
        String gasprice = (String) bundle.get("gasprice1");

        t1.setText(name);
        t2.setText(address);
        t3.setText(price);
        t4.setText(gasprice);

        btn_ording.setOnClickListener(v -> {
            Intent intent = null;
            //判断是否已经登陆了
            if (MyApplication.isLanded()) {

                intent = new Intent(MainActivity.this, OrdGasActivity.class);
                intent.putExtras(bundle);
            } else {
                Log.d("测试->MainActivity", "请先登录");
                intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.putExtras(bundle);
                intent.putExtra("TAG", "OrdGAs");
            }
            startActivity(intent);
        });

        if (bundle.getString("NAME").equals("error")) {
            Toast.makeText(this, "暂无该加油站具体数据。", Toast.LENGTH_SHORT).show();

        } else {
            //聚焦
            LatLng latLng = new LatLng(poiSearch.nodeLocation.latitude + 0.014, poiSearch.nodeLocation.longitude);
            MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);

            mBaiduMap.animateMapStatus(msu);
            mBaiduMap.showInfoWindow(new InfoWindow(mView, poiSearch.nodeLocation, 0));

        }

    }

    //以下为左侧图标点击事件方法
    public void music() {
        Intent intent = new Intent(MainActivity.this, com.com.reoger.music.View.MainActivity.class);
        startActivity(intent);
    }

    public void myInfo() {
        Intent intent = new Intent(MainActivity.this, MyInfoActivity.class);
        startActivity(intent);
    }

    public void gas() {
        if (!Gas_Show) {
//              显示页为第0页的结果
            shapeLoadingDialog.show();

            poiSearch.boundSearch(myLocate);

            Gas_Show = true;
        } else {
            Gas_Show = false;
            mBaiduMap.clear();
        }
    }

    public void poi() {
        if (!POI_true_folse) {

            POI_true_folse = true;
            //降地图移回地图中心
            myLocate.centerToMyLocation();

            LatLng point = new LatLng(myLocate.getmLatitue(), myLocate.getmLongLatitue() + 0.004);
            BitmapDescriptor bitmap = BitmapDescriptorFactory
                    .fromResource(R.drawable.map_en);

            OverlayOptions options = new MarkerOptions()
                    .position(point)  //设置marker的位置
                    .icon(bitmap)  //设置marker图标
                    .zIndex(9)  //设置marker所在层级
                    .draggable(true);  //设置手势拖拽

            mMarker = (Marker) (mBaiduMap.addOverlay(options));

        } else {
            mMarker.remove();
            POI_true_folse = false;
        }

    }

    public void route_to() {
        if (POI_true_folse) {

            POI_true_folse = false;
            mBaiduMap.clear();

            LatLng stPosition = new LatLng(myLocate.getmLatitue(), myLocate.getmLongLatitue());
            LatLng enPosition = mMarker.getPosition();

            PlanNode stNode = PlanNode.withLocation(stPosition);
            PlanNode enNode = PlanNode.withLocation(enPosition);

            travel.search(stNode, enNode);
        } else {
            mBaiduMap.clear();
            mBtnPre.setVisibility(View.INVISIBLE);
            mBtnNext.setVisibility(View.INVISIBLE);
            Toast.makeText(MainActivity.this, "请先在地图上选址！", Toast.LENGTH_SHORT).show();
        }
    }

    public void changeTravelto() {

        //重置浏览节点的路线数据

        mBtnPre.setVisibility(View.INVISIBLE);
        mBtnNext.setVisibility(View.INVISIBLE);
        mBaiduMap.clear();

        // 处理搜索按钮响应
        EditText editSt = (EditText) findViewById(R.id.start);
        EditText editEn = (EditText) findViewById(R.id.end);

        PlanNode stNode = PlanNode.withCityNameAndPlaceName("株洲", editEn.getText().toString());
        PlanNode enNode = PlanNode.withCityNameAndPlaceName("株洲", editSt.getText().toString());


        //起始地点交换
        String temp = editEn.getText().toString().trim();
        editEn.setText(editSt.getText().toString().trim());
        editSt.setText(temp);

        travel.search(stNode, enNode);


    }

    public void travelTo() {

        //重置浏览节点的路线数据
        mBtnPre.setVisibility(View.INVISIBLE);
        mBtnNext.setVisibility(View.INVISIBLE);
        mBaiduMap.clear();

        // 处理搜索按钮响应
        EditText editSt = (EditText) findViewById(R.id.start);
        EditText editEn = (EditText) findViewById(R.id.end);
        //设置起终点信息，对于tranist search 来说，城市名无意义

        PlanNode stNode;
        PlanNode enNode;

        stNode = PlanNode.withCityNameAndPlaceName("株洲", editSt.getText().toString());
        enNode = PlanNode.withCityNameAndPlaceName("株洲", editEn.getText().toString());

        if (!Search_true_folse) {

            Search_true_folse = true;
            travel.search(stNode, enNode);

        } else {
            mBaiduMap.clear();
            Search_true_folse = false;
        }


    }

    public void location(){

        myLocate.centerToMyLocation();
    }
    /**
     * 上一个或下一个节点点击事件
     * 节点浏览示例
     */
    public void nodeClick(View v) {

        /**
         * public java.util.List<T> getAllStep()
         * 获取路线中的所有路段
         * 返回:路线中的所有路段
         * */

        if (travel.route == null || travel.route.getAllStep() == null) {
            return;
        }

        if (nodeIndex == -1 && v.getId() == R.id.pre) {
            return;
        }

        //设置节点索引
        if (v.getId() == R.id.next) {
            if (nodeIndex < travel.route.getAllStep().size() - 1) {
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
        Object step = travel.route.getAllStep().get(nodeIndex);

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

        Toast.makeText(MainActivity.this, "语音播报", Toast.LENGTH_SHORT).show();

        t.speak(nodeTitle);

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

    private void executeAnim(boolean isOpen) {

        float X = imageViews[3].getX();
        float Y = imageViews[3].getY();

        if (isOpen) {
            for (int i = 0; i <= 2; i++) {
                DisplayMetrics metric = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metric);
                int width = metric.widthPixels; // 屏幕宽度（像素）

                float x = (float) Math.sin(angel[i]) * (width / 6);
                float y = -(float) Math.cos(angel[i]) * (width / 6);

                //Log.i("坐标", angle + " " + x + " " + y);


                ObjectAnimator animator1 = ObjectAnimator.ofFloat(imageViews[i], "X", X, x + X);
                ObjectAnimator animator2 = ObjectAnimator.ofFloat(imageViews[i], "Y", Y, y + Y);
                ObjectAnimator animator3 = ObjectAnimator.ofFloat(imageViews[i], "alpha", 0.0f, 1.0f);

                AnimatorSet set = new AnimatorSet();

                set.playTogether(animator1, animator2, animator3);

                set.setDuration(500);
                set.start();
            }
        } else {
            for (int i = 0; i <= 2; i++) {
                PropertyValuesHolder p1 = PropertyValuesHolder.ofFloat("X", imageViews[i].getX(),
                        X);
                PropertyValuesHolder p2 = PropertyValuesHolder.ofFloat("Y", imageViews[i].getY(),
                        Y);
                PropertyValuesHolder p3 = PropertyValuesHolder.ofFloat("alpha", 1.0f, 0.0f);
                ObjectAnimator.ofPropertyValuesHolder(imageViews[i], p1, p2, p3)
                        .setDuration(300 * i).start();
            }
        }
    }

//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//    }

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

                myLocate.centerToMyLocation();

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
        poiSearch.mPoiSearch.destroy();

        ActivityCollector.removeActivity(this);
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

        if (!myLocate.mLocationClient.isStarted()) {
            myLocate.mLocationClient.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //关闭定位
        mBaiduMap.setMyLocationEnabled(false);
        myLocate.mLocationClient.stop();

    }

    @Override
    public void onBackPressed() {

        if ((System.currentTimeMillis() - exitTime) > 2000) {
            // ToastUtil.makeToastInBottom("再按一次退出应用", MainMyselfActivity);
            Toast.makeText(MainActivity.this, "再按一次退出应用", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
            return;
        }
        ActivityCollector.finishAll();
    }

}