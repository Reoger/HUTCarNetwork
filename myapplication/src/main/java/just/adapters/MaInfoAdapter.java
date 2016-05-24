package just.adapters;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.cwp.android.baidutest.MyApplication;
import com.cwp.android.baidutest.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.DeleteListener;
import cn.bmob.v3.listener.FindListener;
import just.activities.MaInfoActivity;
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

    private boolean mIsDelPattern=false;

    private Map<Integer,CheckBox> mCheckMap;

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
            viewHolder.checkBox= (CheckBox) convertView.findViewById(R.id.id_item_checkbox);

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
        if(mIsDelPattern) {
            Log.d("测试->MaInfoAdapter","显示CheckBox"+position);
            viewHolder.checkBox.setVisibility(View.VISIBLE);
            mCheckMap.put(position,viewHolder.checkBox);
        }
        else {
            viewHolder.checkBox.setVisibility(View.GONE);
        }

        return convertView;
    }

    class ViewHolder {
        TextView brand$model,plateNo,mileage,gasolineVolume,enginePerfor,tranPerfor,lamp,scanTime;
        CheckBox checkBox;
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
        mAboutHint.setHint(mData.size()<=0);
    }

    /**
     * 根据vin获取对应的数据
     * @param vin
     */
    private void setData(String vin) {
        mData = MaInfoLocalDBOperation.queryBy(mContext,
                MaInfoConstants.COLUMN_IS_DEL_WITH_CLOUD+" = ? and "+MaInfoConstants.COLUMN_VIN+" = ?",new String[]{"0",vin});
        mAboutHint.setHint(mData.size()<=0);
    }

    public void setIsDelPattern(boolean pattern) {
        mIsDelPattern=pattern;
        if (mIsDelPattern) {
            mCheckMap=new HashMap<>();
        } else {
            mCheckMap=null;
        }
        super.notifyDataSetChanged();
    }

    public boolean getIsDelPattern() {
        return mIsDelPattern;
    }

    public void setSelectedCheckBox(int position) {
        Log.d("测试->MaInfoAdapter","setSelectedCheckBox->"+position);
        CheckBox cb=mCheckMap.get(position);
        cb.setChecked(!cb.isChecked());
    }

    /**
     * @param select true代表全选，false代表取消全选
     */
    public void selectAllCheckBox(boolean select) {
        for (int i=0,len=getCount();i<len;i++) {
            mCheckMap.get(i).setChecked(select);
        }
        super.notifyDataSetChanged();
    }

    public void deleteCheckBox() {
        Log.d("测试->AutoInfoAdapter","准备删除所选的CheckBox");
        new DeleteMaInfoTask().start();
    }


    private List<MaInfo> getNeedsDelObjects() {
        List<MaInfo> list=new ArrayList<>();
        for(int i=0,len=mCheckMap.size();i<len;i++) {
            if(mCheckMap.get(i).isChecked()) {
                list.add(mData.get(i));
            }
        }
        return list;
    }

    private class DeleteMaInfoTask extends Thread {
        private class DelHolder {
            String vin,scanTime;

            DelHolder(String vin,String scanTime) {
                this.vin=vin;
                this.scanTime=scanTime;
            }
        }

        private int count = 0;

        @Override
        public void run() {
            mHandler.sendEmptyMessage(MaInfoActivity.START_DEL);

            try {
                MyApplication.mSyncSemaphore.acquire();
            } catch (InterruptedException e) {
            }

            List<DelHolder> holders = new ArrayList<>();
            List<MaInfo> needsDelList=getNeedsDelObjects();
            for (MaInfo maInfo : needsDelList) {
                String vin = maInfo.getVin();
                String scanTime=maInfo.getScanTime();
                String isSyncToCloud = MaInfoLocalDBOperation.queryGetSpecifiedAttr(mContext, new String[]{MaInfoConstants.COLUMN_IS_SYNC},
                        MaInfoConstants.COLUMN_SCAN_TIME+" = ? and "+MaInfoConstants.COLUMN_VIN+" = ?", new String[]{scanTime,vin});
                if ("0".equals(isSyncToCloud)) {
                    //如果需要删除的数据还没有同步至云端，则只需要直接在本地数据库删除
                    if (MaInfoLocalDBOperation.deleteBy(mContext, MaInfoConstants.COLUMN_SCAN_TIME+" = ? and "+MaInfoConstants.COLUMN_VIN+" = ?", new String[]{scanTime,vin})) {
                        Log.d("测试->DeleteMaInfoTask", "vin="+vin +"--scanTime=" +scanTime+ "->未同步至云端，本地删除成功!");
                        count++;
                    }
                    checkIsFinished();
                } else if ("1".equals(isSyncToCloud)) {
                    //如果同步至了云端，则需要先删除云端的数据
                    DelHolder holder=new DelHolder(vin,scanTime);
                    holders.add(holder);
                }
            }

            //先删除云端的数据
            //如果在云端删除成功，就可以直接在本地删除
            //如果失败，就需要改变AutoInfoConstants.COLUMN_IS_DEL_WITH_CLOUD的状态
            if (holders.size() != 0) {
                for (DelHolder holder: holders) {
                    BmobQuery<MaInfo> query = new BmobQuery<>();
                    query.addWhereEqualTo("vin", holder.vin);
                    query.addWhereEqualTo("scanTime", holder.scanTime);
                    query.setLimit(1);
                    query.addQueryKeys("objectId");
                    query.findObjects(mContext, new FindListener<MaInfo>() {
                        @Override
                        public void onSuccess(List<MaInfo> list) {
                            Log.d("测试->DeleteMaInfoTask", "查询成功");
                            list.get(0).delete(mContext, new DeleteListener() {
                                @Override
                                public void onSuccess() {
                                    if (MaInfoLocalDBOperation.deleteBy(mContext, MaInfoConstants.COLUMN_SCAN_TIME+" = ? and "+MaInfoConstants.COLUMN_VIN+" = ?", new String[]{holder.scanTime,holder.vin})) {
                                        Log.d("测试->DeleteMaInfoTask", "vin="+holder.vin +"--scanTime=" +holder.scanTime + "->本地与云端删除成功!");
                                        count++;
                                    }
                                    checkIsFinished();
                                }

                                @Override
                                public void onFailure(int i, String s) {
                                    MaInfoLocalDBOperation.updateForIsDelWithCloud(mContext,holder.scanTime, holder.vin, 1);
                                    count++;
                                    Log.d("测试->DeleteMaInfoTask","vin="+holder.vin +"--scanTime=" +holder.scanTime + "->删除失败！ i=" + i + ",s=" + s);
                                    checkIsFinished();
                                }
                            });

                        }

                        @Override
                        public void onError(int i, String s) {
                            MaInfoLocalDBOperation.updateForIsDelWithCloud(mContext,holder.scanTime, holder.vin, 1);
                            count++;
                            Log.d("测试->DeleteMaInfoTask","vin="+holder.vin +"--scanTime=" +holder.scanTime + "->查询失败!i=" + i + "s=" + s);
                            checkIsFinished();
                        }
                    });
                }
            }
        }

        private void checkIsFinished() {
            Log.d("测试->DeleteMaInfoTask", "checkIsFinished->mCount=" + count + "，mNeedsDelObjects.size()=" + getNeedsDelObjects().size());
            if (count == getNeedsDelObjects().size()) {
                MyApplication.mSyncSemaphore.release();
                mHandler.sendEmptyMessage(MaInfoActivity.FINISHED_DEL);
            }
        }
    }

}
