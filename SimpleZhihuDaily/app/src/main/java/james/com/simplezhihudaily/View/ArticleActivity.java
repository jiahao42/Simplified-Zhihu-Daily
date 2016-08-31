package james.com.simplezhihudaily.View;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.WebSettings;
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
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import james.com.simplezhihudaily.Model.DeviceInfo;
import james.com.simplezhihudaily.Model.Symbol;
import james.com.simplezhihudaily.R;
import james.com.simplezhihudaily.Util.Util;
import james.com.simplezhihudaily.db.ZhihuDailyDB;
import james.com.simplezhihudaily.db.ZhihuDailyDBhelper;


public class ArticleActivity extends Activity {
    private Intent intent;
    private String id;
    private RequestQueue mQueue;
    private WebView article;
    private String url = "http://news-at.zhihu.com/api/4/news/";
    private String cssUrl;
    private String htmlString;
    private Document document;
    private ArticleActivity articleActivity;
    private ZhihuDailyDB zhihuDailyDB;

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
                    zoomPircture();
                }else if (message.what == Symbol.GET_ARTICLE_FROM_DB){
                    Log.d("db","Got article from db");
                    zoomPircture();
                }
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                htmlString = zhihuDailyDB.getArticle(id);
                if (htmlString != null)
                {
                    Message message = new Message();
                    message.what = Symbol.GET_ARTICLE_FROM_DB;
                    handler.sendMessage(message);
                } else
                {
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url + id, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try
                            {
                                htmlString = response.getString("body");
                            /*
                            此处也要判断 若id对应的content不为空 则不用插入
                             */
                                if (!zhihuDailyDB.hasContent(id))
                                {
                                    zhihuDailyDB.insertContent(id, htmlString);
                                }
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
            }

        }).start();
    }
    private void initWidget(){
        article = (WebView)findViewById(R.id.article);
        articleActivity = this;
        mQueue = Volley.newRequestQueue(articleActivity);
        intent = getIntent();
        Bundle bundle = intent.getBundleExtra("id");
        id = bundle.getString("id");
        article.getSettings().setAppCacheEnabled(true);// 设置启动缓存
        article.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        article.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);//适应屏幕，内容将自动缩
        zhihuDailyDB = ZhihuDailyDB.getInstance(articleActivity);
    }
    private void zoomPircture(){
        document = Jsoup.parse(htmlString);
        Elements imgs = document.getElementsByTag("img");
        for (Element img : imgs){
            if (img.attr("class").equals("content-image")){
                Log.d("img","zoomed");
                img.attr("width","100%").attr("height","auto");
            }
        }
        article.loadDataWithBaseURL("file:///android_asset/.",document.toString(),"text/html; charset=UTF-8", null,null);
    }
}
