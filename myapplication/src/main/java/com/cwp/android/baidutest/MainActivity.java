package com.cwp.android.baidutest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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

//实现BaiduMap.OnMapClickListener接口
//用于实现  onMapClick(LatLng point)  、onMapPoiClick(MapPoi poi) 这两个方法
public class MainActivity extends AppCompatActivity implements BaiduMap.OnMapClickListener {

    // 地图View
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    //地图 目的地标注物
    private Marker mMarker;
    //节点索引,供浏览节点时使用
    private int nodeIndex = -1;
    //popupView
    private TextView popupText = null;
    //用于处理异步信息回调
    public static Handler myHandler;
    //用于更新加油站数据
    private Bundle bundle = null;
    // editSearchKeyEt : POI关键字  editCityEt： 搜索所在的城市
    String editSearchKeyEt, editCityEt;
    //上一个节点、下一个节点
    Button mBtnPre = null;
    Button mBtnNext = null;
    EditText mEditSt;
    EditText mEditEn;

    // isPOIHavingShow： 标注物是否显示 ，isHavingSearch ：是否已经寻路，
    // isGasHavingShow： 加油站是否显示 ，isSatelliteOpen：卫星菜单是否打开
    private boolean isPOIHavingShow;
    private boolean isHavingSearch;
    private boolean isGasHavingShow;
    private boolean isSatelliteOpen;

    //mGasDetailView ：加油站信息布局
    //mPositionInputView  ：起始地点输入布局
    private LinearLayout mGasDetailView;
    private LinearLayout mPositionInputView;

    //记录按下返回键的系统时间
    private long exitTime;

    //左边用于显示图标的listView
    private ListView mListView;

    //卫星菜单的角度
    private double angel[] = {Math.toRadians(270), Math.toRadians(90), Math.toRadians(0)};
    //卫星菜单的图片id
    private int[] ivIds = {R.id.iv_b, R.id.iv_c, R.id.iv_d, R.id.iv_e, R.id.iv_a};
    //用于初始化ImageView
    private ImageView[] imageViews = new ImageView[ivIds.length];
    //listView的图标id数组
    private int[] ls = new int[]{
            R.drawable.map_travel, R.drawable.map_gas, R.drawable.map_localtion,
            R.drawable.map_rout_to, R.drawable.map_sign, R.drawable.map_music,
            R.drawable.map_information, R.drawable.map_back, R.drawable.map_com};
    //用于实现定位的类
    private Locate myLocate;
    //TTs的类
    public static TTS t;
    //Travel 用于路线规划的类
    private Travel travel;
    //POI查询的类
    private POISearch poiSearch;
    //加载对话框
    public static ShapeLoadingDialog shapeLoadingDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        ActivityCollector.addActivity(this);

        init();


        myLocate = new Locate(MainActivity.this, mBaiduMap);

        myLocate.initLocation();

        poiSearch = new POISearch(mBaiduMap);


        mListView.setSelection(Integer.MAX_VALUE / 2);

        editCityEt = "株洲";
        editSearchKeyEt = "加油站";

        //注意初始化顺序
        t = new TTS(MainActivity.this);

