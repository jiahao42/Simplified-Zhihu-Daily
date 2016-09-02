package james.com.simplezhihudaily.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import james.com.simplezhihudaily.Model.Story;
import james.com.simplezhihudaily.Model.Symbol;
import james.com.simplezhihudaily.Model.ThemeStory;
import james.com.simplezhihudaily.Model.TopStory;


public class ZhihuDailyDB {
    //db name
    private static final String DB_NAME = "ZhihuDaily";
    //db version
    private static final int VERSION = 1;
    private static ZhihuDailyDB zhihuDailyDB;
    private SQLiteDatabase db;

    /**
     * use the design pattern -- Singleton
     *
     * @param context
     */
    private ZhihuDailyDB(Context context) {
        ZhihuDailyDBhelper dbHelper = new ZhihuDailyDBhelper(context, DB_NAME, null, VERSION);
        db = dbHelper.getWritableDatabase();
    }

    /**
     * get the instance of ZhihuDailyDB
     *
     * @param context context
     * @return db instance
     */
    public synchronized static ZhihuDailyDB getInstance(Context context) {
        if (zhihuDailyDB == null)
        {
            zhihuDailyDB = new ZhihuDailyDB(context);
        }
        return zhihuDailyDB;
    }

    //preserve instances to db
    public void saveStory(Story story) {
        if (story != null)
        {
            ContentValues values = new ContentValues();
            values.put("title", story.getTitle());
            values.put("date", story.getDate());
            values.put("img", story.getUrls());
            values.put("id", story.getId());
            values.put("attr", story.getAttr());
            values.put("content", story.getContent());
            db.insert("ZhihuNews", null, values);
        }
    }

    public void saveTopStory(TopStory story) {
        if (story != null)
        {
            ContentValues values = new ContentValues();
            values.put("title", story.getTitle());
            values.put("date", story.getDate());
            values.put("img", story.getmyUrls());
            values.put("id", story.getId());
            values.put("attr", story.getAttr());
            values.put("content", story.getContent());
            db.insert("ZhihuNews", null, values);
        }
    }

    public void saveThemeStory(ThemeStory themeStory) {
        if (themeStory != null)
        {
            ContentValues values = new ContentValues();
            values.put("title", themeStory.getTitle());
            values.put("date", themeStory.getDate());
            values.put("img", themeStory.getUrls());
            values.put("id", themeStory.getId());
            values.put("attr", themeStory.getAttr());
            values.put("content", themeStory.getContent());
            values.put("categoryID", themeStory.getCategoryID());
            db.insert("ZhihuNews", null, values);
        }
    }

    /**
     * if user open an article, then insert the content of article
     *
     * @param id      the id of the article
     * @param content content of the article
     */
    public void insertContent(String id, String content) {
        ContentValues values = new ContentValues();
        values.put("content", content);
        db.update(ZhihuDailyDBhelper.TABLE_NAME, values, "id = ?", new String[]{id});
    }

    /**
     * query the id and if it has content,return ture
     *
     * @param id id of article
     * @return true or false
     */
    public boolean hasContent(String id) {
        Cursor cursor = db.query(ZhihuDailyDBhelper.TABLE_NAME, null, "id = ?", new String[]{id}, null, null, null);
        if (cursor != null && cursor.moveToFirst())
        {
            while (!cursor.isAfterLast())
            {
                if (cursor.getString(cursor.getColumnIndex("content")) != null)
                {
                    cursor.close();
                    return true;
                }
                cursor.moveToNext();
            }
            cursor.close();
        }
        return false;
        /*
        cursor.moveToFirst();
        if (cursor.getString(cursor.getColumnIndex("content")) == null){
            cursor.close();
            return false;
        }else {
            cursor.close();
            return true;
        }
        */
    }

    /**
     * 判断数据库中是否存在今日的topStory
     *
     * @param date 今日日期
     * @return 若没找到则返回false 找到则返回ture
     */
    public boolean topStoryInDB(String date) {//注意根据attr字段判断
        Cursor cursor = db.rawQuery("select * from " + ZhihuDailyDBhelper.TABLE_NAME +
                " where date = ? and attr = ?",
                new String[]{date, String.valueOf(Symbol.TopStory)});
        if (cursor.getCount() == 0)
        {
            cursor.close();
            return false;
        } else
        {
            cursor.close();
            return true;
        }
    }

