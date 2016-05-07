package just.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import just.services.AutoInfoSyncService;

/**
 * 用于启动AutoInfoSyncService的广播接收器
 */
public class AutoInfoSyncReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("测试->AuInfoSyncReceiver","成功接收");
        Intent i = new Intent(context, AutoInfoSyncService.class);
        context.startService(i);
    }
}