        travel = new Travel(mBaiduMap);
    }

    /**
     * 下面是一些初始化方法
     */
    public void init() {

        mMapView = (MapView) findViewById(R.id.map);
        mBaiduMap = mMapView.getMap();

        mPositionInputView = (LinearLayout) findViewById(R.id.map_layout_position);
        mGasDetailView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.myview, null);

        //导航功能初始化
        mBtnPre = (Button) findViewById(R.id.pre);
        mBtnNext = (Button) findViewById(R.id.next);
        // 处理搜索按钮响应
        mEditSt = (EditText) findViewById(R.id.start);
        mEditEn = (EditText) findViewById(R.id.end);


        mBaiduMap.setOnMapClickListener(this);

        MapZoom();

        nodeButtonInvisiable();

        setOnEditorActionListener(mEditEn);

        initHandle();

        initSateliteMenu();

        initLoadDialong();

        initlistview();
    }

    //  实例化Handler ,处理网络数据(用于回调加油站详情 数据)。
    public void initHandle() {

        myHandler = new Handler() {
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

    //  初始化加载对话框
    public void initLoadDialong() {

        shapeLoadingDialog = new ShapeLoadingDialog(this);
        shapeLoadingDialog.setLoadingText("加载中...");
    }

    //  初始化左边的listview
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
                    Toast.makeText(MainActivity.this, "Click ", Toast.LENGTH_SHORT).show();
                    break;
            }
        });

    }

    //  初始化卫星菜单
    public void initSateliteMenu() {

        for (int i = 0; i < ivIds.length; i++) {
            imageViews[i] = (ImageView) findViewById(ivIds[i]);
        }
        imageViews[0].setOnClickListener(v -> {

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
                    if (mListView.getVisibility() == View.GONE) {
                        mListView.setVisibility(View.VISIBLE);
                    } else {
                        mListView.setVisibility(View.GONE);
                    }
//                    statelliteMenuExecuteAnim(isSatelliteOpen);
//                    isSatelliteOpen = !isSatelliteOpen;
                    break;
                default:
                    Toast.makeText(MainActivity.this, "Clicked", Toast.LENGTH_SHORT).show();
                    break;
            }

        });
    }

    /**
     * 下面是主要的点击事件处理方法（对应于主布局上面的图标）
     */

    //音乐
    public void music() {

        MyLog.LogE("MainActivity", "music");
//        Intent intent = new Intent(MainActivity.this, BNMainActivity.class);
        MyLog.LogE("MainActivity", "music" + "before intent");
        Intent intent = new Intent(MainActivity.this, com.com.reoger.music.View.MainActivity.class);
        startActivity(intent);
        MyLog.LogE("MainActivity", "music" + "after intent");
    }

    //信息
    public void myInfo() {
        Intent intent = new Intent(MainActivity.this, MyInfoActivity.class);
        startActivity(intent);
    }

    //加油站
    public void gas() {

        nodeButtonInvisiable();
        if (!isGasHavingShow) {
//              显示页为第0页的结果
            shapeLoadingDialog.show();

            poiSearch.boundSearch(myLocate);

            isGasHavingShow = true;
        } else {
            isGasHavingShow = false;
            mBaiduMap.clear();
        }
    }

    //标记
    public void poi() {
        if (!isPOIHavingShow) {

            isPOIHavingShow = true;
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
            isPOIHavingShow = false;
        }

    }

    //从这里出发
    public void route_to() {
        if (isPOIHavingShow) {

            isPOIHavingShow = false;
            mBaiduMap.clear();

            nodeButtonVisiable();

            LatLng stPosition = new LatLng(myLocate.getmLatitue(), myLocate.getmLongLatitue());
            LatLng enPosition = mMarker.getPosition();

            PlanNode stNode = PlanNode.withLocation(stPosition);
            PlanNode enNode = PlanNode.withLocation(enPosition);

//            new BNMainActivity().routeplanToNavi(BNRoutePlanNode.CoordinateType.BD09_MC,stPosition,enPosition);
            travel.search(stNode, enNode);
        } else {
            mBaiduMap.clear();
            nodeButtonInvisiable();
            Toast.makeText(MainActivity.this, "请先在地图上选址！", Toast.LENGTH_SHORT).show();
        }
    }

    //起始点交换
    public void changeTravelto() {

        //重置浏览节点的路线数据
        nodeButtonInvisiable();
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

        nodeButtonVisiable();
    }

    //通过输入起始点出发 （地图上没有对应的图标，在travel()方法中调用）
    public void travelByEditText() {

        nodeButtonInvisiable();
        mBaiduMap.clear();

        PlanNode stNode;
        PlanNode enNode;

        stNode = PlanNode.withCityNameAndPlaceName("株洲", mEditSt.getText().toString());
        enNode = PlanNode.withCityNameAndPlaceName("株洲", mEditEn.getText().toString());

        if (!isHavingSearch) {

            isHavingSearch = true;
            travel.search(stNode, enNode);

        }

        mPositionInputView.setVisibility(View.INVISIBLE);
    }

    //起始点出发  显示出起始地址对话框
    public void travelTo() {

        mBaiduMap.clear();

        nodeButtonInvisiable();

        if (mPositionInputView.getVisibility() == View.INVISIBLE && !isHavingSearch) {

            mPositionInputView.setVisibility(View.VISIBLE);
        } else if (mPositionInputView.getVisibility() == View.VISIBLE) {

            nodeButtonInvisiable();

            mPositionInputView.setVisibility(View.INVISIBLE);
        }

        if (isHavingSearch) {
            mBaiduMap.clear();
            isHavingSearch = false;

            nodeButtonInvisiable();

        }
    }

    //定位
    public void location() {

        myLocate.centerToMyLocation();
    }

