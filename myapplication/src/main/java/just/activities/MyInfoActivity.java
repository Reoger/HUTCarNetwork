package just.activities;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.cwp.android.baidutest.R;

public class MyInfoActivity extends AppCompatActivity implements View.OnClickListener {
    private LinearLayout mAutoInfo;
    private LinearLayout mMaInfo;
    private LinearLayout mIllegalInfo;
    private LinearLayout mBespeakInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_info);
        
        init();
    }

    private void init() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("我的");
        actionBar.setDisplayHomeAsUpEnabled(true);

        mAutoInfo= (LinearLayout) findViewById(R.id.id_ll_auto_info);
        mMaInfo= (LinearLayout) findViewById(R.id.id_ll_ma_info);
        mIllegalInfo= (LinearLayout) findViewById(R.id.id_ll_illegal_info);
        mBespeakInfo= (LinearLayout) findViewById(R.id.id_ll_bespeak_info);
        mAutoInfo.setOnClickListener(this);
        mMaInfo.setOnClickListener(this);
        mIllegalInfo.setOnClickListener(this);
        mBespeakInfo.setOnClickListener(this);
}

    @Override
    public void onClick(View v) {
        Class<?> cls=null;
        switch (v.getId()) {
            case R.id.id_ll_auto_info:cls=AutoInfoActivity.class;break;
            case R.id.id_ll_ma_info:cls=MaInfoActivity.class;break;
            case R.id.id_ll_illegal_info:break;
            case R.id.id_ll_bespeak_info:break;
            default:break;
        }
        if(cls!=null) {
            Intent intent = new Intent(this, cls);
            startActivity(intent);
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
}
