package just.activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cwp.android.baidutest.MainActivity;
import com.cwp.android.baidutest.MyApplication;
import com.cwp.android.baidutest.R;

import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.RequestSMSCodeListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.VerifySMSCodeListener;
import just.beans.MyUser;
import just.utils.ToastUtil;

public class RegisterActivity extends AppCompatActivity {
    private ImageView mIvStep1,mIvStep2,mIvStep3;
    private TextView mTvPrompt1,mTvPrompt2,mTvPrompt3;

    private TextView mTvStep1,mTvStep2,mTvStep3;
    private LinearLayout mLlStep1,mLlStep2,mLlStep3;

    private EditText mEtStep1,mEtStep2,mEtStep3_1,mEtStep3_2,mEtStep3_3;
    private ImageButton mBtStep1,mBtStep2,mBtStep3;

    private Button mBtStepAgain;
    private boolean canClick=false;
    private boolean isReset=false;
    private boolean isContinue=true;

    private static final int REQUEST_SEND =1;
    private static final int SUCCEED_SEND=2;
    private static final int FAILED_SEND=3;

    private static final int START_VALIDATE=4;
    private static final int SUCCEED_VALIDATE=5;
    private static final int FAILED_VALIDATE=6;

    private static final int START_REGISTER =7;
    private static final int SUCCEED_REGISTER=8;
    private static final int FAILED_REGISTER=9;

    private static final int REFRESH_COUNTDOWN=10;
    private static final int AGAIN_REQUEST=11;
    private static final int FINISHED_COUNTDOWN=12;

    private VerCodeMessageReceiver messageReceiver;
    private IntentFilter receiveFilter;

