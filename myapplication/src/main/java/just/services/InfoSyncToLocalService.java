package just.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.cwp.android.baidutest.MyApplication;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import just.beans.AutoInfo;
import just.beans.MaInfo;
import just.operations.AutoInfoLocalDBOperation;
import just.operations.MaInfoLocalDBOperation;
import just.receivers.InfoSyncToLocalReceiver;
import just.utils.NetworkUtil;

/**
 * 用于从云端同步数据至本地
 */
public class InfoSyncToLocalService extends IntentService {

    public InfoSyncToLocalService() {
        super("InfoSyncToLocalService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Context mContext=getApplicationContext();

        if(NetworkUtil.isNetworkAvailable(mContext)) {
            Log.d("测试->MyApplication","startSyncFromCloudService->开始同步云端的数据");
            try {
                MyApplication.mSyncSemaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            BmobQuery<AutoInfo> query1=new BmobQuery<>();
            query1.addWhereEqualTo("username",MyApplication.getUsername());
            query1.setLimit(50);
            query1.findObjects(mContext, new FindListener<AutoInfo>() {
                @Override
                public void onSuccess(List<AutoInfo> list) {
                    for (AutoInfo autoInfo:list)
                        AutoInfoLocalDBOperation.insert(mContext,autoInfo,1);
                    MyApplication.mSyncSemaphore.release();
                }

                @Override
                public void onError(int i, String s) {
                    MyApplication.mSyncSemaphore.release();
                }
            });

            try {
                MyApplication.mSyncSemaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            BmobQuery<MaInfo> query2=new BmobQuery<>();
            query2.addWhereEqualTo("username",MyApplication.getUsername());
            query2.setLimit(1000);
            query2.findObjects(mContext, new FindListener<MaInfo>() {
                @Override
                public void onSuccess(List<MaInfo> list) {
                    for (MaInfo maInfo:list)
                        MaInfoLocalDBOperation.insert(mContext,maInfo,1);
                    MyApplication.mSyncSemaphore.release();
                }

                @Override
                public void onError(int i, String s) {
                    MyApplication.mSyncSemaphore.release();
                }
            });

            try {
                MyApplication.mSyncSemaphore.acquire();
            } catch (InterruptedException e) {
            }

            //设置定时，一段时间再开启同步服务
            AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
            int time = 3 * 60000;//三分钟
            long triggerAtTime = SystemClock.elapsedRealtime() + time;
            Intent i = new Intent(this, InfoSyncToLocalReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
            manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
            MyApplication.mSyncSemaphore.release();
        }
    }
}
