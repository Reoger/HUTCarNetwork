package com.com.reoger.music.View;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import com.com.reoger.music.Inface.IMusic;
import com.com.reoger.music.Utils.CharacterParser;
import com.com.reoger.music.Utils.LogUtils;
import com.com.reoger.music.Utils.MyAdapter;
import com.com.reoger.music.Utils.PinyinComparator;
import com.com.reoger.music.Utils.Utils;
import com.com.reoger.music.constant.Constant;
import com.com.reoger.music.mode.Music;
import com.com.reoger.music.mode.Sequence;
import com.com.reoger.music.service.ServiceForMusic;
import com.cwp.android.baidutest.R;

import java.util.ArrayList;
import java.util.Collections;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    private ListView mMusicList;
    private MyAdapter adaper;
    private boolean mIsMusicPlaying = true;
    private IMusic binder;
    private ArrayList<Music> mMusicData = new ArrayList<>();
    private Intent in;
    private SeekBar mSeekBar;
    private int mCurrSongIndex = 0;//用于记录当前歌曲的索引
    private static final String TAG = "MainActivity";
    private ImageView mImagePause;
    private ImageButton mImageButtonOrder;//用于判断当前是随机播放，顺序播放和循环播放
    private String mCurrSongName;
    private Toolbar toolbar;
    private TextView mTimeStare;
    private TextView mTimeTop;

    private SideBar sideBar;
    private ClearEditText mClearEditText;
    private TextView dialog;
    /**
     * 汉字转换成拼音的类
     */
    private CharacterParser characterParser;
    /***
     * 根据拼音来排列ListView里面的数据类
     */
    private PinyinComparator pinyinComparator;

    public ButtonBroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_music);
        getSupportActionBar().hide();

        initView();
        initEvent();
        initTitle();
        initIntent();

        mMusicList.setOnItemClickListener(this);
        mMusicList.setOnItemLongClickListener(this);
        initButtonReceiver();

    }

    private void initEvent() {
        //设置右侧触摸监听
        sideBar.setOnTouchingLetterChangedListener(s -> {
            //该字母首次出现的位置
            int position = adaper.getPositionForSection(s.charAt(0));
            if (position != -1) {
                mMusicList.setSelection(position);
            }

        });


        // 根据a-z进行排序源数据
        Collections.sort(mMusicData, pinyinComparator);
        adaper = new MyAdapter(this, mMusicData);
        mMusicList.setAdapter(adaper);

        mClearEditText = (ClearEditText) findViewById(R.id.filter_edit);
        //根据输入框输入值的改变来过滤搜索
        mClearEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
                filterData(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * 为ListView填充数据
     *
     * @param
     * @return
     */
    private ArrayList<Music> filledData() {
        ArrayList<Music> date = Utils.getDataFromSD(MainActivity.this);

        for (int i = 0; i < date.size(); i++) {
            Music sortModel = date.get(i);
            String name = sortModel.getmMusicName();
            LogUtils.e("TAG", "name" + name);
            sortModel.setmMusicName(name);
            //汉字转换成拼音
            String pinyin = characterParser.getSelling(name);
            LogUtils.e("TAG", "pingyin" + pinyin);
            String sortString = pinyin.substring(0, 1).toUpperCase();
            LogUtils.e("TAG", "sortString :" + sortString);
            // 正则表达式，判断首字母是否是英文字母
            if (sortString.matches("[A-Z]")) {
                sortModel.setSortLetters(sortString.toUpperCase());
                LogUtils.e("TAG", "sortString.toUpperCase() :" + sortString.toUpperCase());
            } else {
                sortModel.setSortLetters("#");
            }
        }

        return date;
    }

    /**
     * 根据输入框中的值来过滤数据并更新ListView
     *
     * @param filterStr
     */
    private void filterData(String filterStr) {
        ArrayList<Music> filterDateList = new ArrayList<>();

        if (TextUtils.isEmpty(filterStr)) {
            filterDateList = mMusicData;
        } else {
            filterDateList.clear();
            for (Music sortModel : mMusicData) {
                String name = sortModel.getmMusicName();
                if (name.indexOf(filterStr.toString()) != -1 || characterParser.getSelling(name).startsWith(filterStr.toString())) {
                    filterDateList.add(sortModel);
                }
            }
        }

        // 根据a-z进行排序
        Collections.sort(filterDateList, pinyinComparator);
        adaper.updateListView(filterDateList);
    }

    /**
     * 初始化title
     */
    private void initTitle() {
        mCurrSongName = mMusicData.get(mCurrSongIndex).getmMusicName();
        toolbar.setTitle(mCurrSongName);

        //setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.back1);
        toolbar.setOnClickListener(v -> {
//            Intent intent = new Intent(MainActivity.this, NextActivity.class);
//            startActivity(intent);
            for (int i = 0; i < mMusicData.size(); i++) {
                LogUtils.e("TAG", mMusicData.get(i).getmMusicName());
                LogUtils.e("TAG", mMusicData.get(i).getSortLetters());
            }

        });
        toolbar.setNavigationOnClickListener(v -> {
            Intent intentForMain = new Intent(MainActivity.this, com.cwp.android.baidutest.MainActivity.class);
            startActivity(intentForMain);
        });
    }


    /**
     * 初始化服务
     */
    private void initIntent() {
        if (mMusicData.size() > 0) {
            in = new Intent();
            // in.setAction(ACTION);
            LogUtils.e("TAG", "歌曲运行了");
            in.setClass(this, ServiceForMusic.class);
            in.putExtra("path", mMusicData.get(mCurrSongIndex).getmMusicPath());
            startService(in);//隐式启动service
            bindService(in, conn, Context.BIND_AUTO_CREATE);
        }

        new Thread(() -> {
            try {
                Thread.sleep(1000);
                if (binder != null) {
                    binder.init(mSeekBar, mTimeStare, mTimeTop, () -> {
                        nextMusic(null);//自动播放下一曲


                        LogUtils.e(TAG, "这是在主函数里面的的自动播放下一曲");

                    });
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
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


        mSeekBar = (SeekBar) findViewById(R.id.seekBar1);
        mImagePause = (ImageView) findViewById(R.id.ic_pause);
        mImageButtonOrder = (ImageButton) findViewById(R.id.img_zj);
        mTimeStare = (TextView) findViewById(R.id.time_start);
        mTimeTop = (TextView) findViewById(R.id.time_top);
        mCurrSongIndex = Utils.getDate(this);
        mClearEditText = (ClearEditText) findViewById(R.id.filter_edit);
        //实例化汉字转拼音类
        characterParser = CharacterParser.getInstance();
        mMusicData = filledData();
        pinyinComparator = new PinyinComparator();
        sideBar = (SideBar) findViewById(R.id.sidrbar);
        dialog = (TextView) findViewById(R.id.dialog);
        sideBar.setTextView(dialog);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
        //主动获取数据
        Utils.cancelNotication(MainActivity.this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.saveDate(mCurrSongIndex, this);
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
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }

        Utils.cancelNotication(this);
    }

    public boolean onlyPlayMusic() {
        binder.startMusic();
        mImagePause.setBackgroundDrawable(getResources().getDrawable(
                R.drawable.play));
        mIsMusicPlaying = true;
        mCurrSongName = mMusicData.get(mCurrSongIndex).getmMusicName();
        toolbar.setTitle(mCurrSongName);

        showButtonNotify();
        return true;
    }

    /**
     * 点击歌曲时候的处理逻辑
     *
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
                Toast.makeText(MainActivity.this, "顺序播放", Toast.LENGTH_SHORT).show();
                LogUtils.i(TAG, "当前的状态是" + status + "CYCLE");
                break;
            case CYCLE:
                mImageButtonOrder.setBackgroundResource(R.drawable.cycle);
                status = Sequence.RANDER;
                Toast.makeText(MainActivity.this, "单曲循环", Toast.LENGTH_SHORT).show();
                LogUtils.i(TAG, "当前的状态是" + status + "RANDER");
                break;
            case RANDER:
                status = Sequence.OREDR;
                mImageButtonOrder.setBackgroundResource(R.drawable.random);

                Toast.makeText(MainActivity.this, "随机播放", Toast.LENGTH_SHORT).show();
                LogUtils.i(TAG, "当前的状态是" + status + "OREDR");
                break;
        }
    }

    /**
     * 带按钮的通知栏
     */
    public void showButtonNotify() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        RemoteViews mRemoteViews = new RemoteViews(getPackageName(), R.layout.view_custom_button);
        mRemoteViews.setImageViewResource(R.id.custom_song_icon, R.drawable.ic_logo);
        mRemoteViews.setTextViewText(R.id.tv_custom_song_singer, mMusicData.get(mCurrSongIndex).getmMusicArtist());
        mRemoteViews.setTextViewText(R.id.tv_custom_song_name, mCurrSongName);
        Intent intentMain = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(MainActivity.this, 0, intentMain, PendingIntent.FLAG_CANCEL_CURRENT);
        mRemoteViews.setViewVisibility(R.id.ll_custom_button, View.VISIBLE);

        if (mIsMusicPlaying) {
            mRemoteViews.setImageViewResource(R.id.btn_custom_play, R.mipmap.ic_pause);
        } else {
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
                .setContentIntent(pi)
                .setPriority(Notification.PRIORITY_DEFAULT)// 设置该通知优先级
                .setOngoing(true)
                .setSmallIcon(R.drawable.ab_share);
        Notification notify = mBuilder.build();
        notify.flags = Notification.FLAG_ONGOING_EVENT;

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(200, notify);
    }


    public PendingIntent getDefalutIntent(int flags) {
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, new Intent(), flags);
        return pendingIntent;
    }

    /**
     * 带按钮的通知栏点击广播接收
     */
    public void initButtonReceiver() {
        mReceiver = new ButtonBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.ACTION_BUTTON);
        registerReceiver(mReceiver, intentFilter);
    }

    /**
     * 保存数据
     *
     * @param outState
     * @param outPersistentState
     */
    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        LogUtils.e("SAVE", "Save");
        super.onSaveInstanceState(outState, outPersistentState);

    }

    /**
     * 读取数据
     *
     * @param savedInstanceState
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        LogUtils.e("SAVE", "Restore");
        super.onRestoreInstanceState(savedInstanceState);

    }

    /**
     * 长安的监听事件
     *
     * @param parent
     * @param view
     * @param position
     * @param id
     * @return
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        showPopupMenu(view,position);
        return false;
    }

    /**
     * 弹出菜单
     *
     * @param view
     * @return
     */
    public boolean showPopupMenu(View view,int postion) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        Menu menu = popupMenu.getMenu();
        menu.add(Menu.NONE, Menu.FIRST + 0, 0, "删除");
        menu.add(Menu.NONE, Menu.FIRST + 1, 1, "详细");
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case Menu.FIRST + 0:
                 showSureInfo(postion);
                    break;
                case Menu.FIRST + 1:
                    Music music = mMusicData.get(postion);
                    String items[] = new String[4];
                    items[0] = "歌曲名字："+music.getmMusicName();
                    items[1] = "歌手名字："+music.getmMusicArtist();
                    items[2] = "歌曲长度："+music.getmMusicTime();
                    items[3] = "歌曲路径："+music.getmMusicPath();
                    showDetailed(items);
                    break;
            }
            return false;
        });
        popupMenu.show();
        return true;
    }

    /**
     * 显示详细的信息
     *
     * @return
     */
    public boolean showDetailed(String []items) {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);  //先得到构造器
        builder.setTitle("详细信息"); //设置标题
        //builder.setMessage("是否确认退出?"); //设置内容
       // builder.setIcon(R.mipmap.ic_launcher);//设置图标，图片id即可
        //设置列表显示，注意设置了列表显示就不要设置builder.setMessage()了，否则列表不起作用。
        builder.setItems(items, (dialog1, which) -> {
            dialog1.dismiss();
            Toast.makeText(MainActivity.this, items[which], Toast.LENGTH_SHORT).show();
        });
        builder.setPositiveButton("确定", (dialog1, which) -> {
            dialog1.dismiss();
            Toast.makeText(MainActivity.this, "确定", Toast.LENGTH_SHORT).show();
        });
        builder.create().show();

        return true;
    }

    // 弹出确认删除的按钮
    public boolean showSureInfo(final int position) {
        new AlertDialog.Builder(this).setTitle("我的提示").setMessage("确定要删除吗？")
                .setPositiveButton("确定", (dialog1, which) -> {
                    mMusicData.remove(position);
                    // 通过程序我们知道删除了，但是怎么刷新ListView呢？
                    // 只需要重新设置一下adapter
                    adaper.notifyDataSetChanged();
                    Toast.makeText(MainActivity.this,"删除成功",Toast.LENGTH_SHORT).show();
                }).show();
        return true;
    }

    /**
     * 广播监听按钮点击时间
     */
    public class ButtonBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
            if (action.equals(Constant.ACTION_BUTTON)) {
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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(MainActivity.this,
                com.cwp.android.baidutest.MainActivity.class);
        startActivity(intent);

    }
}
