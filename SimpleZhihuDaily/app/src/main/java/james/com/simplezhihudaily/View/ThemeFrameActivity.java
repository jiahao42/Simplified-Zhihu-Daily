package james.com.simplezhihudaily.View;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import james.com.simplezhihudaily.Model.DateControl;
import james.com.simplezhihudaily.Model.StoryAdapter;
import james.com.simplezhihudaily.Model.Symbol;
import james.com.simplezhihudaily.Model.Theme;
import james.com.simplezhihudaily.Model.ThemeStory;
import james.com.simplezhihudaily.Model.ThemeStoryAdapter;
import james.com.simplezhihudaily.Model.Url;
import james.com.simplezhihudaily.R;
import james.com.simplezhihudaily.db.ZhihuDailyDB;

public class ThemeFrameActivity extends Activity {
    private int id;
    private List<ThemeStory> themeStories = new ArrayList<>();
    private Gson gson;
    private RequestQueue mQueue;
    private ZhihuDailyDB zhihuDailyDB;
    private DateControl dateControl;
    private TextView title;
    private ImageView topPicture;
    private TextView description;
    private ListView listView;
    private ThemeStoryAdapter adapter;
    private String[] picUrls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_themeframe);
        initWidget();
    }

    private void initWidget() {
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("id");
        String theme = bundle.getString("id");
        String desc = bundle.getString("desc");
        Log.d("jumpToTheme", theme);
        for (int i = 0; i < MainActivity.themes.length; i++)
        {
            if (theme.equals(MainActivity.themes[i].getName()))
            {
                id = MainActivity.themes[i].getId();
                Log.d("jumpToTheme", String.valueOf(id));
                break;
            }
        }
        title = (TextView) findViewById(R.id.title);
        title.setText(theme);
        topPicture = (ImageView) findViewById(R.id.top_picture);
        listView = (ListView) findViewById(R.id.listView);
        ;

        description = (TextView) findViewById(R.id.description);
        description.setText(desc);
        mQueue = Volley.newRequestQueue(this);
        zhihuDailyDB = ZhihuDailyDB.getInstance(this);
        dateControl = DateControl.getInstance();
        gson = new Gson();
        getCertainThemeStory(id);
    }

    /**
     * 得到专栏文章 受限于API 只能请求今日的专栏内容
     * 所以必须判断请求的日期以及栏目名称
     * 先去数据库请求 若没有 而且不是请求的今日的栏目信息 则不变化
     *
     * @param categoryID
     */
    private void getCertainThemeStory(final int categoryID) {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                if (message.what == Symbol.RECEIVE_SUCCESS)
                {
                    picUrls = new String[themeStories.size()];
                    for (int i = 0; i < themeStories.size(); i++)
                    {
                        picUrls[i] = themeStories.get(i).getUrls();
                    }
                    getPicFromNet();
                } else
                {

                }
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Url.getThemeStory + String.valueOf(categoryID), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try
                        {
                            String stories = response.getString("stories");

                            ThemeStory[] temp = gson.fromJson(stories, ThemeStory[].class);
                            adapter = new ThemeStoryAdapter(ThemeFrameActivity.this, R.layout.story_item, themeStories);
                            themeStories.clear();
                            Collections.addAll(themeStories, temp);
                            listView.setAdapter(adapter);
                            for (int i = 0; i < themeStories.size(); i++)
                            {
                                themeStories.get(i).setDate(String.valueOf(DateControl.getInstance().getToday()));
                                themeStories.get(i).setCategoryID(String.valueOf(categoryID));
                            }
                            /**
                             * 存到数据库 存之前必须知道差值 请求到的条目数与数据库中的条目数的差值
                             */
                            int categoryID = Integer.parseInt(themeStories.get(0).getCategoryID());
                            int count = zhihuDailyDB.isAllThemeStoryInserted(String.valueOf(dateControl.getToday()), themeStories.size(), categoryID);
                            for (int i = 0; i < count; i++)
                            {
                                zhihuDailyDB.saveThemeStory(themeStories.get(i));
                                Log.d("saveThemeStory", themeStories.get(i).toString());
                            }
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

    private void getPicFromNet() {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                if (message.what == Symbol.RECEIVE_SUCCESS)
                {

                } else
                {
                    Bitmap bdrawable = BitmapFactory.decodeResource(ThemeFrameActivity.this.getResources(), R.drawable.error);
                    for (ThemeStory themeStory : themeStories)
                    {
                        themeStory.setBitmap(bdrawable);
                        adapter.notifyDataSetChanged();
                    }
                }

            }
        };
        new

                Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < picUrls.length; i++)
                {
                    final int count = i;
                    ImageRequest imageRequest = new ImageRequest(picUrls[i], new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap response) {
                            Message message = new Message();
                            message.what = Symbol.RECEIVE_SUCCESS;
                            handler.sendMessage(message);
                            themeStories.get(count).setBitmap(response);
                            adapter.notifyDataSetChanged();
                        }
                    }, 0, 0, ImageView.ScaleType.CENTER, Bitmap.Config.RGB_565, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Message message = new Message();
                            message.what = Symbol.RECEIVER_FAILED;
                            handler.sendMessage(message);
                        }
                    });
                    mQueue.add(imageRequest);
                    mQueue.start();
                }
            }
        }

        ).

                start();
    }

    @Override
    public void onBackPressed() {
        MainActivity.mainActivity.spinner.setSelection(0);//将spinner变回今日热闻
        this.finish();
    }
}
