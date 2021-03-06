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
                Log.i("log_tag", "????????????");
                String urlstr="http://192.168.0.235:8888/demo/register/index.php";
                //??????????????????
                URL url = new URL(urlstr);

                HttpURLConnection http= (HttpURLConnection) url.openConnection();
                //???????????????POST??????????????????POST??????????????????????????????&?????????
                String params="uid="+register_account+'&'+"pwd="+register_password;
                http.setDoOutput(true);
                http.setRequestMethod("POST");
                OutputStream out=http.getOutputStream();
                out.write(params.getBytes());//post????????????
                out.flush();
                out.close();
                Log.i("log_tag", "???????????????");
                //???????????????????????????
                //http.setDoInput(true);
                BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(http.getInputStream()));//???????????????
                Log.i("log_tag", "?????????????????????");
                String line="";
                StringBuilder sb=new StringBuilder();//?????????????????????
                while (null!=(line=bufferedReader.readLine())){//?????????????????????null???
                    sb.append(line);//????????????
                }
                Log.d("My Result:", sb.toString());
                String result= sb.toString();//????????????

//                try {
//                    /*????????????????????????JSON??????*/
//                    JSONObject jsonObject= new JSONObject(result);
//                    returnResult=jsonObject.getInt("status");//??????JSON?????????status?????????
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