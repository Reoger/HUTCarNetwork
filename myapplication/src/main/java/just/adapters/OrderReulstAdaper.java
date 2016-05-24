package just.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.com.reoger.music.Utils.LogUtils;
import com.cwp.android.baidutest.R;


import java.util.List;

import just.beans.BodyInfo;
import just.beans.OrdGasInfo;

/**
 * Created by 24540 on 2016/5/21.
 */
public class OrderReulstAdaper extends BaseAdapter {
    private Context mContext;
    private List<BodyInfo> mData;
    private LayoutInflater mInflater;

    public OrderReulstAdaper(Context context, List<BodyInfo> info) {
        this.mContext = context;
        this.mData = info;
        mInflater = LayoutInflater.from(context);
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
        hodler p;
        if (convertView == null) {
            p = new hodler();
            convertView = mInflater.inflate(R.layout.layout_item_order_reulst, null);
            p.TV_NUM =  (TextView) convertView.findViewById(R.id.item_order_num);
            p.TV_body = (TextView) convertView.findViewById(R.id.item_order_content);
            p.TV_time = (TextView) convertView.findViewById(R.id.item_order_time);
            p.TV_name = (TextView) convertView.findViewById(R.id.item_order_name);
            p.TV_type = (TextView) convertView.findViewById(R.id.item_order_pay_type);
            p.TV_fee = (TextView) convertView.findViewById(R.id.item_order_total_fee);
            p.TV_state = (TextView) convertView.findViewById(R.id.item_order_trade_state);

            convertView.setTag(p);
        } else {
            p = (hodler) convertView.getTag();
        }

        p.TV_NUM.setText("第"+position+"条");

        p.TV_body.setText("具体内容："+mData.get(position).getBody());
        p.TV_time.setText("交易时间："+mData.get(position).getCreate_time());
        p.TV_name.setText("交易物品："+mData.get(position).getName());
        p.TV_type.setText("支付类型："+mData.get(position).getPay_type());
        p.TV_fee.setText("总金额  ："+mData.get(position).getTotal_fee());
        p.TV_state.setText("支付状态："+mData.get(position).getTrade_state());

        return convertView;
    }

    class hodler {
        TextView TV_NUM;//序号

        TextView TV_body;//具体内容
        TextView TV_time;//时间
        TextView TV_name;//汽油
        TextView TV_type;//内容
        TextView TV_fee;//总金额
        TextView TV_state;//状态

    }

}
