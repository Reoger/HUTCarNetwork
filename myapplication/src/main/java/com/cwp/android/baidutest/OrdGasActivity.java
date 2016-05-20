package com.cwp.android.baidutest;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import just.adapters.AutoInfoAdapter;
import just.beans.AutoInfo;

public class OrdGasActivity extends AppCompatActivity {
    private ListView mLvForIllegal;
    private AutoInfoAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ord_gas);

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
                });
        mLvForIllegal.setAdapter(mAdapter);

        mLvForIllegal.setOnItemClickListener(((parent, view, position, id) -> {
            AutoInfo autoInfo = mAdapter.getItem(position);
            Log.d("测试->OrdGasActivity", "已经获取汽车的相关信息----");
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
}
