package james.com.simplezhihudaily.View;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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

import james.com.simplezhihudaily.Model.Comment;
import james.com.simplezhihudaily.Model.CommentAdapter;
import james.com.simplezhihudaily.Model.Symbol;
import james.com.simplezhihudaily.Model.Url;
import james.com.simplezhihudaily.R;
import james.com.simplezhihudaily.db.ZhihuDailyDB;

import static android.R.id.message;
import static james.com.simplezhihudaily.R.drawable.error;
import static james.com.simplezhihudaily.Util.Util.ListUtils.setDynamicHeight;

public class CommentActivity extends Activity {
    private String idOfArticle;
    private CommentAdapter longCommentAdapter;
    private CommentAdapter shortCommentAdapter;
    private TextView sumOfCommentsText;
    private String sumOfComments;
    private TextView numberOfLongCommentsText;
    private int numberOfLongComments;
    private TextView numberOfShortCommentsText;
    private int numberOfShortComments;
    private ListView shortCommentListView;
    private ListView longCommentListView;
    private ZhihuDailyDB zhihuDailyDB;
    private Gson gson;
    private List<Comment> long_comments;
    private List<Comment> short_comments;
    public CommentActivity commentActivity;
    public RequestQueue mQueue;
    static Resources res;
    static Bitmap errorBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_comment);
        initWidget();
    }

    private void initWidget() {
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("id");
        idOfArticle = bundle.getString("idOfArticle");
        sumOfComments = bundle.getString("sumOfComments");
        commentActivity = this;
        sumOfCommentsText = (TextView) findViewById(R.id.number_of_comments);
        numberOfLongCommentsText = (TextView) findViewById(R.id.number_of_long_comments);
        numberOfShortCommentsText = (TextView) findViewById(R.id.number_of_short_comments);
        shortCommentListView = (ListView) findViewById(R.id.short_comments);
        longCommentListView = (ListView) findViewById(R.id.long_comments);
        zhihuDailyDB = ZhihuDailyDB.getInstance(this);
        //longCommentAdapter = new CommentAdapter(commentActivity, R.layout.comment_item, long_comments);
        //shortCommentAdapter = new CommentAdapter(commentActivity, R.layout.comment_item, short_comments);
        String temp = sumOfComments + "条点评";
        sumOfCommentsText.setText(temp);
        long_comments = new ArrayList<>();
        short_comments = new ArrayList<>();
        mQueue = Volley.newRequestQueue(commentActivity);
        gson = new Gson();
        res = getResources();
        errorBitmap = BitmapFactory.decodeResource(res, R.drawable.error);
        getComments();
    }

    private void getComments() {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                if (message.what == Symbol.RECEIVE_SUCCESS)
                {
                    getAvatar();
                }
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                /**
                 * 优先从数据库读
                 */
                if (zhihuDailyDB.isCommentInserted(idOfArticle))
                {
                    long_comments = zhihuDailyDB.getComments(idOfArticle,Symbol.LongComment);
                    longCommentAdapter.notifyDataSetChanged();
                    short_comments = zhihuDailyDB.getComments(idOfArticle,Symbol.ShortComment);
                    shortCommentAdapter.notifyDataSetChanged();
                } else
                {
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Url.getComment + idOfArticle + "/long-comments", null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try
                            {
                                /**
                                 * 注意 长评论有很大可能为空
                                 */
                                String string = response.getString("comments");
                                //if (!string.equals("[]"))
                                Log.d("Comment", string);
                                Comment[] temp = gson.fromJson(string, Comment[].class);
                                for (Comment comment:temp){
                                    comment.processTime();
                                }
                                numberOfLongComments = temp.length;
                                String tempString = numberOfLongComments + "条长评";
                                numberOfLongCommentsText.setText(tempString);
                                longCommentAdapter = new CommentAdapter(commentActivity, R.layout.comment_item, long_comments);
                                long_comments.clear();
                                Collections.addAll(long_comments, temp);
                                longCommentListView.setAdapter(longCommentAdapter);
                                setDynamicHeight(longCommentListView);
                                longCommentAdapter.notifyDataSetChanged();//先notify一下 让文字先显示出来 再去请求图片
                                for (Comment comment : long_comments){
                                    comment.setType(Symbol.LongComment);
                                    zhihuDailyDB.saveComments(comment);
                                }
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
                            Message message = new Message();
                            message.what = Symbol.RECEIVER_FAILED;
                            handler.sendMessage(message);
                        }
                    });
                    mQueue.add(jsonObjectRequest);
                    mQueue.start();
                }
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Url.getComment + idOfArticle + "/short-comments", null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String string = null;
                        try
                        {
                            string = response.getString("comments");
                            //if (!string.equals("[]")
                            Comment[] temp = gson.fromJson(string, Comment[].class);
                            for (Comment comment:temp){
                                comment.processTime();
                            }
                            Log.d("Comment", temp[0].toString());
                            numberOfShortComments = temp.length;
                            String tempString = numberOfShortComments + "条短评";
                            numberOfShortCommentsText.setText(tempString);
                            shortCommentAdapter = new CommentAdapter(commentActivity, R.layout.comment_item, short_comments);
                            short_comments.clear();
                            Collections.addAll(short_comments, temp);
                            shortCommentListView.setAdapter(shortCommentAdapter);
                            setDynamicHeight(shortCommentListView);
                            shortCommentAdapter.notifyDataSetChanged();//先notify一下 让文字先显示出来 再去请求图片
                            for (Comment comment : short_comments){
                                comment.setType(Symbol.ShortComment);
                                zhihuDailyDB.saveComments(comment);
                            }
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
                        Message message = new Message();
                        message.what = Symbol.RECEIVER_FAILED;
                        handler.sendMessage(message);
                    }
                });
                mQueue.add(jsonObjectRequest);
                mQueue.start();
            }
        }).start();
    }
    private void getAvatar(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < long_comments.size();i++){
                    final int count = i;
                    ImageRequest imageRequest = new ImageRequest(long_comments.get(count).getAvatar(), new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap response) {
                            long_comments.get(count).setBitmap(response);
                            longCommentAdapter.notifyDataSetChanged();
                        }
                    }, 0, 0, ImageView.ScaleType.CENTER, Bitmap.Config.RGB_565, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            long_comments.get(count).setBitmap(errorBitmap);
                        }
                    });
                    mQueue.add(imageRequest);
                    mQueue.start();
                }
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < short_comments.size();i++){
                    final int count = i;
                    ImageRequest imageRequest = new ImageRequest(short_comments.get(i).getAvatar(), new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap response) {
                            short_comments.get(count).setBitmap(response);
                            shortCommentAdapter.notifyDataSetChanged();
                        }
                    }, 0, 0, ImageView.ScaleType.FIT_CENTER, Bitmap.Config.RGB_565, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            short_comments.get(count).setBitmap(errorBitmap);
                        }
                    });
                    mQueue.add(imageRequest);
                    mQueue.start();
                }
            }
        }).start();
    }
}
