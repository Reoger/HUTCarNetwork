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

import org.w3c.dom.Text;

import just.beans.BodyInfo;

/**
 * Created by 24540 on 2016/5/24.
 */
public class DetailedBilingActivity extends AppCompatActivity{

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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detaile_biling);

        initView();

        mInfo = (BodyInfo) getIntent().getSerializableExtra("info");
        setDate(mInfo);
        s ="结账二维码:总金额是"+mInfo.getTotal_fee()+""+"账单号是"+mInfo.getOut_trade_no();
        createResult(s);
    }

    private void initView() {
        mCarInfo = (TextView)findViewById(R.id.biling_car_info);
        mName = (TextView)findViewById(R.id.biling_name);
        mMoney = (TextView)findViewById(R.id.biling_money);
        mState = (TextView)findViewById(R.id.biling_state);
        mId = (TextView)findViewById(R.id.biling_id);
        mTime = (TextView)findViewById(R.id.biling_time);
        mUserName = (TextView)findViewById(R.id.biling_username);
        mType =(TextView)findViewById(R.id.biling_type);
        mImage = (ImageView) findViewById(R.id.biling_image);
    }

    /**
     * 生成二维码
     * @param s
     */
    private void createResult(String s) {
        Bitmap log = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        if(s.equals("")){
            Toast.makeText(this,"输出为空",Toast.LENGTH_SHORT).show();
        }else{
            Bitmap bitmap = EncodingUtils.createQRCode(s,500,500,log);
            mImage.setImageBitmap(bitmap);
        }
    }

    public void setDate(BodyInfo date) {
         mName.setText(date.getName());
         mMoney.setText(date.getTransaction_id());
         mState.setText(date.getTrade_state());
         mId.setText(date.getOut_trade_no());
         mTime.setText(date.getCreate_time());
         mType.setText(date.getPay_type());
         //mUserName.setText();
         //mCarInfo;
    }
}
