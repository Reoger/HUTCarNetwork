package com.cwp.android.baidutest;


import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.com.reoger.music.Utils.LogUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Calendar;

import c.b.BP;
import c.b.PListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import just.activities.ActivityCollector;
import just.beans.OrdGasInfo;
import utils.ShapeLoadingDialog;

public class PayActivity extends AppCompatActivity {

    private RadioGroup radioGroup;
    private RadioButton radioButton1;
    private RadioButton radioButton2;

    private Button btn_pay_ok;
    private Button add, sub;

    //false汽油 true柴油
    boolean TypeGas;

    int allPrice;//需要支付的总价格
    private double mQuantity;

    private ProgressDialog mDialog;
    OrdGasInfo info = new OrdGasInfo();
    private String mObjectId;

    private ShapeLoadingDialog shapeLoadingDialog;

    Bundle bundle;

    private TextView brand, model, licensePlateNum, engineNum, bodyLevel, vin, stationName, stationAddress, price, quantity;
    private TextView mEditDate;
    private boolean mFlag=true;//用于判断是否加油成功

    private Calendar calendar;//日历类



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        ActivityCollector.addActivity(this);
        BP.init(getApplication(), "11c50a59fafd8add5a2c19107b769f9d");
        init();
        change(allPrice);

    }

    public void init() {

        shapeLoadingDialog = new ShapeLoadingDialog(this);
        shapeLoadingDialog.setLoadingText("加载中...");


        bundle = getIntent().getExtras();

        add = (Button) findViewById(R.id.add);
        sub = (Button) findViewById(R.id.sub);
        btn_pay_ok = (Button) findViewById(R.id.pay_ok);

        brand = (TextView) findViewById(R.id.brand);
        model = (TextView) findViewById(R.id.model);
        licensePlateNum = (TextView) findViewById(R.id.licensePlateNum);
        engineNum = (TextView) findViewById(R.id.engineNum);
        bodyLevel = (TextView) findViewById(R.id.bodyLevel);
//        vin = (TextView) findViewById(R.id.vin);
        stationName = (TextView) findViewById(R.id.stationName);
        stationAddress = (TextView) findViewById(R.id.stationAddress);
        price = (TextView) findViewById(R.id.price);
        quantity = (TextView) findViewById(R.id.quantity);
        mEditDate = (TextView) findViewById(R.id.edit_date);

        radioGroup = (RadioGroup) findViewById(R.id.RadioGroup);
        radioButton1 = (RadioButton) findViewById(R.id.RadioButton1);

        radioButton2 = (RadioButton) findViewById(R.id.RadioButton2);

        calendar = Calendar.getInstance(); //默认显示当前的时间

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == radioButton1.getId()) {
                TypeGas = false;
                change(allPrice);
//                    Toast.makeText(getApplicationContext(), "汽油", 1).show();
//                    price.setText(bundle.getString("gasprice1"));
            } else if (checkedId == radioButton2.getId()) {
                TypeGas = true;
//                    Toast.makeText(getApplicationContext(), "柴油", 1).show();
//                    price.setText(bundle.getString("price1"));
            }

            change(allPrice);
        });


        mEditDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    PayActivity.this, DateSet, calendar
                    .get(Calendar.YEAR), calendar
                    .get(Calendar.MONTH), calendar
                    .get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        }
        );

        btn_pay_ok.setOnClickListener(v -> {//支付接口
            if (mEditDate.getText().toString().equals("")) {
                Toast.makeText(PayActivity.this, "请输入加油的日期", Toast.LENGTH_SHORT).show();

            } else {
//                showMainDialog();
                shapeLoadingDialog.show();
                String type = TypeGas ? "柴油" : "汽油";
                String name = "加油站名字" + bundle.getString("NAME");
                String address = "地址" + bundle.getString("ADDRESS");
                String price = "汽油价格" + bundle.getString("price1");
                String price2 = "柴油价格" + bundle.getString("gasprice1");

                BP.pay(PayActivity.this, type, name, 0.02, false, new PListener() {
                    @Override
                    public void orderId(String s) {
                        LogUtils.d("TAG", "订单编号：" + s);
                        //保存数据
                        saveDateOnYun(s);
                    }

                    @Override
                    public void succeed() {
                        Toast.makeText(getApplicationContext(), "成功支付", Toast.LENGTH_SHORT).show();//支付接口
                        updateBoolean(false);
                        shapeLoadingDialog.dismiss();
                    }

                    @Override
                    public void fail(int code, String s) {
                        shapeLoadingDialog.dismiss();
                        updateBoolean(false);

                        if (code == -3) {
                            if(copyApkFromAssets(PayActivity.this, "BmobPayPlugin.apk", Environment.getExternalStorageDirectory().getAbsolutePath()+"/BmobPayPlugin.apk")){
                                android.support.v7.app.AlertDialog.Builder m = new android.support.v7.app.AlertDialog.Builder(PayActivity.this)
                                        .setIcon(R.mipmap.ic_launcher).setMessage("检测到未安装微信支付插件，是否安装？")
                                        .setIcon(R.mipmap.ic_launcher)
                                        .setPositiveButton("yes", (dialog, which) -> {
                                            Intent intent = new Intent(Intent.ACTION_VIEW);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            intent.setDataAndType(Uri.parse("file://" + Environment.getExternalStorageDirectory().getAbsolutePath()+"/BmobPayPlugin.apk"),
                                                    "application/vnd.android.package-archive");
                                            startActivity(intent);
                                        });
                                m.show();
                            }
                    }
                        Toast.makeText(getApplicationContext(), "支付失败", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void unknow() {
                        updateBoolean(false);
                        shapeLoadingDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "未知错误", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        add.setOnClickListener(v -> {

            allPrice += 10;
            change(allPrice);

        });

        sub.setOnClickListener(v -> {

            allPrice -= 10;
            if (allPrice < 0) {
                allPrice = 0;
            }
            change(allPrice);

        });

//        Log.e("********Ord******", bundle.getString("USERNAME"));
        Log.e("********Ord******", bundle.getString("BRAND"));
        Log.e("********Ord******", bundle.getString("MODEL"));
        Log.e("********Ord******", bundle.getString("LICENSEPLATENUM"));
        Log.e("********Ord******", bundle.getString("ENGINENUM"));
        Log.e("********Ord******", bundle.getString("BODYLEVEL"));
        Log.e("********Ord******", bundle.getString("VIN"));

        Log.e("********Ord******", bundle.getString("NAME"));
        Log.e("********Ord******", bundle.getString("ADDRESS"));
        Log.e("********Ord******", bundle.getString("price1"));
        Log.e("********Ord******", bundle.getString("gasprice1"));

        brand.setText(bundle.getString("BRAND"));
        model.setText(bundle.getString("MODEL"));
        licensePlateNum.setText(bundle.getString("LICENSEPLATENUM"));
        engineNum.setText(bundle.getString("ENGINENUM"));
        bodyLevel.setText(bundle.getString("BODYLEVEL"));

        stationName.setText(bundle.getString("NAME"));
        stationAddress.setText(bundle.getString("ADDRESS"));
        price.setText(allPrice + "");

    }


    void change(int priceNum) {

        double priceTemp = 1;

        if (TypeGas) {
            priceTemp = Double.parseDouble(bundle.getString("price1"));

        } else {
            priceTemp = Double.parseDouble(bundle.getString("gasprice1"));

        }
        Log.e("********temp******", priceTemp + "");
        if (priceTemp == 0) {
            priceTemp = 1;
        }

        mQuantity = priceNum / priceTemp;

        DecimalFormat decimalFormat = new DecimalFormat("0.00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
        String p = decimalFormat.format(mQuantity);//format 返回的是字符串
//        mQuantity = Math.round((priceNum / priceTemp * 100) / 100);


        Log.e("********temp******", mQuantity + "");

        quantity.setText(p + " L ");
        price.setText(allPrice + " RMB ");
    }

    /**
     * 保存数据到云端
     */
    public void saveDateOnYun(String data) {

        info.setPayId(data);
        info.setLicensePlateNum(bundle.getString("LICENSEPLATENUM"));
        info.setBrand(bundle.getString("BRAND"));
        info.setEngineNum(bundle.getString("ENGINENUM"));
        info.setModel(bundle.getString("MODEL"));
        info.setName(MyApplication.getName());
        info.setReservationTime(mEditDate.getText().toString());//预约时间
        info.setUsername(MyApplication.getUsername());
        info.setLiter(mQuantity);
        info.setmIsUsed(mFlag);
        String type = TypeGas ? "柴油" : "汽油";
        info.setGoodsName(type);
        info.save(PayActivity.this, new SaveListener() {

            @Override
            public void onSuccess() {
//                mDialog.dismiss();
                mObjectId = info.getObjectId();
                LogUtils.i("TAG", "保存到云端成功");
            }

            @Override
            public void onFailure(int i, String s) {
                Toast.makeText(PayActivity.this, "保存到云端失败", Toast.LENGTH_SHORT).show();
                LogUtils.i("TAG", "保存到云端失败");



//                mDialog.dismiss();
            }
        });
    }

    private void showMainDialog() {
        mDialog = new ProgressDialog(PayActivity.this);
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setTitle("Loading...");
        mDialog.setMessage("正在加载中，请稍后...");
        mDialog.setCancelable(false);
        mDialog.setButton("取消", (dialog, which) -> {
            finish();
        });
        mDialog.show();
    }

    private void  updateBoolean(boolean flag){

        info.setValue("mIsUsed",flag);
        info.update(this, mObjectId, new UpdateListener() {
            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
                Log.i("bmob","更新成功：");
            }

            @Override
            public void onFailure(int code, String msg) {
                // TODO Auto-generated method stub
                Log.i("bmob","更新失败："+msg);
            }
        });
    }

    public boolean copyApkFromAssets(Context context, String fileName, String path) {
        boolean copyIsFinish = false;
        try {
            InputStream is = context.getAssets().open(fileName);
            File file = new File(path);
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            byte[] temp = new byte[1024];
            int i = 0;
            while ((i = is.read(temp)) > 0) {
                fos.write(temp, 0, i);
            }
            fos.close();
            is.close();
            copyIsFinish = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return copyIsFinish;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }

    /**
     * @description 日期设置匿名类
     */
    DatePickerDialog.OnDateSetListener DateSet = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // 每次保存设置的日期
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            String str = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
          LogUtils.e("TAG",str+"你好");

            if (!mEditDate.isFocused()) {
                mEditDate.setText(str);
            }
        }
    };

}
