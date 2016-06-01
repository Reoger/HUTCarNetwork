package just.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

/**
 * Created by Just on 2016/5/30.
 */

public class ToastUtil {

    public static void showToastForErrorCode(int error, Context context) {
        String temp=BmobErrorCodeUtil.getErrorString(error);
        if(!TextUtils.isEmpty(temp)) {
            Toast.makeText(context,temp,Toast.LENGTH_SHORT).show();
        }
    }

    public static void showOrdinaryToast(String s,Context context) {
        Toast.makeText(context,s,Toast.LENGTH_SHORT).show();
    }
}
