package just.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.cwp.android.baidutest.R;

import just.adapters.AutoInfoAdapter;
import just.interfaces.AboutHint;

public class AutoInfoActivity extends AppCompatActivity {
    private ListView mLvAutoInfo;
    private AutoInfoAdapter mAdapter;
    private TextView mTvHint;

    public static final int START_DEL = 1;
    public static final int FINISHED_DEL = 2;

    public static final int MANUAL_ADD_AUTO_INFO=3;
    public static final int SCAN_ADD_AUTO_INFO=4;

    private Button mAdd;

    private Handler mHandler = new Handler() {
        private ProgressDialog progressDialog;

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == START_DEL) {
                progressDialog = new ProgressDialog(AutoInfoActivity.this);
                progressDialog.setTitle("正在删除选择项");
                progressDialog.setMessage("请等待...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            } else if (msg.what == FINISHED_DEL) {
                progressDialog.dismiss();
                progressDialog = null;
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
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        View customView = getLayoutInflater().inflate(R.layout.actionbar_for_auto_info_activity, null);
        actionBar.setCustomView(customView, new
                ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT));

        mTvHint = (TextView) findViewById(R.id.id_tv_hint);

        mLvAutoInfo = (ListView) findViewById(R.id.id_lv_auto_info);

        AboutHint myHint = isShow -> {
            mTvHint.setVisibility(isShow ? View.VISIBLE : View.GONE);
        };

        mAdd= (Button) findViewById(R.id.id_bt_add_auto_info);
        mAdd.setOnClickListener(v -> {
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

        mAdapter = new AutoInfoAdapter(this, mHandler, myHint);

        mLvAutoInfo.setAdapter(mAdapter);
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
//            case 2:
//                cls=CaptureActivity.class;
//                requestCode=SCAN_ADD_AUTO_INFO;
//                break;
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
                    String result=data.getExtras().getString("result");
                    dealAddResult(result);
                }
        }
    }

    private void dealAddResult(String result) {

    }
}
