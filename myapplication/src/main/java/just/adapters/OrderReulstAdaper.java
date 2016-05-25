package just.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.com.reoger.music.Utils.LogUtils;
import com.cwp.android.baidutest.R;


import org.w3c.dom.Text;

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
            p.TV_id = (TextView) convertView.findViewById(R.id.item_order_id_weiyi);

            convertView.setTag(p);
        } else {
            p = (hodler) convertView.getTag();
        }

        p.TV_NUM.setText("第"+position+1+"条");

        String type = mData.get(position).getPay_type().equals("WECHATPAY")?"微信支付":"其他支付";
        String state = mData.get(position).getTrade_state().equals("NOTPAY")?"未支付":"支付成功";
        p.TV_body.setText(mData.get(position).getBody());
        p.TV_time.setText(mData.get(position).getCreate_time());
        p.TV_name.setText(mData.get(position).getName());
        p.TV_type.setText(type);
        p.TV_id.setText(mData.get(position).getOut_trade_no());
        p.TV_fee.setText("￥"+mData.get(position).getTotal_fee());
        p.TV_state.setText(state);

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
        TextView TV_id;//交易号

    }

}
