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
import com.cwp.android.baidutest.R;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import just.beans.MyUser;

public class LoginActivity extends AppCompatActivity {
    private EditText mEtUsername, mEtPassword;
    private Button mBtRegister, mBtLogin, mBtForget,mBtUsernameClear,mBtPasswordClear,mBtEyePassword;

    private static final int START_VERIFY=1;
    private static final int SUCCEED_VERIFY=2;
    private static final int FAILED_VERIFY=3;

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
                    MyApplication.setUsername(mEtUsername.getText().toString());
                    MyApplication.setName((String) msg.obj);
                    progressDialog.dismiss();
                    progressDialog =null;
                    Log.d("测试->LoginActivity","验证成功");

                    //直接用登陆的时候，应该开启一个从云端同步数据到本地的服务
                    MyApplication.startSyncFromCloudService();

                    Intent intent=new Intent(LoginActivity.this, MainActivity.class);
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
//        mBtForget= (Button) findViewById(R.id.id_bt_login_forget);
        mBtUsernameClear= (Button) findViewById(R.id.id_bt_username_clear);
        mBtPasswordClear= (Button) findViewById(R.id.id_bt_password_clear);
//        mBtEyePassword= (Button) findViewById(R.id.id_bt_pwd_eye);

        mBtUsernameClear.setOnClickListener(v -> {
            mEtUsername.setText("");
            mEtPassword.setText("");
        });
        mBtPasswordClear.setOnClickListener(v -> {
            mEtPassword.setText("");
        });

//        mBtEyePassword.setOnClickListener(v -> {
//            if(mEtPassword.getInputType() == (InputType.TYPE_TEXT_VARIATION_PASSWORD)){
//                mBtEyePassword.setBackgroundResource(R.drawable.ic_login_no_eye);
//                mEtPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
//            }else if(mEtPassword.getInputType() == (InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)) {
//                mBtEyePassword.setBackgroundResource(R.drawable.ic_login_eye);
//                mEtPassword.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
//            }
//            mEtPassword.setSelection(mEtPassword.getText().toString().length());
//        });
        mBtLogin.setOnClickListener(v -> {
            Log.d("测试->LoginActivity","登陆");
            String username=mEtUsername.getText().toString();
            String password=mEtPassword.getText().toString();
            if (TextUtils.isEmpty(username)||username.length()!=11) {
                Log.d("测试->LoginActivity","请输入有效长度的用户名!");
                return;
            }
            else if(TextUtils.isEmpty(password)) {
                Log.d("测试->LoginActivity","密码不能为空!");
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
                                String name=list.get(0).getTableName();
                                saveLoginInfoToLocal(username,name);
                                mHandler.sendEmptyMessage(SUCCEED_VERIFY);
                            }

                            @Override
                            public void onError(int i, String s) {
                                saveLoginInfoToLocal(username,"null");
                                mHandler.sendEmptyMessage(SUCCEED_VERIFY);
                            }
                        });
                    }

                    @Override
                    public void onFailure(int i, String s) {
                        Message message=Message.obtain();
                        message.what=FAILED_VERIFY;
                        if(i==9016) {
                            message.obj="无网络连接,请检查您的手机网络!";
                        }
                        else if(i==101) {
                            message.obj="用户名或密码不正确,请重新输入!";
                        }
                        mHandler.sendMessage(message);
                    }
                });
            }).start();
        });
        mBtRegister.setOnClickListener(v -> {
            Log.d("测试->LoginActivity","注册");
            Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
            startActivity(intent);
        });
//        mBtForget.setOnClickListener(v -> {
//            Log.d("测试->LoginActivity","忘记密码");
//        });
    }

    private void saveLoginInfoToLocal(String username,String name) {
        SharedPreferences.Editor editor = getSharedPreferences(MyInfoActivity.FILE_NAME,
                MODE_PRIVATE).edit();
        editor.putString(MyInfoActivity.USERNAME, username);
        editor.putString(MyInfoActivity.NAME, name);
        editor.commit();
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
}
