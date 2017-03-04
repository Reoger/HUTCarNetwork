package com.cwp.android.baidutest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.baidu.navisdk.adapter.BNOuterLogUtil;
import com.baidu.navisdk.adapter.BNOuterTTSPlayerCallback;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNRoutePlanNode.CoordinateType;
import com.baidu.navisdk.adapter.BNaviSettingManager;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.baidu.navisdk.adapter.BaiduNaviManager.NaviInitListener;
import com.baidu.navisdk.adapter.BaiduNaviManager.RoutePlanListener;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import utils.MyLog;

public class BNMainActivity extends Activity {


	public static List<Activity> activityList = new LinkedList<Activity>();

	private static final String APP_FOLDER_NAME = "BNSDKSimpleDemo";

	private Button mWgsNaviBtn = null;
	private Button mGcjNaviBtn = null;
	private Button mBdmcNaviBtn = null;
	private Button mDb06ll = null;
	private String mSDCardPath = null;

	public static final String ROUTE_PLAN_NODE = "routePlanNode";
	public static final String SHOW_CUSTOM_ITEM = "showCustomItem";
	public static final String RESET_END_NODE = "resetEndNode";
	public static final String VOID_MODE = "voidMode";

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		activityList.add(this);
		
		setContentView(R.layout.map_navi_layout);

		
		mWgsNaviBtn = (Button) findViewById(R.id.wgsNaviBtn);
		mGcjNaviBtn = (Button) findViewById(R.id.gcjNaviBtn);
		mBdmcNaviBtn = (Button) findViewById(R.id.bdmcNaviBtn);
		mDb06ll = (Button) findViewById(R.id.mDb06llNaviBtn);
		BNOuterLogUtil.setLogSwitcher(true);
		  
