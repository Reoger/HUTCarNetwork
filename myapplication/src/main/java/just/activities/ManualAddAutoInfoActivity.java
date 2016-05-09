package just.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cwp.android.baidutest.R;

import java.util.Date;

import just.beans.AutomobileInfo;

public class ManualAddAutoInfoActivity extends Activity {
    private EditText etBrand,etModel,etBodyLevel,etPlateNum,etEngineNum,etVin;
    private Button btConfirm,btCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_add_auto_info);
        
        init();
    }

    private void init() {
        etBrand= (EditText) findViewById(R.id.id_et_add_brand);
        etModel= (EditText) findViewById(R.id.id_et_add_model);
        etBodyLevel= (EditText) findViewById(R.id.id_et_add_body_level);
        etPlateNum= (EditText) findViewById(R.id.id_et_add_plate_num);
        etEngineNum= (EditText) findViewById(R.id.id_et_add_engine_num);
        etVin= (EditText) findViewById(R.id.id_et_add_vin);

        btConfirm= (Button) findViewById(R.id.id_bt_add_confirm);
        btCancel= (Button) findViewById(R.id.id_bt_add_cancel);

        btConfirm.setOnClickListener(v -> {
            String brand="品牌:"+etBrand.getText().toString().trim();
            String model="型号:"+etModel.getText().toString().trim();
            String body="车声级别:"+etBodyLevel.getText().toString().trim();
            String plate="车牌号码:"+etPlateNum.getText().toString().trim();
            String engine="发动机号:"+etEngineNum.getText().toString().trim();
            String vin="车架号:"+etVin.getText().toString().trim();
            if(!TextUtils.isEmpty(brand)&&!TextUtils.isEmpty(model)&&
                    !TextUtils.isEmpty(plate)&&!TextUtils.isEmpty(engine)&&
                    !TextUtils.isEmpty(body)&&!TextUtils.isEmpty(vin)) {
                Intent intent = new Intent();
                String result=brand+"\n"+model+"\n"+body+"\n"+plate+"\n"+engine+"\n"+vin;
                intent.putExtra("result", result);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                Toast.makeText(this,"所填信息不能为空",Toast.LENGTH_SHORT).show();
            }
        });

        btCancel.setOnClickListener(v -> {
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
