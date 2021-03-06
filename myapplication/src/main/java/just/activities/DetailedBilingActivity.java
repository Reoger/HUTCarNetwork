package just.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.cwp.android.baidutest.R;
import com.xys.libzxing.zxing.encoding.EncodingUtils;


import just.beans.BodyInfo;

/**
 * Created by 24540 on 2016/5/24.
 */
public class DetailedBilingActivity extends AppCompatActivity {

    private ImageView mImage;
    private String s;
    private BodyInfo mInfo;

    private TextView mName;
    private TextView mMoney;
    private TextView mState;
    private TextView mId;
    private TextView mTime;
    private TextView mType;
    private TextView mUserName;
    private TextView mCarInfo;
    private TextView mTransactionId;
    private TextView mCanUsed;
    private TextView mLiter;
    private TextView mBilingBody;
    private TextView mOritenTime;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detaile_biling);
        ActivityCollector.addActivity(this);

        initView();

        mInfo = (BodyInfo) getIntent().getSerializableExtra("info");
        setDate(mInfo);
        if (mInfo.ismCanUsed()) {
            s = mInfo.getObjectId();//二位码中的信息只有一个ObjectId的信息
            createResult(s);
        } else {
            Toast.makeText(DetailedBilingActivity.this, "该账单未支付，不能生成二维码", Toast.LENGTH_SHORT).show();
        }

    }

    private void initView() {
        mCarInfo = (TextView) findViewById(R.id.biling_car_info);
        mName = (TextView) findViewById(R.id.biling_name);
        mMoney = (TextView) findViewById(R.id.biling_money);
        mState = (TextView) findViewById(R.id.biling_state);
        mId = (TextView) findViewById(R.id.biling_id);
        mTime = (TextView) findViewById(R.id.biling_time);
        mUserName = (TextView) findViewById(R.id.biling_username);
        mType = (TextView) findViewById(R.id.biling_type);
        mUserName = (TextView) findViewById(R.id.biling_username);
        mCarInfo = (TextView) findViewById(R.id.biling_car_info);
        mTransactionId = (TextView) findViewById(R.id.biling_id_jiaoyi);
        mCanUsed = (TextView) findViewById(R.id.biling_can_cost);
        mLiter = (TextView) findViewById(R.id.biling_liter);
        mBilingBody = (TextView) findViewById(R.id.biling_body);
        mOritenTime = (TextView) findViewById(R.id.biling_time_reservation);

        mImage = (ImageView) findViewById(R.id.biling_image);
    }

    /**
     * 生成二维码
     *
     * @param
     */
    private void createResult(String s) {
        Bitmap log = BitmapFactory.decodeResource(getResources(), R.drawable.ic_logo);
        if (s.equals("")) {
            Toast.makeText(this, "输出为空", Toast.LENGTH_SHORT).show();
        } else {
            Bitmap bitmap = EncodingUtils.createQRCode(s, 500, 500, log);
            mImage.setImageBitmap(bitmap);
        }
    }

    public void setDate(BodyInfo date) {
        mName.setText(date.getName());
        mMoney.setText(date.getTotal_fee());
        mState.setText(date.getTrade_state().equals("SUCCESS")?"支付成功":"支付失败");
        mId.setText(date.getOut_trade_no());
        mTime.setText(date.getCreate_time());
        mType.setText(date.getPay_type().equals("WECHATPAY")?"微信支付":"其他支付");
        mUserName.setText(date.getUsename());
        mCarInfo.setText(date.getCar_info());
        mTransactionId.setText(date.getTransaction_id());
        mBilingBody.setText(date.getBody());
        mOritenTime.setText(date.getOrien_time());
        mCanUsed.setText(date.ismCanUsed() ? "可以消费" : "不能消费");
        mLiter.setText(date.getLiter()+"升");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}
