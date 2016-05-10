package just.adapters;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

import just.beans.MaInfo;
import just.constants.MaInfoConstants;
import just.interfaces.AboutHint;
import just.operations.MaInfoLocalDBOperation;

public class MaInfoAdapter extends BaseAdapter {
    private List<MaInfo> mData;
    private Context mContext;
    private LayoutInflater mInflater;

    private AboutHint mAboutHint;

    private Handler mHandler;
    public MaInfoAdapter(Context context, AboutHint aboutHint,Handler handler) {
        mInflater = LayoutInflater.from(context);
        mContext=context;
        mAboutHint=aboutHint;
        mHandler=handler;
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
        return null;
    }

    class ViewHolder {

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
