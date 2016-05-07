package just.adapters;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cwp.android.baidutest.R;

import java.util.List;

import just.beans.AutomobileInfo;
import just.constants.AutoInfoConstants;
import just.interfaces.AboutHint;
import just.operations.AutoInfoLocalDBOperation;

/**
 * Created by Just on 2016/5/5.
 */
public class AutoInfoAdapter extends BaseAdapter {
    private List<AutomobileInfo> mData;
    private Context mContext;
    private LayoutInflater mInflater;

    private Handler mHandler;

    private AboutHint mAboutHint;

    public AutoInfoAdapter(Context context, Handler handler, AboutHint aboutHint) {
        mInflater = LayoutInflater.from(context);
        mContext=context;
        mHandler=handler;
        mAboutHint=aboutHint;
        setData();
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_auto_info, null);
            viewHolder.brand = (TextView) convertView.findViewById(R.id.id_item_tv_brand);
            viewHolder.model = (TextView) convertView.findViewById(R.id.id_item_tv_model);
            viewHolder.plateNo = (TextView) convertView.findViewById(R.id.id_item_tv_plate_no);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        AutomobileInfo automobileInfo = mData.get(position);
        viewHolder.brand.setText(automobileInfo.getBrand());
        viewHolder.model.setText(automobileInfo.getModel());
        viewHolder.plateNo.setText(automobileInfo.getLicensePlateNum());

        return convertView;
    }

    private void setData() {
        mData = AutoInfoLocalDBOperation.queryBy(mContext,null, AutoInfoConstants.COLUMN_IS_DEL_WITH_CLOUD+" = ?",new String[]{"0"});
        mAboutHint.setHint(mData.size()<=0?true:false);
    }

    private class ViewHolder {
        TextView brand;
        TextView model;
        TextView plateNo;
    }
}