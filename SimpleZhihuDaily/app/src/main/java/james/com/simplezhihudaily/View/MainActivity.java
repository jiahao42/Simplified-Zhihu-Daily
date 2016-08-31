package james.com.simplezhihudaily.View;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import james.com.simplezhihudaily.db.ZhihuDailyDB;

import static android.R.attr.id;
import static android.R.attr.inAnimation;

/**
 * TODO
 * 1.默认从本地数据库读，设置下拉刷新，刷新后才调用之前写的方法
 * 2.下滑时要隐藏标题栏
 * 3.要写点赞，评论菜单，且单击后该菜单会出现或消失
 * 4.分栏目，抽屉式
 * 5.不显眼的注册登录功能，连接后台
 */


public class MainActivity extends Activity {
    private List<NewsInfo> newsList = new ArrayList<>();
    private RequestQueue mQueue;
    private MainActivity mainActivity = this;
    private Gson gson = new Gson();
    private String[] picUrls;
    private NewsAdapter adapter;
    private ZhihuDailyDB zhihuDailyDB;
    private TextView bottom;
    private RelativeLayout topBar;
    private ListView listView;


    private boolean mIsShowTitle = false;
    private float mTranslateY;
    private boolean mIsfirstVisible = true;
    private float mBottomHeight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initWidget();
        setListener();
        doLogic();
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
                    Bitmap bitmap = BitmapFactory.decodeResource(res, R.drawable.error);
                    for (int i = 0; i < newsList.size(); i++)
                    {
                        Log.d("TAG", newsList.get(i).getUrls());
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
                            String date = gson.fromJson(response.getString("date"), String.class);
                            for (int i = 0; i < newsInfo.length; i++)
                            {
                                newsInfo[i].setDate(date);
                                Log.d("newsInfo", newsInfo[i].getTitle());
                            }
                            int count = zhihuDailyDB.isInserted(date, newsInfo.length);
                            for (int i = 0; i < count; i++)
                            {
                                    /*
                                    将其保存到数据库
                                    此处需要判断内容是否存在 否则会重复存储信息
                                    这里我选择用日期来判断
                                    但是假如用户今天没有打开过该app则无法缓存信息
                                    所以其实最好是后台开一个服务来下载
                                    这里有一个很tricky的问题 知乎日报的东西是每天逐渐增加的
                                    所以我之前直接根据日期来判断是否还要插入新的基本数据是不对的
                                    应该同时传入数组长度比较 然后直接返回更新title的个数
                                     */
                                Log.d("TAG", "saving..." + i);
                                zhihuDailyDB.saveBaseNews(newsInfo[i]);//注意新来的新闻在头部
                            }
                            /*
                            此处得到了所有的带有基本信息的对象集合
                             */
                            Log.d("date", newsInfo[0].getDate());
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
        final Handler get_pics_handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                if (message.what == Symbol.RECEIVE_SUCCESS)
                {
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
                            bundle.putInt("count", count);
                            message.setData(bundle);
                            message.what = Symbol.RECEIVE_SUCCESS;
                            get_pics_handler.sendMessage(message);
                            Log.d("imageRequest", "received" + count);
                        }
                    }, 0, 0, ImageView.ScaleType.CENTER, Bitmap.Config.RGB_565, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("getPics", "server_error");
                        }
                    });
                    mQueue.add(imageRequest);
                    Log.d("imageRequest", "added" + count);
                }
                mQueue.start();
            }
        }).start();
    }

    private void initWidget() {
        adapter = new NewsAdapter(MainActivity.this, R.layout.news_item, newsList);
        listView = (ListView) findViewById(R.id.title_list);
        bottom = (TextView) findViewById(R.id.bottom);
        topBar = (RelativeLayout) findViewById(R.id.top_bar);
        zhihuDailyDB = ZhihuDailyDB.getInstance(mainActivity);
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed(); 	不要调用父类的方法
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus)
        {
            //获取listview的高度   那么bottom开始的位置是mTranslateY+height值
            int height = listView.getHeight();
            mBottomHeight = mTranslateY + height;
            showHideTitle(false, 0);
        }
    }

    private void setListener() {
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount)
            {
                //判断当前是否在显示list的第一项数据
                mIsfirstVisible = firstVisibleItem == 0;
                //手掼滑动太快时非显示第一项还显示标题时，隐藏掉标题
                if (mIsShowTitle && !mIsfirstVisible)
                {
                    showHideTitle(false, 500);
                }
            }
        });
        listView.setOnTouchListener(new View.OnTouchListener() {
            private float lastX;
            private float lastY;
            boolean isChange = false;

            /*
            通过给listview设置touch listener，
            监听手掼是向下滑动的(Y比X移动距离大)且滑动距离足够大时，判断是向下反之亦然是向上。
            event move过程会有多次回调，为了保证在一次dowm 向下滑动时，需要在down 时设置标志，
            来保证一次down 向下滑动时只调用动画一次 做显示标题动作。
             */
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        lastX = event.getX();
                        lastY = event.getY();
                        isChange = false;
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        float x = event.getX();
                        float y = event.getY();
                        float xGapDistance = Math.abs(x - lastX);
                        float yGapDistance = Math.abs(y - lastY);
                        boolean isDown = y - lastY > 5;
                        //没有显示标题时，且是向下的，就显示
                        boolean isShow = yGapDistance > 8 && xGapDistance < 8 && !mIsShowTitle && isDown;
                        boolean isHide = yGapDistance > 8 && xGapDistance < 8 && mIsShowTitle && !isDown;
                        lastX = x;
                        lastY = y;
                        //一次down，只变化一次，防止一次滑动时抖动下，造成某一个的向下时,y比lastY小
                        if (!isChange && mIsfirstVisible && isShow)
                        {
                            // 显示此标题
                            showHideTitle(true, 500);
                            isChange = true;
                        }//显示标题时，且是向上的，就隐蔽
                        else if (!isChange && mIsfirstVisible && isHide)
                        {
                            // 隐蔽标题
                            showHideTitle(false, 500);
                            isChange = true;
                        }
                        break;

                    default:
                        break;
                }
                return false;
            }
        });
    }

    private void showHideTitle(boolean isShow, int duration) {
        if (isShow)
        {
            ObjectAnimator.ofFloat(listView, "y", 0, mTranslateY).setDuration(duration).start();
            ObjectAnimator.ofFloat(topBar, "y", -mTranslateY, 0).setDuration(duration).start();
            ObjectAnimator.ofFloat(bottom, "y", mBottomHeight - mTranslateY, mBottomHeight).setDuration(duration).start();

        } else
        {//隐藏时，把标题隐藏了，底部出来了
            ObjectAnimator.ofFloat(listView, "y", mTranslateY, 0).setDuration(duration).start();
            ObjectAnimator.ofFloat(topBar, "y", 0f, -mTranslateY).setDuration(duration).start();
            ObjectAnimator.ofFloat(bottom, "y", mBottomHeight, mBottomHeight - mTranslateY).setDuration(duration).start();
        }
        mIsShowTitle = isShow;
    }

    private void doLogic() {
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                NewsInfo newsInfo = newsList.get(position);
                Toast.makeText(MainActivity.this, newsInfo.getTitle(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, ArticleActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("id", String.valueOf(newsInfo.getId()));
                intent.putExtra("id", bundle);
                startActivity(intent);
            }
        });
        listView.setAdapter(adapter);
        //将标题栏高度50dp转成显示的高度
        mTranslateY = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
    }

}


