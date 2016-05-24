package com.cwp.android.baidutest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

import c.b.BP;
import c.b.PListener;

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

    Bundle bundle;

    private TextView brand, model, licensePlateNum, engineNum, bodyLevel, vin, stationName, stationAddress, price, quantity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        BP.init(getApplication(),"11c50a59fafd8add5a2c19107b769f9d");
        init();
        change(allPrice);

    }

    public void init() {
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

        radioGroup = (RadioGroup) findViewById(R.id.RadioGroup);
        radioButton1 = (RadioButton) findViewById(R.id.RadioButton1);

        radioButton2 = (RadioButton) findViewById(R.id.RadioButton2);

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


        btn_pay_ok.setOnClickListener(v -> {//支付接口
            String type = TypeGas?"柴油":"汽油";
            String name = "加油站名字"+bundle.getString("NAME");
            String address = "地址"+bundle.getString("ADDRESS");
            String price = "汽油价格"+bundle.getString("price1");
            String price2 = "柴油价格"+bundle.getString("gasprice1");

            BP.pay(PayActivity.this, type, name+address+price+price2, allPrice, false, new PListener() {
                @Override
                public void orderId(String s) {

                }

                @Override
                public void succeed() {
                    Toast.makeText(getApplicationContext(), "成功支付", Toast.LENGTH_SHORT).show();//支付接口
                }

                @Override
                public void fail(int i, String s) {
                    Toast.makeText(getApplicationContext(), "成功失败", Toast.LENGTH_SHORT).show();//支付接口

                }

                @Override
                public void unknow() {

                }
            });
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
        Log.e("********temp******", priceTemp+"");
        if (priceTemp == 0) {
            priceTemp = 1;
        }

        mQuantity = priceNum /priceTemp;

        DecimalFormat decimalFormat=new DecimalFormat("0.00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
        String p = decimalFormat.format(mQuantity);//format 返回的是字符串
//        mQuantity = Math.round((priceNum / priceTemp * 100) / 100);


        Log.e("********temp******",mQuantity+"");

        quantity.setText(p + " L ");
        price.setText(allPrice + " RMB ");
    }


}
