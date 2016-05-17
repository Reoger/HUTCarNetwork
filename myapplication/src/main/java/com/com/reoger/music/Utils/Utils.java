package com.com.reoger.music.Utils;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.provider.MediaStore;


import com.com.reoger.music.View.MainActivity;
import com.com.reoger.music.mode.Music;
import com.cwp.android.baidutest.R;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by 24540 on 2016/4/19.
 */
public class Utils {
    static int mSize = 0;


    /**
     * 获取手机和sd卡目录内的所有歌曲信息，
     *
     * @return
     */
    public static ArrayList<Music> getDataFromSD(Context context) {
        ArrayList<Music> data = new ArrayList<>();

        ContentResolver musicResolcer = context.getContentResolver();
        Cursor musicCursor = musicResolcer.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Audio.Media.SIZE + ">8000", null, null);
        int musicColumnIndex;
        if (musicCursor != null && musicCursor.getCount() > 0) {
            for (musicCursor.moveToFirst(); !musicCursor.isAfterLast();
                 musicCursor.moveToNext()) {
                Music item = new Music();
                musicColumnIndex = musicCursor.getColumnIndex(
                        MediaStore.Audio.AudioColumns._ID);
                int musicRating = musicCursor.getInt(musicColumnIndex);
                item.setmMusicRating(musicRating);

                //取得音乐播放路径
                musicColumnIndex = musicCursor.getColumnIndex(
                        MediaStore.Audio.AudioColumns.DATA);

                item.setmMusicPath(musicCursor.getString(musicColumnIndex));
                //获取音乐的名字
                musicColumnIndex = musicCursor.getColumnIndex(
                        MediaStore.Audio.AudioColumns.TITLE);
                item.setmMusicName(musicCursor.getString(musicColumnIndex));
                //获取音乐的演唱者
                musicColumnIndex = musicCursor
                        .getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST);
                item.setmMusicArtist(musicCursor.getString(musicColumnIndex));
                //获取歌曲的时间
                musicColumnIndex = musicCursor.getColumnIndex(
                        MediaStore.Audio.AudioColumns.DURATION);
                int musicTime = musicCursor.getInt(musicColumnIndex);//单位是毫秒
                LogUtils.e("Utils",musicTime+"时间");
                if(musicTime<59*1000){//过滤掉时间少于一分钟的歌曲
                    continue;
                }
                //
                // Time musicTime = new Time();
                // musicTime.set(musicTime);
                String readableTime = ":";
                int m = musicTime % 60000 / 1000;
                int o = musicTime / 60000;
                if (o == 0) {
                    readableTime = "00" + readableTime;
                } else if (0 < o && o < 10) {
                    readableTime = "0" + o + readableTime;
                } else {
                    readableTime = o + readableTime;
                }
                if (m < 10) {
                    readableTime = readableTime + "0" + m;
                } else {
                    readableTime = readableTime + m;
                }

                item.setmMusicTime(readableTime);

                data.add(item);
            }
        }
        mSize = data.size();
        return data;
    }

    /**
     * 显示通知
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static boolean showNotification(Context context, String songName) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(context,MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        //这里存在利用PendingIntent重新进入Activity时会重新开启另外一个Service
        //从而会同时播放两首歌
        Notification noti = new Notification.Builder(context)
                .setContentTitle("正在播放歌曲 ")
                .setContentText(songName)
                .setSmallIcon(R.drawable.ab_share)
                //.setLargeIcon(aBitmap)
                .setContentIntent(pi)
                .build();

        manager.notify(1,noti);
        //   service.startForeground(1,noti);
        return true;
    }
    /**
     * 取消通知
     */
    public static void cancelNotication(Context context){
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(200);
    }

    /**
     * 保存数据
     * @param index
     */
    public static void saveDate(int index,Context context){
        SharedPreferences.Editor editor = context.getSharedPreferences("data",
                Context.MODE_PRIVATE).edit();
        editor.putInt("index",index);
        editor.commit();
    }

    /**
     * 读取数据
     * @return
     */
    public static int getDate(Context context){
        SharedPreferences pre  = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        int a = pre.getInt("index",0);
        return a;
    }

    /**
     * 自定义的通知连
     */
//    public static void showButtonNotify(Context context){
//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
//        RemoteViews mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_noti);
//        mRemoteViews.setImageViewResource(R.id.noti_play, R.drawable.up);
//        //API3.0 以上的时候显示按钮，否则消失
//        mRemoteViews.setTextViewText(R.id.noti_song_name, "周杰伦");
//      //  mRemoteViews.setTextViewText(R.id.tv_custom_song_name, "七里香");
//        boolean isPlay = true;
//        if(isPlay){
//            mRemoteViews.setImageViewResource(R.id.noti_image, R.drawable.ab_android);
//        }else{
//            mRemoteViews.setImageViewResource(R.id.noti_image, R.drawable.ab_share);
//        }
//        String INTENT_BUTTONID_TAG = "com.text";
//        String ACTION_BUTTON = "com.reoger";
//        String BUTTON_PREV_ID = "play";
//        String BUTTON_PALY_ID ="prev";
//        String BUTTON_NEXT_ID = "next";
//
//        //点击的事件处理
//        Intent buttonIntent = new Intent(ACTION_BUTTON);
//        /* 上一首按钮 */
//        buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_PREV_ID);
//        //这里加了广播，所及INTENT的必须用getBroadcast方法
//        PendingIntent intent_prev = PendingIntent.getBroadcast(context, 1, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        mRemoteViews.setOnClickPendingIntent(R.id.noti_up, intent_prev);
//        /* 播放/暂停  按钮 */
//        buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_PALY_ID);
//        PendingIntent intent_paly = PendingIntent.getBroadcast(context, 2, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        mRemoteViews.setOnClickPendingIntent(R.id.noti_play, intent_paly);
//        /* 下一首 按钮  */
//        buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_NEXT_ID);
//        PendingIntent intent_next = PendingIntent.getBroadcast(context, 3, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        mRemoteViews.setOnClickPendingIntent(R.id.noti_next, intent_next);
//
//        mBuilder.setContent(mRemoteViews)
//          //      .setContentIntent(getDefalutIntent(Notification.FLAG_ONGOING_EVENT))
//                .setWhen(System.currentTimeMillis())// 通知产生的时间，会在通知信息里显示
//                .setTicker("正在播放")
//                .setPriority(Notification.PRIORITY_DEFAULT)// 设置该通知优先级
//                .setOngoing(true)
//                .setSmallIcon(R.drawable.ab_android);
//        Notification notify = mBuilder.build();
//        notify.flags = Notification.FLAG_ONGOING_EVENT;
//        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        manager.notify(1001, notify);
//    }
}
/**
 * 良好的变成习惯就是 在代码中尽量不要出现不知含义的数字
 */