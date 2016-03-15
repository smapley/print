package com.smapley.print.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.smapley.print.R;
import com.smapley.print.util.HttpUtils;
import com.smapley.print.util.MyData;

import java.util.HashMap;

/**
 * Created by smapley on 15/10/26.
 */
public class Login extends Activity {
    private EditText log_et_username;
    private EditText log_et_password;
    private EditText log_et_ming;

    private String log_st_usernmae;
    private String log_st_password;
    private String log_st_ming;

    private boolean isLogin = false;
    private SharedPreferences sp_user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        initData();
        if (isLogin) {
            startActivity(new Intent(Login.this, MainActivity.class));
            finish();
        }
        initParams();


    }

    private void initData() {
        sp_user = getSharedPreferences("user", MODE_PRIVATE);

        isLogin = sp_user.getBoolean("login", false);
        MyData.Login = isLogin;

        log_st_usernmae = sp_user.getString("username", "");
        log_st_password = sp_user.getString("password", "");
        log_st_ming = sp_user.getString("ming", "");
        MyData.UserName = log_st_usernmae;
        MyData.PassWord = log_st_password;
        MyData.Ming = log_st_ming;
    }

    protected void initParams() {


        log_et_username = (EditText) findViewById(R.id.log_et_username1);
        log_et_password = (EditText) findViewById(R.id.log_et_password1);
        log_et_ming = (EditText) findViewById(R.id.log_et_ming);
        log_et_ming.setText(log_st_ming);
        log_et_password.setText(log_st_password);
        log_et_username.setText(log_st_usernmae);
    }


    private void doLogin() {


        new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("zhang", log_st_usernmae);
                map.put("mi", log_st_password);
                map.put("ming", log_st_ming);
                mhandler.obtainMessage(1, HttpUtils.updata(map, MyData.URL_reg)).sendToTarget();
            }
        }).start();
    }

    public Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                switch (msg.what) {
                    case 1:
                        int result = JSON.parseObject(msg.obj.toString(), new TypeReference<Integer>() {
                        });
                        if (result > 0) {
                            SharedPreferences.Editor editor = sp_user.edit();
                            editor.putString("username", log_st_usernmae);
                            editor.putString("password", log_st_password);
                            editor.putString("ming", log_st_ming);
                            editor.putBoolean("login", true);
                            editor.commit();
                            MyData.UserName = log_st_usernmae;
                            MyData.PassWord = log_st_password;
                            MyData.Ming = log_st_ming;
                            MyData.Login = true;
                            startActivity(new Intent(Login.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(Login.this, "登陆失败！", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(Login.this, "网络连接失败！", Toast.LENGTH_SHORT).show();

            }
        }
    };


    public void checkLogin(View view) {
        log_st_usernmae = log_et_username.getText().toString();
        log_st_password = log_et_password.getText().toString();
        log_st_ming = log_et_ming.getText().toString();
        if (log_st_usernmae != null && !log_st_usernmae.equals("")) {
            if (log_st_password != null && !log_st_password.equals("")) {
                doLogin();
            } else {
                Toast.makeText(Login.this, "用户名不能为空！", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(Login.this, "密码不能为空！", Toast.LENGTH_SHORT).show();

        }
    }
}
