package just.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.cwp.android.baidutest.R;

import just.adapters.AutoInfoAdapter;
import just.beans.AutoInfo;

public class IllegalActivity extends Activity {
    private ListView mLvForIllegal;
    private AutoInfoAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_illegal);
        
        init();
    }

    private void init() {
        findViewById(R.id.id_ib_top_bar_back).setOnClickListener(v -> {
            finish();
        });
        findViewById(R.id.id_ib_top_bar_add).setVisibility(View.INVISIBLE);
        ((TextView)findViewById(R.id.id_tv_top_bar_title)).setText("汽车违章信息");

        mLvForIllegal= (ListView) findViewById(R.id.id_lv_auto_info_for_illegal);
        TextView tvHint=(TextView) findViewById(R.id.id_tv_hint);
        mAdapter = new AutoInfoAdapter(this,
                isShow -> {tvHint.setVisibility(isShow ? View.VISIBLE : View.GONE);});
        mLvForIllegal.setAdapter(mAdapter);

        mLvForIllegal.setOnItemClickListener(((parent, view, position, id) -> {
            AutoInfo autoInfo =mAdapter.getItem(position);
            Log.d("测试->IllegalActivity","已经获取汽车的相关信息----");
        }));
    }
}
