package james.com.simplezhihudaily.View;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
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

import james.com.simplezhihudaily.Model.DateControl;
import james.com.simplezhihudaily.Model.NewsAdapter;
import james.com.simplezhihudaily.Model.Story;
import james.com.simplezhihudaily.Model.Symbol;
import james.com.simplezhihudaily.Model.TopStory;
import james.com.simplezhihudaily.Model.Url;
import james.com.simplezhihudaily.R;
import james.com.simplezhihudaily.Util.Util;
import james.com.simplezhihudaily.db.ZhihuDailyDB;

import static android.R.attr.top;

/**
 * TODO
 * 1.默认从本地数据库读，设置下拉刷新，刷新后才调用之前写的方法
 * 2.下滑时要隐藏标题栏
 * 3.要写点赞，评论菜单，且单击后该菜单会出现或消失
 * 4.分栏目，点击显示下拉列表，可以获取每个栏目的信息
 * 5.不显眼的注册登录功能，连接后台
 * 6.写设置界面，比如可以选择3G情况下不自动加载图片等
 */


public class MainActivity extends Activity {
    private List<Story> newsList = new ArrayList<>();
    private TopStory[] topStories;
    private RequestQueue mQueue;
    private MainActivity mainActivity = this;
    private Gson gson = new Gson();
    private String[] picUrls;
    private NewsAdapter adapter;
    private ZhihuDailyDB zhihuDailyDB;
    private TextView bottom;
    private RelativeLayout topBar;
    private ListView listView;
    private RefreshableView refreshableView;
    private ImageView beforeTheDay;
    private ImageView afterTheDay;
    private TextView titleDate;
    private DateControl dateControl;
    private TextView titleText;
    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;
    private Button button5;


