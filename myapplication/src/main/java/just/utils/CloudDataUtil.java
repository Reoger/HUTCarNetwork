package just.utils;

import android.content.Context;
import android.util.Log;

import com.cwp.android.baidutest.MyApplication;

import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.Semaphore;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobRealTimeData;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.ValueEventListener;
import just.beans.AutoInfo;
import just.beans.MaInfo;
import just.constants.AutoInfoConstants;
import just.operations.AutoInfoLocalDBOperation;

public class CloudDataUtil {
    private static MyActivityUtil mCloudDataUtil;

    public static MyActivityUtil getInstance() {
        if(mCloudDataUtil ==null) {
            synchronized (MyActivityUtil.class) {
                if(mCloudDataUtil ==null) {
                    mCloudDataUtil =new MyActivityUtil();
                }
            }
        }
        return mCloudDataUtil;
    }


}
