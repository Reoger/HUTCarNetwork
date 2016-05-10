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

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.DeleteListener;
import cn.bmob.v3.listener.FindListener;
import just.activities.AutoInfoActivity;
import just.beans.AutoInfo;
import just.constants.AutoInfoConstants;
import just.interfaces.AboutHint;
import just.operations.AutoInfoLocalDBOperation;

/**
 * Created by Just on 2016/5/5.
 */
public class AutoInfoAdapter extends BaseAdapter {
    private List<AutoInfo> mData;
    private Context mContext;
    private LayoutInflater mInflater;

    private AboutHint mAboutHint;

    private Handler mHandler;

    public AutoInfoAdapter(Context context, AboutHint aboutHint,Handler handler) {
        mInflater = LayoutInflater.from(context);
        mContext=context;
        mAboutHint=aboutHint;
        mHandler=handler;
        setData();
    }

    public AutoInfoAdapter(Context context,AboutHint aboutHint) {
        mAboutHint=aboutHint;
        mInflater = LayoutInflater.from(context);
        mData = AutoInfoLocalDBOperation.queryBy(context, AutoInfoConstants.COLUMN_IS_DEL_WITH_CLOUD+" = ?",new String[]{"0"});
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public AutoInfo getItem(int position) {
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

        AutoInfo autoInfo = mData.get(position);
        viewHolder.brand.setText(autoInfo.getBrand());
        viewHolder.model.setText(autoInfo.getModel());
        viewHolder.plateNo.setText(autoInfo.getLicensePlateNum());

        return convertView;
    }

    /**
     * 查询所有的数据
     */
    private void setData() {
        mData = AutoInfoLocalDBOperation.queryBy(mContext, AutoInfoConstants.COLUMN_IS_DEL_WITH_CLOUD+" = ?",new String[]{"0"});
        mAboutHint.setHint(mData.size()<=0?true:false);
    }

    private class ViewHolder {
        TextView brand;
        TextView model;
        TextView plateNo;
    }

    @Override
    public void notifyDataSetChanged() {
        setData();
        super.notifyDataSetChanged();
    }

    public void deletePosition(int position) {
        AutoInfo autoInfo =mData.get(position);
        String vin = autoInfo.getVin();
        String isSyncToCloud = AutoInfoLocalDBOperation.
                queryGetSpecifiedAttr(mContext,
                        new String[]{AutoInfoConstants.COLUMN_IS_SYNC},
                        " vin = ?", new String[]{vin});
        if ("0".equals(isSyncToCloud)) {
            //如果需要删除的数据还没有同步至云端，则只需要直接在本地数据库删除
            if (AutoInfoLocalDBOperation.deleteBy(mContext, AutoInfoConstants.COLUMN_VIN + " = ?", new String[]{autoInfo.getVin()})) {
                Log.d("测试->DeleteAutoInfoTask", vin + "未同步至云端，本地删除成功!");
                mHandler.sendEmptyMessage(AutoInfoActivity.FINISHED_DEL);
            }
        } else if ("1".equals(isSyncToCloud)) {
            //如果同步至了云端，则需要先删除云端的数据
            BmobQuery<AutoInfo> query = new BmobQuery<>();
            query.addWhereEqualTo("vin", vin);
            query.setLimit(1);
            query.addQueryKeys("objectId");
            query.findObjects(mContext, new FindListener<AutoInfo>() {
                @Override
                public void onSuccess(List<AutoInfo> list) {
                    Log.d("测试->DeleteAutoInfoTask", "查询成功");
                    list.get(0).delete(mContext, new DeleteListener() {
                        @Override
                        public void onSuccess() {
                            if (AutoInfoLocalDBOperation.deleteBy(mContext, AutoInfoConstants.COLUMN_VIN + " = ?", new String[]{vin})) {
                                Log.d("测试->DeleteAutoInfoTask", "vin=" + vin + ",本地与云端删除成功!");
                                mHandler.sendEmptyMessage(AutoInfoActivity.FINISHED_DEL);
                            }
                        }

                        @Override
                        public void onFailure(int i, String s) {
                            AutoInfoLocalDBOperation.updateForIsDelWithCloud(mContext, vin, 1);
                            Log.d("测试->DeleteAutoInfoTask", "删除失败！ i=" + i + ",s=" + s);
                            mHandler.sendEmptyMessage(AutoInfoActivity.FINISHED_DEL);
                        }
                    });

                }

                @Override
                public void onError(int i, String s) {
                    AutoInfoLocalDBOperation.updateForIsDelWithCloud(mContext, vin, 1);
                    Log.d("测试->DeleteAutoInfoTask", "查询失败:失败编码->" + i + ",失败原因->" + s);
                    mHandler.sendEmptyMessage(AutoInfoActivity.FINISHED_DEL);
                }
            });
        }
    }
}