    private boolean mIsShowTitle = false;
    private float mTranslateY;
    private boolean mIsfirstVisible = true;
    private float mBottomHeight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_try);
        initWidget();
        //setListener();
        //doLogic();
        getNewsUrl("latest");
    }

    private final Handler get_url_array_handler = new Handler() {
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
                    adapter.notifyDataSetChanged();//将notify放进了循环 如果网特别差 图片还可以一张张跳出来
                }
                getNewsPics();
            }
        }
    };

    private void getNewsUrl(final String certainDate) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String finalUrl;
                if (certainDate.equals("latest"))
                {
                    finalUrl = Url.getLatestNews;
                } else
                {
                    finalUrl = Url.getNewsBefore + certainDate;
                }
                mQueue = Volley.newRequestQueue(mainActivity);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                        finalUrl, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try
                        {
                            Log.d("TAG", response.getJSONArray("stories").toString());
                            //获得所有日报的Json信息 注意是一个数组 将其转化成对象后 再通过bundle传递
                            /**
                             * 注意：
                             *      只有当日的新闻才会有"top_stories"这个json数组
                             *      所以 当请求往日的数据时 并不能得到顶端栏目的数据
                             *      只能通过数据库里现有的来读 如果读不到了 那么就保持原样即可
                             *      或者更偷懒的方法是：顶端栏目始终不变
                             */
                            Story[] story = gson.fromJson(response.getString("stories"), Story[].class);
                            if (certainDate.equals("latest")){//若是请求最新数据才有"top_stories"
                                topStories = gson.fromJson(response.getString("top_stories"),TopStory[].class);
                            }
                            for (int i = 0; i < topStories.length; i++){
                                Log.d("test",topStories[i].toString());
                            }
                            String date = gson.fromJson(response.getString("date"), String.class);
                            if (certainDate.equals("latest")){
                                titleDate.setText(Util.analyzeDate(String.valueOf(date)));
                            }
                            for (int i = 0; i < story.length; i++)
                            {
                                if (certainDate.equals("latest") && i < 5){//每天固定五张图 就硬编码了
                                    topStories[i].setDate(date);
                                }
                                story[i].setDate(date);
                                Log.d("newsInfo", story[i].getTitle());
                            }
                            /*
                            实例化控制日期的单件
                             */
                            dateControl = DateControl.getInstance(Integer.parseInt(date));
                            Log.d("today is : ", String.valueOf(dateControl.getToday()));
                            /*
                            要存储topStory比较简单 
                            因为固定是5张图片 所以不需要传入长度 但是需要isStory字段为1
                             */
                            // TODO: 2016/9/2
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
                            int count = zhihuDailyDB.isInserted(date, story.length);
                            for (int i = 0; i < count; i++)
                            {
                                Log.d("TAG", "saving..." + i);
                                zhihuDailyDB.saveBaseStory(story[i]);//注意新来的新闻在头部
                            }
                            for (TopStory topStory : topStories)
                            {
                                zhihuDailyDB.saveTopStory(topStory);
                            }
                            /*
                            此处得到了所有的带有基本信息的对象集合
                             */
                            adapter = new NewsAdapter(MainActivity.this, R.layout.news_item, newsList);
                            newsList.clear();
                            Collections.addAll(newsList, story);
                            listView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();//先notify一下 让文字先显示出来 再去请求图片
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
    private void getNewsPics() {
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
                            //Log.d("imageRequest", "received" + count);
                        }
                    }, 0, 0, ImageView.ScaleType.CENTER, Bitmap.Config.RGB_565, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("getPics", "server_error");
                        }
                    });
                    mQueue.add(imageRequest);
                    //Log.d("imageRequest", "added" + count);
                }
                mQueue.start();
            }
        }).start();
    }

    private void initWidget() {
        listView = (ListView) findViewById(R.id.title_list);
        //bottom = (TextView) findViewById(R.id.bottom);
        beforeTheDay = (ImageView) findViewById(R.id.arrow_left);
        afterTheDay = (ImageView) findViewById(R.id.arrow_right);
        titleDate = (TextView) findViewById(R.id.title_date);
        titleText = (TextView) findViewById(R.id.title);
        button1 = (Button)findViewById(R.id.btn1);
        button2 = (Button)findViewById(R.id.btn2);
        button3 = (Button)findViewById(R.id.btn3);
        button4 = (Button)findViewById(R.id.btn4);
        button5 = (Button)findViewById(R.id.btn5);
        titleText.bringToFront();
        initListener();
        //topBar = (RelativeLayout) findViewById(R.id.top_bar);
        zhihuDailyDB = ZhihuDailyDB.getInstance(mainActivity);
        adapter = new NewsAdapter(MainActivity.this, R.layout.news_item, newsList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Story story = newsList.get(position);
                Toast.makeText(mainActivity, story.getTitle(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(mainActivity, ArticleActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("id", String.valueOf(story.getId()));
                intent.putExtra("id", bundle);
                startActivity(intent);

            }
        });
        refreshableView = (RefreshableView) findViewById(R.id.refreshable_view);
        refreshableView.setOnRefreshListener(new RefreshableView.PullToRefreshListener() {
            @Override
            public void onRefresh() {
                try
                {
                    getNewsUrl("latest");
                    dateControl.backToToday();//刷新后在点左箭头应该重新回到昨天的内容
                    Thread.sleep(3000);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                //getNewsUrl();
                refreshableView.finishRefreshing();
            }
        }, 0);
    }

    private void initListener() {
        /**
         *  获取前一天的数据
         *  1. 先从数据库读取date
         *  2.1 若有date 则提取出id
         *  2.2 若无date 则构造一个date向服务器发出请求 获取特定日期的新闻列表
         *  3. 重新设置适配器
         *
         *  注意：知乎过往文章的请求是这样的：
         *      比如我要请求9月1日的文章，那么url中应该是9月2日
         *  而在数据库则没有这个问题，应当引起注意
         */
        beforeTheDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //DateControl dateControl = DateControl.getInstance();
                dateControl.subOneDay();
                if (zhihuDailyDB.hasTheDate(String.valueOf(dateControl.getCursor())))
                {
                    titleDate.setText(Util.analyzeDate(String.valueOf(dateControl.getCursor())));
                    /*
                    newsList = new ArrayList<NewsInfo>();
                    不能使用上述方法 因为这样的话等于重新建立了一个对象 该对象并不是观察者
                     */
                    adapter = new NewsAdapter(MainActivity.this, R.layout.news_item, newsList);
                    newsList.clear();
                    newsList.addAll(zhihuDailyDB.loadStory(String.valueOf(dateControl.getCursor())));
                    listView.setAdapter(adapter);
                    /*
                    从数据库中取出来的对象 只有配图的url 而没有配图的图片 所以得去服务器请求
                     */
                    adapter.notifyDataSetChanged();//先出现文字 再开始请求图片
                    getPicFromNet();
                } else
                {
                    titleDate.setText(Util.analyzeDate(String.valueOf(dateControl.getCursor())));
                    dateControl.addOneDay();
                    getNewsUrl(String.valueOf(dateControl.getCursor()));
                    dateControl.subOneDay();
                }
            }
        });
        afterTheDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //DateControl dateControl = DateControl.getInstance();
                if (!dateControl.addOneDay())
                {
                    Toast.makeText(mainActivity, "已经是最后一天了", Toast.LENGTH_SHORT).show();
                } else
                {
                    if (zhihuDailyDB.hasTheDate(String.valueOf(dateControl.getCursor())))
                    {
                        titleDate.setText(Util.analyzeDate(String.valueOf(dateControl.getCursor())));
                    /*
                    newsList = new ArrayList<NewsInfo>();
                    不能使用上述方法 因为这样的话等于重新建立了一个对象 该对象并不是观察者
                    无法使用notifyDataSetChanged()
                     */
                        // ... Modify adapter ... do anything else you need to do
                        // To clear the recycled views list :
                        adapter = new NewsAdapter(MainActivity.this, R.layout.news_item, newsList);
                        newsList.clear();
                        newsList.addAll(zhihuDailyDB.loadStory(String.valueOf(dateControl.getCursor())));
                        listView.setAdapter (adapter);
                    /*
                    从数据库中取出来的对象 只有配图的url 而没有配图的图片 所以得去服务器请求
                     */
                        adapter.notifyDataSetChanged();//先出现文字 再开始请求图片
                        getPicFromNet();
                    } else
                    {
                        dateControl.addOneDay();
                        titleDate.setText(Util.analyzeDate(String.valueOf(dateControl.getCursor())));
                        dateControl.subOneDay();
                        getNewsUrl(String.valueOf(dateControl.getCursor()));
                    }
                }

            }
        });
    }
    private void getPicFromNet(){
        picUrls = new String[newsList.size()];
        Resources res = getResources();
        Bitmap bitmap = BitmapFactory.decodeResource(res, R.drawable.error);
        for (int i = 0; i < newsList.size(); i++)
        {
            Log.d("TAG", newsList.get(i).getUrls());
            picUrls[i] = newsList.get(i).getUrls();
            newsList.get(i).setBitmap(bitmap);
            adapter.notifyDataSetChanged();//将notify放进了循环 如果网特别差 图片还可以一张张跳出来
        }
        getNewsPics();
    }

    /**
     * 在应用不被杀死的情况下重新打开应用不会出现Splash Activity
     */
    @Override
    public void onBackPressed() {
        // super.onBackPressed(); 	不要调用父类的方法
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }



    /*
    以下都是通过判断滑动来隐藏标题栏的代码 暂未调用
     */
    /*
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
    */

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
                Story story = newsList.get(position);
                Toast.makeText(MainActivity.this, story.getTitle(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, ArticleActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("id", String.valueOf(story.getId()));
                intent.putExtra("id", bundle);
                startActivity(intent);
            }
        });
        listView.setAdapter(adapter);
        //将标题栏高度50dp转成显示的高度
        mTranslateY = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
    }

}


