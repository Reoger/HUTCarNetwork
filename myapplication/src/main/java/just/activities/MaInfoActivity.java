package just.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.cwp.android.baidutest.MyApplication;
import com.cwp.android.baidutest.R;
import com.xys.libzxing.zxing.activity.CaptureActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.bmob.v3.listener.SaveListener;
import just.adapters.MaInfoAdapter;
import just.beans.AutoInfo;
import just.beans.MaInfo;
import just.constants.AutoInfoConstants;
import just.operations.AutoInfoLocalDBOperation;
import just.operations.MaInfoLocalDBOperation;

public class MaInfoActivity extends Activity {
    private Spinner mSpinner;
    private ListView mLvMaInfo;
    private MaInfoAdapter mAdapter;

    private static final int SCAN_ADD_MA_INFO=1;

    public static final int START_ADD = 1;
    public static final int FINISHED_ADD = 2;
    public static final int START_DEL = 3;
    public static final int FINISHED_DEL = 4;

    private Handler mHandler=new Handler() {
        private ProgressDialog progressDialog;

        @Override
        public void handleMessage(Message msg) {
            if(msg.what==START_ADD) {
                progressDialog = new ProgressDialog(MaInfoActivity.this);
                progressDialog.setTitle("正在添加");
                progressDialog.setMessage("请等待...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
            else if(msg.what==START_DEL) {
                progressDialog = new ProgressDialog(MaInfoActivity.this);
                progressDialog.setTitle("正在删除");
                progressDialog.setMessage("请等待...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
            else if(msg.what==FINISHED_ADD||msg.what==FINISHED_DEL) {
                progressDialog.dismiss();
                progressDialog=null;
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ma_info);

        init();
    }

    private void init() {
        findViewById(R.id.id_ib_top_bar_add).setOnClickListener(v -> {
            Intent intent=new Intent(this, CaptureActivity.class);
            startActivityForResult(intent,SCAN_ADD_MA_INFO);
        });
        findViewById(R.id.id_ib_top_bar_back).setOnClickListener(v -> {
            finish();
        });
        ((TextView)findViewById(R.id.id_tv_top_bar_title)).setText("汽车维护信息");

        mLvMaInfo= (ListView) findViewById(R.id.id_lv_ma_info);
        TextView tvHint=(TextView) findViewById(R.id.id_tv_hint);
        mAdapter = new MaInfoAdapter(this, isShow -> {tvHint.setVisibility(isShow ? View.VISIBLE : View.GONE);}, mHandler);
        mLvMaInfo.setAdapter(mAdapter);

        mSpinner= (Spinner) findViewById(R.id.id_spinner);
        ArrayAdapter<String> adapter= new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,getAutoList());
        //设置样式
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //加载适配器
        mSpinner.setAdapter(adapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode==SCAN_ADD_MA_INFO) {
            if (resultCode == RESULT_OK) {
                Date time=new Date();
                String result=data.getExtras().getString("result");
                Log.d("测试->MaInfoActivity",result);

//                new OperationTask(result,time).start();
            }
        }
    }

    private List<String> getAutoList() {
        List<String> list1=new ArrayList<>();
        list1.add("全部");
        List<AutoInfo> list2=AutoInfoLocalDBOperation.queryBy(this,
                AutoInfoConstants.COLUMN_IS_DEL_WITH_CLOUD+" = ?",
                new String[]{"0"});
        for(AutoInfo autoInfo :list2) {
            String temp= autoInfo.getBrand()+"-"+ autoInfo.getModel()+"-"+ autoInfo.getLicensePlateNum();
            list1.add(temp);
        }
        return list1;
    }

    private class OperationTask extends Thread {
        public static final int OPERATION_ADD=1;
        public static final int OPERATION_DEL=2;

        private int mOperation;

        private String result;
        private Date date;

        public OperationTask(String result,Date date) {
            mOperation=OPERATION_ADD;
            this.result=result;
            this.date=date;
        }

        @Override
        public void run() {
            if(mOperation==OPERATION_ADD) {
                mHandler.sendEmptyMessage(START_ADD);
                dealAddResult(result,date);
            }
            else if(mOperation==OPERATION_DEL) {
                mHandler.sendEmptyMessage(START_DEL);
            }
        }
    }

    private void dealAddResult(String s,Date date) {
        String[] result=s.split("[:\n]");

        Log.d("测试->MaInfoActivity",""+result.length);

        String vin=result[11];
        String username=MyApplication.getUsername();
        List<AutoInfo> list=AutoInfoLocalDBOperation.queryBy(this,AutoInfoConstants.COLUMN_VIN+" = ?",new String[]{vin});

        MaInfo maInfo=new MaInfo();
        maInfo.setVin(vin);
        maInfo.setMileage(Float.parseFloat(result[13].replace("km","")));
        maInfo.setGasolineVolume(Integer.parseInt(result[15]));
        maInfo.setEnginePerfor(result[17]);
        maInfo.setTransmissionPerfor(result[19]);
        maInfo.setLamp(result[21]);
        maInfo.setUsername(username);

        //维护信息中的汽车信息不存在，需要自动添加汽车信息
        if(list.size()<=0) {
            AutoInfo autoInfo=new AutoInfo();
            autoInfo.setBrand(result[1]);
            autoInfo.setModel(result[3]);
            autoInfo.setLicensePlateNum(result[5]);
            autoInfo.setEngineNum(result[7]);
            autoInfo.setBodyLevel(result[9]);
            autoInfo.setUsername(MyApplication.getUsername());
            autoInfo.setVin(vin);
            autoInfo.setAddTime(date);
            autoInfo.save(this, new SaveListener() {
                @Override
                public void onSuccess() {
                    AutoInfoLocalDBOperation.insert(MaInfoActivity.this,autoInfo,1);
                    Log.d("测试->MaInfoActivity","汽车信息成功同步至云端");

                    syncToCloud(maInfo);
                }

                @Override
                public void onFailure(int i, String s) {
                    AutoInfoLocalDBOperation.insert(MaInfoActivity.this,autoInfo,0);

                    //i=9016 表示The network is not available。
                    Log.d("测试->MaInfoActivity","汽车信息同步云端失败:错误编号-"+i+"，错误原因-"+s);

                    syncToCloud(maInfo);
                }
            });
        }
        else {
            syncToCloud(maInfo);
        }
    }

    private void syncToCloud(MaInfo maInfo) {
        maInfo.save(this, new SaveListener() {
            @Override
            public void onSuccess() {
                saveToLocal(maInfo,1);
                Log.d("测试->MaInfoActivity","维护信息成功同步至云端");
            }

            @Override
            public void onFailure(int i, String s) {
                saveToLocal(maInfo,0);

                //i=9016 表示The network is not available。
                Log.d("测试->MaInfoActivity","维护信息同步云端失败:错误编号-"+i+"，错误原因-"+s);
            }
        });
    }

    private void saveToLocal(MaInfo maInfo, int isSyncToCloud) {
        MaInfoLocalDBOperation.insert(this,maInfo,isSyncToCloud);
        mHandler.sendEmptyMessage(FINISHED_ADD);
    }
}
