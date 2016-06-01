package just.utils;

import java.util.HashMap;
import java.util.Map;

public class BmobErrorCodeUtil {
    private static Map<Integer,String> mBmobErrorCode;

    public static String getErrorString(int key) {
        if(mBmobErrorCode==null) {
            synchronized (BmobErrorCodeUtil.class) {
                if(mBmobErrorCode==null) {
                    mBmobErrorCode=new HashMap<>();
                    mBmobErrorCode.put(9010,"网络超时");
                    mBmobErrorCode.put(9015,"其他错误");
                    mBmobErrorCode.put(9016,"无网络连接，请检查您的手机网络！");
                    mBmobErrorCode.put(-1,"未安装微信，或者微信没获得网络权限等");
                    mBmobErrorCode.put(-2,"微信支付用户中断操作");
                    mBmobErrorCode.put(-3,"未安装微信支付插件");
                    mBmobErrorCode.put(7777,"微信客户端未安装");
                    mBmobErrorCode.put(8888,"微信客户端版本不支持微信支付");
                    mBmobErrorCode.put(209,"该手机号码已经存在");
                    mBmobErrorCode.put(150,"订单号是空的");
                    mBmobErrorCode.put(10002,"你要查询的订单号不存在");
                    mBmobErrorCode.put(10010,"该手机号发送短信达到限制");
                    mBmobErrorCode.put(101,"用户名或密码不正确");
                    mBmobErrorCode.put(207,"验证码错误");
                }
            }
        }
        return mBmobErrorCode.get(key);
    }
}
