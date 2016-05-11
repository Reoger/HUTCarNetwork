package just.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;


import com.cheshouye.api.client.WeizhangIntentService;
import com.cheshouye.api.client.json.CarInfo;
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

        /**
         * 接口的初始化操作
         */

        Intent weizhangIntent = new Intent(this, WeizhangIntentService.class);
        weizhangIntent.putExtra("appId",1704);// 您的appId
        weizhangIntent.putExtra("appKey", "ecd215026a372d727020edc01dabf8a7");// 您的appKey
        startService(weizhangIntent);


        init();
    }

    private void init() {
        findViewById(R.id.id_ib_top_bar_add).setVisibility(View.INVISIBLE);
        findViewById(R.id.id_ib_top_bar_back).setOnClickListener(v -> {
            finish();
        });
        ((TextView)findViewById(R.id.id_tv_top_bar_title)).setText("汽车违章信息");

        mLvForIllegal= (ListView) findViewById(R.id.id_lv_auto_info_for_illegal);
        TextView tvHint=(TextView) findViewById(R.id.id_tv_hint);
        mAdapter = new AutoInfoAdapter(this,
                isShow -> {tvHint.setVisibility(isShow ? View.VISIBLE : View.GONE);});
        mLvForIllegal.setAdapter(mAdapter);

        mLvForIllegal.setOnItemClickListener(((parent, view, position, id) -> {
            AutoInfo autoInfo =mAdapter.getItem(position);
            Log.d("测试->IllegalActivity","已经获取汽车的相关信息----");
            //选择要查询的地点
          //  Intent intent = new Intent(IllegalActivity.this,ProvinceList.class);
          //  startActivity(intent);
            startResultList();
        }));
    }

    public void startResultList(){
        CarInfo car = new CarInfo();
        car.setChepai_no("粤B12345");
        car.setChejia_no("123456");
        car.setEngine_no("");
        car.setRegister_no("");

        //这里需要通过用户选择要查询的地区，
        car.setCity_id(109);

        Bundle bundle = new Bundle();
        bundle.putSerializable("carInfo", car);
        Intent intent = new Intent();
        intent.putExtras(bundle);
        intent.setClass(IllegalActivity.this, WeizhangResul.class);
        startActivity(intent);

    }

}
