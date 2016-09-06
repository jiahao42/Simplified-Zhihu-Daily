package james.com.simplezhihudaily.View;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
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
import james.com.simplezhihudaily.Model.DeviceInfo;
import james.com.simplezhihudaily.Model.Story;
import james.com.simplezhihudaily.Model.StoryAdapter;
import james.com.simplezhihudaily.Model.Symbol;
import james.com.simplezhihudaily.Model.Theme;
import james.com.simplezhihudaily.Model.TopStory;
import james.com.simplezhihudaily.Model.Url;
import james.com.simplezhihudaily.R;
import james.com.simplezhihudaily.Util.Util;
import james.com.simplezhihudaily.db.ZhihuDailyDB;

import static james.com.simplezhihudaily.R.drawable.error;

/**
 * TODO
 * 1.默认从本地数据库读，设置下拉刷新，刷新后才调用之前写的方法  **DONE**
 * 2.下滑时要隐藏标题栏
 * 3.要写点赞，评论菜单，且单击后该菜单会出现或消失
 * 4.分栏目，点击显示下拉列表，可以获取每个栏目的信息   **DONE**
 * 5.不显眼的注册登录功能，连接后台
 * 6.写设置界面，比如可以选择3G情况下不自动加载图片等
 * 7.要做推送消息功能（总之要实践Service和Broadcast!!!)
 * 8.分享功能
 * 9.持久化Cookie
 */

// TODO: 2016/9/3 还没有给TopStory添加链接 无法通过它进入文章


public class MainActivity extends Activity implements AdapterView.OnItemSelectedListener ,View.OnClickListener{
    private List<Story> StoryList = new ArrayList<>();
    private TopStory[] topStories;
    private String[] storyPicUrls;
    private String[] topStoryPicUrls;
    private RequestQueue mQueue;
    public static MainActivity mainActivity;
    private Gson gson = new Gson();
    private StoryAdapter adapter;
    private ZhihuDailyDB zhihuDailyDB;
    private TextView bottom;
    private RelativeLayout topBar;
    private ListView listView;
    private RefreshableView refreshableView;
    private SlidingSwitcherView slidingSwitcherView;
    private ImageView beforeTheDay;
    private ImageView afterTheDay;
    private TextView titleDate;
    private DateControl dateControl;
    private TextView titleText;
    private Button[] topImage;
    private boolean getTopStoryFlag = true;
    private static final int numberOfTopStories = 5;
    private float deviceHeight;
    public Spinner spinner;
    public static Theme[] themes;
    public static List<String> spinnerList;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String [] themePics;

