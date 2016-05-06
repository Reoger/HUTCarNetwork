package just.adapters;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.List;

import just.beans.AutomobileInfo;
import just.interfaces.AboutHint;

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
        return null;
    }

    private void setData() {
        //从数据库获取数据

        mAboutHint.setHint(mData.size()<=0?true:false);
    }
}