		initListener();
		if (initDirs()) {
			initNavi();		
		}
		
	
//		 BNOuterLogUtil.setLogSwitcher(true);
	}


	@Override
	protected void onResume() {
		super.onResume();
	}

	private void initListener() {
	
		if (mWgsNaviBtn != null) {
			mWgsNaviBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if (BaiduNaviManager.isNaviInited()) {
						routeplanToNavi(CoordinateType.WGS84);
					}
				}

			});
		}
		if (mGcjNaviBtn != null) {
			mGcjNaviBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if (BaiduNaviManager.isNaviInited()) {
						routeplanToNavi(CoordinateType.GCJ02);
					}
				}

			});
		}
		if (mBdmcNaviBtn != null) {
			mBdmcNaviBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					if (BaiduNaviManager.isNaviInited()) {
						routeplanToNavi(CoordinateType.BD09_MC);
					}
				}
			});
		}

		if (mDb06ll != null) {
			mDb06ll.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (BaiduNaviManager.isNaviInited()) {
						routeplanToNavi(CoordinateType.BD09LL);
					}
				}
			});
		}


	}


	private boolean initDirs() {
		mSDCardPath = getSdcardDir();
		if (mSDCardPath == null) {
			return false;
		}
		File f = new File(mSDCardPath, APP_FOLDER_NAME);
		if (!f.exists()) {
			try {
				f.mkdir();
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	String authinfo = null;

	/**
	 * 内部TTS播报状态回传handler
	 */
	private Handler ttsHandler = new Handler() {
	    public void handleMessage(Message msg) {
	        int type = msg.what;
	        switch (type) {
	            case BaiduNaviManager.TTSPlayMsgType.PLAY_START_MSG: {
	                 showToastMsg("Handler : TTS play start");
	                break;
	            }
	            case BaiduNaviManager.TTSPlayMsgType.PLAY_END_MSG: {
	                 showToastMsg("Handler : TTS play end");
	                break;
	            }
	            default :
	                break;
	        }
	    }
	};
	
	/**
	 * 内部TTS播报状态回调接口
	 */
	private BaiduNaviManager.TTSPlayStateListener ttsPlayStateListener = new BaiduNaviManager.TTSPlayStateListener() {
        
        @Override
        public void playEnd() {
//            showToastMsg("TTSPlayStateListener : TTS play end");
        }
        
        @Override
        public void playStart() {
//            showToastMsg("TTSPlayStateListener : TTS play start");
        }
    };
	
	public void showToastMsg(final String msg) {
	    BNMainActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(BNMainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
	}
	
	private void initNavi() {	
	
		BNOuterTTSPlayerCallback ttsCallback = null;

		BaiduNaviManager.getInstance().init(this, mSDCardPath, APP_FOLDER_NAME, new NaviInitListener() {
			@Override
			public void onAuthResult(int status, String msg) {
				if (0 == status) {
					authinfo = "key校验成功!";
				} else {
					authinfo = "key校验失败, " + msg;
				}
				BNMainActivity.this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						Toast.makeText(BNMainActivity.this, authinfo, Toast.LENGTH_LONG).show();
					}
				});
			}

			public void initSuccess() {
				Toast.makeText(BNMainActivity.this, "百度导航引擎初始化成功", Toast.LENGTH_SHORT).show();
				initSetting();
			}

			public void initStart() {
				Toast.makeText(BNMainActivity.this, "百度导航引擎初始化开始", Toast.LENGTH_SHORT).show();
			}

			public void initFailed() {
				Toast.makeText(BNMainActivity.this, "百度导航引擎初始化失败", Toast.LENGTH_SHORT).show();
			}


		},  null, ttsHandler, null);

	}

	private String getSdcardDir() {
		if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
			return Environment.getExternalStorageDirectory().toString();
		}
		return null;
	}

	private void routeplanToNavi(CoordinateType coType) {
		BNRoutePlanNode sNode = null;
		BNRoutePlanNode eNode = null;
		switch (coType) {
			case GCJ02: {
				sNode = new BNRoutePlanNode(116.30142, 40.05087, "百度大厦", null, coType);
				eNode = new BNRoutePlanNode(116.39750, 39.90882, "北京天安门", null, coType);
				break;
			}
			case WGS84: {
				sNode = new BNRoutePlanNode(116.300821, 40.050969, "百度大厦", null, coType);
				eNode = new BNRoutePlanNode(116.397491, 39.908749, "北京天安门", null, coType);
				break;
			}
			case BD09_MC: {
				sNode = new BNRoutePlanNode(12947471, 4846474, "百度大厦", null, coType);
				eNode = new BNRoutePlanNode(12958160, 4825947, "北京天安门", null, coType);
				break;
			}
			case BD09LL: {
				sNode = new BNRoutePlanNode(116.30784537597782, 40.057009624099436, "百度大厦", null, coType);
				eNode = new BNRoutePlanNode(116.40386525193937, 39.915160800132085, "北京天安门", null, coType);
				break;
			}
			default:
				;
			}
			if (sNode != null && eNode != null) {
				List<BNRoutePlanNode> list = new ArrayList<BNRoutePlanNode>();
				list.add(sNode);
				list.add(eNode);
				BaiduNaviManager.getInstance().launchNavigator(this, list, 1, true, new DemoRoutePlanListener(sNode));
			}
	}

	public  void routeplanToNavi(CoordinateType coType,LatLng stPosition ,LatLng enPosition) {
		BNRoutePlanNode sNode = null;
		BNRoutePlanNode eNode = null;
		switch (coType) {
			case GCJ02: {
				sNode = new BNRoutePlanNode(116.30142, 40.05087, "百度大厦", null, coType);
				eNode = new BNRoutePlanNode(116.39750, 39.90882, "北京天安门", null, coType);
				break;
			}
			case WGS84: {
				sNode = new BNRoutePlanNode(116.300821, 40.050969, "百度大厦", null, coType);
				eNode = new BNRoutePlanNode(116.397491, 39.908749, "北京天安门", null, coType);
				break;
			}
			case BD09_MC: {
				sNode = new BNRoutePlanNode(12947471, 4846474, "百度大厦", null, coType);
				eNode = new BNRoutePlanNode(12958160, 4825947, "北京天安门", null, coType);
				break;
			}
			case BD09LL: {
				sNode = new BNRoutePlanNode(stPosition.latitude, stPosition.longitude,null, null, coType);
				eNode = new BNRoutePlanNode(enPosition.latitude, enPosition.longitude, null, null, coType);
				break;
			}
			default:
				;
		}
		if (sNode != null && eNode != null) {
			List<BNRoutePlanNode> list = new ArrayList<BNRoutePlanNode>();
			list.add(sNode);
			list.add(eNode);
			BaiduNaviManager.getInstance().launchNavigator(this, list, 1, true, new DemoRoutePlanListener(sNode));
		}
	}




	public class DemoRoutePlanListener implements RoutePlanListener {

		private BNRoutePlanNode mBNRoutePlanNode = null;

		public DemoRoutePlanListener(BNRoutePlanNode node) {
			mBNRoutePlanNode = node;
		}

		@Override
		public void onJumpToNavigator() {
			/*
			 * 设置途径点以及resetEndNode会回调该接口
			 */
		 
			for (Activity ac : activityList) {
			   
				if (ac.getClass().getName().endsWith("BNGuideActivity")) {
				 
					return;
				}
			}
			MyLog.LogE("BNMainActivity","intent");

			Intent intent = new Intent(BNMainActivity.this, BNGuideActivity.class);
			Bundle bundle = new Bundle();
			bundle.putSerializable(ROUTE_PLAN_NODE, (BNRoutePlanNode) mBNRoutePlanNode);
			intent.putExtras(bundle);

			MyLog.LogE("BNMainActivity","before startActivity");
			startActivity(intent);
			MyLog.LogE("BNMainActivity","after startActivity");

		}

		@Override
		public void onRoutePlanFailed() {
			// TODO Auto-generated method stub
			Toast.makeText(BNMainActivity.this, "算路失败", Toast.LENGTH_SHORT).show();
		}
	}
	
	private void initSetting(){
	    BNaviSettingManager.setDayNightMode(BNaviSettingManager.DayNightMode.DAY_NIGHT_MODE_DAY);
	    BNaviSettingManager.setShowTotalRoadConditionBar(BNaviSettingManager.PreViewRoadCondition.ROAD_CONDITION_BAR_SHOW_ON);
	    BNaviSettingManager.setVoiceMode(BNaviSettingManager.VoiceMode.Veteran);
        BNaviSettingManager.setPowerSaveMode(BNaviSettingManager.PowerSaveMode.DISABLE_MODE);
        BNaviSettingManager.setRealRoadCondition(BNaviSettingManager.RealRoadCondition.NAVI_ITS_ON);
	}

	private BNOuterTTSPlayerCallback mTTSCallback = new BNOuterTTSPlayerCallback() {

		@Override
		public void stopTTS() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "stopTTS");
		}

		@Override
		public void resumeTTS() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "resumeTTS");
		}

		@Override
		public void releaseTTSPlayer() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "releaseTTSPlayer");
		}

		@Override
		public int playTTSText(String speech, int bPreempt) {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "playTTSText" + "_" + speech + "_" + bPreempt);

			return 1;
		}

		@Override
		public void phoneHangUp() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "phoneHangUp");
		}

		@Override
		public void phoneCalling() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "phoneCalling");
		}

		@Override
		public void pauseTTS() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "pauseTTS");
		}

		@Override
		public void initTTSPlayer() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "initTTSPlayer");
		}

		@Override
		public int getTTSState() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "getTTSState");
			return 1;
		}
	};

}
