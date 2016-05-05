package com.com.reoger.music.View;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import com.com.reoger.music.Inface.IMusic;
import com.com.reoger.music.Inface.IsMusicOver;
import com.com.reoger.music.Utils.LogUtils;
import com.com.reoger.music.Utils.MyAdapter;
import com.com.reoger.music.Utils.Utils;
import com.com.reoger.music.constant.Constant;
import com.com.reoger.music.mode.Sequence;
import com.com.reoger.music.service.ServiceForMusic;
import com.cwp.android.baidutest.R;

import java.util.ArrayList;
import java.util.HashMap;



public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private ListView mMusicList;
    private MyAdapter adaper;
    private boolean mIsMusicPlaying = true;
    private IMusic binder;
    private ArrayList<HashMap<String, Object>> mMusicData = new ArrayList<HashMap<String, Object>>();
    private Intent in;
    private static final String ACTION = "com.create.musictest.service";
    private SeekBar mSeekBar;
    private int mCurrSongIndex = 0;//用于记录当前歌曲的索引
    private static final String TAG = "MainActivity";
    private ImageView mImagePause;
    private ImageButton mImageButtonOrder;//用于判断当前是随机播放，顺序播放和循环播放
    private String mCurrSongName;
    private Toolbar toolbar;
    private TextView mTimeStare;
    private TextView mTimeTop;

    public ButtonBroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        initView();
     //   initTitle();
        initIntent();
        mMusicList.setAdapter(adaper);
        mMusicList.setOnItemClickListener(this);
        initButtonReceiver();

    }

    /**
     * 初始化title
     */
    private void initTitle() {
        mCurrSongName = mMusicData.get(mCurrSongIndex).get("musicName") + "";
        toolbar.setTitle(mCurrSongName);
      //  setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ab_android);
        toolbar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NextActivity.class);
            startActivity(intent);
        });
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "你点击了我", Toast.LENGTH_SHORT).show();

                showButtonNotify();
            }
        });
    }


    /**
     * 初始化服务
     */
    private void initIntent() {
        if (mMusicData.size() > 0) {
            in = new Intent();
            // in.setAction(ACTION);
            in.setClass(this, ServiceForMusic.class);
            in.putExtra("path", mMusicData.get(mCurrSongIndex).get("path") + "");
            startService(in);//隐式启动service
            bindService(in, conn, Context.BIND_AUTO_CREATE);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    if (binder != null) {
                        binder.init(mSeekBar, mTimeStare,mTimeTop,new IsMusicOver() {
                            @Override
                            public void onMusicOver() {
                                nextMusic(null);//自动播放下一曲


                                LogUtils.e(TAG, "这是在主函数里面的的自动播放下一曲");

                            }
                        });
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (IMusic) service;

            LogUtils.d(TAG, binder + "");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtils.d(TAG, "onServiceDisconnected");
        }
    };

    /**
     * 初始化控件,进行绑定
     */
    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mMusicList = (ListView) findViewById(R.id.musicList);
        mMusicData = Utils.getDataFromSD(this);
        adaper = new MyAdapter(this, mMusicData);
        mSeekBar = (SeekBar) findViewById(R.id.seekBar1);
        mImagePause = (ImageView) findViewById(R.id.ic_pause);
        mImageButtonOrder = (ImageButton) findViewById(R.id.img_zj);
        mTimeStare = (TextView) findViewById(R.id.time_start);
        mTimeTop = (TextView) findViewById(R.id.time_top);
        mCurrSongIndex = Utils.getDate(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    public void playMusic(View view) {

        if (mIsMusicPlaying) {//当前在正在播放歌曲
            binder.pauseMusic();//暂停播放
            mImagePause.setBackgroundDrawable(getResources().getDrawable(
                    R.drawable.pause));
            LogUtils.d(TAG, "当前正在播放，点击暂停了");
            mIsMusicPlaying = false;

        } else {
            binder.resumeMusic();
            LogUtils.e(TAG, "当前没有播放，点击播放了");
            // binder.startMusic();
            mImagePause.setBackgroundDrawable(getResources().getDrawable(
                    R.drawable.play));
            mIsMusicPlaying = true;
        }
        toolbar.setTitle(mCurrSongName);
    }

    public void upMusic(View view) {
        mCurrSongIndex--;
        if (mCurrSongIndex <= 0) {
            mCurrSongIndex = 0;
            Toast.makeText(MainActivity.this, "这已经是第一首歌了", Toast.LENGTH_SHORT).show();
            LogUtils.d(TAG, "这已经是第一首歌了");
            return;
        }
        if (in != null && binder != null) {
            binder.restMusic();
            binder.removeMusic();
            binder.stopMusic();
            unbindService(conn);
            stopService(in);
        }

        initIntent();
        if (onlyPlayMusic()) {
            LogUtils.d(TAG, "下一曲");
        }
    }

    public void nextMusic(View view) {

        switch (status) {
            case CYCLE:
                mCurrSongIndex++;
                Toast.makeText(MainActivity.this, "顺序播放", Toast.LENGTH_SHORT).show();
                break;
            case RANDER://单曲循环
                Toast.makeText(MainActivity.this, "单曲循环", Toast.LENGTH_SHORT).show();
                break;
            case OREDR://随机播放
                int temp = (int) (0 + Math.random() * (mMusicData.size() - 0 + 1));
                if (temp == mCurrSongIndex) {
                    mCurrSongIndex = 1 + temp;
                } else {
                    mCurrSongIndex = temp;
                }
                LogUtils.d(TAG, mCurrSongIndex + " mMusicData.size()" + mMusicData.size());
                Toast.makeText(MainActivity.this, "随机播放" + mCurrSongIndex, Toast.LENGTH_SHORT).show();
                break;
        }

        if (mCurrSongIndex >= mMusicData.size()) {
            Toast.makeText(MainActivity.this, "这已经是最后一首歌了" + mCurrSongIndex, Toast.LENGTH_SHORT).show();
            mCurrSongIndex = 0;
        }
        if (in != null && binder != null) {
            binder.restMusic();
            binder.stopMusic();
            binder.removeMusic();
            unbindService(conn);
            stopService(in);
        }

        initIntent();
        if (onlyPlayMusic()) {
            LogUtils.d(TAG, "下一曲");
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.cancelNotication(MainActivity.this);
        Toast.makeText(MainActivity.this, "我又满血复活了", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
     //   Utils.showNotification(MainActivity.this, mMusicData.get(mCurrSongIndex).get("musicName") + "");
        showButtonNotify();
        Utils.saveDate(mCurrSongIndex, this);
     //   finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //这里需要添加保存数据的逻辑代码
        if(mReceiver != null){
            unregisterReceiver(mReceiver);
        }
        Utils.saveDate(mCurrSongIndex, this);
    }

    public boolean onlyPlayMusic() {
        binder.startMusic();
        mImagePause.setBackgroundDrawable(getResources().getDrawable(
                R.drawable.play));
        mIsMusicPlaying = true;
        mCurrSongName = mMusicData.get(mCurrSongIndex).get("musicName") + "";
        toolbar.setTitle(mCurrSongName);

        showButtonNotify();
        return true;
    }

    /**
     * 点击歌曲时候的处理逻辑
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mCurrSongIndex = position;
        if (in != null && binder != null) {
            binder.restMusic();
            binder.removeMusic();
            binder.stopMusic();
            unbindService(conn);
            stopService(in);
        }
        initIntent();
        if (onlyPlayMusic()) {
            LogUtils.d(TAG, "text");
        }
    }

    Sequence status = Sequence.OREDR;

    /**
     * 判断是循环播放，循序播放还是随机播放
     */
    public void sequence(View view) {//有三个状态

        switch (status) {
            case OREDR:
                mImageButtonOrder.setBackgroundResource(R.drawable.order);
                status = Sequence.CYCLE;
                LogUtils.i(TAG, "当前的状态是" + status + "CYCLE");
                break;
            case CYCLE:
                mImageButtonOrder.setBackgroundResource(R.drawable.cycle);
                status = Sequence.RANDER;
                LogUtils.i(TAG, "当前的状态是" + status + "RANDER");
                break;
            case RANDER:
                status = Sequence.OREDR;
                mImageButtonOrder.setBackgroundResource(R.drawable.random);
                LogUtils.i(TAG, "当前的状态是" + status + "OREDR");
                break;
        }
    }
    /**
     * 带按钮的通知栏
     */
    public void showButtonNotify(){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        RemoteViews mRemoteViews = new RemoteViews(getPackageName(), R.layout.view_custom_button);
        mRemoteViews.setImageViewResource(R.id.custom_song_icon, R.drawable.ab_android);
        mRemoteViews.setTextViewText(R.id.tv_custom_song_singer,mMusicData.get(mCurrSongIndex).get("musicArtist")+"" );
        mRemoteViews.setTextViewText(R.id.tv_custom_song_name, mCurrSongName);

        mRemoteViews.setViewVisibility(R.id.ll_custom_button, View.VISIBLE);

            if(mIsMusicPlaying){
                mRemoteViews.setImageViewResource(R.id.btn_custom_play, R.mipmap.ic_pause);
            }else{
                mRemoteViews.setImageViewResource(R.id.btn_custom_play, R.mipmap.ic_play);
            }
        //点击的事件处理
        Intent buttonIntent = new Intent(Constant.ACTION_BUTTON);
		/* 上一首按钮 */
        buttonIntent.putExtra(Constant.INTENT_BUTTONID_TAG, Constant.BUTTON_PREV_ID);
        //这里加了广播，所及INTENT的必须用getBroadcast方法
        PendingIntent intent_prev = PendingIntent.getBroadcast(this, 1, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.btn_custom_prev, intent_prev);
		/* 播放/暂停  按钮 */
        buttonIntent.putExtra(Constant.INTENT_BUTTONID_TAG, Constant.BUTTON_PALY_ID);
        PendingIntent intent_paly = PendingIntent.getBroadcast(this, 2, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.btn_custom_play, intent_paly);
		/* 下一首 按钮  */
        buttonIntent.putExtra(Constant.INTENT_BUTTONID_TAG, Constant.BUTTON_NEXT_ID);
        PendingIntent intent_next = PendingIntent.getBroadcast(this, 3, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.btn_custom_next, intent_next);

        mBuilder.setContent(mRemoteViews)
                .setContentIntent(getDefalutIntent(Notification.FLAG_ONGOING_EVENT))
                .setWhen(System.currentTimeMillis())// 通知产生的时间，会在通知信息里显示
                .setTicker("正在播放")
                .setPriority(Notification.PRIORITY_DEFAULT)// 设置该通知优先级
                .setOngoing(true)
                .setSmallIcon(R.drawable.ab_share);
        Notification notify = mBuilder.build();
        notify.flags = Notification.FLAG_ONGOING_EVENT;
        //会报错，还在找解决思路
//		notify.contentView = mRemoteViews;
//		notify.contentIntent = PendingIntent.getActivity(this, 0, new Intent(), 0);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(200, notify);
    }


    public  PendingIntent getDefalutIntent(int flags){
        PendingIntent pendingIntent= PendingIntent.getActivity(this, 1, new Intent(), flags);
        return pendingIntent;
    }


    /** 带按钮的通知栏点击广播接收 */
    public void initButtonReceiver(){
        mReceiver = new ButtonBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.ACTION_BUTTON);
        registerReceiver(mReceiver, intentFilter);
    }

    /**
     *	 广播监听按钮点击时间
     */
    public class ButtonBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
            if(action.equals(Constant.ACTION_BUTTON)){
                //通过传递过来的ID判断按钮点击属性或者通过getResultCode()获得相应点击事件
                int buttonId = intent.getIntExtra(Constant.INTENT_BUTTONID_TAG, 0);
                switch (buttonId) {
                    case Constant.BUTTON_PREV_ID:
                        LogUtils.d(TAG, "上一首");
                        upMusic(null);
                        break;
                    case Constant.BUTTON_PALY_ID:
                        playMusic(null);
                        showButtonNotify();
                        break;
                    case Constant.BUTTON_NEXT_ID:
                        LogUtils.d(TAG, "下一首");
                        nextMusic(null);

                        break;
                    default:
                        break;
                }
            }
        }
    }


}
