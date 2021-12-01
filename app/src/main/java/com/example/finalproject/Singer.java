package com.example.finalproject;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

public class Singer extends AppCompatActivity {
    //模型地址、facenet类、要比较的两张图片
    public static String model_path="file:///android_asset/20180402-114759.pb";
    public Facenet facenet;
    public Bitmap bitmap1;
    public Bitmap bitmap2;
    //图片显示的空间
    //public ImageView imageView1;
    public ImageView imageView2;
    //
    public MTCNN mtcnn;
    public TextView textView3;
    public EditText editText1;
    public EditText editText2;
    public EditText editText3;

    public String singer = null;


    //从assets中读取图片
    private  Bitmap readFromAssets(String filename){
        Bitmap bitmap;
        AssetManager asm=getAssets();
        try {
            InputStream is=asm.open(filename);
            bitmap= BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            Log.e("Singer","[*]failed to open "+filename);
            e.printStackTrace();
            return null;
        }
        return Utils.copyBitmap(bitmap);
    }
    public void textviewLog(String msg){
        TextView textView=(TextView)findViewById(R.id.textView);
        textView.append("\n"+msg);
    }
    public void showScore(double score,long time){
        TextView textView=(TextView)findViewById(R.id.textView2);
        textView.setText("[*]Facial recognition Time:"+time+"\n");
        if (score<=0){
            if (score<-1.5)textView.append("[*]图二检测不到人脸");
            else textView.append("[*]图一检测不到人脸");
        }else{
            textView.append("[*]Similarity:"+score+" [When score < 1.1, the similarity is considerable]");
        }
    }

