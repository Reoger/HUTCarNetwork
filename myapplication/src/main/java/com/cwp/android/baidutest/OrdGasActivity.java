package com.cwp.android.baidutest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import just.activities.ActivityCollector;
import just.adapters.AutoInfoAdapter;
import just.beans.AutoInfo;

public class OrdGasActivity extends AppCompatActivity {
    private ListView mLvForIllegal;
    private AutoInfoAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ord_gas);
        ActivityCollector.addActivity(this);

        init();
    }



    private void init() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("预约加油");
        actionBar.setDisplayHomeAsUpEnabled(true);

        mLvForIllegal = (ListView) findViewById(R.id.id_lv_auto_info_for_illegal);
        TextView tvHint = (TextView) findViewById(R.id.id_tv_hint);
        mAdapter = new AutoInfoAdapter(this,
                isShow -> {
                    tvHint.setVisibility(isShow ? View.VISIBLE : View.GONE);
                },false);
        mLvForIllegal.setAdapter(mAdapter);

        mLvForIllegal.setOnItemClickListener(((parent, view, position, id) -> {
            AutoInfo autoInfo = mAdapter.getItem(position);
            Log.d("测试->OrdGasActivity", "已经获取汽车的相关信息----");

            Intent intent = new Intent (this,PayActivity.class);
            Bundle bundle = getIntent().getExtras();

            bundle.putString("USERNAME",autoInfo.getUsername());
            bundle.putString("BRAND",autoInfo.getBrand());
            bundle.putString("MODEL",autoInfo.getModel());
            bundle.putString("LICENSEPLATENUM",autoInfo.getLicensePlateNum());
            bundle.putString("ENGINENUM",autoInfo.getEngineNum());
            bundle.putString("BODYLEVEL",autoInfo.getBodyLevel());
            bundle.putString("VIN",autoInfo.getVin());
            intent.putExtras(bundle);
            startActivity(intent);

        }));
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


    @Override
    protected void onDestroy() {

        OrdGasActivity.this.finish();
        ActivityCollector.removeActivity(this);
        super.onDestroy();
    }
}
