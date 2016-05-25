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
    private  AutoInfo autoInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_illegal);
        ActivityCollector.addActivity(this);

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
        findViewById(R.id.id_ib_top_bar_back).setOnClickListener(v -> {
            finish();
        });
        findViewById(R.id.id_ib_top_bar_add).setVisibility(View.INVISIBLE);
        ((TextView)findViewById(R.id.id_tv_top_bar_title)).setText("汽车违章信息");

        mLvForIllegal= (ListView) findViewById(R.id.id_lv_auto_info_for_illegal);
        TextView tvHint=(TextView) findViewById(R.id.id_tv_hint);
        mAdapter = new AutoInfoAdapter(this,
                isShow -> {tvHint.setVisibility(isShow ? View.VISIBLE : View.GONE);},false);
        mLvForIllegal.setAdapter(mAdapter);

        mLvForIllegal.setOnItemClickListener(((parent, view, position, id) -> {
             autoInfo =mAdapter.getItem(position);
            Log.d("测试->IllegalActivity","已经获取汽车的相关信息----");
            //选择要查询的地点
            Intent intent = new Intent(IllegalActivity.this,ProvinceList.class);
            startActivityForResult(intent,0x20);
          //  startResultList();
        }));
    }

    public void startResultList(String cityId){
        CarInfo car = new CarInfo();
        car.setChepai_no(autoInfo.getLicensePlateNum());
        car.setChejia_no(autoInfo.getVin());
        car.setEngine_no(autoInfo.getEngineNum());
        car.setRegister_no("");

        int a = Integer.valueOf(cityId);
        //这里需要通过用户选择要查询的地区，
        car.setCity_id(a);

        Bundle bundle = new Bundle();
        bundle.putSerializable("carInfo", car);
        Intent intent = new Intent();
        intent.putExtras(bundle);
        intent.setClass(IllegalActivity.this, WeizhangResul.class);
        startActivity(intent);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null)
            return;
        Log.d("TAG","resultCode "+resultCode);
        Bundle bundle = data.getExtras();
        // 获取城市name
        String cityName = bundle.getString("city_name");
        String cityId = bundle.getString("city_id");
        Log.e("TAG", cityName + cityId);
        startResultList(cityId);//查询结果
     }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}
