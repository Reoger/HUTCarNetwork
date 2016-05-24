package just.activities;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import com.cwp.android.baidutest.MyApplication;
import com.cwp.android.baidutest.R;

import just.utils.MyActivityUtil;

public class SettingActivity extends AppCompatActivity {
    private LinearLayout mLlAbout;
    private Button mBtLogoutAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        init();
    }

    private void init() {
        mLlAbout= (LinearLayout) findViewById(R.id.id_ll_setting_about);
        mLlAbout.setOnClickListener(v -> {});
        mBtLogoutAccount = (Button) findViewById(R.id.id_bt_setting_logout_account);
        if(!MyApplication.isLanded()) {
            mBtLogoutAccount.setText("登陆");
            mBtLogoutAccount.setBackgroundColor(getResources().getColor(R.color.not_landing));
        }
        mBtLogoutAccount.setOnClickListener(v -> {
            String currentState= mBtLogoutAccount.getText().toString();
            Class<?> cls=null;
            if(currentState.equals("登陆")) {
                cls=LoginActivity.class;
            }
            else if(currentState.equals("退出当前账号")) {
                MyApplication.logoutCurrentAccount();
                cls=MyInfoActivity.class;
            }
            Intent intent=new Intent(SettingActivity.this,cls);
            startActivity(intent);
            Activity activity= MyActivityUtil.getInstance().getTemporaryActivityForKey("KEY_FOR_SETTING");
            if(activity!=null) {
                activity.finish();
            }
            MyActivityUtil.getInstance().removeTemporaryActivityForKey("KEY_FOR_SETTING");
            finish();
        });
    }
}
