package com.james.simplezhihudaily.View;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

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

import java.io.IOException;

import com.james.simplezhihudaily.Model.StoryExtra;
import com.james.simplezhihudaily.Util.Symbol;
import com.james.simplezhihudaily.Util.Url;
import com.james.simplezhihudaily.R;
import com.james.simplezhihudaily.db.ZhihuDailyDB;

import static android.media.CamcorderProfile.get;


public class ArticleActivity extends Activity {
    private Intent intent;
    private String idOfArticle;
    private RequestQueue mQueue;
    private WebView article;
    private String cssUrl;
    private String htmlString;
    private Document document;
    private ArticleActivity articleActivity;
    private ZhihuDailyDB zhihuDailyDB;
    private StoryExtra storyExtra;
    private Gson gson;
    private TextView comments;
    private TextView thumb;
    private TextView share;

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
                htmlString = zhihuDailyDB.getArticle(idOfArticle);
                if (htmlString != null)
                {
                    Message message = new Message();
                    message.what = Symbol.GET_ARTICLE_FROM_DB;
                    handler.sendMessage(message);
                    Log.d("aboutDB","get_article_fromDB");
                } else
                {
                    Log.d("aboutDB","get_article_fromNET");
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Url.getArticleContent + idOfArticle, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try
                            {
                                htmlString = response.getString("body");
                            /*
                            此处也要判断 若id对应的content不为空 则不用插入
                             */
                                if (!zhihuDailyDB.hasContent(idOfArticle))
                                {
                                    zhihuDailyDB.insertContent(idOfArticle, htmlString);
                                }
                                cssUrl = response.getString("css");
                                Message message = new Message();
                                message.what = Symbol.RECEIVE_SUCCESS;
                                handler.sendMessage(message);
                            } catch (JSONException e)
                            {
                                /**
                                 * 有时候会出现 没有body的情况 此时只有一个sharedURL
                                 * 处理一下
                                 */
                                try
                                {
                                    htmlString = response.getString("share_url");
                                    getQuotedArticle(htmlString);
                                } catch (JSONException e1)
                                {
                                    e1.printStackTrace();
                                }
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
        share = (TextView)findViewById(R.id.share);
        comments = (TextView)findViewById(R.id.comments);
        thumb = (TextView)findViewById(R.id.thumb);
        articleActivity = this;
        mQueue = Volley.newRequestQueue(articleActivity);
        gson = new Gson();
        intent = getIntent();
        Bundle bundle = intent.getBundleExtra("id");
        idOfArticle = bundle.getString("idOfArticle");
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
        initListener();
        zhihuDailyDB = ZhihuDailyDB.getInstance(articleActivity);
    }
    private void initListener(){
        comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("idOfArticle",idOfArticle);
                bundle.putString("sumOfComments",String .valueOf(storyExtra.getSumOfComment()));
                Intent intent = new Intent(articleActivity,CommentActivity.class);
                intent.putExtra("id",bundle);
                startActivity(intent);
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showShareDialog();
            }
        });
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
        htmlString = document.toString();
        htmlString = "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />" + htmlString;
        article.loadDataWithBaseURL("file:///android_asset/",htmlString,"text/html; charset=UTF-8", null,null);
    }

    /**
     * 这里之所以要用handler 看起来多此一举
     * 实际上是因为 不允许在子线程中调度WebView
     * @param html  link to the site
     */
    private void getQuotedArticle(final String html){
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message message){
                if (message.what == Symbol.RECEIVE_SUCCESS){
                    zoomPicture();
                }
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                Document document = null;
                try
                {
                    document = Jsoup.connect(html).get();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
                htmlString = document.toString();
                Message message = new Message();
                message.what = Symbol.RECEIVE_SUCCESS;
                handler.sendMessage(message);
            }
        }).start();
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
     *      idOfArticle : 评论者的唯一标识符
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
                if (message.what == Symbol.RECEIVE_SUCCESS){
                    thumb.setText(String.valueOf(storyExtra.getPopularity()));
                    comments.setText(String.valueOf(storyExtra.getSumOfComment()));
                }else if (message.what == Symbol.RECEIVER_FAILED){
                    thumb.setText("...");
                    comments.setText("...");
                }

            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Url.getStoryExtra + idOfArticle, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            storyExtra = gson.fromJson(response.toString(), StoryExtra.class);
                            Log.d("storyExtra", storyExtra.toString());
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


    /**
     * 弹出分享列表
     */
    private void showShareDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(ArticleActivity.this);
        builder.setTitle("选择分享类型");
        builder.setItems(new String[]{"邮件","短信","其他"}, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                dialog.dismiss();
                switch (which) {
                    case 0:	//邮件
                        sendMail(Url.getArticleContent + idOfArticle);
                        break;

                    case 1:	//短信
                        sendSMS(Url.getArticleContent + idOfArticle);
                        break;

                    case 3:	//调用系统分享
                        Intent intent=new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_SUBJECT,"分享");
                        intent.putExtra(Intent.EXTRA_TEXT, "Share this for you:"+"http://www.google.com.hk/");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(Intent.createChooser(intent, "share"));
                        break;

                    default:
                        break;
                }

            }
        });
        builder.setNegativeButton( "取消" ,  new  DialogInterface.OnClickListener() {
            @Override
            public   void  onClick(DialogInterface dialog,  int  which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }


    /**
     * 发送邮件
     * @param emailUrl
     */
    private void sendMail(String emailUrl){
        Intent email = new Intent(android.content.Intent.ACTION_SEND);
        email.setType("plain/text");

        String emailBody = "Share this for you:" + emailUrl;
        //邮件主题
        email.putExtra(android.content.Intent.EXTRA_SUBJECT, "Share");
        //邮件内容
        email.putExtra(android.content.Intent.EXTRA_TEXT, emailBody);

        startActivityForResult(Intent.createChooser(email,  "请选择邮件发送内容" ), 1001 );
    }


    /**
     * 发短信
     */
    private   void  sendSMS(String webUrl){
        String smsBody = "Share this for you:" + webUrl;
        Uri smsToUri = Uri.parse( "smsto:" );
        Intent sendIntent =  new  Intent(Intent.ACTION_VIEW, smsToUri);
        //sendIntent.putExtra("address", "123456"); // 电话号码，这行去掉的话，默认就没有电话
        //短信内容
        sendIntent.putExtra( "sms_body", smsBody);
        sendIntent.setType( "vnd.android-dir/mms-sms" );
        startActivityForResult(sendIntent, 1002 );
    }
}