    public String get_music(String singer) throws IOException{
        StringBuilder writer = new StringBuilder();
        String returnResult = null;
        Log.i("log_tag", "指令上传");
        String urlstr="http://192.168.0.235:8888/demo/singer/index.php";
        //建立网络连接
        URL url = new URL(urlstr);
        HttpURLConnection http= (HttpURLConnection) url.openConnection();
        String params="singer="+singer;
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

    //比较bitmap1和bitmap2(会先切割人脸在比较)
    public double compareFaces(){
        //(1)圈出人脸，人脸检测(可能会有多个人脸)
        /*安卓自带人脸检测实现
        Rect rect1 = FaceDetect.detectBiggestFace(bitmap1);
        if (rect1==null) return -1;
        Rect rect2 = FaceDetect.detectBiggestFace(bitmap2);
        if (rect2==null) return -2;*/
        Bitmap bm1=Utils.copyBitmap(bitmap1);
        Bitmap bm2=Utils.copyBitmap(bitmap2);
        Vector<Box> boxes=mtcnn.detectFaces(bitmap1,40);
        Vector<Box> boxes1=mtcnn.detectFaces(bitmap2,40);
        if (boxes.size()==0) return -1;
        if (boxes1.size()==0)return -2;
        for (int i=0;i<boxes.size();i++) Utils.drawBox(bitmap1,boxes.get(i),1+bitmap1.getWidth()/500 );
        for (int i=0;i<boxes1.size();i++) Utils.drawBox(bitmap2,boxes1.get(i),1+bitmap2.getWidth()/500 );
        Log.i("Main","[*]boxNum"+boxes1.size());
        Rect rect1=boxes.get(0).transform2Rect();
        Rect rect2=boxes1.get(0).transform2Rect();
        //MTCNN检测到的人脸框，再上下左右扩展margin个像素点，再放入facenet中。
        int margin=20; //20这个值是facenet中设置的。自己应该可以调整。
        Utils.rectExtend(bitmap1,rect1,margin);
        Utils.rectExtend(bitmap2,rect2,margin);
        //要比较的两个人脸，加厚Rect
        Utils.drawRect(bitmap1,rect1,1+bitmap1.getWidth()/100 );
        Utils.drawRect(bitmap2,rect2,1+bitmap2.getWidth()/100 );
        //(2)裁剪出人脸(只取第一张)
        Bitmap face1=Utils.crop(bitmap1,rect1);
        Bitmap face2=Utils.crop(bitmap2,rect2);
        //(显示人脸)
        //imageView1.setImageBitmap(bitmap1);
        imageView2.setImageBitmap(bitmap2);
        //(3)特征提取
        FaceFeature ff1=facenet.recognizeImage(face1);
        FaceFeature ff2=facenet.recognizeImage(face2);
        bitmap1=bm1;
        bitmap2=bm2;
        //(4)比较
        return ff1.compare(ff2);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singer);
        textView3 = (TextView)findViewById(R.id.textView3);
        editText1 = (EditText)findViewById(R.id.music1);
        editText2 = (EditText)findViewById(R.id.music2);
        editText3 = (EditText)findViewById(R.id.music3);
        mtcnn=new MTCNN(getAssets());
        //imageView1=(ImageView)findViewById(R.id.imageView);
        imageView2=(ImageView)findViewById(R.id.imageView2);
        //载入facenet
        long t_start=System.currentTimeMillis();
        facenet=new Facenet(getAssets());
        long t2=System.currentTimeMillis();
        //textviewLog("[*]模型载入成功,Time[ms]:"+(t2-t_start));
        //先从assets中读取图片
        bitmap1=readFromAssets("justin1.jpg");
        bitmap2=readFromAssets("trump2.jpg");
//        long t1=System.currentTimeMillis();
//        double score=compareFaces();
//        showScore(score,System.currentTimeMillis()-t1);
        //Log.d("MainActivity","[*] end,score="+score);


        //以下是控件事件绑定之类；添加自己上传图片的功能

//        imageView1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d("Singer","[*]you click me ");
//                Intent intent= new Intent(Intent.ACTION_PICK,null);
//                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
//                startActivityForResult(intent, 0x1);
//            }
//        });

        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(Intent.ACTION_PICK,null);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
                startActivityForResult(intent, 0x2);
            }
        });
        Button btn=(Button)findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long t1=System.currentTimeMillis();
                bitmap1=readFromAssets("justin1.jpg");
                double score=compareFaces();
                if(score < 1){
                    textView3.setText("Singer: Justin");
                    showScore(score,System.currentTimeMillis()-t1);
                    singer = "Justin";
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String result = get_music(singer);
                                result = result.replace("[", "");
                                result = result.replace("]", "");
                                String[] final_result = result.split(",");
                                int result_size = final_result.length;
                                Log.d("The size is:", String.valueOf(result_size));

                                editText1.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        editText1.setText(final_result[0]);
                                    }
                                });
                                editText2.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        editText2.setText(final_result[1]);
                                    }
                                });
                                editText3.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        editText3.setText(final_result[2]);
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }


                t1=System.currentTimeMillis();
                bitmap1=readFromAssets("adam1.jpg");
                score=compareFaces();
                if(score < 1){
                    textView3.setText("Singer: Adam");
                    showScore(score,System.currentTimeMillis()-t1);
                    singer = "Adam";
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String result = get_music(singer);
                                result = result.replace("[", "");
                                result = result.replace("]", "");
                                String[] final_result = result.split(",");
                                int result_size = final_result.length;
                                Log.d("The size is:", String.valueOf(result_size));

                                editText1.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        editText1.setText(final_result[0]);
                                    }
                                });
                                editText2.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        editText2.setText(final_result[1]);
                                    }
                                });
                                editText3.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        editText3.setText(final_result[2]);
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }


                t1=System.currentTimeMillis();
                bitmap1=readFromAssets("adele1.jpg");
                score=compareFaces();
                if(score < 1){
                    textView3.setText("Singer: Adele");
                    showScore(score,System.currentTimeMillis()-t1);
                    singer = "Adele";
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String result = get_music(singer);
                                result = result.replace("[", "");
                                result = result.replace("]", "");
                                String[] final_result = result.split(",");
                                int result_size = final_result.length;
                                Log.d("The size is:", String.valueOf(result_size));

                                editText1.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        editText1.setText(final_result[0]);
                                    }
                                });
                                editText2.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        editText2.setText(final_result[1]);
                                    }
                                });
                                editText3.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        editText3.setText(final_result[2]);
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }


                t1=System.currentTimeMillis();
                bitmap1=readFromAssets("maroon1.jpg");
                score=compareFaces();
                if(score < 1){
                    textView3.setText("Singer: Maroon5");
                    showScore(score,System.currentTimeMillis()-t1);
                    singer = "Maroon5";
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String result = get_music(singer);
                                result = result.replace("[", "");
                                result = result.replace("]", "");
                                String[] final_result = result.split(",");
                                int result_size = final_result.length;
                                Log.d("The size is:", String.valueOf(result_size));

                                editText1.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        editText1.setText(final_result[0]);
                                    }
                                });
                                editText2.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        editText2.setText(final_result[1]);
                                    }
                                });
                                editText3.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        editText3.setText(final_result[2]);
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if(data==null)return;
        try {
            Bitmap bm = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
            if (bm.getWidth()>1000)  bm=Utils.resize(bm,1000);
            if (requestCode == 0x1 && resultCode == RESULT_OK) {
                //imageView1.setImageURI(data.getData());
                bitmap1=Utils.copyBitmap(bm);
                //imageView1.setImageBitmap(bitmap1);
            }else {
                //imageView2.setImageURI(data.getData());
                bitmap2=Utils.copyBitmap(bm);
                imageView2.setImageBitmap(bitmap2);
            }
        }catch (Exception e){
            Log.d("MainActivity","[*]"+e);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}