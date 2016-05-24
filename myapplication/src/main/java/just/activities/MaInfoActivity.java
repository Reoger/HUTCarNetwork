package just.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
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
    private ArrayAdapter<String> mAdapter1;
    private List<String> mAutoInfoList;
    private int currentSpinnerSelected;

    private ListView mLvMaInfo;
    private MaInfoAdapter mAdapter2;

    private LinearLayout mLlBatchOperation;
    private Button mBtDel;
    private Button mBtAll;
    private Button mBtRevoke;

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

                mAdapter1.notifyDataSetChanged();
                if (currentSpinnerSelected!=0) {
                    String selected=mAutoInfoList.get(currentSpinnerSelected);
                    String vin=selected.split("[-]")[3].trim();
                    mAdapter2.notifyDataSetChanged(vin);
                }
                else {
                    mAdapter2.notifyDataSetChanged();
                }
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
        findViewById(R.id.id_ib_top_bar_back).setOnClickListener(v -> {
            finish();
        });
        findViewById(R.id.id_ib_top_bar_add).setOnClickListener(v -> {
            Intent intent=new Intent(this, CaptureActivity.class);
            startActivityForResult(intent,SCAN_ADD_MA_INFO);
        });
        ((TextView)findViewById(R.id.id_tv_top_bar_title)).setText("汽车维护信息");

        mSpinner= (Spinner) findViewById(R.id.id_spinner);
        mAutoInfoList=getAutoList();
        mAdapter1= new ArrayAdapter<>(MaInfoActivity.this, android.R.layout.simple_spinner_item,mAutoInfoList);
        //设置样式
        mAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //加载适配器
        mSpinner.setAdapter(mAdapter1);
        mSpinner.setSelection(currentSpinnerSelected);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentSpinnerSelected=position;
                if (currentSpinnerSelected!=0) {
                    String selected=mAutoInfoList.get(currentSpinnerSelected);
                    String vin=selected.split("[-]")[3].trim();
                    mAdapter2.notifyDataSetChanged(vin);
                }
                else {
                    mAdapter2.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mLvMaInfo= (ListView) findViewById(R.id.id_lv_ma_info);
        TextView tvHint=(TextView) findViewById(R.id.id_tv_hint);
        mAdapter2 = new MaInfoAdapter(this, isShow -> {tvHint.setVisibility(isShow ? View.VISIBLE : View.GONE);}, mHandler,mAutoInfoList);
        mLvMaInfo.setAdapter(mAdapter2);

        mLlBatchOperation = (LinearLayout) findViewById(R.id.id_ll_batch_operation);
        mBtDel= (Button) findViewById(R.id.id_bt_del);
        mBtAll= (Button) findViewById(R.id.id_bt_all);
        mBtRevoke= (Button) findViewById(R.id.id_bt_revoke);
        mBtDel.setOnClickListener(v -> {
            mAdapter2.deleteCheckBox();
        });
        mBtAll.setOnClickListener(v -> {
            if("全选".equals(mBtAll.getText())) {
                mAdapter2.selectAllCheckBox(true);
                mBtAll.setText("取消全选");
            }
            else if("取消全选".equals(mBtAll.getText())) {
                mAdapter2.selectAllCheckBox(false);
                mBtAll.setText("全选");
            }
        });
        mBtRevoke.setOnClickListener(v -> {
            mAdapter2.setIsDelPattern(false);
            mLlBatchOperation.setVisibility(View.GONE);
        });

        mLvMaInfo.setOnItemLongClickListener((parent, view, position, id) -> {
            Log.d("测试->MaInfoActivity","长按生效");
            mAdapter2.setIsDelPattern(true);
            mLlBatchOperation.setVisibility(View.VISIBLE);
            return true;
        });

        mLvMaInfo.setOnItemClickListener(((parent, view, position, id) -> {
            if (mAdapter2.getIsDelPattern()) {
                mAdapter2.setSelectedCheckBox(position);
            }
        }));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode==SCAN_ADD_MA_INFO) {
            if (resultCode == RESULT_OK) {
                Date time=new Date();
                String result=data.getExtras().getString("result");
                if(!result.substring(0,6).equals("维护信息->")) {
                    Log.d("测试","维护信息二维码格式不正确");
                    return;
                }
                Log.d("测试->MaInfoActivity",result);

                new AddOperationTask(result,time).start();
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
            String temp= autoInfo.getBrand()+"-"+ autoInfo.getModel()+"-"+ autoInfo.getLicensePlateNum()+"-"+autoInfo.getVin();
            list1.add(temp);
        }
        return list1;
    }

    private class AddOperationTask extends Thread {
        private String result;
        private Date date;

        public AddOperationTask(String result, Date date) {
            this.result=result;
            this.date=date;
        }

        @Override
        public void run() {
            mHandler.sendEmptyMessage(START_ADD);
            dealAddResult(result,date);
        }
    }

    private void dealAddResult(String s,Date date) {
        String[] result=s.split("[:\n]");

        String vin=result[12].trim();
        String username=MyApplication.getUsername();
        List<AutoInfo> list=AutoInfoLocalDBOperation.queryBy(this,AutoInfoConstants.COLUMN_VIN+" = ?",new String[]{vin});

        MaInfo maInfo=new MaInfo();
        maInfo.setVin(vin);
        maInfo.setMileage(Float.parseFloat(result[14].replace("km","").trim()));
        maInfo.setGasolineVolume(Integer.parseInt(result[16].trim()));
        maInfo.setEnginePerfor(result[18].trim());
        maInfo.setTransmissionPerfor(result[20].trim());
        maInfo.setLamp(result[22].trim());
        maInfo.setScanTime(date);
        maInfo.setUsername(username);

        //维护信息中的汽车信息不存在，需要自动添加汽车信息
        if(list.size()<=0) {
            AutoInfo autoInfo=new AutoInfo();
            autoInfo.setBrand(result[2]);
            autoInfo.setModel(result[4]);
            autoInfo.setLicensePlateNum(result[6]);
            autoInfo.setEngineNum(result[8]);
            autoInfo.setBodyLevel(result[10]);
            autoInfo.setUsername(MyApplication.getUsername());
            autoInfo.setVin(vin);
            autoInfo.setAddTime(date);
            autoInfo.save(this, new SaveListener() {
                @Override
                public void onSuccess() {
                    AutoInfoLocalDBOperation.insert(MaInfoActivity.this,autoInfo,1);
                    Log.d("测试->MaInfoActivity","汽车信息成功同步至云端");

                    String temp= autoInfo.getBrand()+"-"+ autoInfo.getModel()+"-"+ autoInfo.getLicensePlateNum()+"-"+autoInfo.getVin();
                    mAutoInfoList.add(temp);

                    syncToCloud(maInfo);
                }

                @Override
                public void onFailure(int i, String s) {
                    AutoInfoLocalDBOperation.insert(MaInfoActivity.this,autoInfo,0);

                    //i=9016 表示The network is not available。
                    Log.d("测试->MaInfoActivity","汽车信息同步云端失败:错误编号-"+i+"，错误原因-"+s);

                    String temp= autoInfo.getBrand()+"-"+ autoInfo.getModel()+"-"+ autoInfo.getLicensePlateNum()+"-"+autoInfo.getVin();
                    mAutoInfoList.add(temp);

                    saveToLocal(maInfo,0);
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
                Log.d("测试->MaInfoActivity","维护信息成功同步至云端");
                saveToLocal(maInfo,1);
            }

            @Override
            public void onFailure(int i, String s) {
                //i=9016 表示The network is not available。
                Log.d("测试->MaInfoActivity","维护信息同步云端失败:错误编号-"+i+"，错误原因-"+s);

                saveToLocal(maInfo,0);
            }
        });
    }

    private void saveToLocal(MaInfo maInfo, int isSyncToCloud) {
        MaInfoLocalDBOperation.insert(this,maInfo,isSyncToCloud);
        mHandler.sendEmptyMessage(FINISHED_ADD);
    }
}
