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
import java.util.concurrent.Semaphore;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.DeleteListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import just.beans.AutoInfo;
import just.beans.MaInfo;
import just.constants.AutoInfoConstants;
import just.constants.MaInfoConstants;
import just.operations.AutoInfoLocalDBOperation;
import just.operations.MaInfoLocalDBOperation;
import just.receivers.AutoAndMaInfoSyncReceiver;
import just.utils.NetworkUtil;

/**
 * 用于将本地汽车信息与云端的同步
 */
public class InfoSyncToCloudService extends IntentService {
    private boolean isContinueSync=true;//用于判断某些情况下是否继续同步数据
    private Context mContext;

    private Semaphore mIsNextSemaphore;//信号量的使用是为了使每块逻辑中的回调方法执行完毕之后再执行下一块的逻辑

    public InfoSyncToCloudService() {
            super("InfoSyncToCloudService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mContext=getApplicationContext();
        mIsNextSemaphore=new Semaphore(1);

        try {
            MyApplication.mSyncSemaphore.acquire();
        } catch (InterruptedException e) {
        }
        Log.d("测试->A&M-InfoSyncService","已成功开启服务");

        if(NetworkUtil.isNetworkAvailable(mContext)) {
            try {
                mIsNextSemaphore.acquire();
            } catch (InterruptedException e) {
            }
            List<AutoInfo> list1 = AutoInfoLocalDBOperation.queryBy(mContext,
                    AutoInfoConstants.COLUMN_IS_SYNC + " = ?",
                    new String[]{"0"});
            int total=list1.size();
            if (total != 0) {
                final int finalTotal1 = total;
                for(int i=0;i<total;i++) {
                    AutoInfo autoInfo=list1.get(i);
                    final int finalI = i;
                    autoInfo.save(this, new SaveListener() {
                        @Override
                        public void onSuccess() {
                            AutoInfoLocalDBOperation.updateForIsSyncToCloud(mContext, autoInfo.getVin(), 1);
                            Log.d("测试->A&M-InfoSyncService", "成功同步至云端");
                            if(finalI == finalTotal1 -1) {
                                mIsNextSemaphore.release();
                            }
                        }

                        @Override
                        public void onFailure(int i, String s) {
                            Log.d("测试->A&M-InfoSyncService", "同步云端失败:错误编号-"+i+"，错误原因-"+s);
                            if(i==9016) {
                                isContinueSync=false;
                                mIsNextSemaphore.release();
                            }
                        }
                    });
                    if(!isContinueSync) {
                        Log.d("测试->A&M-InfoSyncService","由于异常原因，退出当前同步!");
                        break;
                    }
                 }
            }
            else {
                mIsNextSemaphore.release();
            }

            isContinueSync=true;

            try {
                mIsNextSemaphore.acquire();
            } catch (InterruptedException e) {
            }
            //实现删除云端本该删除的数据
            List<AutoInfo> list2 = AutoInfoLocalDBOperation.queryBy(mContext,
                    AutoInfoConstants.COLUMN_IS_SYNC + " = ? and "+AutoInfoConstants.COLUMN_IS_DEL_WITH_CLOUD+" = ?",
                    new String[]{"1","1"});

            total=list2.size();
            if(total!=0) {
                final int finalTotal = total;
                for(int i=0;i<total;i++) {
                    AutoInfo autoInfo=list2.get(i);
                    String vin= autoInfo.getVin();
                    BmobQuery<AutoInfo> query = new BmobQuery<>();
                    query.addWhereEqualTo("vin", vin);
                    query.setLimit(1);
                    query.addQueryKeys("objectId");
                    final int finalI = i;
                    query.findObjects(mContext, new FindListener<AutoInfo>() {
                        @Override
                        public void onSuccess(List<AutoInfo> list) {
                            Log.d("测试->A&M-InfoSyncService", "查询成功");
                            list.get(0).delete(mContext, new DeleteListener() {
                                @Override
                                public void onSuccess() {
                                    if (AutoInfoLocalDBOperation.deleteBy(mContext, AutoInfoConstants.COLUMN_VIN + " = ?", new String[]{vin})) {
                                        Log.d("测试->A&M-InfoSyncService", "vin=" + vin + ",本地与云端删除成功!");
                                    }
                                    if(finalI == finalTotal -1) {
                                        mIsNextSemaphore.release();

                                    }
                                }

                                @Override
                                public void onFailure(int i, String s) {
                                    AutoInfoLocalDBOperation.updateForIsDelWithCloud(mContext, vin, 1);
                                    Log.d("测试->A&M-InfoSyncService", "删除失败！ i=" + i + ",s=" + s);
                                    if(finalI == finalTotal -1) {
                                        mIsNextSemaphore.release();
                                    }
                                }
                            });
                        }

                        @Override
                        public void onError(int i, String s) {
                            Log.d("测试->A&M-InfoSyncService", "查询失败:失败编码->" + i + ",失败原因->" + s);
                            if(i==9016) {
                                isContinueSync=false;
                                if(finalI ==finalTotal-1) {
                                    mIsNextSemaphore.release();
                                }
                            }
                        }
                    });

                    if(!isContinueSync) {
                        Log.d("测试->A&M-InfoSyncService","由于异常原因，退出当前同步!");
                        break;
                    }
                }
            }
            else {
                mIsNextSemaphore.release();
            }

            isContinueSync=true;
            try {
                mIsNextSemaphore.acquire();
            } catch (InterruptedException e) {
            }

            List<MaInfo> list3 = MaInfoLocalDBOperation.queryBy(mContext,
                    MaInfoConstants.COLUMN_IS_SYNC + " = ?",
                    new String[]{"0"});
            total=list3.size();
            if (total != 0) {
                final int finalTotal2 = total;
                for (int i=0;i<total;i++) {
                    MaInfo maInfo=list3.get(i);
                    final int finalI = i;
                    maInfo.save(this, new SaveListener() {
                        @Override
                        public void onSuccess() {
                            MaInfoLocalDBOperation.updateForIsSyncToCloud(mContext, maInfo.getScanTime(),maInfo.getVin(), 1);
                            Log.d("测试->A&M-InfoSyncService", "成功同步至云端");
                            if (finalI == finalTotal2 -1) {
                                mIsNextSemaphore.release();
                            }
                        }

                        @Override
                        public void onFailure(int i, String s) {
                            Log.d("测试->A&M-InfoSyncService", "同步云端失败:错误编号-"+i+"，错误原因-"+s);
                            if(i==9016) {
                                isContinueSync=false;
                                mIsNextSemaphore.release();
                            }
                        }
                    });
                    if(!isContinueSync) {
                        Log.d("测试->A&M-InfoSyncService","由于异常原因，退出当前同步!");
                        break;
                    }
                }
            }
            else {
                mIsNextSemaphore.release();
            }

            isContinueSync=true;
            try {
                mIsNextSemaphore.acquire();
            } catch (InterruptedException e) {
            }

            //实现删除云端本该删除的数据
            List<MaInfo> list4 = MaInfoLocalDBOperation.queryBy(mContext,
                    MaInfoConstants.COLUMN_IS_SYNC + " = ? and "+MaInfoConstants.COLUMN_IS_DEL_WITH_CLOUD+" = ?",
                    new String[]{"1","1"});
            total=list4.size();
            if(list4.size()!=0) {
                final int finalTotal3 = total;
                for (int i=0;i<total;i++) {
                    MaInfo maInfo=list4.get(i);
                    String vin= maInfo.getVin();
                    String scanTime=maInfo.getScanTime();
                    BmobQuery<MaInfo> query = new BmobQuery<>();
                    query.addWhereEqualTo("vin", vin);
                    query.addWhereEqualTo("scanTime", scanTime);
                    query.setLimit(1);
                    query.addQueryKeys("objectId");
                    final int finalI = i;
                    query.findObjects(mContext, new FindListener<MaInfo>() {
                        @Override
                        public void onSuccess(List<MaInfo> list) {
                            list.get(0).delete(mContext, new DeleteListener() {
                                @Override
                                public void onSuccess() {
                                    if (MaInfoLocalDBOperation.deleteBy(mContext,
                                            MaInfoConstants.COLUMN_VIN + " = ? and "+MaInfoConstants.COLUMN_SCAN_TIME+" = ?", new String[]{vin,scanTime})) {
                                    }
                                    if (finalI == finalTotal3 -1) {
                                        mIsNextSemaphore.release();
                                    }
                                }

                                @Override
                                public void onFailure(int i, String s) {
                                    MaInfoLocalDBOperation.updateForIsDelWithCloud(mContext, scanTime,vin, 1);
                                    if (finalI == finalTotal3 -1) {
                                        mIsNextSemaphore.release();
                                    }
                                }
                            });

                        }

                        @Override
                        public void onError(int i, String s) {
                            if(i==9016) {
                                isContinueSync=false;
                                mIsNextSemaphore.release();
                            }
                        }
                    });

                    if(!isContinueSync) {
                        Log.d("测试->A&M-InfoSyncService","由于异常原因，退出当前同步!");
                        break;
                    }
                }
            }
            else {
                mIsNextSemaphore.release();
            }
        }

        try {
            mIsNextSemaphore.acquire();
        } catch (InterruptedException e) {
        }

        MyApplication.mSyncSemaphore.release();

        //设置定时，一段时间再开启同步服务
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int time = 60000;//一分钟
        long triggerAtTime = SystemClock.elapsedRealtime() + time;
        Intent i = new Intent(this, AutoAndMaInfoSyncReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        mIsNextSemaphore.release();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("测试->A&M-InfoSyncService","onDestroy以执行");
    }
}
