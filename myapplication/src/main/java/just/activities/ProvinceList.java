package just.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.cheshouye.api.client.WeizhangClient;
import com.cheshouye.api.client.json.ProvinceInfoJson;
import com.cwp.android.baidutest.R;

import just.adapters.ListAdapter;
import just.beans.ListModel;


public class ProvinceList extends Activity {
	private ListView lv_list;
	private ListAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.csy_activity_citys);
		ActivityCollector.addActivity(this);

		
		//标题
		TextView txtTitle = (TextView) findViewById(R.id.txtTitle);
		txtTitle.setText("选择查询地-省份");
		
		//返回按钮
		Button btnBack = (Button) findViewById(R.id.btnBack);
		btnBack.setVisibility(View.VISIBLE);
		btnBack.setOnClickListener(v -> finish());
		
		
		lv_list = (ListView) findViewById(R.id.lv_1ist);

		mAdapter = new ListAdapter(this, getData2());
		lv_list.setAdapter(mAdapter);

		lv_list.setOnItemClickListener((parent, view, position, id) -> {

            TextView txt_name = (TextView) view.findViewById(R.id.txt_name);

            Intent intent = new Intent();
            intent.putExtra("province_name", txt_name.getText());
            intent.putExtra("province_id", txt_name.getTag().toString());

            intent.setClass(ProvinceList.this, CityList.class);
            startActivityForResult(intent, 20);

        });

	}

	/**
	 * title:获取省份信息
	 * 
	 * @return
	 */
	private List<ListModel> getData2() {

		List<ListModel> list = new ArrayList<>();
		List<ProvinceInfoJson> provinceList = WeizhangClient.getAllProvince();
		
		//开通数量提示
		TextView txtListTip = (TextView) findViewById(R.id.list_tip);
		txtListTip.setText("全国已开通"+provinceList.size()+"个省份, 其它省将陆续开放");
		
		for (ProvinceInfoJson provinceInfoJson : provinceList) {
			String provinceName = provinceInfoJson.getProvinceName();
			int provinceId = provinceInfoJson.getProvinceId();
			
			ListModel model = new ListModel();
			model.setTextName(provinceName);
			model.setNameId(provinceId);
			list.add(model);
		}
		return list;
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
		Intent intent = new Intent();
		intent.putExtra("city_name", cityName);
		intent.putExtra("city_id", cityId);
		setResult(2002, intent);

		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ActivityCollector.removeActivity(this);
	}
}
