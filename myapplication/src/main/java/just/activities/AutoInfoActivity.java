package just.activities;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
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
        actionBar.setTitle("个人车辆信息");
        actionBar.setDisplayHomeAsUpEnabled(true);

        mTvHint = (TextView) findViewById(R.id.id_tv_hint);

        mLvAutoInfo = (ListView) findViewById(R.id.id_lv_auto_info);

        AboutHint myHint = isShow -> {
            mTvHint.setVisibility(isShow ? View.VISIBLE : View.GONE);
        };

        mAdapter = new AutoInfoAdapter(this, mHandler, myHint);

        mLvAutoInfo.setAdapter(mAdapter);
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
}