/**
 * 下面是一些辅助方法 ，地图点击事件，路线按钮可见性，卫星菜单执行动画等方法
 */

    /**
     * 当后台数据完成后显示加油站的信息
     * t1是加油站名称  t2是加油站地址  t3是油价 t4也是 价格
     */
    public void updata(Bundle bundle) {

        TextView t1, t2, t3, t4;

        Button btn_ording = (Button) mGasDetailView.findViewById(R.id.ording);

        t1 = (TextView) mGasDetailView.findViewById(R.id.textView1);
        t2 = (TextView) mGasDetailView.findViewById(R.id.textView2);
        t3 = (TextView) mGasDetailView.findViewById(R.id.textView3);
        t4 = (TextView) mGasDetailView.findViewById(R.id.textView4);


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

        if ((bundle.getString("NAME")).equals("error")) {
            Toast.makeText(this, "暂无该加油站具体数据。", Toast.LENGTH_SHORT).show();

        } else {
            //聚焦
            LatLng latLng = new LatLng(poiSearch.nodeLocation.latitude + 0.014, poiSearch.nodeLocation.longitude);
            MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);

            mBaiduMap.animateMapStatus(msu);
            mBaiduMap.showInfoWindow(new InfoWindow(mGasDetailView, poiSearch.nodeLocation, 0));

        }

    }

    /**
     * 设置起始地址输入框，输入完成后就开始 规划路线
     */
    public void setOnEditorActionListener(EditText editEn) {

        if (editEn != null) {
            editEn.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_GO) {

                    //点击出发按钮后，执行下面代码
                    //根据起始地址 规划路线
                    travelByEditText();
                    //详细路线按钮显示
                    nodeButtonVisiable();

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    //隐藏软键盘
                    imm.hideSoftInputFromWindow(mPositionInputView.getWindowToken(), 0);

                    return true;
                }
                Toast.makeText(MainActivity.this, "false", Toast.LENGTH_SHORT).show();
                return false;
            });
        }
    }

    /**
     * 缩放地图，让地图更加美观
     */
     public void MapZoom() {

        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15.0f);
        mBaiduMap.setMapStatus(msu);

    }

    /**
     * public java.util.List<T> getAllStep()
     * 获取路线中的所有路段
     * 返回:路线中的所有路段
     */
    public void nodeClick(View v) {


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

//        Toast.makeText(MainActivity.this, "语音播报", Toast.LENGTH_SHORT).show();

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

    /**
     * 显示 路线详细步骤的控制 Button
     */
    public void nodeButtonVisiable() {

        mBtnPre.setVisibility(View.VISIBLE);
        mBtnNext.setVisibility(View.VISIBLE);

    }

    /**
     * 隐藏 路线详细步骤的 控制 Button
     */
    public void nodeButtonInvisiable() {

        mBtnPre.setVisibility(View.INVISIBLE);
        mBtnNext.setVisibility(View.INVISIBLE);

    }

    /**
     * 卫星菜单的动画效果
     */
    private void statelliteMenuExecuteAnim(boolean isOpen) {

//        float X = imageViews[3].getX();
//        float Y = imageViews[3].getY();
//
//        if (isOpen) {
//            for (int i = 0; i <= 2; i++) {
//                DisplayMetrics metric = new DisplayMetrics();
//                getWindowManager().getDefaultDisplay().getMetrics(metric);
//                int width = metric.widthPixels; // 屏幕宽度（像素）
//
//                float x = (float) Math.sin(angel[i]) * (width / 6);
//                float y = -(float) Math.cos(angel[i]) * (width / 6);
//
//                //Log.i("坐标", angle + " " + x + " " + y);
//
//                ObjectAnimator animator1 = ObjectAnimator.ofFloat(imageViews[i], "X", X, x + X);
//                ObjectAnimator animator2 = ObjectAnimator.ofFloat(imageViews[i], "Y", Y, y + Y);
//                ObjectAnimator animator3 = ObjectAnimator.ofFloat(imageViews[i], "alpha", 0.0f, 1.0f);
//
//                AnimatorSet set = new AnimatorSet();
//
//                set.playTogether(animator1, animator2, animator3);
//                set.setDuration(500);
//                set.start();
//            }
//        } else {
//            for (int i = 0; i <= 2; i++) {
//                PropertyValuesHolder p1 = PropertyValuesHolder.ofFloat("X", imageViews[i].getX(),
//                        X);
//                PropertyValuesHolder p2 = PropertyValuesHolder.ofFloat("Y", imageViews[i].getY(),
//                        Y);
//                PropertyValuesHolder p3 = PropertyValuesHolder.ofFloat("alpha", 1.0f, 0.0f);
//                ObjectAnimator.ofPropertyValuesHolder(imageViews[i], p1, p2, p3)
//                        .setDuration(300 * i).start();
//            }
//        }
    }

    /**
     * 地图的单击 事件
     * 隐藏当前 InfoWindow
     */
    @Override
    public void onMapClick(LatLng point) {

        mBaiduMap.hideInfoWindow();
    }

    /**
     * 地图内 Poi 单击事件回调方法
     */
    @Override
    public boolean onMapPoiClick(MapPoi poi) {

        return false;
    }

    /**
     * 以下为生命周期及菜单选项
     */

    /**
     * 菜单创建方法
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * 菜单选择项 点击事件
     */
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

    /**
     * 地图 和 Activity 销毁时 的 处理方法
     * 在activity执行onDestroy时执行mMapView.onDestroy()，
     * 实现地图生命周期管理
     */
    @Override
    protected void onDestroy() {

        super.onDestroy();
        mMapView.onDestroy();
        poiSearch.mPoiSearch.destroy();

        ActivityCollector.removeActivity(this);
    }

    /**
     * 在activity执行onResume时执行mMapView. onResume ()，
     * 实现地图生命周期管理
     */
    @Override
    protected void onResume() {

        super.onResume();

        mMapView.onResume();
    }

    /**
     * 在activity执行onPause时执行mMapView. onPause ()，
     * 实现地图生命周期管理
     */
    @Override
    protected void onPause() {

        super.onPause();
        mMapView.onPause();
    }

    /**
     * 生命周期的管理
     */
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

    /**
     * 按两次back 退出程序两次间隔时间节点
     * 设置为2000 ms
     */
    @Override
    public void onBackPressed() {

        if ((System.currentTimeMillis() - exitTime) > 2000) {

            Toast.makeText(MainActivity.this, "再按一次退出应用", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
            return;
        }
        ActivityCollector.finishAll();
    }

}