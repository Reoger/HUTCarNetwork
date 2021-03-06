package just.activities;



import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.com.reoger.music.Utils.LogUtils;
import com.cwp.android.baidutest.MyApplication;
import com.cwp.android.baidutest.R;


import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import just.adapters.OrderReulstAdaper;
import just.beans.BodyInfo;
import just.beans.OrdGasInfo;

public class OrdGasInfoActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    private static final String X_Bmob_Application_Id = "11c50a59fafd8add5a2c19107b769f9d";
    private static final String X_Bmob_REST_API_Key = "90801024742efbf9e06a93cbb86070dc";
    private static final String dataUrl = "https://api.bmob.cn/1/pay/";

    private AlertDialog.Builder mBuilder;
    private ProgressDialog mDialog;
    private OrderReulstAdaper adaper;

//    private Semaphore mSemaphore=new Semaphore(0);

    private ListView mListView;
    private TextView mTextView;
    private List<OrdGasInfo> mDate = new ArrayList<>();
    private List<String> jsonDate = new ArrayList<>();
    private List<BodyInfo> mBodyData = new ArrayList<>();

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 10086:
                    mDialog.dismiss();
                    if(mBodyData.size()==0||mDate.size()==0){
                        mTextView.setText("没有查找到相关的数据");
                        mTextView.setVisibility(View.VISIBLE);
                        Toast.makeText(OrdGasInfoActivity.this,"没有查找到相关的数据",Toast.LENGTH_SHORT).show();
                    }else{
                        mTextView.setVisibility(View.GONE);
                        adaper.notifyDataSetChanged();
                    }
                    break;
                case 10010:
                    mDialog.dismiss();
                    mTextView.setText("查询失败，请检查你的网络设置");
                    mTextView.setVisibility(View.VISIBLE);
                    Toast.makeText(OrdGasInfoActivity.this,"查询失败，请检查你的网络设置",Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 查看订单信息
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ord_gas_info);
        ActivityCollector.addActivity(this);


        mListView = (ListView) findViewById(R.id.list_order_info);
        mTextView = (TextView) findViewById(R.id.no_pay_info);

        showMainDialog();
        getObjectIdDateFromYun();//从云端获取订单id信息
        adaper = new OrderReulstAdaper(OrdGasInfoActivity.this, mBodyData);
        mListView.setAdapter(adaper);
        mListView.setOnItemClickListener(OrdGasInfoActivity.this);
    }


    public void getObjectIdDateFromYun() {
        new Thread(() -> {

            BmobQuery<OrdGasInfo> query = new BmobQuery<>();
          //  query.addWhereNotEqualTo("PayId", "0");
            //通过用户名查询相关的信息
        query.addWhereEqualTo("username", MyApplication.getUsername());
            Log.i("AATT", "查询ing");
            query.setLimit(10);
            //执行查询方法
            query.findObjects(OrdGasInfoActivity.this, new FindListener<OrdGasInfo>() {
                @Override
                public void onSuccess(List<OrdGasInfo> object) {
                    mDate = object; //有待验证
                    LogUtils.i("AATT", "到这里，获取到了mData的值");
                    if(mDate.size() ==0){
                        Message msg = new Message();
                        msg.what = 10086;
                        handler.sendMessage(msg);
                    }else{
                        getDateFromOrderInfoCloube(mDate);
                    }

                }

                @Override
                public void onError(int code, String msg) {
                    // TODO Auto-generated method stub
                    Log.i("AATT", "查询失败" + code + " msg =" + msg);
                    Message msgw = new Message();
                    msgw.what = 10010;
                    handler.sendMessage(msgw);
                }
            });
        }).start();

    }

    /**
     * 通过传入的id查询相对应的订单信息
     */
    private void getDateFromOrderInfoCloube(final List<OrdGasInfo> listdata) {
        LogUtils.i("AATT", "然后，在这里进入了获取对应订单信息的值");
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                for (int i = 0; i < listdata.size(); i++) {
                    String ObjectId = listdata.get(i).getPayId();
                    String realUrl = dataUrl + ObjectId;
                    URL url = new URL(realUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    connection.setRequestProperty("X-Bmob-REST-API-Key", X_Bmob_REST_API_Key);
                    connection.setRequestProperty("X-Bmob-Application-Id", X_Bmob_Application_Id);
                    InputStream in = connection.getInputStream();
                    // 下面对获取到的输入流进行读取
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    jsonDate.add(response.toString());
                    //这里获取到了返回的数据
                    LogUtils.i("AATT", "这里，得到了json数据，得到之后就拿去解析了" + response.toString());
                }
                parseJSONWithJSONObject(jsonDate);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }





    /**
     * 将json格式的数据解析并存储到mBodyData中。
     *
     * @param
     */
    private void parseJSONWithJSONObject(List<String> jsonData) {
        LogUtils.i("AATT", "这里就是正在解析json格式");
        for (int i = 0; i < jsonData.size(); i++) {
            String oneItem = jsonData.get(i);
            try {
                //  JSONArray jsonArray = new JSONArray(jsonData);
                BodyInfo item = new BodyInfo();

                JSONObject jsonObject = new JSONObject(oneItem);
                String body = jsonObject.getString("body");
                String create_time = jsonObject.getString("create_time");
                String name = jsonObject.getString("name");
                String pay_type = jsonObject.getString("pay_type");
                String total_fee = jsonObject.getString("total_fee");
                String trade_state = jsonObject.getString("trade_state");
                String transaction_id = jsonObject.getString("transaction_id");
                String out_trade_no = jsonObject.getString("out_trade_no");

                String Car_info = mDate.get(i).getBrand();
                String username = mDate.get(i).getUsername();
                String orienTime = mDate.get(i).getReservationTime();
                boolean mCanUsed= mDate.get(i).ismIsUsed();
                double liter = mDate.get(i).getLiter();
                String Object = mDate.get(i).getObjectId();

                item.setBody(body);
                item.setCreate_time(create_time);
                item.setName(name);
                item.setPay_type(pay_type);
                item.setTotal_fee(total_fee);
                item.setTrade_state(trade_state);
                item.setOut_trade_no(out_trade_no);
                item.setTransaction_id(transaction_id);

                item.setCar_info(Car_info);
                item.setUsename(username);
                item.setOrien_time(orienTime);
                item.setmCanUsed(mCanUsed);
                item.setLiter(liter);
                item.setObjectId(Object);

                mBodyData.add(item);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Collections.reverse(mBodyData);//将数据反转排序，即按照最近的时间排序
        Message message = new Message();
        message.what = 10086;
        LogUtils.i("AATT", "json数据解析完毕，sendMessage");
        handler.sendMessage(message); // 将Message对象发送出去
    }

    private void showMainDialog(){
        mDialog = new ProgressDialog(OrdGasInfoActivity.this);
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setTitle("Loading...");
        mDialog.setMessage("正在加载中，请稍后...");
        mDialog.setCancelable(false);
        mDialog.setButton("取消", (dialog, which) -> {
            finish();
        });
        mDialog.show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(OrdGasInfoActivity.this,DetailedBilingActivity.class);
        BodyInfo info = mBodyData.get(position);
        Bundle bundle = new Bundle();
        bundle.putSerializable("info",info);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}