    private Handler mHandler=new Handler() {
        static final int STEP1=1;
        static final int STEP2=2;
        static final int STEP3=3;
        static final int FINISHED=4;

        ProgressDialog progressDialog;

        @Override
        public void handleMessage(Message msg) {
            if(progressDialog==null) {
                progressDialog=new ProgressDialog(RegisterActivity.this);
            }

            switch (msg.what) {
                case REQUEST_SEND:
                    showProgressDialog("第一步","正在请求短信验证码...");
                    break;
                case SUCCEED_SEND:
                    setNextStepView(STEP2);
                    break;
                case FAILED_SEND:
                    dealFailedResult(STEP1);
                    break;

                case START_VALIDATE:
                    showProgressDialog("第二步","正在验证...");
                    break;
                case SUCCEED_VALIDATE:
                    setNextStepView(STEP3);
                    break;
                case FAILED_VALIDATE:
                    dealFailedResult(STEP2);
                    break;

                case START_REGISTER:
                    showProgressDialog("第三步","正在注册...");
                    break;
                case SUCCEED_REGISTER:
                    setNextStepView(FINISHED);
                    break;
                case FAILED_REGISTER:
                    dealFailedResult(STEP3);
                    break;

                case REFRESH_COUNTDOWN:
                    int i= (int) msg.obj;
                    mBtStepAgain.setText(i+"s");
                    break;
                case AGAIN_REQUEST:
                    showProgressDialog("重新请求验证码","正在重新请求短信验证码...");
                    break;
                case FINISHED_COUNTDOWN:
                    mBtStepAgain.setTextColor(getResources().getColor(R.color.step_again));
                    mBtStepAgain.setText("重发");
                    break;
                default:break;
            }
        }

        private void showProgressDialog(String title,String message) {
            progressDialog.setTitle(title);
            progressDialog.setMessage(message);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        private void setNextStepView(int nextStep) {
            mBtStep2.setTag("未获取");

            if(nextStep==STEP2) {
                new Thread(()-> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    int i=60;
                    while (i>=1&&!isReset&&isContinue) {
                        try {
                            Thread.sleep(950);
                            Message message=Message.obtain();
                            message.what=REFRESH_COUNTDOWN;
                            message.obj=i;
                            mHandler.sendMessage(message);
                            i--;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    //倒计时完成
                    mHandler.sendEmptyMessage(FINISHED_COUNTDOWN);
                    isReset=false;
                    isContinue=true;
                    canClick=true;
                }).start();

                ToastUtil.showOrdinaryToast("请求验证码成功",RegisterActivity.this);

                mTvStep1.setVisibility(View.VISIBLE);
                mLlStep1.setVisibility(View.INVISIBLE);

                mIvStep1.setImageResource(R.drawable.ic_register_step1_unfocus);
                mIvStep2.setImageResource(R.drawable.ic_register_step2_focus);

                mTvPrompt1.setText("设置完成");
                mTvPrompt2.setTextColor(getResources().getColor(R.color.step_focus));

                mTvStep2.setVisibility(View.INVISIBLE);
                mLlStep2.setVisibility(View.VISIBLE);

                canClick=false;
                mBtStepAgain.setTextColor(getResources().getColor(R.color.colorWhite));
            } else if(nextStep==STEP3) {
                mTvStep2.setTag("已获取");

                isContinue=false;

                mTvStep2.setVisibility(View.VISIBLE);
                mLlStep2.setVisibility(View.INVISIBLE);

                mIvStep2.setImageResource(R.drawable.ic_register_step2_unfocus);
                mIvStep3.setImageResource(R.drawable.ic_register_step3_focus);

                mTvPrompt2.setText("验证成功");
                mTvPrompt3.setTextColor(getResources().getColor(R.color.step_focus));

                mTvStep3.setVisibility(View.INVISIBLE);
                mLlStep3.setVisibility(View.VISIBLE);
            } else if(nextStep==FINISHED) {
                ToastUtil.showOrdinaryToast("注册成功",RegisterActivity.this);

                mTvStep3.setVisibility(View.VISIBLE);
                mLlStep3.setVisibility(View.INVISIBLE);

                mTvPrompt3.setText("注册成功");

                try {
                    Thread.sleep(2000);
                    resetAll();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Intent intent=new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
            progressDialog.dismiss();
        }

        private void dealFailedResult(int currentStep) {
            if(currentStep==STEP1) {

            } else if(currentStep==STEP2) {

            } else if(currentStep==STEP3) {

            }
            progressDialog.dismiss();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ActivityCollector.addActivity(this);

        init();

        receiveFilter = new IntentFilter();
        receiveFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        messageReceiver = new VerCodeMessageReceiver();
        registerReceiver(messageReceiver, receiveFilter);
    }

    private void init() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("注册");
        actionBar.setDisplayHomeAsUpEnabled(true);

        mIvStep1= (ImageView) findViewById(R.id.id_iv_step1);
        mIvStep2= (ImageView) findViewById(R.id.id_iv_step2);
        mIvStep3= (ImageView) findViewById(R.id.id_iv_step3);

        mTvPrompt1= (TextView) findViewById(R.id.id_tv_prompt1);
        mTvPrompt2= (TextView) findViewById(R.id.id_tv_prompt2);
        mTvPrompt3= (TextView) findViewById(R.id.id_tv_prompt3);

        mTvStep1= (TextView) findViewById(R.id.id_tv_register_step1);
        mTvStep2= (TextView) findViewById(R.id.id_tv_register_step2);
        mTvStep3= (TextView) findViewById(R.id.id_tv_register_step3);

        mLlStep1= (LinearLayout) findViewById(R.id.id_ll_register_step1);
        mLlStep2= (LinearLayout) findViewById(R.id.id_ll_register_step2);
        mLlStep3= (LinearLayout) findViewById(R.id.id_ll_register_step3);

        mEtStep1= (EditText) findViewById(R.id.id_et_register_step1);
        mEtStep2= (EditText) findViewById(R.id.id_et_register_step2);
        mEtStep3_1= (EditText) findViewById(R.id.id_et_register_step3_1);
        mEtStep3_2= (EditText) findViewById(R.id.id_et_register_step3_2);
        mEtStep3_3= (EditText) findViewById(R.id.id_et_register_step3_3);

        mBtStep1= (ImageButton) findViewById(R.id.id_bt_register_step1);
        assert mBtStep1 != null;
        mBtStep1.setOnClickListener(v -> {
            String number=mEtStep1.getText().toString();
            if (!TextUtils.isEmpty(number)&&number.length()==11) {
                new Thread(()-> {
                    mHandler.sendEmptyMessage(REQUEST_SEND);

                    BmobSMS.requestSMSCode(RegisterActivity.this, number, "test",new RequestSMSCodeListener() {

                        @Override
                        public void done(Integer smsId,BmobException ex) {
                            if(ex==null){//验证码发送成功
                                Log.d("测试->RegisterActivity","请求验证码成功");
                                ToastUtil.showOrdinaryToast("验证码请求成功",RegisterActivity.this);
                                mHandler.sendEmptyMessage(SUCCEED_SEND);
                            }
                            else {
                                Log.d("测试->RegisterActivity","请求验证码失败:code ="+ex.getErrorCode()+",msg = "+ex.getLocalizedMessage());
                                ToastUtil.showToastForErrorCode(ex.getErrorCode(),RegisterActivity.this);
                                mHandler.sendEmptyMessage(FAILED_SEND);
                            }
                        }
                    });

//                    try {
//                        Thread.sleep(3000);
//                    } catch (InterruptedException e) {
//                    }
//                    mHandler.sendEmptyMessage(SUCCEED_SEND);
                }).start();
            }
            else {
                Log.d("测试->RegisterActivity","请输入合法的手机号码");
                ToastUtil.showOrdinaryToast("请输入合法的手机号码",this);
            }
        });

        mBtStep2= (ImageButton) findViewById(R.id.id_bt_register_step2);
        assert mBtStep2 != null;
        mTvStep2.setTag("未获取");
        mBtStep2.setOnClickListener(v -> {
            String code=mEtStep2.getText().toString();
            if(!TextUtils.isEmpty(code)) {
                if (code.length()==6) {
                    new Thread(()->{
                        mHandler.sendEmptyMessage(START_VALIDATE);
                        BmobSMS.verifySmsCode(RegisterActivity.this,mEtStep1.getText().toString(), code, new VerifySMSCodeListener() {
                            @Override
                            public void done(BmobException ex) {
                                if(ex==null){//短信验证码已验证成功
                                    Log.i("测试->RegisterActivity", "验证通过");
                                    ToastUtil.showOrdinaryToast("验证通过",RegisterActivity.this);
                                    mHandler.sendEmptyMessage(SUCCEED_VALIDATE);
                                }else{
                                    Log.i("测试->RegisterActivity", "验证失败：code ="+ex.getErrorCode()+",msg = "+ex.getLocalizedMessage());
                                    ToastUtil.showToastForErrorCode(ex.getErrorCode(),RegisterActivity.this);
                                    mHandler.sendEmptyMessage(FAILED_VALIDATE);
                                }
                            }
                        });

//                        try {
//                            Thread.sleep(3000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        mHandler.sendEmptyMessage(SUCCEED_VALIDATE);
                    }).start();
                }else {
                    Log.i("测试->RegisterActivity", "验证码格式错误");
                    ToastUtil.showOrdinaryToast("验证码需为6位数",RegisterActivity.this);
                }
            }
        });

        mBtStepAgain= (Button) findViewById(R.id.id_bt_register_step2_again);
        mBtStepAgain.setOnClickListener(v -> {
            if (canClick) {
                new Thread(()-> {
                    mHandler.sendEmptyMessage(AGAIN_REQUEST);

                    BmobSMS.requestSMSCode(RegisterActivity.this, mEtStep1.getText().toString(), "test",new RequestSMSCodeListener() {

                        @Override
                        public void done(Integer smsId,BmobException ex) {
                            if(ex==null){//验证码发送成功
                                Log.d("测试->RegisterActivity","请求验证码成功");
                                ToastUtil.showOrdinaryToast("验证码请求成功",RegisterActivity.this);
                                mHandler.sendEmptyMessage(SUCCEED_SEND);
                            }
                            else {
                                Log.d("测试->RegisterActivity","请求验证码失败:code ="+ex.getErrorCode()+",msg = "+ex.getLocalizedMessage());
                                ToastUtil.showToastForErrorCode(ex.getErrorCode(),RegisterActivity.this);
                                mHandler.sendEmptyMessage(FAILED_SEND);
                            }
                        }
                    });
                }).start();
            }
        });

        mBtStep3= (ImageButton) findViewById(R.id.id_bt_register_step3);
        assert mBtStep3 != null;
        mBtStep3.setOnClickListener(v -> {
            String password1=mEtStep3_1.getText().toString();
            String password2=mEtStep3_2.getText().toString();
            String name=mEtStep3_3.getText().toString().trim();
            if (!TextUtils.isEmpty(password1)&& !TextUtils.isEmpty(password2)&&!TextUtils.isEmpty(name)) {
                if(!password1.equals(password2)) {
                    Log.d("测试->RegisterActivity","两次密码不一致");
                    ToastUtil.showOrdinaryToast("两次密码不一致",RegisterActivity.this);
                    return;
                }
                else if(password1.length()<6) {
                    Log.d("测试->RegisterActivity","密码长度不能小于6");
                    ToastUtil.showOrdinaryToast("密码长度不能小于6",RegisterActivity.this);
                    return;
                }
                new Thread(()-> {
                    mHandler.sendEmptyMessage(START_REGISTER);

                    MyUser user=new MyUser();
                    user.setUsername(mEtStep1.getText().toString());
                    Log.d("测试->RegisterActivity",mEtStep1.getText().toString());
                    user.setPassword(password1);
                    user.setName(name);
                    user.signUp(RegisterActivity.this, new SaveListener() {
                        @Override
                        public void onSuccess() {
                            Log.i("测试->RegisterActivity", "注册成功");
                            ToastUtil.showOrdinaryToast("注册成功",RegisterActivity.this);

                            MyApplication.saveLoginInfo(mEtStep1.getText().toString(),name);

                            mHandler.sendEmptyMessage(SUCCEED_REGISTER);
                        }

                        @Override
                        public void onFailure(int i, String s) {
                            Log.i("测试->RegisterActivity", "注册失败:i="+i+",s="+s);
                            ToastUtil.showToastForErrorCode(i,RegisterActivity.this);
                            mHandler.sendEmptyMessage(FAILED_REGISTER);
                        }
                    });

//                    try {
//                        Thread.sleep(3000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    mHandler.sendEmptyMessage(SUCCEED_REGISTER);
                }).start();
            }
            else {
                Log.d("测试->RegisterActivity","所填信息不能为空");
                ToastUtil.showOrdinaryToast("所填信息不能为空",this);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.id_item_menu_reset:
                resetAll();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    class VerCodeMessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("测试->RegisterActivity","截获信息，mBtStep2.getTag()="+mBtStep2.getTag());
            if ("未获取".equals(mBtStep2.getTag())) {
                Bundle bundle = intent.getExtras();
                Object[] pdus = (Object[]) bundle.get("pdus"); // 提取短信消息
                SmsMessage[] messages = new SmsMessage[pdus.length];
                for (int i = 0; i < messages.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }
                StringBuilder fullMessage = new StringBuilder();
                for (SmsMessage message : messages) {
                    fullMessage.append(message.getMessageBody()); // 获取短信内容
                }
                if("比目科技".equals(fullMessage.substring(1,5))) {
                    Log.d("测试->RegisterActivity","自动获取验证码:"+fullMessage.substring(12,18));
                    mBtStep2.setTag("已获取");
                    mEtStep2.setText(fullMessage.substring(12,18));
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
        registerReceiver(messageReceiver, receiveFilter);
    }

    private void resetAll() {
        mEtStep1.setText("");
        mTvPrompt1.setText("设置账号");
        mTvPrompt1.setTextColor(getResources().getColor(R.color.step_focus));
        mIvStep1.setImageResource(R.drawable.ic_register_step1_focus);
        mTvStep1.setVisibility(View.INVISIBLE);
        mLlStep1.setVisibility(View.VISIBLE);

        mEtStep2.setText("");
        mEtStep2.setTag("未获取");
        mTvPrompt2.setText("验证手机号码");
        mTvPrompt2.setTextColor(getResources().getColor(R.color.step_unfocus));
        mIvStep2.setImageResource(R.drawable.ic_register_step2_unfocus);
        mTvStep2.setVisibility(View.VISIBLE);
        mLlStep2.setVisibility(View.INVISIBLE);

        mEtStep3_1.setText("");
        mEtStep3_2.setText("");
        mTvPrompt3.setText("设置密码");
        mTvPrompt3.setTextColor(getResources().getColor(R.color.step_unfocus));
        mIvStep3.setImageResource(R.drawable.ic_register_step3_unfocus);
        mTvStep3.setVisibility(View.VISIBLE);
        mLlStep3.setVisibility(View.INVISIBLE);

        isReset=true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_register_reset,menu);
        return true;
    }

}
