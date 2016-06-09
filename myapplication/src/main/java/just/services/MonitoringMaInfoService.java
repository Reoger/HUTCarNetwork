package just.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

/**
 * Created by Just on 2016/5/25.
 * 用于数据实时同步的服务
 * 转换思想用于检测服务器的维护信息
 */
public class MonitoringMaInfoService extends IntentService {

    public MonitoringMaInfoService() {
        super("MonitoringMaInfoService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("测试->MonMaInfoService","+++currentThread="+Thread.currentThread());
        Context mContext=getApplicationContext();


    }

    /**
     * 有可能由于意外原因，导致连接失败
     * 这时需要开启一个定时服务，重新开启此服务
     */
//    private void dealFailed() {
////设置定时，一段时间再开启同步服务
//        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
//        int time = 60000;//一分钟
//        long triggerAtTime = SystemClock.elapsedRealtime() + time;
//        Intent i = new Intent(this, MonitoringMaInfoRestartReceiver.class);
//        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
//        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("测试->MonMaInfoService","onDestroy已执行");
    }
}
