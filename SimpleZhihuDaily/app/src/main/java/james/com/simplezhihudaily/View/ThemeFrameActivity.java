package james.com.simplezhihudaily.View;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import james.com.simplezhihudaily.Model.DateControl;
import james.com.simplezhihudaily.Model.Symbol;
import james.com.simplezhihudaily.Model.Theme;
import james.com.simplezhihudaily.Model.ThemeStory;
import james.com.simplezhihudaily.Model.Url;
import james.com.simplezhihudaily.R;

public class ThemeFrameActivity extends Activity {
    private int id;
    private ThemeStory[] themeStories;
    private Gson gson;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_theme);
        initWidget();
    }
    private void initWidget(){
        gson = new Gson();
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("id");
        String theme = bundle.getString("id");
        for (int i = 0;i < MainActivity.themes.length;i++){
            if (theme.equals(MainActivity.themes[i].getName())){
                id = MainActivity.themes[i].getId();
                break;
            }
        }
        getCertainThemeStory(id);
    }

    /**
     * 得到专栏文章 受限于API 只能请求今日的专栏内容
     * 所以必须判断请求的日期以及栏目名称
     * 先去数据库请求 若没有 而且不是请求的今日的栏目信息 则不变化
     * @param id
     */
    private void getCertainThemeStory(final int id){
        final Handler handler = new Handler(){

        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Url.getThemeStory + String.valueOf(id), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try
                        {
                            String stories = response.getString("stories");
                            themeStories = gson.fromJson(stories, ThemeStory[].class);
                            for (int i = 0; i < themeStories.length; i++)
                            {
                                themeStories[i].setDate(String.valueOf(DateControl.getInstance().getToday()));
                                themeStories[i].setCategoryID(String.valueOf(id));
                            }
                            /**
                             * 存到数据库
                             */
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
            }
        }).start();
    }
}
