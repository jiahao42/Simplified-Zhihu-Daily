package james.com.simplezhihudaily.View;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import james.com.simplezhihudaily.Model.DeviceInfo;
import james.com.simplezhihudaily.Model.StoryAdapter;
import james.com.simplezhihudaily.Model.Story;
import james.com.simplezhihudaily.Model.Symbol;
import james.com.simplezhihudaily.Model.TopStory;
import james.com.simplezhihudaily.Model.Url;
import james.com.simplezhihudaily.R;
import james.com.simplezhihudaily.Util.Util;
import james.com.simplezhihudaily.db.ZhihuDailyDB;

import static james.com.simplezhihudaily.R.dimen.listView;

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
    private String[] storyPicUrls;
    private String[] topStoryPicUrls;
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


    private boolean mIsShowTitle = false;
    private float mTranslateY;
    private boolean mIsfirstVisible = true;
    private float mBottomHeight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_try);
        initWidget();
        //doLogic();
        getNewsUrl("latest");
    }

    private final Handler getUrlArrayHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            if (message.what == Symbol.RECEIVE_SUCCESS)
            {
                storyPicUrls = new String[newsList.size()];
                Resources res = getResources();
                Bitmap bitmap = BitmapFactory.decodeResource(res, R.drawable.error);
                /**
                 * 下面得到Story的Url
                 */
                for (int i = 0; i < newsList.size(); i++)
                {
                    Log.d("TAG", newsList.get(i).getUrls());
                    storyPicUrls[i] = newsList.get(i).getUrls();
                    newsList.get(i).setBitmap(bitmap);
                    adapter.notifyDataSetChanged();//将notify放进了循环 如果网特别差 图片还可以一张张跳出来
                }
                getStoryPics();
                /**
                 * 下面得到topStory的Urls
                 */
                topStoryPicUrls = new String[numberOfTopStories];
                for (int i = 0; i < numberOfTopStories; i++)
                {
                    topStoryPicUrls[i] = topStories[i].getmyUrls();
                }
                getTopStoryPics();
            }
        }
    };

    /**
     * 根据传入的日期来得到当天的新闻列表
     *
     * @param certainDate 要查询的日期
     */
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
                                if (zhihuDailyDB.topStoryInDB(date))
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
                            int count = zhihuDailyDB.isInserted(date, story.length);
                            for (int i = 0; i < count; i++)
                            {
                                Log.d("TAG", "saving..." + i);
                                zhihuDailyDB.saveStory(story[i]);//注意新来的新闻在头部
                            }
                            /*
                            此处得到了所有的带有基本信息的对象集合
                             */
                            adapter = new james.com.simplezhihudaily.Model.StoryAdapter(MainActivity.this, R.layout.news_item, newsList);
                            newsList.clear();
                            Collections.addAll(newsList, story);
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
                mQueue.add(jsonObjectRequest);
                mQueue.start();
            }
        }).start();
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
                            newsList.get(count).setBitmap(response);
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
                    mQueue.add(imageRequest);
                    //Log.d("imageRequest", "added" + count);
                }
                mQueue.start();
            }
        }).start();
    }


    private void initWidget() {
        /**
         * 初始化控件
         */
        listView = (ListView) findViewById(R.id.title_list);
        beforeTheDay = (ImageView) findViewById(R.id.arrow_left);
        afterTheDay = (ImageView) findViewById(R.id.arrow_right);
        titleDate = (TextView) findViewById(R.id.title_date);
        titleText = (TextView) findViewById(R.id.title);
        topBar = (RelativeLayout) findViewById(R.id.top_bar);
        bottom = (TextView) findViewById(R.id.bottom);
        slidingSwitcherView = (SlidingSwitcherView) findViewById(R.id.slidingLayout);
        topImage = new Button[numberOfTopStories];
        topImage[0] = (Button) findViewById(R.id.btn1);
        topImage[1] = (Button) findViewById(R.id.btn2);
        topImage[2] = (Button) findViewById(R.id.btn3);
        topImage[3] = (Button) findViewById(R.id.btn4);
        topImage[4] = (Button) findViewById(R.id.btn5);
        titleText.bringToFront();
        zhihuDailyDB = ZhihuDailyDB.getInstance(mainActivity);
        adapter = new StoryAdapter(MainActivity.this, R.layout.news_item, newsList);
        listView.setAdapter(adapter);
        setListViewListener();
        initDateListener();

        refreshableView = (RefreshableView) findViewById(R.id.refreshable_view);
        refreshableView.setOnRefreshListener(new RefreshableView.PullToRefreshListener() {
            @Override
            public void onRefresh() {
                try
                {
                    getTopStoryFlag = true;
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
                    newsList = new ArrayList<NewsInfo>();
                    不能使用上述方法 因为这样的话等于重新建立了一个对象 该对象并不是观察者
                     */
                    adapter = new StoryAdapter(MainActivity.this, R.layout.news_item, newsList);
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
                    titleDate.setText(Util.parseDate(String.valueOf(dateControl.getCursor())));
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
                        titleDate.setText(Util.parseDate(String.valueOf(dateControl.getCursor())));
                    /*
                    newsList = new ArrayList<NewsInfo>();
                    不能使用上述方法 因为这样的话等于重新建立了一个对象 该对象并不是观察者
                    无法使用notifyDataSetChanged()
                     */
                        // ... Modify adapter ... do anything else you need to do
                        // To clear the recycled views list :
                        adapter = new james.com.simplezhihudaily.Model.StoryAdapter(MainActivity.this, R.layout.news_item, newsList);
                        newsList.clear();
                        newsList.addAll(zhihuDailyDB.loadStory(String.valueOf(dateControl.getCursor())));
                        for (int i = 0; i < newsList.size(); i++)
                        {
                            Log.d("whatsWrong", newsList.get(i).toString());
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
                        getNewsUrl(String.valueOf(dateControl.getCursor()));
                    }
                }

            }
        });
    }

    /**
     *
     */
    private void getPicFromNet() {
        storyPicUrls = new String[newsList.size()];
        Resources res = getResources();
        Bitmap bitmap = BitmapFactory.decodeResource(res, R.drawable.error);
        for (int i = 0; i < newsList.size(); i++)
        {
            Log.d("TAG", newsList.get(i).getUrls());
            storyPicUrls[i] = newsList.get(i).getUrls();
            newsList.get(i).setBitmap(bitmap);
            adapter.notifyDataSetChanged();//将notify放进了循环 如果网特别差 图片还可以一张张跳出来
        }
        getStoryPics();
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
                Story story = newsList.get(position);
                Toast.makeText(mainActivity, story.getTitle(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(mainActivity, ArticleActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("id", String.valueOf(story.getId()));
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
                Log.d("onTouch","start");
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





    private class StoryAdapter extends ArrayAdapter<Story> {
        private int resourceID;
        private View view;
        private ViewHolder viewHolder;
        private int length;
        public StoryAdapter(Context context, int textViewResourceID, List<Story> objects){
            super(context,textViewResourceID,objects);
            length = objects.size();
            resourceID = textViewResourceID;
        }
        public int getLength(){
            return length;
        }

        /**
         *
         * @param position
         * @param convertView   将之前加载好的布局进行缓存，以便之后可以重用
         * @param parent
         * @return
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            Story story = getItem(position);
            if (convertView == null){//如果不为空则直接对convertView进行重用
                view = LayoutInflater.from(getContext()).inflate(resourceID,null);
                viewHolder = new ViewHolder();
                viewHolder.title = (TextView)view.findViewById(R.id.title);
                viewHolder.imageView = (ImageView) view.findViewById(R.id.title_image);
                view.setTag(viewHolder);
                view.setOnTouchListener(new View.OnTouchListener() {
                    /*
                通过给listview设置touch listener，
                如果手是向下滑动的(Y比X移动距离大)且滑动距离足够大时，判断是向下反之亦然是向上。
                event move过程会有多次回调，为了保证在一次dowm 向下滑动时，需要在down 时设置标志，
                来保证一次down 向下滑动时只调用动画一次 做显示标题动作。
                 */
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        float lastX;
                        float lastY;
                        boolean isChange = false;
                        Log.d("onTouch","start");
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
            }else {
                view = convertView;
                viewHolder = (ViewHolder)view.getTag();
            }
            viewHolder.title.setText(story.getTitle());
            viewHolder.imageView.setImageBitmap(story.getBitmap());
            return view;
        }
        private class ViewHolder
        {
            TextView title;
            ImageView imageView;
        }

    }
}


