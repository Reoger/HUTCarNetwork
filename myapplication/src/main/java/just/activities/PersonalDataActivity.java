package just.activities;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.cwp.android.baidutest.MyApplication;
import com.cwp.android.baidutest.R;

public class PersonalDataActivity extends AppCompatActivity {
    private TextView mTvUsername,mTvName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_data);

        init();
    }

    private void init() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("个人资料");
        actionBar.setDisplayHomeAsUpEnabled(true);

        mTvUsername= (TextView) findViewById(R.id.id_tv_per_data_username);
        mTvName= (TextView) findViewById(R.id.id_tv_per_data_name);

        mTvUsername.setText(MyApplication.getUsername());
        String name=MyApplication.getName();
        if(!MyApplication.NULL_NAME.equals(name)) {
            mTvName.setText(name);
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
