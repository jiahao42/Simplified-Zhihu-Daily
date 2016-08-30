package james.com.simplezhihudaily.View;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import james.com.simplezhihudaily.Model.NewsAdapter;
import james.com.simplezhihudaily.Model.NewsInfo;
import james.com.simplezhihudaily.Model.Symbol;
import james.com.simplezhihudaily.R;

import static android.R.attr.id;


public class MainActivity extends AppCompatActivity {
    private List<NewsInfo> newsList = new ArrayList<>();
    private RequestQueue mQueue;
    private MainActivity mainActivity = this;
    private Gson gson = new Gson();
    private String[] picUrls;
    private NewsAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        adapter = new NewsAdapter(MainActivity.this, R.layout.news_item, newsList);
        ListView listView = (ListView) findViewById(R.id.title_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                NewsInfo newsInfo = newsList.get(position);
                Toast.makeText(MainActivity.this, newsInfo.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });
        get_news_url();
    }

    private void get_news_url() {
        final Handler get_url_array_handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                if (message.what == Symbol.RECEIVE_SUCCESS)
                {
                    picUrls = new String[newsList.size()];
                    Resources res = getResources();
                    Bitmap bitmap= BitmapFactory.decodeResource(res, R.drawable.error);
                    for (int i = 0; i < newsList.size(); i++)
                    {
                        Log.d("TAG",newsList.get(i).getUrls());
                        picUrls[i] = newsList.get(i).getUrls();
                        newsList.get(i).setBitmap(bitmap);
                    }
                    get_news_pics();
                }
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                mQueue = Volley.newRequestQueue(mainActivity);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("http://news-at.zhihu.com/api/4/news/latest",
                        null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try
                        {
                            Log.d("TAG", response.getJSONArray("stories").toString());
                            //获得所有日报的Json信息 注意是一个数组 将其转化成对象后 再通过bundle传递
                            NewsInfo[] newsInfo = gson.fromJson(response.getString("stories"), NewsInfo[].class);
                            for (int i = 0;i < newsInfo.length; i++){
                                Log.d("newsInfo",newsInfo[i].getTitle());
                            }
                            Collections.addAll(newsList, newsInfo);
                            Message message = new Message();
                            message.what = Symbol.RECEIVE_SUCCESS;
                            get_url_array_handler.sendMessage(message);
                        } catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("MainActivity", "server_error");
                    }
                });
                mQueue.add(jsonObjectRequest);
                mQueue.start();
            }
        }).start();
    }

    //获取新闻图片
    private void get_news_pics() {
        final Handler get_pics_handler = new Handler(){
            @Override
            public void handleMessage(Message message){
                if (message.what == Symbol.RECEIVE_SUCCESS){
                    adapter.notifyDataSetChanged();
                }
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < picUrls.length; i++)
                {
                    final int count = i;
                    ImageRequest imageRequest = new ImageRequest(picUrls[count], new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap response) {
                            newsList.get(count).setBitmap(response);
                            Message message = new Message();
                            Bundle bundle = new Bundle();
                            bundle.putInt("count",count);
                            message.setData(bundle);
                            message.what = Symbol.RECEIVE_SUCCESS;
                            get_pics_handler.sendMessage(message);
                            Log.d("imageRequest","received"+count);
                        }
                    }, 0, 0, ImageView.ScaleType.CENTER, Bitmap.Config.RGB_565, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("getPics", "server_error");
                        }
                    });
                    mQueue.add(imageRequest);
                    Log.d("imageRequest","added"+count);
                }
                mQueue.start();
            }
        }).start();
    }
}

