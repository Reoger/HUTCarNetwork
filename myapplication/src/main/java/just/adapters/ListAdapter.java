package just.adapters;

import java.util.List;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cwp.android.baidutest.R;

import just.beans.ListModel;

public class ListAdapter extends BaseAdapter {
	
	private  List<ListModel> mDate;
	private Context mContext;

	public ListAdapter( Context mContext,List mDate){
		this.mContext=mContext;
		this.mDate=mDate;		
	}
	

	@Override
	public int getCount() {
		return mDate.size();
	}

	@Override
	public Object getItem(int position) {
		return mDate.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View view = View.inflate(mContext, R.layout.csy_listitem_citys, null);
		
		//初始化
		ListModel model=mDate.get(position) ;
		TextView txt_name =(TextView) view.findViewById(R.id.txt_name);

		

		//绑定数据
		txt_name.setText(model.getTextName());
		txt_name.setTag(model.getNameId());
		//返回
		return view;
	}

}
