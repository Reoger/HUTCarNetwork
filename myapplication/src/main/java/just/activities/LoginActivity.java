package just.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.cwp.android.baidutest.MainActivity;
import com.cwp.android.baidutest.MyApplication;
import com.cwp.android.baidutest.OrdGasActivity;
import com.cwp.android.baidutest.R;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import just.beans.MaInfo;
import just.beans.MyUser;
import just.utils.ToastUtil;

public class LoginActivity extends AppCompatActivity {
    private EditText mEtUsername, mEtPassword;
    private Button mBtRegister, mBtLogin,mBtUsernameClear,mBtPasswordClear;

    public static final int START_VERIFY=1;
    public static final int SUCCEED_VERIFY=2;
    public static final int FAILED_VERIFY=3;

    private Handler mHandler=new Handler() {
        ProgressDialog progressDialog;
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case START_VERIFY:
                    progressDialog =new ProgressDialog(LoginActivity.this);
                    progressDialog.setMessage("正在验证...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    break;
                case SUCCEED_VERIFY:
                    progressDialog.dismiss();
                    progressDialog =null;
                    Log.d("测试->LoginActivity","验证成功");
                    ToastUtil.showOrdinaryToast("验证成功",LoginActivity.this);

                    Intent intent=null;
                    String tag=getIntent().getStringExtra("TAG");
                    if(!TextUtils.isEmpty(tag)&&tag.equals("OrdGAs")) {
                        intent=new Intent(LoginActivity.this, OrdGasActivity.class);
                    }
                    else {
                        intent=new Intent(LoginActivity.this, MyInfoActivity.class);
                    }
                    startActivity(intent);
                    finish();
                    break;
                case FAILED_VERIFY:
                    progressDialog.dismiss();
                    progressDialog =null;
                    String result=(String) msg.obj;
                    Log.d("测试->LoginActivity",result);
                    AlertDialog.Builder builder=new AlertDialog.Builder(LoginActivity.this);
                    AlertDialog alertDialog=builder.create();
                    builder.setTitle("验证失败:");
                    builder.setMessage(result);
                    builder.setCancelable(false);
                    builder.setPositiveButton("确认",(dialog,which)-> {
                        mEtUsername.setText("");
                        mEtPassword.setText("");
                        alertDialog.dismiss();
                    });
                    builder.show();
                    break;
                default:break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ActivityCollector.addActivity(this);
        init();
    }

    private void init() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("登陆界面");
        actionBar.setDisplayHomeAsUpEnabled(true);

        mEtUsername= (EditText) findViewById(R.id.id_et_username);
        mEtPassword= (EditText) findViewById(R.id.id_et_password);
        mEtUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length()>0) {
                    mBtUsernameClear.setVisibility(View.VISIBLE);
                    mBtPasswordClear.setVisibility(View.INVISIBLE);
                }
                else if(s.length()==0){
                    mBtPasswordClear.setVisibility(View.INVISIBLE);
                }
            }
        });
        mEtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if(s.length()>0) {
                    mBtPasswordClear.setVisibility(View.VISIBLE);
                    mBtUsernameClear.setVisibility(View.INVISIBLE);

                }
                else if(s.length()==0){
                    mBtPasswordClear.setVisibility(View.INVISIBLE);
                }
            }
        });

        mBtRegister= (Button) findViewById(R.id.id_bt_register);
        mBtLogin= (Button) findViewById(R.id.id_bt_login);
        mBtUsernameClear= (Button) findViewById(R.id.id_bt_username_clear);
        mBtPasswordClear= (Button) findViewById(R.id.id_bt_password_clear);

        mBtUsernameClear.setOnClickListener(v -> {
            mEtUsername.setText("");
            mEtPassword.setText("");
        });
        mBtPasswordClear.setOnClickListener(v -> {
            mEtPassword.setText("");
        });

        mBtLogin.setOnClickListener(v -> {
            Log.d("测试->LoginActivity","登陆");
            String username=mEtUsername.getText().toString();
            String password=mEtPassword.getText().toString();
            if (TextUtils.isEmpty(username)||username.length()!=11) {
                Log.d("测试->LoginActivity","请输入有效长度的用户名!");
                ToastUtil.showOrdinaryToast("请输入有效长度的用户名!",this);
                return;
            }
            else if(TextUtils.isEmpty(password)) {
                Log.d("测试->LoginActivity","密码不能为空!");
                ToastUtil.showOrdinaryToast("密码不能为空!",this);
                return;
            }
            mHandler.sendEmptyMessage(START_VERIFY);
            new Thread(()->{
                MyUser user=new MyUser();
                user.setUsername(username);
                user.setPassword(password);
                user.login(LoginActivity.this, new SaveListener() {
                    @Override
                    public void onSuccess() {
                        BmobQuery<MyUser> query=new BmobQuery<MyUser>();
                        query.addWhereEqualTo("username",username);
                        query.setLimit(1);
                        query.findObjects(LoginActivity.this, new FindListener<MyUser>() {
                            @Override
                            public void onSuccess(List<MyUser> list) {
                                String name=list.get(0).getName();
                                MyApplication.saveLoginInfo(username,name);

                                mHandler.sendEmptyMessage(SUCCEED_VERIFY);
                            }

                            @Override
                            public void onError(int i, String s) {
                                MyApplication.saveLoginInfo(username,MyApplication.NULL_NAME);
                                mHandler.sendEmptyMessage(SUCCEED_VERIFY);
                            }
                        });
                    }

                    @Override
                    public void onFailure(int i, String s) {
                        ToastUtil.showToastForErrorCode(i,LoginActivity.this);
                        mHandler.sendEmptyMessage(FAILED_VERIFY);
                    }
                });
            }).start();
        });
        mBtRegister.setOnClickListener(v -> {
            Log.d("测试->LoginActivity","注册");
            Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}