    private boolean mIsShowTitle = false;
    private float mTranslateY;
    private boolean mIsfirstVisible = true;
    private float mBottomHeight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);
        initWidget();
        //doLogic();
        getStoryUrl("latest");
    }

    private final Handler getUrlArrayHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            if (message.what == Symbol.RECEIVE_SUCCESS)
            {
                storyPicUrls = new String[StoryList.size()];
                Resources res = getResources();
                Bitmap bitmap = BitmapFactory.decodeResource(res, error);
                /**
                 * 下面得到Story的Url
                 */
                for (int i = 0; i < StoryList.size(); i++)
                {
                    Log.d("TAG", StoryList.get(i).getUrls());
                    storyPicUrls[i] = StoryList.get(i).getUrls();
                    StoryList.get(i).setBitmap(bitmap);
                    adapter.notifyDataSetChanged();//将notify放进了循环 如果网特别差 图片还可以一张张跳出来
                }
                /**
                 * 根据Url去请求Story的配图
                 */
                getStoryPics();
                /**
                 * 下面得到topStory的Urls
                 */
                topStoryPicUrls = new String[numberOfTopStories];
                for (int i = 0; i < numberOfTopStories; i++)
                {
                    topStoryPicUrls[i] = topStories[i].getmyUrls();
                }
                /**
                 * 根据Url去请求TopStory的配图
                 */
                getTopStoryPics();
            }
        }
    };

    /**
     * 根据传入的日期来得到当天的新闻列表
     *
     * @param certainDate 要查询的日期
     */
    private void getStoryUrl(final String certainDate) {
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
                            //获得所有日报的Json信息 注意是一个数组 将其转化成对象后 再通过bundle传递\
                            /**
                             * 获得日期是一个很重要的节点
                             */
                            String date = gson.fromJson(response.getString("date"), String.class);
                            /**
                             * 注意：
                             *      只有当日的新闻才会有"top_stories"这个json数组
                             *      所以 当请求往日的数据时 并不能得到顶端栏目的数据
                             *      只能通过数据库里现有的来读 如果读不到了 那么就保持原样即可
                             *      或者更偷懒的方法是：顶端栏目始终不变
                             */

                            /**
                             *      以下都是处理topStory的代码
                             */
                            if (certainDate.equals("latest"))
                            {
                                titleDate.setText(Util.parseDate(String.valueOf(date)));
                                //为了防止重复存储top_story的信息 在此先检查一下 并设置flag 已存在则不读
                                if (zhihuDailyDB.isTopStoryInDB(date))
                                {
                                    topStories = zhihuDailyDB.loadTopStory(date);
                                    for (int i = 0; i < numberOfTopStories; i++)
                                    {
                                        topImage[i].setText(topStories[i].getTitle());
                                    }
                                    getTopStoryFlag = false;
                                }
                            }
                            if (certainDate.equals("latest") && getTopStoryFlag)
                            {//若是请求最新数据才有"top_stories" 且要flag为true
                                topStories = gson.fromJson(response.getString("top_stories"), TopStory[].class);
                                for (int i = 0; i < numberOfTopStories; i++)
                                {
                                    topImage[i].setText(topStories[i].getTitle());
                                }
                            }
                            if (certainDate.equals("latest") && getTopStoryFlag)
                            {
                                for (TopStory topStory : topStories)
                                {
                                    topStory.setDate(date);
                                }
                            }
                            if (getTopStoryFlag)
                            {
                                for (TopStory topStory : topStories)
                                {
                                    zhihuDailyDB.saveTopStory(topStory);
                                }
                                getTopStoryFlag = false;
                            }


                            /**
                             *      下面都是处理Story的代码
                             */
                            Story[] story = gson.fromJson(response.getString("stories"), Story[].class);
                            for (int i = 0; i < story.length; i++)
                            {
                                story[i].setDate(date);
                                Log.d("newsInfo", story[i].getTitle());
                            }
                            /**
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
                            int count = zhihuDailyDB.isAllStoryInserted(date, story.length);
                            for (int i = 0; i < count; i++)
                            {
                                Log.d("TAG", "saving..." + i);
                                zhihuDailyDB.saveStory(story[i]);//注意新来的新闻在头部
                            }
                            /*
                            此处得到了所有的带有基本信息的对象集合
                             */
                            adapter = new StoryAdapter(MainActivity.this, R.layout.story_item, StoryList);
                            StoryList.clear();
                            Collections.addAll(StoryList, story);
                            listView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();//先notify一下 让文字先显示出来 再去请求图片
                            Message message = new Message();
                            message.what = Symbol.RECEIVE_SUCCESS;
                            getUrlArrayHandler.sendMessage(message);
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
                jsonObjectRequest.setShouldCache(true);
                mQueue.add(jsonObjectRequest);
                mQueue.start();
            }
        }).start();
    }

    /**
     * 获取栏目名称
     *
     */
    private void getThemes() {
        final Handler getThemeHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                spinnerList = new ArrayList<String>();
                spinnerList.add("今日热闻");
                spinnerList.add("登录/注册");
                spinnerList.add("我的设置");
                if (message.what == Symbol.RECEIVE_SUCCESS)
                {
                    for (Theme theme : themes)
                    {
                        spinnerList.add(theme.getName());
                    }
                    themePics = new String[themes.length];
                    for (int i = 0; i < themes.length; i++){
                        themePics[i] = themes[i].getUrl();
                    }
                    getThemePics();
                } else
                {//没请求到数据则直接到本地取
                    List<Theme> list = new ArrayList<>();
                    list.addAll(zhihuDailyDB.getTheme());
                    for (int i = 0; i < list.size(); i++)
                    {
                        spinnerList.add(list.get(i).getName());
                    }
                }
                initSpinner();
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                mQueue = Volley.newRequestQueue(mainActivity);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Url.getThemes, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try
                        {
                            themes = gson.fromJson(response.getString("others"), Theme[].class);
                            int count = 0;
                            if (zhihuDailyDB.howManyThemeInDB() != 0){
                                for (int i = 0; i < themes.length; i++)
                                {
                                    zhihuDailyDB.saveThemes(themes[i]);
                                }
                            }
                            Message message = new Message();
                            message.what = Symbol.RECEIVE_SUCCESS;
                            getThemeHandler.sendMessage(message);
                        } catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("server-error", "getTheme");
                        Message message = new Message();
                        message.what = Symbol.RECEIVER_FAILED;
                        getThemeHandler.sendMessage(message);
                    }
                });
                jsonObjectRequest.setShouldCache(true);
                mQueue.add(jsonObjectRequest);
                mQueue.start();
            }
        }).start();
    }

    private void getThemePics(){
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message message){
                if (message.what == Symbol.RECEIVE_SUCCESS){

                }else {
                    for(Theme theme:themes){
                        Bitmap icon = BitmapFactory.decodeResource(mainActivity.getResources(),
                                R.drawable.error);
                        theme.setBitmap(icon);
                    }
                }
            }

        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0;i < themes.length; i++){
                    final int count = i;
                    ImageRequest imageRequest = new ImageRequest(themes[i].getUrl(), new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap response) {
                            themes[count].setBitmap(response);
                        }
                    }, 0, 0, ImageView.ScaleType.FIT_XY, Bitmap.Config.RGB_565, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Message message = new Message();
                            message.what = Symbol.RECEIVER_FAILED;
                            handler.sendMessage(message);
                        }
                    });
                    imageRequest.setShouldCache(true);
                    mQueue.add(imageRequest);
                    mQueue.start();
                }
            }
        }).start();
    }

    /*
    初始化spinner
     */
    private void initSpinner() {
        ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<String>(this,
                R.layout.custom_spiner_text_item, spinnerList);
        Log.d("Spinner", spinnerList.get(5));
        stringArrayAdapter
                .setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        spinner.setAdapter(stringArrayAdapter);
        if (spinner != null)
        {
            spinner.setOnItemSelectedListener(this);
        }
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    */
    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                               long arg3)
    {
        if (arg0.getId() == R.id.spinner)
        {
            Log.d("Spinner", String.valueOf(arg0.getId()));
            String itemString = spinner.getItemAtPosition(arg2).toString();
            Intent intent;
            String desc;
            // TODO: 2016/9/5 有时间要去掉硬编码
            switch (itemString){
                case "今日热闻":
                    Toast.makeText(this,"已经在今日热闻了",Toast.LENGTH_SHORT).show();
                    break;
                case "我的设置":
                    intent = new Intent(mainActivity,SettingActivity.class);
                    startActivity(intent);
                    break;
                case "登录/注册":
                    intent = new Intent(mainActivity,LoginActivity.class);
                    startActivity(intent);
                    break;
                case "日常心理学":
                    desc = "了解自己和别人，了解彼此的欲望和局限。";
                    jumpToThemeFrame(getThemeIndex("日常心理学"));
                    break;
                case "用户推荐日报":
                    desc = "内容由知乎用户推荐，海纳主题百万，趣味上天入地";
                    jumpToThemeFrame(getThemeIndex("用户推荐日报"));
                    break;
                case "电影日报":
                    desc = "除了经典和新片，我们还关注技术和产业";
                    jumpToThemeFrame(getThemeIndex("电影日报"));
                    break;
                case "不许无聊":
                    desc = "为你发现最有趣的新鲜事，建议在 WiFi 下查看";
                    jumpToThemeFrame(getThemeIndex("不许无聊"));
                    break;
                case "设计日报":
                    desc = "好设计需要打磨和研习，我们分享灵感和路径";
                    jumpToThemeFrame(getThemeIndex("设计日报"));
                    break;
                case "大公司日报":
                    desc = "商业世界变化越来越快，就是这些家伙干的";
                    jumpToThemeFrame(getThemeIndex("大公司日报"));
                    break;
                case "财经日报":
                    desc = "从业者推荐的财经金融资讯";
                    jumpToThemeFrame(getThemeIndex("财经日报"));
                    break;
                case "互联网安全":
                    desc = "把黑客知识科普到你的面前";
                    jumpToThemeFrame(getThemeIndex("互联网安全"));
                    break;
                case "开始游戏":
                    desc = "如果你喜欢游戏，就从这里开始";
                    jumpToThemeFrame(getThemeIndex("开始游戏"));
                    break;
                case "音乐日报":
                    desc = "有音乐就很好";
                    jumpToThemeFrame(getThemeIndex("音乐日报"));
                    break;
                case "动漫日报":
                    desc = "用技术的眼睛仔细看懂每一部动画和漫画";
                    jumpToThemeFrame(getThemeIndex("动漫日报"));
                    break;
                case "体育日报":
                    desc = "关注体育，不吵架。";
                    jumpToThemeFrame(getThemeIndex("体育日报"));
                    break;
                default:
                    Toast.makeText(this, "你选中的是" + itemString, Toast.LENGTH_SHORT)
                            .show();
                    break;
            }
        }
    }

    /**
     * 跳到专栏界面
     * @param index 数组下标
     */
    private void jumpToThemeFrame(int index){
        Bundle bundle;
        Intent intent;
        bundle = new Bundle();
        bundle.putString("desc",themes[index].getDescription());
        bundle.putString("name",themes[index].getName());
        bundle.putParcelable("bitmap",themes[index].getBitmap());
        intent = new Intent(mainActivity,ThemeFrameActivity.class);
        intent.putExtra("id",bundle);
        startActivity(intent);
    }

    /**
     * 通过标题栏目返回该标题在数组中的下标
     * @param name  栏目名
     * @return  下标
     */
    private int getThemeIndex(String name){
        int index = 0;
        for (int i = 0; i < themes.length;i++){
            if (themes[i].getName().equals(name)){
                index = i;
                break;
            }
        }
        return index;
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {

    }

    /**
     * 获取Story的图片
     */
    private void getStoryPics() {
        final Handler getStoryPics = new Handler() {
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
                for (int i = 0; i < storyPicUrls.length; i++)
                {
                    final int count = i;
                    ImageRequest imageRequest = new ImageRequest(storyPicUrls[count], new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap response) {
                            StoryList.get(count).setBitmap(response);
                            Message message = new Message();
                            Bundle bundle = new Bundle();
                            bundle.putInt("count", count);
                            message.setData(bundle);
                            message.what = Symbol.RECEIVE_SUCCESS;
                            getStoryPics.sendMessage(message);
                            //Log.d("imageRequest", "received" + count);
                        }
                    }, 0, 0, ImageView.ScaleType.CENTER, Bitmap.Config.RGB_565, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("getPics", "server_error");
                        }
                    });
                    imageRequest.setShouldCache(true);
                    mQueue.add(imageRequest);
                    //Log.d("imageRequest", "added" + count);
                }
                mQueue.start();
            }
        }).start();
    }

    /**
     * 获取topStory的图片
     */
    private void getTopStoryPics() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < numberOfTopStories; i++)
                {
                    final int count = i;
                    ImageRequest imageRequest = new ImageRequest(topStoryPicUrls[count], new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap response) {
                            BitmapDrawable bdrawable = new BitmapDrawable(mainActivity.getResources(), response);
                            topImage[count].setBackground(bdrawable);
                        }
                    }, 0, 0, ImageView.ScaleType.CENTER, Bitmap.Config.RGB_565, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("getPics", "server_error");
                        }
                    });
                    imageRequest.setShouldCache(true);
                    mQueue.add(imageRequest);
                    //Log.d("imageRequest", "added" + count);
                }
                mQueue.start();
            }
        }).start();
    }

    /**
     * 此方法是为了获取往日的配图
     */
    private void getPicFromNet() {
        storyPicUrls = new String[StoryList.size()];
        Resources res = getResources();
        Bitmap bitmap = BitmapFactory.decodeResource(res, error);
        for (int i = 0; i < StoryList.size(); i++)
        {
            Log.d("TAG", StoryList.get(i).getUrls());
            storyPicUrls[i] = StoryList.get(i).getUrls();
            StoryList.get(i).setBitmap(bitmap);
            adapter.notifyDataSetChanged();//将notify放进了循环 如果网特别差 图片还可以一张张跳出来
        }
        getStoryPics();
    }


    private void initWidget() {
        /**
         * 初始化控件
         */
        mainActivity = this;
        listView = (ListView) findViewById(R.id.title_list);
        beforeTheDay = (ImageView) findViewById(R.id.arrow_left);
        afterTheDay = (ImageView) findViewById(R.id.arrow_right);
        titleDate = (TextView) findViewById(R.id.title_date);
        titleText = (TextView) findViewById(R.id.title);
        topBar = (RelativeLayout) findViewById(R.id.top_bar);
        bottom = (TextView) findViewById(R.id.bottom);
        spinner = (Spinner) findViewById(R.id.spinner);
        slidingSwitcherView = (SlidingSwitcherView) findViewById(R.id.slidingLayout);
        editor = getSharedPreferences("themes", MODE_PRIVATE).edit();
        sharedPreferences = getSharedPreferences("themes", MODE_PRIVATE);


        topImage = new Button[numberOfTopStories];
        topImage[0] = (Button) findViewById(R.id.btn1);
        topImage[1] = (Button) findViewById(R.id.btn2);
        topImage[2] = (Button) findViewById(R.id.btn3);
        topImage[3] = (Button) findViewById(R.id.btn4);
        topImage[4] = (Button) findViewById(R.id.btn5);
        for (int i = 0; i < numberOfTopStories; i++){
            topImage[i].setOnClickListener(this);
        }
        titleText.bringToFront();
        zhihuDailyDB = ZhihuDailyDB.getInstance(mainActivity);
        adapter= new StoryAdapter(MainActivity.this, R.layout.story_item, StoryList);
        listView.setAdapter(adapter);
        setListViewListener();
        initDateListener();
        getThemes();

        refreshableView = (RefreshableView) findViewById(R.id.refreshable_view);
        refreshableView.setOnRefreshListener(new RefreshableView.PullToRefreshListener() {
            @Override
            public void onRefresh() {
                try
                {
                    getTopStoryFlag = true;
                    getStoryUrl("latest");
                    dateControl.backToToday();//刷新后在点左箭头应该重新回到昨天的内容
                    Thread.sleep(3000);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                //getStoryUrl();
                refreshableView.finishRefreshing();
            }
        }, 0);
    }

    /**
     * 为顶部滚动的图片注册点击事件
     * @param v     链接的下标
     */
        public void onClick(View v){
            switch (v.getId()){
                case R.id.btn1:
                    initTopLinks(0);
                    break;
                case R.id.btn2:
                    initTopLinks(1);
                    break;
                case R.id.btn3:
                    initTopLinks(2);
                    break;
                case R.id.btn4:
                    initTopLinks(3);
                    break;
                case R.id.btn5:
                    initTopLinks(4);
                    break;
            }
        }

    /**
     * 注册topStory的点击事件
     * @param id    传入文章的ID
     */
    private void initTopLinks(int id){
        Intent intent = new Intent(mainActivity,ArticleActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("idOfArticle",String .valueOf(topStories[id].getId()));
        intent.putExtra("id",bundle);
        startActivity(intent);
    }
    /**
     * 初始化查看前一天新闻与后一天新闻的监听器
     */
    private void initDateListener() {
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
                    titleDate.setText(Util.parseDate(String.valueOf(dateControl.getCursor())));
                    /*
                    StoryList = new ArrayList<NewsInfo>();
                    不能使用上述方法 因为这样的话等于重新建立了一个对象 该对象并不是观察者
                     */
                    adapter = new StoryAdapter(MainActivity.this, R.layout.story_item, StoryList);
                    StoryList.clear();
                    StoryList.addAll(zhihuDailyDB.loadStory(String.valueOf(dateControl.getCursor())));
                    listView.setAdapter(adapter);
                    /*
                    从数据库中取出来的对象 只有配图的url 而没有配图的图片 所以得去服务器请求
                     */
                    adapter.notifyDataSetChanged();//先出现文字 再开始请求图片
                    getPicFromNet();
                } else
                {
                    titleDate.setText(Util.parseDate(String.valueOf(dateControl.getCursor())));
                    dateControl.addOneDay();
                    getStoryUrl(String.valueOf(dateControl.getCursor()));
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
                        titleDate.setText(Util.parseDate(String.valueOf(dateControl.getCursor())));
                    /*
                    StoryList = new ArrayList<NewsInfo>();
                    不能使用上述方法 因为这样的话等于重新建立了一个对象 该对象并不是观察者
                    无法使用notifyDataSetChanged()
                     */
                        // ... Modify adapter ... do anything else you need to do
                        // To clear the recycled views list :
                        adapter = new StoryAdapter(MainActivity.this, R.layout.story_item, StoryList);
                        StoryList.clear();
                        StoryList.addAll(zhihuDailyDB.loadStory(String.valueOf(dateControl.getCursor())));
                        for (int i = 0; i < StoryList.size(); i++)
                        {
                            Log.d("whatsWrong", StoryList.get(i).toString());
                        }
                        listView.setAdapter(adapter);
                    /*
                    从数据库中取出来的对象 只有配图的url 而没有配图的图片 所以得去服务器请求
                     */
                        adapter.notifyDataSetChanged();//先出现文字 再开始请求图片
                        getPicFromNet();
                    } else
                    {
                        dateControl.addOneDay();
                        titleDate.setText(Util.parseDate(String.valueOf(dateControl.getCursor())));
                        dateControl.subOneDay();
                        getStoryUrl(String.valueOf(dateControl.getCursor()));
                    }
                }

            }
        });
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

    /**
     * Activity生命周期中，onStart, onResume, onCreate都不是真正visible的时间点，
     * 真正的visible时间点是onWindowFocusChanged()函数被执行时。
     * 译注：从onWindowFocusChanged被执行起，用户可以与应用进行交互了，而这之前，对用户的操作需要做一点限制。
     * <p>
     * 这个onWindowFocusChanged指的是这个Activity得到或者失去焦点的时候 就会call
     * 也就是说 如果你想要做一个Activity一加载完毕，就触发什么的话 完全可以用这个！！！
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus)
        {
            //获取listview的高度   那么bottom开始的位置是mTranslateY+height值
            int height = listView.getHeight();
            mBottomHeight = mTranslateY + height;
            showHideTitle(true, 0);
        }
    }


    private void setListViewListener() {
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount)
            {
                //Log.d("onTouch","onScroll");
                //判断当前是否在显示list的第一项数据
                mIsfirstVisible = firstVisibleItem == 0;
                //手掼滑动太快时非显示第一项还显示标题时，隐藏掉标题
                if (mIsShowTitle && !mIsfirstVisible)
                {
                    showHideTitle(true, 500);
                }
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Story story = StoryList.get(position);
                Toast.makeText(mainActivity, story.getTitle(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(mainActivity, ArticleActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("idOfArticle", String.valueOf(story.getId()));
                intent.putExtra("id", bundle);
                startActivity(intent);

            }
        });
        listView.setOnTouchListener(new View.OnTouchListener() {
            private float lastX;
            private float lastY;
            boolean isChange = false;

            /*
            通过给listview设置touch listener，
            如果手是向下滑动的(Y比X移动距离大)且滑动距离足够大时，判断是向下反之亦然是向上。
            event move过程会有多次回调，为了保证在一次dowm 向下滑动时，需要在down 时设置标志，
            来保证一次down 向下滑动时只调用动画一次 做显示标题动作。
             */
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("onTouch", "start");
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        lastX = event.getX();
                        lastY = event.getY();
                        isChange = false;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float x = event.getX();
                        float y = event.getY();
                        float xGapDistance = Math.abs(x - lastX);//移动的距离
                        float yGapDistance = Math.abs(y - lastY);
                        boolean isDown = yGapDistance > 4;
                        //没有显示标题时，且是向下的，就显示
                        //boolean isShow = yGapDistance > 8 && xGapDistance < 8 && !mIsShowTitle && isDown;
                        //boolean isHide = yGapDistance > 8 && xGapDistance < 8 && mIsShowTitle && !isDown;
                        boolean isShow = !mIsShowTitle && isDown;
                        boolean isHide = mIsShowTitle && !isDown;
                        Log.d("onTouchShow", String.valueOf(isShow));
                        Log.d("onTouchHide", String.valueOf(isHide));
                        lastX = x;
                        lastY = y;
                        //一次down，只变化一次，防止一次滑动时抖动下，造成某一个的向下时,y比lastY小
                        if (!isChange && mIsfirstVisible && isHide)
                        {
                            // 显示此标题
                            showHideTitle(true, 500);
                            Log.d("onTouchInvokeTrue", "I'm in true");
                            isChange = true;
                        }//显示标题时，且是向上的，就隐蔽
                        else if (!isChange && mIsfirstVisible && isShow)
                        {
                            // 隐蔽标题
                            showHideTitle(false, 500);
                            Log.d("onTouchInvokeFalse", "I'm in false");
                            isChange = true;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        //if(!mIsTouchHandeled){
                        int position = listView.pointToPosition((int) event.getX(), (int) event.getY());
                        if (position != ListView.INVALID_POSITION)
                        {
                            listView.performItemClick(listView.getChildAt(position - listView.getFirstVisiblePosition()), position, listView.getItemIdAtPosition(position));
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
            /*
            ofFloat的第四个参数是控件最后所在的位置 全都设置为0f是因为
            他们在layout_xml中本身已经定义了位置了
            如果没有 则要传入类似 getResources().getDimension(R.dimen.top_bar)
             */
            ObjectAnimator.ofFloat(slidingSwitcherView, "translationY", slidingSwitcherView.getTranslationY(),
                    0f).setDuration(duration).start();
            ObjectAnimator.ofFloat(listView, "translationY", listView.getTranslationY(),
                    0f).
                    setDuration(duration).start();
            ObjectAnimator.ofFloat(topBar, "translationY", topBar.getTranslationY(), 0f).setDuration(duration).start();
            ObjectAnimator.ofFloat(bottom, "translationY", bottom.getTranslationY(), -bottom.getTranslationY()).setDuration(duration).start();
            mIsShowTitle = true;
        } else
        {//隐藏时，把标题隐藏了，底部出来了
            ObjectAnimator.ofFloat(slidingSwitcherView, "translationY", slidingSwitcherView.getTranslationY(),
                    -slidingSwitcherView.getTranslationY()).setDuration(duration).start();
            ObjectAnimator.ofFloat(listView, "translationY", listView.getTranslationY(),
                    -getResources().getDimension(R.dimen.listView)).setDuration(duration).start();
            ObjectAnimator.ofFloat(topBar, "translationY", topBar.getTranslationY(), -topBar.getTranslationY()).
                    setDuration(duration).start();
            ObjectAnimator.ofFloat(bottom, "translationY", bottom.getTranslationY(),
                    getResources().getDimension(R.dimen.listView) + listView.getHeight() - bottom.getHeight()).
                    setDuration(duration).start();
        }
    }

    private void doLogic() {
        //将标题栏+viewPage高度290dp转成显示的高度
        mTranslateY = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 290, getResources().getDisplayMetrics());
        DeviceInfo deviceInfo = Util.getDevicesPix(mainActivity);
        deviceHeight = (float) deviceInfo.height / deviceInfo.density;
    }

}


