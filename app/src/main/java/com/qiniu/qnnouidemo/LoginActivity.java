package com.qiniu.qnnouidemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.qiniu.qnnouidemo.uitil.BZUser;
import com.qiniu.qnnouidemo.uitil.SpUtil;
import com.qlive.core.QLiveCallBack;
import com.qlive.jsonutil.JsonUtils;
import com.qlive.qnlivekit.uitil.OKHttpManger;
import com.qlive.qnlivekit.uitil.UserManager;
import com.qlive.sdk.QLive;
import com.qlive.sdk.QUserInfo;
import com.qlive.uikitcore.dialog.LoadingDialog;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;
import okio.Buffer;

import kotlin.Unit;
import okhttp3.FormBody;

/**
 * 登录。验证码登录时未注册手机号会自动注册
 */
public class LoginActivity extends AppCompatActivity {
    private EditText etLoginPhone,etLoginVerificationCode;
    private TextView cbAgreement,tvSmsTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        etLoginPhone=findViewById(R.id.et_login_phone);
        etLoginVerificationCode=findViewById(R.id.et_login_verification_code);
        cbAgreement=findViewById(R.id.cbAgreement);
        tvSmsTime=findViewById(R.id.tvSmsTime);
        init();
    }
    public void init() {
        initOtherView();
        String lastPhone = SpUtil.get("login").readString("phone", "");
        if (!TextUtils.isEmpty(lastPhone)) {
            etLoginPhone.setText(lastPhone);
            etLoginVerificationCode.setText("8888");
        }
    }

    private void  auth()   {
        Log.d("登录：","QLive.auth");
        QLive.auth(new QLiveCallBack<Void>() {
            @Override
            public void onError(int code, String msg) {
                Toast.makeText(LoginActivity.this, "2="+msg, Toast.LENGTH_SHORT).show();
                runOnUiThread(() -> LoadingDialog.INSTANCE.cancelLoadingDialog());
            }

            @Override
            public void onSuccess(Void unused) {
                Log.d("登录：","QLive.auth.onSuccess");
                //绑定用户信息
                suspendSetUser();
            }
        });
    }

    /**
     *  //绑定用户信息 绑定后房间在线用户能返回绑定设置的字段
     */
    private void suspendSetUser() {
        QUserInfo qUserInfo= new QUserInfo();
        qUserInfo.avatar=UserManager.INSTANCE.getUser().data.avatar;
        HashMap<String, String> map=new HashMap<String, String>();
        map.put("phone", phone);
        map.put("customFiled", "i am customFile");
        qUserInfo.extension=map;
        qUserInfo.nick=UserManager.INSTANCE.getUser().data.nickname;
        QLive.setUser(qUserInfo, new QLiveCallBack<Void>() {
            @Override
            public void onError(int i, String s) {
                runOnUiThread(() -> LoadingDialog.INSTANCE.cancelLoadingDialog());
                Toast.makeText(LoginActivity.this,"登录失败="+s,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(Void unused) {
                runOnUiThread(() -> LoadingDialog.INSTANCE.cancelLoadingDialog());
                SpUtil.get("login").saveData("phone", phone);
                Toast.makeText(LoginActivity.this,"登录成功=",Toast.LENGTH_SHORT).show();

                //启动跳转到直播列表
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            }
        });
    }
    private BZUser user;
    //demo自己的登陆
    private void login(String phoneNumber,String smsCode){
            new Thread(()-> {
        try {
            Log.d("登录：","login");
            FormBody body = new FormBody.Builder()
                    .add("phone", phoneNumber)
                    .add("smsCode", smsCode)
                    .build();
            Buffer buffer = new Buffer();
            body.writeTo(buffer);
            Request request = new Request.Builder()
                    .url(DemoApplication.demo_url+"/v1/signUpOrIn")
                    //  .addHeader(headerKey, headerValue)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .post(body)
                    .build();
            Call call = OKHttpManger.INSTANCE.getOkHttp().newCall(request);
            Response resp = call.execute();

            String userJson = resp.body().string();

            user = JsonUtils.INSTANCE.parseObject(userJson, BZUser.class);
            Log.e("==",user.code+"=登录="+userJson);
            if(user.code==0){
                UserManager.INSTANCE.onLogin(user);
                //登陆
                auth();
            }else{
                runOnUiThread(()->{
                    runOnUiThread(() -> LoadingDialog.INSTANCE.cancelLoadingDialog());
                    Toast.makeText(this,"登录失败="+user.message,Toast.LENGTH_SHORT).show();
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(()->{
                runOnUiThread(() -> LoadingDialog.INSTANCE.cancelLoadingDialog());
                Toast.makeText(this,"登录失败="+e.getMessage(),Toast.LENGTH_SHORT).show();
            });
        }
    }).start();
    }

    private void initOtherView() {

    }
    //同意协议
    public void onClickAgreement(View view){
        cbAgreement.setSelected(!cbAgreement.isSelected());
    }
    //发送验证码
    public void onClickSMS(View view){
        String phone = etLoginPhone.getText().toString();
        if (phone.isEmpty()) {
            return;
        }
        new Thread(()->{
            try {
                FormBody body = new FormBody.Builder()
                        .add("phone", phone)
                        .build();
                Buffer buffer =new Buffer();
                body.writeTo(buffer);
                Request request = new Request.Builder()
                        .url(DemoApplication.demo_url+"/v1/getSmsCode")
                        //  .addHeader(headerKey, headerValue)
                        .addHeader("Content-Type", "application/x-www-form-urlencoded")
                        .post(body)
                        .build();
                Call call = OKHttpManger.INSTANCE.getOkHttp().newCall(request);
                Response resp = call.execute();
                runOnUiThread(()->{
                    Toast.makeText(this,"发送成功",Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(()->{
                    Toast.makeText(this,"发送失败",Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    String phone="";
    //登陆按钮
    public void onClickLogin(View view){

        phone = etLoginPhone.getText().toString();
        String code = etLoginVerificationCode.getText().toString();
        if (phone.isEmpty()) {
            Toast.makeText(this, "请输入手机号", Toast.LENGTH_SHORT).show();
            return;
        }
        if (code.isEmpty()) {
            Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show();
            return;
        }

//        if (!cbAgreement.isSelected()) {
//            Toast.makeText(this, "请同意 七牛云服务用户协议 和 隐私权政策", Toast.LENGTH_SHORT).show()
//            return;
//        }
//            QLive.getLiveUIKit().getPage(RoomPage::class.java).anchorCustomLayoutID = R.layout.my_activity_room_pusher
        LoadingDialog.INSTANCE.showLoading(getSupportFragmentManager());
        //demo登陆
        login(phone, code);
    }
}
