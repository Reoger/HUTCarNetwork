package just.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.cwp.android.baidutest.R;

import just.adapters.AutoInfoAdapter;
import just.interfaces.AboutHint;

public class AutoInfoActivity extends Activity {
    private ListView mLvAutoInfo;
    private AutoInfoAdapter mAdapter;
    private TextView mTvHint;

    public static final int START_DEL=1;
    public static final int FINISHED_DEL=2;

    private ImageButton mIbBack;
    private ImageButton mIbAdd;

    private Handler mHandler=new Handler() {
        private ProgressDialog progressDialog;

        @Override
        public void handleMessage(Message msg) {
            if(msg.what==START_DEL) {
                progressDialog = new ProgressDialog(AutoInfoActivity.this);
                progressDialog.setTitle("正在删除选择项");
                progressDialog.setMessage("请等待...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
            else if(msg.what==FINISHED_DEL) {
                progressDialog.dismiss();
                progressDialog=null;
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
        mIbBack= (ImageButton) findViewById(R.id.id_ib_back);
        mIbAdd= (ImageButton) findViewById(R.id.id_ib_add);
        mIbBack.setOnClickListener(v -> {
            finish();
        });
        mIbAdd.setOnClickListener(v -> {

        });

        mTvHint= (TextView) findViewById(R.id.id_tv_hint);

        mLvAutoInfo= (ListView) findViewById(R.id.id_lv_auto_info);
        AboutHint myHint=new AboutHint() {
            @Override
            public void setHint(boolean isShow) {
                mTvHint.setVisibility(isShow?View.VISIBLE:View.GONE);
            }
        };
        mAdapter=new AutoInfoAdapter(this,mHandler,myHint);

        mLvAutoInfo.setAdapter(mAdapter);
    }
}
