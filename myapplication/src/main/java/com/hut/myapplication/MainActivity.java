package com.hut.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("测试","++++++++++++++");
        Toast.makeText(MainActivity.this,"总算是可以了",Toast.LENGTH_LONG).show();
    }
}
