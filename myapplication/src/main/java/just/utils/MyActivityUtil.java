package just.utils;

import android.app.Activity;
import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Created by Just on 2016/5/24.
 */
public class MyActivityUtil {
    private static MyActivityUtil mActivityUtil;

    private Map<String,Activity> mTemporaryActivityMap=new HashMap<>();

    public void addTemporaryActivity(String key,Activity activity) {
        mTemporaryActivityMap.put(key, activity);
    }

    public Activity getTemporaryActivityForKey(String key) {
        return mTemporaryActivityMap.get(key);
    }

    public void removeTemporaryActivityForKey(String key) {
        mTemporaryActivityMap.remove(key);
    }

    public static MyActivityUtil getInstance() {
        if(mActivityUtil==null) {
            synchronized (MyActivityUtil.class) {
                if(mActivityUtil==null) {
                    mActivityUtil=new MyActivityUtil();
                }
            }
        }
        return mActivityUtil;
    }
}
