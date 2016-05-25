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


public class JSON_DATA {

    //配置您申请的KEY
    //    public static final String APPKEY ="48139c54ab6344268d2a4bc8a989f08c";

    //2.检索周边加油站
    public static void getRequest2(double lat, double lon) {

        Bundle bundle = new Bundle();
        String result = "";
        String urlAddress = "http://apis.juhe.cn/oil/local?key=48139c54ab6344268d2a4bc8a989f08c&lon=" + lon + "&lat=" + lat + "&format=2&r=200";
        //请求接口地址
        Log.e("********DATA******", "111111111");
        Log.e("********DATA_lat******", "1**" + lat);
        Log.e("********DATA_lon******", "1**" + lon);

        URL url;
        try {
            url = new URL(urlAddress);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(5 * 1000);
            urlConnection.connect();

            InputStreamReader in = new InputStreamReader(urlConnection.getInputStream());

            BufferedReader buffer = new BufferedReader(in);
            Log.e("********DATA******", "2222222222");
            String line = null;

            while ((line = buffer.readLine()) != null) {

                result += line;
            }

            in.close();
            urlConnection.disconnect();
            Log.e("********DATA******", result);

        } catch (IOException e) {

            e.printStackTrace();
        }

        try {

            Log.e("*****JSON*******", "解析数据进行中》》》");
            //第一层数据
            JSONObject object = new JSONObject(result);

            Log.e("*****JSON*******", "解析数据进行中》》》+id=" + object.getString("resultcode"));
            Log.e("*****JSON*******", "解析数据进行中》》》+reason=" + object.getString("reason"));

            //result数据
            JSONObject object1 = object.getJSONObject("result");
            //result中的data数组
            JSONArray array = object1.getJSONArray("data");
            //得到data数组中的位于第一位的对象
            JSONObject currentObject = array.getJSONObject(0);
            //获取id
            String id = currentObject.getString("id");

            String NAME = currentObject.getString("name");
            bundle.putString("NAME", NAME);

            String ADDRESS = currentObject.getString("address");
            bundle.putString("ADDRESS", ADDRESS);

            //获取discount 的值
            String discount = currentObject.getString("discount");

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
                Log.e("*****JSON*******", "解析数据进行中》》》+name=" + name + " price:" + gaspricedata);

                bundle.putString("name" + i, name);
                bundle.putString("gasprice" + i, gaspricedata);

            }

            Message msg = new Message();
            Log.e("*******JSON******", "已经结束了解析！");
            Log.e("*******JSON******", "已经结束了解析1！");
            msg.what = 0x12;
            msg.setData(bundle);

            MainActivity.myhandler.sendMessage(msg);


        } catch (Exception e) {

            Message msg = new Message();
            Log.e("*******JSON******", "已经结束了解析！");
            Log.e("*******JSON******", "已经结束了解析1！");
            msg.what = 0x13;
            MainActivity.myhandler.sendMessage(msg);
//            msg.setData(bundle);
            e.printStackTrace();
        }
    }

}

