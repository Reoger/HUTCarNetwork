package just.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import just.services.InfoSyncToCloudService;
import just.services.InfoSyncToLocalService;

/**
 * Created by Just on 2016/5/29.
 */
public class InfoSyncToLocalReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("测试->InfoSyncToLocalRe","成功接收");
        Intent i = new Intent(context, InfoSyncToLocalService.class);
        context.startService(i);
    }
}
