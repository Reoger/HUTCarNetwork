package com.cwp.android.baidutest;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import utils.CloseUtils;
import utils.MyLog;


public class GasJsonDataParse {

    private static GasJsonDataParse mJsonDataParse;
    /**
     * 配置您申请的KEY
     * 聚合平台的APIKey
     */
    public static final String APPKEY = "48139c54ab6344268d2a4bc8a989f08c";

    private static Bundle bundle;


    //懒汉模式
    public static GasJsonDataParse getInstance() {

        bundle = new Bundle();

        if (mJsonDataParse == null) {
            mJsonDataParse = new GasJsonDataParse();
        }
        return mJsonDataParse;
    }

    private GasJsonDataParse() {

    }

    public void getGasDetailsData(double lat, double lon) {

        String data = null;
        data = getUrlData(lat, lon);

        dataParse(data);
    }

    //2.检索周边加油站
    private String getUrlData(double lat, double lon) {

        String result = "";
        URL url;
        //请求接口地址
        String urlAddress = "http://apis.juhe.cn/oil/local?key=" + APPKEY + "&lon=" + lon + "&lat=" + lat + "&format=2&r=200";

        MyLog.LogE("********DATA******", "111111111111");
        MyLog.LogE("********DATA_lat******", "1**" + lat);
        MyLog.LogE("********DATA_lon******", "1**" + lon);

        /**
         *解析网络数据
         */

        HttpURLConnection urlConnection = null;
        InputStreamReader in = null;
        BufferedReader buffer = null;
        try {
            url = new URL(urlAddress);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(5 * 1000);
            urlConnection.connect();

            in = new InputStreamReader(urlConnection.getInputStream());
            buffer = new BufferedReader(in);

            String line = null;

            while ((line = buffer.readLine()) != null) {

                result += line;
            }

            MyLog.LogE("********DATA******", result);

        } catch (IOException e) {
            MyLog.LogE("****DATA*****", "加油站，网络数据请求错误");
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            CloseUtils.close(in);
            CloseUtils.close(buffer);
        }

        return result;
    }


    private void dataParse(String data) {

        /**
         *解析Json数据
         */

        try {

            MyLog.LogE("*****JSON*******", "解析数据进行中》》》");
            //第一层数据
            JSONObject object = new JSONObject(data);

            MyLog.LogE("*****JSON*******", "解析数据进行中》》》+id=" + object.getString("resultcode"));
            MyLog.LogE("*****JSON*******", "解析数据进行中》》》+reason=" + object.getString("reason"));

            //result数据
            JSONObject object1 = object.getJSONObject("result");
            //result中的data数组
            JSONArray array = object1.getJSONArray("data");
            //得到data数组中的位于第一位的对象
            JSONObject currentObject = array.getJSONObject(0);
            //获取id
            //String id = currentObject.getString("id");

            String NAME = currentObject.getString("name");
            bundle.putString("NAME", NAME);

            String ADDRESS = currentObject.getString("address");
            bundle.putString("ADDRESS", ADDRESS);

            //获取discount 的值
            //String discount = currentObject.getString("discount");

            //得到price数组
            JSONArray price = currentObject.getJSONArray("price");
            //dedgasprice数组
            JSONArray gasprice = currentObject.getJSONArray("gastprice");
            //遍历price 数组
            for (int i = 0; i < price.length(); i++) {

                String type = price.getJSONObject(i).getString("type");
                String price1 = price.getJSONObject(i).getString("price");
                Log.e("*****JSON*******", "解析数据进行中》》》+type=" + type + " price:" + price1);

                bundle.putString("type" + i, type);
                bundle.putString("price" + i, price1);

            }
            //遍历gasprice数组
            for (int i = 0; i < gasprice.length(); i++) {

                String name = gasprice.getJSONObject(i).getString("name");
                String gaspricedata = gasprice.getJSONObject(i).getString("price");
                MyLog.LogE("*****JSON*******", "解析数据进行中》》》+name=" + name + " price:" + gaspricedata);

                bundle.putString("name" + i, name);
                bundle.putString("gasprice" + i, gaspricedata);

            }

            sendMessgeToUI(0x12,bundle);

            MyLog.LogE("*******JSON******", "已经结束了解析！");


        } catch (Exception e) {

            sendMessgeToUI(0x13,null);
            e.printStackTrace();
        }
    }

    private void sendMessgeToUI(int what,Bundle bundle) {

        Message msg = new Message();
        //0x12 是正确，0x13是错误
        switch (what) {
            case 0x12:

                msg.what = 0x12;
                msg.setData(bundle);
                break;
            case 0x13:
                msg.what = 0x13;
                break;

            default:
                break;
        }

        MainActivity.myhandler.sendMessage(msg);
    }
}

