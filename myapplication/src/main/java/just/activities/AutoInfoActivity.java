package just.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.cwp.android.baidutest.MyApplication;
import com.cwp.android.baidutest.R;
import com.xys.libzxing.zxing.activity.CaptureActivity;

import java.util.Date;

import cn.bmob.v3.listener.SaveListener;
import just.adapters.AutoInfoAdapter;
import just.beans.AutoInfo;
import just.operations.AutoInfoLocalDBOperation;
import just.swipemenulistview.SwipeMenuItem;
import just.swipemenulistview.SwipeMenuListView;

public class AutoInfoActivity extends Activity {
    private SwipeMenuListView mLvAutoInfo;
    private AutoInfoAdapter mAdapter;

    public static final int MANUAL_ADD_AUTO_INFO=1;
    public static final int SCAN_ADD_AUTO_INFO=2;

    public static final int START_ADD = 1;
    public static final int FINISHED_ADD = 2;
    public static final int START_DEL = 3;
    public static final int FINISHED_DEL = 4;

    private Handler mHandler=new Handler() {
        private ProgressDialog progressDialog;

        @Override
        public void handleMessage(Message msg) {
            if(msg.what==START_ADD) {
                progressDialog = new ProgressDialog(AutoInfoActivity.this);
                progressDialog.setTitle("正在添加");
                progressDialog.setMessage("请等待...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
            else if(msg.what==START_DEL) {
                Log.d("测试","-------------------");
                progressDialog = new ProgressDialog(AutoInfoActivity.this);
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
        setContentView(R.layout.activity_auto_info);

        init();
    }

    private void init() {
        mLvAutoInfo = (SwipeMenuListView) findViewById(R.id.id_lv_auto_info);

        findViewById(R.id.id_ib_top_bar_add).setOnClickListener(v -> {
            AlertDialog dialog = new AlertDialog.Builder(AutoInfoActivity.this).create();
            dialog.show();
            Window window=dialog.getWindow();
            window.setContentView(R.layout.dialog_for_auto_info_add_mode);
            window.findViewById(R.id.id_bt_manual).setOnClickListener(v1-> {
                dialog.dismiss();
                turnAdd(1);
            });
            dialog.getWindow().findViewById(R.id.id_bt_scan).setOnClickListener(v2-> {
                dialog.dismiss();
                turnAdd(2);
            });
            dialog.getWindow().findViewById(R.id.id_bt_cancel_auto_info_add_mode).setOnClickListener(v3-> {
                dialog.dismiss();
            });
        });
        findViewById(R.id.id_ib_top_bar_back).setOnClickListener(v -> {
            finish();
        });
        ((TextView)findViewById(R.id.id_tv_top_bar_title)).setText("个人汽车信息");

        TextView tvHint=(TextView) findViewById(R.id.id_tv_hint);
        mAdapter = new AutoInfoAdapter(this,
                isShow -> {tvHint.setVisibility(isShow ? View.VISIBLE : View.GONE);},
                mHandler);

        mLvAutoInfo.setAdapter(mAdapter);

        // set creator
        mLvAutoInfo.setMenuCreator(menu->{
//                // create "open" item
//                SwipeMenuItem openItem = new SwipeMenuItem(
//                        getApplicationContext());
//                // set item background
//                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
//                        0xCE)));
//                // set item width
//                openItem.setWidth(dp2px(90));
//                // set item title
//                openItem.setTitle("Open");
//                // set item title fontsize
//                openItem.setTitleSize(18);
//                // set item title font color
//                openItem.setTitleColor(Color.WHITE);
//                // add to menu
//                menu.addMenuItem(openItem);

            // create "delete" item
            SwipeMenuItem deleteItem = new SwipeMenuItem(
                    getApplicationContext());
            // set item background
            deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                    0x3F, 0x25)));
            // set item width
            deleteItem.setWidth(dp2px(90));
            // set a icon
            deleteItem.setIcon(R.drawable.ic_delete);
            // add to menu
            menu.addMenuItem(deleteItem);
        });

        mLvAutoInfo.setOnMenuItemClickListener(((position, menu, index) -> {
            switch (index) {
                case 0:
                    new OperationTask(position).start();
                    break;
            }
        }));

        mLvAutoInfo.setOnItemClickListener(((parent, view, position, id) -> {
            AutoInfo autoInfo =mAdapter.getItem(position);
            String s="品牌:"+ autoInfo.getBrand()+"\n型号:"+ autoInfo.getModel()+
                    "\n车牌级别:"+ autoInfo.getBodyLevel()+
                    "\n车牌号:"+ autoInfo.getLicensePlateNum()+
                    "\n发动机号:"+ autoInfo.getEngineNum()+
                    "\n车架号"+ autoInfo.getVin();
            new AlertDialog.Builder(this)
                    .setTitle("详细信息")
                    .setMessage(s)
                    .setPositiveButton("确定",((dialog, which) -> {
                        dialog.dismiss();
                    })).create().show();
        }));
    }

    /**
     * 跳转到添加页面
     * which=1，代表手动添加
     * which=2，代表扫码添加
     */
    private void turnAdd(int which) {
        Class<?> cls=null;
        int requestCode=-1;
        switch (which) {
            case 1:
                cls=ManualAddAutoInfoActivity.class;
                requestCode=MANUAL_ADD_AUTO_INFO;
                break;
            case 2:
                cls=CaptureActivity.class;
                requestCode=SCAN_ADD_AUTO_INFO;
                break;
            default:break;
        }
        if(cls!=null&&requestCode!=-1) {
            Intent intent=new Intent(this,cls);
            startActivityForResult(intent,requestCode);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode==MANUAL_ADD_AUTO_INFO||requestCode==SCAN_ADD_AUTO_INFO) {
                if (resultCode == RESULT_OK) {
                    Date time=new Date();
                    String result=data.getExtras().getString("result");
                    Log.d("测试->AutoInfoActivity",result);

                    new OperationTask(result,time).start();
                }
        }
    }

    private void dealAddResult(String s,Date date) {
        String[] result=s.split("[:\n]");
        AutoInfo autoInfo=new AutoInfo();
        autoInfo.setBrand(result[1]);
        autoInfo.setModel(result[3]);
        autoInfo.setBodyLevel(result[5]);
        autoInfo.setEngineNum(result[7]);
        autoInfo.setLicensePlateNum(result[9]);
        autoInfo.setVin(result[11]);
        autoInfo.setUsername(MyApplication.getUsername());
        autoInfo.setAddTime(date);

        syncToCloud(autoInfo);
    }

    private void syncToCloud(AutoInfo autoInfo) {
        autoInfo.save(this, new SaveListener() {
            @Override
            public void onSuccess() {
                Log.d("测试->AutoInfoActivity","成功同步至云端");

                saveToLocal(autoInfo,1);
            }

            @Override
            public void onFailure(int i, String s) {
                //i=9016 表示The network is not available。
                Log.d("测试->AutoInfoActivity","同步云端失败:错误编号-"+i+"，错误原因-"+s);

                saveToLocal(autoInfo,0);
            }
        });
    }

    private void saveToLocal(AutoInfo autoInfo, int isSyncToCloud) {
        AutoInfoLocalDBOperation.insert(this,autoInfo,isSyncToCloud);
        mHandler.sendEmptyMessage(FINISHED_ADD);
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    private class OperationTask extends Thread {
        public static final int OPERATION_ADD=1;
        public static final int OPERATION_DEL=2;

        private int mOperation;

        private String result;
        private Date date;

        private int position;

        public OperationTask(String result,Date date) {
            mOperation=OPERATION_ADD;
            this.result=result;
            this.date=date;
        }

        public OperationTask(int position) {
            mOperation=OPERATION_DEL;
            this.position=position;
        }

        @Override
        public void run() {
            if(mOperation==OPERATION_ADD) {
                mHandler.sendEmptyMessage(START_ADD);
                dealAddResult(result,date);
            }
            else if(mOperation==OPERATION_DEL) {
                try {
                    MyApplication.mSyncSemaphore.acquire();
                } catch (InterruptedException e) {
                }
                mHandler.sendEmptyMessage(START_DEL);
                mAdapter.deletePosition(position);
            }
        }
    }
}
