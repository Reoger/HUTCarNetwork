package just.adapters;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cwp.android.baidutest.MyApplication;
import com.cwp.android.baidutest.R;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.DeleteListener;
import cn.bmob.v3.listener.FindListener;
import just.activities.AutoInfoActivity;
import just.beans.AutoInfo;
import just.beans.MaInfo;
import just.constants.AutoInfoConstants;
import just.constants.MaInfoConstants;
import just.interfaces.AboutHint;
import just.operations.AutoInfoLocalDBOperation;
import just.operations.MaInfoLocalDBOperation;

public class AutoInfoAdapter extends BaseAdapter {
    private List<AutoInfo> mData;
    private Context mContext;
    private LayoutInflater mInflater;

    private AboutHint mAboutHint;

    private Handler mHandler;

    private boolean isShowPrompt;

    public AutoInfoAdapter(Context context, AboutHint aboutHint,Handler handler,boolean isShowPrompt) {
        this(context,aboutHint,isShowPrompt);
        mHandler=handler;
    }

    /**
     *
     * @param context
     * @param aboutHint
     * @param isShowPrompt 1-代表显示用于显示提示右滑的图标，0-代表不显示
     */
    public AutoInfoAdapter(Context context,AboutHint aboutHint,boolean isShowPrompt) {
        mContext=context;
        mAboutHint=aboutHint;
        mInflater = LayoutInflater.from(context);
        mData = AutoInfoLocalDBOperation.queryBy(context, AutoInfoConstants.COLUMN_IS_DEL_WITH_CLOUD+" = ? and "+AutoInfoConstants.COLUMN_USERNAME+" = ?",new String[]{"0",MyApplication.getUsername()});
        mAboutHint.setHint(mData.size()<=0?true:false);
        this.isShowPrompt=isShowPrompt;
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
            viewHolder.prompt = (ImageView) convertView.findViewById(R.id.id_item_iv_prompt);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        AutoInfo autoInfo = mData.get(position);
        viewHolder.brand.setText(autoInfo.getBrand());
        viewHolder.model.setText(autoInfo.getModel());
        viewHolder.plateNo.setText(autoInfo.getLicensePlateNum());
        viewHolder.prompt.setVisibility(isShowPrompt?View.VISIBLE:View.INVISIBLE);

        return convertView;
    }

    /**
     * 查询所有的数据
     */
    private void setData() {
        mData = AutoInfoLocalDBOperation.queryBy(mContext, AutoInfoConstants.COLUMN_IS_DEL_WITH_CLOUD+" = ? and "+AutoInfoConstants.COLUMN_USERNAME+" = ?",new String[]{"0",MyApplication.getUsername()});
        mAboutHint.setHint(mData.size()<=0?true:false);
    }

    private class ViewHolder {
        TextView brand;
        TextView model;
        TextView plateNo;
        ImageView prompt;
    }

    @Override
    public void notifyDataSetChanged() {
        setData();
        super.notifyDataSetChanged();
    }

    public void deletePosition(int position) {
        Log.d("测试->AutoInfoAdapter","deletePosition->"+position);
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
                delRelatedMaInfo(vin);
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
                                delRelatedMaInfo(vin);
                            }
                        }

                        @Override
                        public void onFailure(int i, String s) {
                            AutoInfoLocalDBOperation.updateForIsDelWithCloud(mContext, vin, 1);
                            Log.d("测试->DeleteAutoInfoTask", "删除失败！ i=" + i + ",s=" + s);
                            delRelatedMaInfo(vin);
                        }
                    });

                }

                @Override
                public void onError(int i, String s) {
                    AutoInfoLocalDBOperation.updateForIsDelWithCloud(mContext, vin, 1);
                    Log.d("测试->DeleteAutoInfoTask", "查询失败:失败编码->" + i + ",失败原因->" + s);
                    delRelatedMaInfo(vin);
                }
            });
        }
    }

    /**
     * 删除与需要删除的汽车信息相关的维护信息
     * 实际上只是将维护信息的isDelWithCloud改为1即可
     * 暂时不在云端删除，留给后台的Service去做，避免耗时的操作
     * @param vin
     */
    private void delRelatedMaInfo(String vin) {
        //因为车架号是唯一的，所以这里不需要以username作为限制条件
        List<MaInfo> needsDelList= MaInfoLocalDBOperation.queryBy(mContext, MaInfoConstants.COLUMN_VIN+" = ?",new String[]{vin});
        for (MaInfo maInfo:needsDelList) {
            MaInfoLocalDBOperation.updateForIsDelWithCloud(mContext,maInfo.getScanTime(),vin,1);
        }
        MyApplication.mSyncSemaphore.release();
        mHandler.sendEmptyMessage(AutoInfoActivity.FINISHED_DEL);
    }
}