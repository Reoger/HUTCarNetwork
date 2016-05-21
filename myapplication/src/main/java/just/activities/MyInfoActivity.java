package just.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.cwp.android.baidutest.MyApplication;
import com.cwp.android.baidutest.R;

public class MyInfoActivity extends AppCompatActivity {
    //未登陆需要显示的内容
    private LinearLayout mLlHint;

    //已经登陆需要显示的内容
    private LinearLayout mLlAll;
    private LinearLayout mAutoInfo;
    private LinearLayout mMaInfo;
    private LinearLayout mIllegalInfo;
    private LinearLayout mBespeakInfo;

    public static final String FILE_NAME="LoginInfo";
    public static final String USERNAME="username";
    public static final String NAME="name";

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

        mLlHint= (LinearLayout) findViewById(R.id.id_fl_auto_info_hint);
        mLlAll= (LinearLayout) findViewById(R.id.id_ll_auto_info_all);
        if(!isLogged()) {
            mLlAll.setVisibility(View.GONE);
            mLlHint.setOnClickListener(v -> {
                Intent intent=new Intent(MyInfoActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            });
        }

        else {
            mLlAll.setVisibility(View.VISIBLE);
            mLlHint.setVisibility(View.GONE);

            mAutoInfo= (LinearLayout) findViewById(R.id.id_ll_auto_info);
            mMaInfo= (LinearLayout) findViewById(R.id.id_ll_ma_info);
            mIllegalInfo= (LinearLayout) findViewById(R.id.id_ll_illegal_info);
            mBespeakInfo= (LinearLayout) findViewById(R.id.id_ll_bespeak_info);
            mAutoInfo.setOnClickListener(v -> turnActivity(AutoInfoActivity.class));
            mMaInfo.setOnClickListener(v -> turnActivity(MaInfoActivity.class));
            mIllegalInfo.setOnClickListener(v -> turnActivity(IllegalActivity.class));
            mBespeakInfo.setOnClickListener(v -> turnActivity(OrdGasInfoActivity.class));

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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isLogged() {
        SharedPreferences pref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        String username = pref.getString(USERNAME, "null");
        String name = pref.getString(NAME, "null");
        if (username.equals("null")) {
            return false;
        } else {
            MyApplication.setUsername(username);
            MyApplication.setName(name);
            return true;
        }
    }
}
