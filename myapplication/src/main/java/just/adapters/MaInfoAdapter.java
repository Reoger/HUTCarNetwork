package just.adapters;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cwp.android.baidutest.R;

import java.util.List;

import just.beans.AutoInfo;
import just.beans.MaInfo;
import just.constants.MaInfoConstants;
import just.interfaces.AboutHint;
import just.operations.MaInfoLocalDBOperation;

public class MaInfoAdapter extends BaseAdapter {
    private List<MaInfo> mData;
    private Context mContext;
    private LayoutInflater mInflater;

    private List<String> mAutoInfoList;

    private AboutHint mAboutHint;

    private Handler mHandler;

    public MaInfoAdapter(Context context, AboutHint aboutHint,Handler handler,List<String> list) {
        mInflater = LayoutInflater.from(context);
        mContext=context;
        mAboutHint=aboutHint;
        mHandler=handler;
        mAutoInfoList=list;
        setData();
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public MaInfo getItem(int position) {
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
            convertView = mInflater.inflate(R.layout.item_ma_info, null);
            viewHolder.brand$model = (TextView) convertView.findViewById(R.id.id_item_brand$model);
            viewHolder.plateNo = (TextView) convertView.findViewById(R.id.id_item_plate_no);
            viewHolder.mileage = (TextView) convertView.findViewById(R.id.id_item_mileage);
            viewHolder.gasolineVolume = (TextView) convertView.findViewById(R.id.id_item_gasoline_volume);
            viewHolder.enginePerfor = (TextView) convertView.findViewById(R.id.id_item_engine_perfor);
            viewHolder.tranPerfor = (TextView) convertView.findViewById(R.id.id_item_tran_perfor);
            viewHolder.lamp = (TextView) convertView.findViewById(R.id.id_item_lamp);
            viewHolder.scanTime = (TextView) convertView.findViewById(R.id.id_item_scan_time);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        MaInfo maInfo = mData.get(position);

        for(String s:mAutoInfoList) {
            String[] info=s.split("[-]");
            if(info.length==4&&info[3].equals(maInfo.getVin())) {
                String str1=info[0].trim()+"("+info[1].trim()+")";
                String str2=info[2].trim();
                viewHolder.brand$model .setText(str1);
                viewHolder.plateNo.setText(str2);
                break;
            }
        }
        viewHolder.mileage.setText(""+maInfo.getMileage());
        viewHolder.gasolineVolume.setText(""+maInfo.getGasolineVolume());
        viewHolder.enginePerfor.setText(maInfo.getEnginePerfor());
        viewHolder.tranPerfor .setText(maInfo.getTransmissionPerfor());
        viewHolder.lamp.setText(maInfo.getLamp());
        viewHolder.scanTime.setText(maInfo.getScanTime());

        return convertView;
    }

    class ViewHolder {
        TextView brand$model,plateNo,mileage,gasolineVolume,enginePerfor,tranPerfor,lamp,scanTime;
    }

    @Override
    public void notifyDataSetChanged() {
        setData();
        super.notifyDataSetChanged();
    }

    public void notifyDataSetChanged(String vin) {
        setData(vin);
        super.notifyDataSetChanged();
    }

    /**
     * 获取所有数据
     */
    private void setData() {
        //从数据库获取数据
        mData = MaInfoLocalDBOperation.queryBy(mContext, MaInfoConstants.COLUMN_IS_DEL_WITH_CLOUD+" = ?",new String[]{"0"});
        mAboutHint.setHint(mData.size()<=0?true:false);
    }

    /**
     * 根据vin获取对应的数据
     * @param vin
     */
    private void setData(String vin) {
        mData = MaInfoLocalDBOperation.queryBy(mContext,
                MaInfoConstants.COLUMN_IS_DEL_WITH_CLOUD+" = ? and "+MaInfoConstants.COLUMN_VIN+" = ?",new String[]{"0",vin});
        mAboutHint.setHint(mData.size()<=0?true:false);
    }
}