    public TopStory[] loadTopStory(String date){
        Cursor cursor = db.query(ZhihuDailyDBhelper.TABLE_NAME,
                null,"date = ? AND attr = ?",new String[]{date,String.valueOf(Symbol.TopStory)},
                null,null,null,null);
        cursor.moveToFirst();
        TopStory[] list = new TopStory[cursor.getCount()];
        int count = 0;
        do
        {
            TopStory topStory = new TopStory();
            topStory.setDate(date);
            topStory.setTitle(cursor.getString(cursor.getColumnIndex("title")));
            topStory.setId(cursor.getInt(cursor.getColumnIndex("id")));
            topStory.setmyUrls(cursor.getString(cursor.getColumnIndex("img")));
            topStory.setContent(cursor.getString(cursor.getColumnIndex("content")));
            list[count++] = topStory;
        }while (cursor.moveToNext());
        return list;
    }

    /**
     * to see how many news is already in the db
     *
     * @param date   date
     * @param length the latest number of news
     * @return should insert how many news
     */
    public int isInserted(String date, int length) {
        //Cursor cursor = db.query(ZhihuDailyDBhelper.TABLE_NAME, null, "date = ?", new String[]{date}, null, null, null, null);
        Cursor cursor = db.rawQuery("select * from " + ZhihuDailyDBhelper.TABLE_NAME +
                        " where date = ? and attr = ?",
                new String[]{date, String.valueOf(Symbol.Story)});
        cursor.moveToFirst();
        int count = cursor.getCount();
        cursor.close();
        Log.d("how many in the db", String.valueOf(count));
        return length - count;//直接返回差距个数 最少为0 最多不限
    }

    /**
     * get article from db through id
     *
     * @param id id of the article
     * @return
     */
    public String getArticle(String id) {
        Cursor cursor = db.query(ZhihuDailyDBhelper.TABLE_NAME, null, "id = ?",
                new String[]{id}, null, null, null, null);
        if (cursor.getCount() == 0)
        {
            cursor.close();
            return null;
        } else
        {
            Log.d("getArticle", String.valueOf(cursor.getColumnIndex("content")));
            cursor.moveToFirst();
            String htmlString = cursor.getString(cursor.getColumnIndex("content"));
            cursor.close();
            return htmlString;
        }
    }

    public boolean hasTheDate(String certainDate) {
        Cursor cursor = db.query(ZhihuDailyDBhelper.TABLE_NAME, null, "date = ?",
                new String[]{certainDate}, null, null, null, null);
        if (cursor.getCount() == 0)
        {
            cursor.close();
            return false;
        } else
        {
            cursor.close();
            return true;
        }
    }

    /**
     * read the news of a certain day
     *
     * @param date which day do you want to read ?
     */
    public List<Story> loadStory(String date) {
        Log.d("TheDateIWantToReadInDB", date);
        List<Story> list = new ArrayList<>();
        Cursor cursor = db.query(ZhihuDailyDBhelper.TABLE_NAME, null, "date = ? AND attr = ?",
                new String[]{date,String.valueOf(Symbol.Story)}, null, null, null, null);
        Log.d("HowManyDate", String.valueOf(cursor.getCount()));
        if (cursor.moveToFirst())
        {
            do
            {
                Story story = new Story();
                story.setId(cursor.getInt(cursor.getColumnIndex("id")));
                story.setDate(date);
                story.setContent(cursor.getString(cursor.getColumnIndex("content")));
                Log.d("whatsInTheDB", cursor.getString(cursor.getColumnIndex("img")));
                story.setUrls(cursor.getString(cursor.getColumnIndex("img")));
                story.setTitle(cursor.getString(cursor.getColumnIndex("title")));
                Log.d("whatsInTheDB", story.toString());
                list.add(story);
            } while (cursor.moveToNext());
        }
        if (null != cursor)
        {
            cursor.close();
        }
        return list;
    }
}
