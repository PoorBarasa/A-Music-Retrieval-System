package com.example.finalproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private EditText editText_account;
    private  EditText editText_password;
    private Button button_login;
    private Button button_register;
    private String account;
    private String password;

    @Override
    protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText_account = (EditText) findViewById(R.id.edit_account);
        editText_password = (EditText) findViewById(R.id.edit_password);
        button_login = (Button) findViewById(R.id.button_login);
        button_register = (Button) findViewById(R.id.button_register);

        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.i("log_tag", "按键：登录");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            int result = login();
                            Log.i("log_tag", "正在登录");
                            //login()为向php服务器提交请求的函数，返回数据类型为int
                            if (result == 1) {
                                Log.e("log_tag", "登陆成功！");
                                //Toast toast=null;
                                //Looper.prepare();
                                Intent intent = new Intent(MainActivity.this, FunctionWindow.class);
                                startActivity(intent);
                                finish();
                                // Toast.makeText(MainActivity.this, "登陆成功！", Toast.LENGTH_SHORT).show();
                                //Looper.loop();
                            } else if (result == -2) {
                                Log.e("log_tag", "密码错误！");
                                //Toast toast=null;
                                Looper.prepare();
                                Toast.makeText(MainActivity.this, "密码错误！", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            } else if (result == -1) {
                                Log.e("log_tag", "不存在该用户！");
                                //Toast toast=null;
                                Looper.prepare();
                                Toast.makeText(MainActivity.this, "不存在该用户！", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                        } catch (IOException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }).start();
            }

            private int login() throws IOException {
                int returnResult = 0;
                /*获取用户名和密码*/
                String user_id = editText_account.getText().toString();
                String input_pwd = editText_password.getText().toString();
                if (user_id == null || user_id.length() <= 0) {
                    Looper.prepare();
                    Toast.makeText(MainActivity.this, "请输入账号", Toast.LENGTH_LONG).show();
                    Looper.loop();
                    return 0;

                }
                if (input_pwd == null || input_pwd.length() <= 0) {
                    Looper.prepare();
                    Toast.makeText(MainActivity.this, "请输入密码", Toast.LENGTH_LONG).show();
                    Looper.loop();
                    return 0;
                }
                Log.i("log_tag", "密码上传");
                String urlstr = "http://192.168.0.235:8888/demo/test/index.php";
                //建立网络连接
                URL url = new URL(urlstr);

                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                //往网页写入POST数据，和网页POST方法类似，参数间用‘&’连接
                String params = "uid=" + user_id + '&' + "pwd=" + input_pwd;
                Log.i("uid=", user_id);
                Log.i("pwd=", input_pwd);
                http.setDoOutput(true);
                http.setRequestMethod("POST");
                OutputStream out = http.getOutputStream();
                out.write(params.getBytes());//post提交参数
                out.flush();
                out.close();
                Log.i("log_tag", "获得输入流");
                //读取网页返回的数据
                //http.setDoInput(true);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(http.getInputStream()));//获得输入流
                Log.i("log_tag", "获得输入流成功");
                String line = "";
                StringBuilder sb = new StringBuilder();//建立输入缓冲区
                while (null != (line = bufferedReader.readLine())) {//结束会读入一个null值
                    sb.append(line);//写缓冲区
                }
                Log.d("My Result:", sb.toString());
                String result = sb.toString();//返回结果

                try {
                    /*获取服务器返回的JSON数据*/
                    JSONObject jsonObject = new JSONObject(result);
                    returnResult = jsonObject.getInt("status");//获取JSON数据中status字段值
                } catch (Exception e) {
                    // TODO: handle exception
                    Log.e("log_tag", "the Error parsing data " + e.toString());
                }
                Log.d("return is:", String.valueOf(returnResult));
                return returnResult;
            }
        });


        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, enroll.class);
                startActivity(intent);
                finish();
            }
        });

    }
}

