package james.com.simplezhihudaily.View;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import james.com.simplezhihudaily.Model.StoryExtra;
import james.com.simplezhihudaily.Model.Symbol;
import james.com.simplezhihudaily.Model.Url;
import james.com.simplezhihudaily.R;
import james.com.simplezhihudaily.db.ZhihuDailyDB;


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
    private StoryExtra storyExtra;
    private Gson gson;
    private ImageView comments;
    private ImageView thumb;
    private ImageView share;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_article);
        initWidget();
        get_article();
        getStoryExtra();
    }

    /**
     * 发起网络请求 得到文章内容html 然后再进行图片大小处理使之适配屏幕大小
     */
    private void get_article(){
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message message){
                if (message.what == Symbol.RECEIVE_SUCCESS){
                    zoomPicture();
                }else if (message.what == Symbol.GET_ARTICLE_FROM_DB){
                    Log.d("db","Got article from db");
                    zoomPicture();
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
                    Log.d("aboutDB","get_article_fromDB");
                } else
                {
                    Log.d("aboutDB","get_article_fromNET");
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

    /**
     * 初始化控件
     */
    private void initWidget(){
        article = (WebView)findViewById(R.id.article);
        share = (ImageView)findViewById(R.id.share);
        comments = (ImageView)findViewById(R.id.comments);
        thumb = (ImageView)findViewById(R.id.thumb);
        articleActivity = this;
        mQueue = Volley.newRequestQueue(articleActivity);
        gson = new Gson();
        intent = getIntent();
        Bundle bundle = intent.getBundleExtra("id");
        id = bundle.getString("id");
        article.getSettings().setAppCacheEnabled(true);// 设置启动缓存
        article.getSettings().getDomStorageEnabled();
        article.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        article.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);//适应屏幕，内容将自动缩
        article.getSettings().setJavaScriptEnabled(true);
        article.getSettings().setLoadsImagesAutomatically(true);
        article.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                if(!article.getSettings().getLoadsImagesAutomatically()) {
                    article.getSettings().setLoadsImagesAutomatically(true);
                }
            }
        });
        zhihuDailyDB = ZhihuDailyDB.getInstance(articleActivity);
    }

    /**
     * 将html中可能过大的图片适应屏幕
     * 此方法根据观察知乎API得到 不具有适用性
     */
    private void zoomPicture(){
        document = Jsoup.parse(htmlString);
        Elements imgs = document.getElementsByTag("img");
        for (Element img : imgs){
            if (img.attr("class").equals("content-image") || !img.hasAttr("class")){
                Log.d("img","zoomed");
                img.attr("width","100%").attr("height","auto");
            }
        }
        article.loadDataWithBaseURL("file:///android_asset/.",document.toString(),"text/html; charset=UTF-8", null,null);
    }

    /**
     *      ***** 知乎文章的额外信息分析 *****
     *      额外信息包括：
     *      1.赞数
     *      2.长评论总数
     *      3.短评论总数
     *      4.评论总数 = 长评论总数 + 短评论总数
     *
     *      ***** 长评论内容 *****
     *      ***** 短评论内容 *****
     *      comments : 长评论列表，形式为数组（请注意，其长度可能为 0）
     *      author : 评论作者
     *      id : 评论者的唯一标识符
     *      content : 评论的内容
     *      likes : 评论所获『赞』的数量
     *      time : 评论时间
     *      avatar : 用户头像图片的地址
     *
     */

    private void getStoryExtra(){
        final Handler getStoryExtra = new Handler(){
            @Override
            public void handleMessage(Message message){
                //好像不用做什么事啊 - -
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Url.getStoryExtra + id, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        storyExtra = gson.fromJson(response.toString(), StoryExtra.class);
                        Message message = new Message();
                        message.what = Symbol.RECEIVE_SUCCESS;
                        getStoryExtra.sendMessage(message);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Message message = new Message();
                        message.what = Symbol.RECEIVER_FAILED;
                        getStoryExtra.sendMessage(message);
                    }
                });
                mQueue.add(jsonObjectRequest);
                mQueue.start();
            }
        }).start();
    }

}
