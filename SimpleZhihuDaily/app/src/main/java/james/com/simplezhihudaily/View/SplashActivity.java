package james.com.simplezhihudaily.View;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import james.com.simplezhihudaily.Model.DeviceInfo;
import james.com.simplezhihudaily.Model.Symbol;
import james.com.simplezhihudaily.R;
import james.com.simplezhihudaily.Util.Util;


public class SplashActivity extends Activity {
    private final int SPLASH_DISPLAY_LENGHT = 6000; // 延迟六秒
    private ImageView splash_img;
    private RequestQueue mQueue;
    private final SplashActivity splashActivity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);
        splash_img = (ImageView) findViewById(R.id.splash_img);
        mQueue = Volley.newRequestQueue(splashActivity);

        new Handler().postDelayed(new Runnable() {
            public void run() {
                Intent mainIntent = new Intent(SplashActivity.this,
                        MainActivity.class);
                SplashActivity.this.startActivity(mainIntent);
                SplashActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGHT);
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                if (msg.what == Symbol.RECEIVE_SUCCESS){
                    Bundle bundle = msg.getData();
                    String url = bundle.getString("url");
                    Log.d("----URL----",url);
                    Toast.makeText(splashActivity,"开始获取图片",Toast.LENGTH_SHORT).show();
                    getPicture(url);
                }
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("http://news-at.zhihu.com/api/4/start-image/1080*1776", null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try
                        {
                            String imgUrl = response.getString("img");
                            Message msg = new Message();
                            msg.what = Symbol.RECEIVE_SUCCESS;
                            Bundle bundle = new Bundle();
                            bundle.putString("url",imgUrl);
                            Log.d("In Thread",imgUrl);
                            msg.setData(bundle);
                            handler.sendMessage(msg);

                        } catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Log.d("Fetching url","Failed");
                    }
                });
                mQueue.add(jsonObjectRequest);
                mQueue.start();
            }
        }).start();
    }
    private void getPicture(final String url){
        new Thread(new Runnable() {
            @Override
            public void run() {
                ImageRequest imageRequest = new ImageRequest(url, new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        splash_img.setImageBitmap(response);
                    }
                }, 0, 0, ImageView.ScaleType.CENTER_CROP,Bitmap.Config.RGB_565, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Fetching img","Failed");
                    }
                });
                mQueue.add(imageRequest);
                mQueue.start();
            }
        }).start();
    }
}

