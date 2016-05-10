package just.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.cwp.android.baidutest.MyApplication;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.DeleteListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import just.beans.AutoInfo;
import just.constants.AutoInfoConstants;
import just.operations.AutoInfoLocalDBOperation;
import just.receivers.AutoInfoSyncReceiver;
import just.utils.NetworkUtil;

/**
 * 用于将本地汽车信息与云端的同步
 */
public class AutoInfoSyncService extends IntentService {
    private boolean isContinueSync=true;//用于判断某些情况下是否继续同步数据
    private Context mContext;

    public AutoInfoSyncService() {
            super("AutoInfoSyncService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mContext=getApplicationContext();
        try {
            MyApplication.mSyncSemaphore.acquire();
        } catch (InterruptedException e) {
        }
        Log.d("测试->AutoInfoSyncService","已成功开启服务");
        if(NetworkUtil.isNetworkAvailable(mContext)) {
            List<AutoInfo> list1 = AutoInfoLocalDBOperation.queryBy(mContext,
                    AutoInfoConstants.COLUMN_IS_SYNC + " = ?",
                    new String[]{"0"});
            if (list1.size() != 0) {
                for (AutoInfo autoInfo : list1) {
                    autoInfo.save(this, new SaveListener() {
                        @Override
                        public void onSuccess() {
                            AutoInfoLocalDBOperation.updateForIsSyncToCloud(mContext, autoInfo.getVin(), 1);
                            Log.d("测试->AutoInfoSyncService", "成功同步至云端");
                        }

                        @Override
                        public void onFailure(int i, String s) {
                            Log.d("测试->AutoInfoSyncService", "同步云端失败:错误编号-"+i+"，错误原因-"+s);
                            if(i==9016) {
                                isContinueSync=false;
                            }
                        }
                    });
                    if(!isContinueSync) {
                        Log.d("测试->AutoInfoSyncService","由于异常原因，退出当前同步!");
                        break;
                    }
                 }
            }
            isContinueSync=true;

            //实现删除云端本该删除的数据
            List<AutoInfo> list2 = AutoInfoLocalDBOperation.queryBy(mContext,
                    AutoInfoConstants.COLUMN_IS_SYNC + " = ? and "+AutoInfoConstants.COLUMN_IS_DEL_WITH_CLOUD+" = ?",
                    new String[]{"1","1"});
            if(list2.size()!=0) {
                for (AutoInfo autoInfo : list2) {
                    String vin= autoInfo.getVin();
                    BmobQuery<AutoInfo> query = new BmobQuery<>();
                    query.addWhereEqualTo("vin", vin);
                    query.setLimit(1);
                    query.addQueryKeys("objectId");
                    query.findObjects(mContext, new FindListener<AutoInfo>() {
                        @Override
                        public void onSuccess(List<AutoInfo> list) {
                            Log.d("测试->DeleteAutoInfoTask", "查询成功");
                            list.get(0).delete(mContext, new DeleteListener() {
                                @Override
                                public void onSuccess() {
                                    if (AutoInfoLocalDBOperation.deleteBy(mContext, AutoInfoConstants.COLUMN_VIN + " = ?", new String[]{vin})) {
                                        Log.d("测试->DeleteAutoInfoTask", "vin=" + vin + ",本地与云端删除成功!");
                                    }
                                }

                                @Override
                                public void onFailure(int i, String s) {
                                    AutoInfoLocalDBOperation.updateForIsDelWithCloud(mContext, vin, 1);
                                    Log.d("测试->DeleteAutoInfoTask", "删除失败！ i=" + i + ",s=" + s);
                                }
                            });

                        }

                        @Override
                        public void onError(int i, String s) {
                            Log.d("测试->DeleteAutoInfoTask", "查询失败:失败编码->" + i + ",失败原因->" + s);
                            if(i==9016) {
                                isContinueSync=false;
                            }
                        }
                    });

                    if(!isContinueSync) {
                        Log.d("测试->AutoInfoSyncService","由于异常原因，退出当前同步!");
                        break;
                    }
                }
            }
        }

        MyApplication.mSyncSemaphore.release();

        //设置定时，一段时间再开启同步服务
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int time = 60000;//一分钟
        long triggerAtTime = SystemClock.elapsedRealtime() + time;
        Intent i = new Intent(this, AutoInfoSyncReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("测试->AutoInfoSyncService","onDestroy以执行");
    }
}
