package com.example.finalproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class enroll extends AppCompatActivity {

    private EditText editText_register_account;
    private EditText editText_register_password;
    private String register_account;
    private String register_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll);
        Button button_register = (Button) findViewById(R.id.enroll_register);
        Button button_back = (Button) findViewById(R.id.enroll_back);
        editText_register_account = (EditText) findViewById(R.id.register_account);
        editText_register_password = (EditText) findViewById(R.id.register_password);
        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register_account = editText_register_account.getText().toString();
                register_password = editText_register_password.getText().toString();
                if (register_account.length() < 4) {
                    Toast.makeText(enroll.this, "please keep username length more than 4 words", Toast.LENGTH_SHORT).show();
                } else if (register_password.length() < 6){
                    Toast.makeText(enroll.this, "please keep password length more than 6 words", Toast.LENGTH_SHORT).show();
                }else{
                    Log.i("log_tag", "enroll");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                int result = register();
                                Log.d("My Result:", String.valueOf(result));
                            } catch (IOException e) {
                                System.out.println(e.getMessage());}
                        }
                    }).start();

                    // close this activity and return to the login demo while uploading the registration information
                    Intent intent = new Intent("com.example.broadcastbestpractice.return");
                    sendBroadcast(intent);
                }

            }
            private int register() throws IOException {
                int returnResult=0;
                Log.i("log_tag", "密码上传");
                String urlstr="http://192.168.0.235:8888/demo/register/index.php";
                //建立网络连接
                URL url = new URL(urlstr);

                HttpURLConnection http= (HttpURLConnection) url.openConnection();
                //往网页写入POST数据，和网页POST方法类似，参数间用‘&’连接
                String params="uid="+register_account+'&'+"pwd="+register_password;
                http.setDoOutput(true);
                http.setRequestMethod("POST");
                OutputStream out=http.getOutputStream();
                out.write(params.getBytes());//post提交参数
                out.flush();
                out.close();
                Log.i("log_tag", "获得输入流");
                //读取网页返回的数据
                //http.setDoInput(true);
                BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(http.getInputStream()));//获得输入流
                Log.i("log_tag", "获得输入流成功");
                String line="";
                StringBuilder sb=new StringBuilder();//建立输入缓冲区
                while (null!=(line=bufferedReader.readLine())){//结束会读入一个null值
                    sb.append(line);//写缓冲区
                }
                Log.d("My Result:", sb.toString());
                String result= sb.toString();//返回结果

//                try {
//                    /*获取服务器返回的JSON数据*/
//                    JSONObject jsonObject= new JSONObject(result);
//                    returnResult=jsonObject.getInt("status");//获取JSON数据中status字段值
//                } catch (Exception e) {
//                    // TODO: handle exception
//                    Log.e("log_tag", "the Error parsing data "+e.toString());
//                }
                Log.d("return is:", result);
                returnResult = 1;
                return returnResult;
            }

        });

        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(enroll.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}