package james.com.simplezhihudaily.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import james.com.simplezhihudaily.Model.Comment;
import james.com.simplezhihudaily.Model.Story;
import james.com.simplezhihudaily.Model.Symbol;
import james.com.simplezhihudaily.Model.Theme;
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

    /**
     * 一次存一个Story对象进数据库
     * @param story
     */
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
            db.insert(ZhihuDailyDBhelper.TABLE_NAME, null, values);
        }
    }

    /**
     * 将一个topStory存到数据库
     * @param topStory
     */
    public void saveTopStory(TopStory topStory) {
        if (topStory != null)
        {
            ContentValues values = new ContentValues();
            values.put("title", topStory.getTitle());
            values.put("date", topStory.getDate());
            values.put("img", topStory.getmyUrls());
            values.put("id", topStory.getId());
            values.put("attr", topStory.getAttr());
            values.put("content", topStory.getContent());
            db.insert(ZhihuDailyDBhelper.TABLE_NAME, null, values);
        }
    }

    /**
     * 将一个themeStory存到数据库
     * @param themeStory
     */
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
            db.insert(ZhihuDailyDBhelper.TABLE_NAME, null, values);
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
    public boolean isTopStoryInDB(String date) {//注意根据attr字段判断
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

    /**
     * 从数据库中取出所有topStory对象
     * @param date  日期
     * @return  对象的集合
     */
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
    public int isAllStoryInserted(String date, int length) {
        //Cursor cursor = db.query(ZhihuDailyDBhelper.TABLE_NAME, null, "date = ?", new String[]{date}, null, null, null, null);
        Cursor cursor = db.rawQuery("select * from " + ZhihuDailyDBhelper.TABLE_NAME +
                        " where date = ? and attr = ?",
                new String[]{date, String.valueOf(Symbol.Story)});
        cursor.moveToFirst();
        int count = cursor.getCount();
        cursor.close();
        Log.d("how many Story in DB", String.valueOf(count));
        return length - count;//直接返回差距个数 最少为0 最多不限
    }

    /**
     * 对比请求到的专栏文章条目数与数据库中的，并返回差值，防止重复存储
     * @param date  要查询的日期
     * @param length    请求到的文章条目数
     * @param categoryID    要查询的栏目ID
     * @return      数量差值
     */
    public int isAllThemeStoryInserted(String date,int length,int categoryID){
        Cursor cursor = db.query(ZhihuDailyDBhelper.TABLE_NAME,null,"date = ? AND categoryID = ? AND attr = ?",new String[]{
                date,String.valueOf(categoryID),String.valueOf(Symbol.ThemeStory)},null,null,null);
        cursor.moveToFirst();
        int count = cursor.getCount();
        cursor.close();
        Log.d("how many Theme in DB",String.valueOf(count));
        return length - count;
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

    /**
     * 查看数据库中是否有特定日期的数据
     * @param certainDate   日期
     * @return  boolean
     */
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

    /**
     * 存储Theme到数据库
     * @param theme
     */
    public void saveThemes(Theme theme){
        ContentValues contentValues = null;
        if (theme != null)
        {
            contentValues = new ContentValues();
            contentValues.put("id", theme.getId());
            contentValues.put("description", theme.getDescription());
            contentValues.put("name", theme.getName());
            contentValues.put("img", theme.getUrl());
        }
        db.insert(ZhihuDailyDBhelper.TABLE_NAME_THEME, null, contentValues);
    }

    /**
     * 获得数据库中的栏目数 若与请求到的不相同则进行插入操作
     * @return  栏目数量
     */
    public int howManyThemeInDB(){
        Cursor cursor = db.query(ZhihuDailyDBhelper.TABLE_NAME_THEME,null,null,null,null,null,null);
        cursor.moveToFirst();
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    /**
     * get themes from db
     * @return  themes
     */
    public List<Theme> getTheme(){
        List<Theme> list = new ArrayList<>();
        Cursor cursor = db.query(ZhihuDailyDBhelper.TABLE_NAME_THEME,null,null,null,null,null,null);
        cursor.moveToFirst();
        do
        {
            Theme theme = new Theme();
            theme.setId(cursor.getInt(cursor.getColumnIndex("id")));
            theme.setDescription(cursor.getString(cursor.getColumnIndex("description")));
            theme.setName(cursor.getString(cursor.getColumnIndex("name")));
            theme.setUrl(cursor.getString(cursor.getColumnIndex("img")));
            list.add(theme);
        }while (cursor.moveToNext());
        cursor.close();
        return list;
    }

    /**
     * 下面是管理 评论Comment 的方法
     */

    public boolean isCommentInserted(String id){
        Cursor cursor = db.query(ZhihuDailyDBhelper.TABLE_NAME_COMMENT,null,"id = ?",new String[]{id},null,null,null);
        cursor.moveToFirst();
        if (cursor.getCount() == 0){
            cursor.close();
            return false;
        }else {
            cursor.close();
            return true;
        }
    }

    public void saveComments(Comment comment){
        ContentValues contentValues = new ContentValues();
        contentValues.put("author",comment.getAuthor());
        contentValues.put("avatar",comment.getAvatar());
        contentValues.put("time",comment.getTime());
        contentValues.put("content",comment.getContent());
        contentValues.put("likes",comment.getLikes());
        contentValues.put("id",comment.getId());
        db.insert(ZhihuDailyDBhelper.TABLE_NAME_COMMENT,null,contentValues);
    }

    public List<Comment> getComments(String id){
        Cursor cursor = db.query(ZhihuDailyDBhelper.TABLE_NAME_COMMENT,null,"id = ?",new String[]{id},null,null,null);
        List<Comment> comments = new ArrayList<>();
        cursor.moveToFirst();
        do
        {
            Comment comment = new Comment();
            comment.setAuthor(cursor.getString(cursor.getColumnIndex("author")));
            comment.setAvatar(cursor.getString(cursor.getColumnIndex("avatar")));
            comment.setContent(cursor.getString(cursor.getColumnIndex("content")));
            comment.setId(cursor.getInt(cursor.getColumnIndex("id")));
            comment.setLikes(cursor.getString(cursor.getColumnIndex("likes")));
            comment.setTime(cursor.getString(cursor.getColumnIndex("time")));
            comments.add(comment);
        }while (cursor.moveToNext());
        cursor.close();
        return comments;
    }

}
