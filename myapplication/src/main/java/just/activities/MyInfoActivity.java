package just.activities;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ViewFlipper;

import com.cwp.android.baidutest.MyApplication;
import com.cwp.android.baidutest.R;

import java.io.Serializable;

import just.utils.MyActivityUtil;

public class MyInfoActivity extends AppCompatActivity implements Serializable {
    //未登陆需要显示的内容
    private LinearLayout mLlHint;

    //已经登陆需要显示的内容
    private LinearLayout mLlAll;
    private LinearLayout mAutoInfo;
    private LinearLayout mMaInfo;
    private LinearLayout mIllegalInfo;
    private LinearLayout mBespeakInfo;
    private LinearLayout mSetting;

    //ViewFlipper的定义
    private ViewFlipper mViewFlipper;

    private MyInfoActivity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_info);
        mActivity=this;
        init();
    }

    private void init() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("我的信息");
        actionBar.setDisplayHomeAsUpEnabled(true);

        mLlHint= (LinearLayout) findViewById(R.id.id_fl_auto_info_hint);
        mLlAll= (LinearLayout) findViewById(R.id.id_ll_auto_info_all);
        if(!MyApplication.isLanded()) {
            mLlAll.setVisibility(View.GONE);
            mLlHint.setOnClickListener(v -> {
                Intent intent=new Intent(MyInfoActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            });
        }

        else {
            mViewFlipper =(ViewFlipper) findViewById(R.id.mviewFlipper);

            mViewFlipper.setInAnimation(this,R.anim.slide_in);  //设置图片进入时的动画
            mViewFlipper.setOutAnimation(this, R.anim.slide_out);//设置图片切出时的动画
            mViewFlipper.startFlipping();

            mLlAll.setVisibility(View.VISIBLE);
            mLlHint.setVisibility(View.GONE);

            mAutoInfo= (LinearLayout) findViewById(R.id.id_ll_auto_info);
            mMaInfo= (LinearLayout) findViewById(R.id.id_ll_ma_info);
            mIllegalInfo= (LinearLayout) findViewById(R.id.id_ll_illegal_info);
            mBespeakInfo= (LinearLayout) findViewById(R.id.id_ll_bespeak_info);
            mSetting= (LinearLayout) findViewById(R.id.id_ll_setting);
            mAutoInfo.setOnClickListener(v -> turnActivity(AutoInfoActivity.class));
            mMaInfo.setOnClickListener(v -> turnActivity(MaInfoActivity.class));
            mIllegalInfo.setOnClickListener(v -> turnActivity(IllegalActivity.class));
            mBespeakInfo.setOnClickListener(v -> turnActivity(OrdGasInfoActivity.class));
            mSetting.setOnClickListener(v -> {
                MyActivityUtil.getInstance().addTemporaryActivity("KEY_FOR_SETTING",mActivity);
                turnActivity(SettingActivity.class);
            });

            MyApplication.startSyncToCloudService();
        }
    }

    private void turnActivity(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.id_item_menu_setting:
                Intent intent=new Intent(this,SettingActivity.class);
                MyActivityUtil.getInstance().addTemporaryActivity("KEY_FOR_SETTING",mActivity);
                startActivity(intent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_for_my_info,menu);
        return true;
    }
}
