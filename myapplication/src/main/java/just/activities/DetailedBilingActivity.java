package just.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.Toast;

import com.cwp.android.baidutest.R;
import com.xys.libzxing.zxing.encoding.EncodingUtils;

/**
 * Created by 24540 on 2016/5/24.
 */
public class DetailedBilingActivity extends AppCompatActivity{

    private ImageView mImage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detaile_biling);

        mImage = (ImageView) findViewById(R.id.biling_image);

        Bitmap log = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        String a = "你就一个大傻逼";
        if(a.equals("")){
            Toast.makeText(this,"输出为空",Toast.LENGTH_SHORT).show();
        }else{
            Bitmap bitmap = EncodingUtils.createQRCode(a,500,500,log);
            mImage.setImageBitmap(bitmap);
        }
    }
}
