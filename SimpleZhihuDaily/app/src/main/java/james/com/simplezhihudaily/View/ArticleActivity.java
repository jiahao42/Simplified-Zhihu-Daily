package james.com.simplezhihudaily.View;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.WebView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import james.com.simplezhihudaily.Model.Symbol;
import james.com.simplezhihudaily.R;


public class ArticleActivity extends Activity {
    private Intent intent;
    private int id;
    private RequestQueue mQueue;
    private WebView article;
    private String url = "http://news-at.zhihu.com/api/4/news/";
    private String cssUrl;
    private String htmlString;
    private Document document;
    private String body;
    private ArticleActivity articleActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        initWidget();
        get_article();
    }
    private void get_article(){
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message message){
                if (message.what == Symbol.RECEIVE_SUCCESS){
                    document = Jsoup.parse(htmlString,"UTF-8");
                    body = document.body().toString();
                    article.loadData(body,"text/html; charset=UTF-8", null);
                    Log.d("body",body);
                }
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url + id, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try
                        {
                            htmlString = response.getString("body");
                            cssUrl = response.getString("css");
                            Message message = new Message();
                            message.what = Symbol.RECEIVE_SUCCESS;
                            handler.sendMessage(message);
                        } catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
                mQueue.add(jsonObjectRequest);
                mQueue.start();
            }

        }).start();
    }
    private void initWidget(){
        article = (WebView)findViewById(R.id.article);
        articleActivity = this;
        mQueue = Volley.newRequestQueue(articleActivity);
        intent = getIntent();
        Bundle bundle = intent.getBundleExtra("id");
        id = bundle.getInt("id");
    }
}
