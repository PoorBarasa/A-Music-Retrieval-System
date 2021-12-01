package com.example.finalproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class FunctionWindow extends AppCompatActivity {

    private String button_rock = null;
    private String button_classical = null;
    private String button_metal = null;
    private String button_blue = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_function_window);
        Random random = new Random();
        CheckBox checkBox_rock = (CheckBox)findViewById(R.id.button_rock);
        CheckBox checkBox_classical = (CheckBox)findViewById(R.id.button_classical);
        CheckBox checkBox_metal = (CheckBox)findViewById(R.id.button_metal);
        CheckBox checkBox_blue = (CheckBox)findViewById(R.id.button_blue);
        EditText editText_1 = (EditText)findViewById(R.id.text_view1);
        EditText editText_2 = (EditText)findViewById(R.id.text_view2);
        Button button_get_music = (Button)findViewById(R.id.button_get_music);
        Button button_singer = (Button)findViewById(R.id.button_singer);

        button_singer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FunctionWindow.this, Singer.class);
                startActivity(intent);
                finish();
            }
        });

        button_get_music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkBox_rock.isChecked())
                {
                    button_rock = "rock";
                }
                if (checkBox_classical.isChecked())
                {
                    button_classical = "classical";
                }
                if (checkBox_metal.isChecked())
                {
                    button_metal = "metal";
                }
                if (checkBox_blue.isChecked())
                {
                    button_blue = "blue";
                }

                Log.i("log_tag", "按键：登录");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String result = get_music();
                            result = result.replace("[", "");
                            result = result.replace("]", "");
                            String[] final_result = result.split(",");
                            int result_size = final_result.length;
                            Log.d("The size is:", String.valueOf(result_size));
                            int random_music = random.nextInt(result_size);
                            editText_1.post(new Runnable() {
                                @Override
                                public void run() {
                                    editText_1.setText(final_result[random_music]);
                                }
                            });
                            editText_2.post(new Runnable() {
                                @Override
                                public void run() {
                                    editText_2.setText(final_result[result_size-random_music]);
                                }
                            });
                            button_rock = null;
                            button_classical = null;
                            button_metal = null;
                            button_blue = null;

                        } catch (IOException e) {
                            System.out.println(e.getMessage());
                        }

                    }
                }).start();
            }
            private String get_music() throws IOException{
                StringBuilder writer = new StringBuilder();
                String returnResult = null;
                Log.i("log_tag", "指令上传");
                String urlstr="http://192.168.0.235:8888/demo/news/index.php";
                //建立网络连接
                URL url = new URL(urlstr);
                HttpURLConnection http= (HttpURLConnection) url.openConnection();
                String params="rock="+button_rock+'&'+"classical="+button_classical+'&'+"metal="+button_metal+'&'+"blue="+button_blue;
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

                try {

                    JSONObject jsonObject= new JSONObject(result);
                    returnResult = jsonObject.getString("music");
                    returnResult = returnResult.replace("\\","");

                }catch (Exception e){
                    // TODO: handle exception
                    Log.e("log_tag", "the Error parsing data "+e.toString());
                }
                Log.d("return is:", returnResult);
                return returnResult;
            }
        });

    }
}