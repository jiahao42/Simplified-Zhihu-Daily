package james.com.simplezhihudaily.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import james.com.simplezhihudaily.Model.NewsInfo;


public class ZhihuDailyDB {
    //db name
    public static final String DB_NAME = "ZhihuDaily";
    //db version
    public static final int VERSION = 1;
    public static ZhihuDailyDB zhihuDailyDB;
    private SQLiteDatabase db;

    /**
     * use the design pattern -- Singleton
     * @param context
     */
    private ZhihuDailyDB(Context context){
        ZhihuDailyDBhelper dbHelper = new ZhihuDailyDBhelper(context, DB_NAME, null,VERSION);
        db = dbHelper.getWritableDatabase();
    }

    /**
     * get the instance of ZhihuDailyDB
     * @param context context
     * @return db instance
     */
    public synchronized static ZhihuDailyDB getInstance(Context context){
        if (zhihuDailyDB == null){
            zhihuDailyDB = new ZhihuDailyDB(context);
        }
        return zhihuDailyDB;
    }
    //preserve instances to db
    public void saveBaseNews(NewsInfo newsInfo){
        if (newsInfo != null){
            ContentValues values = new ContentValues();
            values.put("title", newsInfo.getTitle());
            values.put("date", newsInfo.getDate());
            values.put("img",newsInfo.getUrls());
            values.put("id",newsInfo.getId());
            values.put("content",newsInfo.getContent());
            db.insert("ZhihuNews" , null, values);
        }
    }

    /**
     * if user open an article, then insert the content of article
     * @param id    the id of the article
     * @param content   content of the article
     */
    public void insertContent(String id,String content){
        ContentValues values = new ContentValues();
        values.put("content",content);
        db.update(ZhihuDailyDBhelper.TABLE_NAME,values,"id = ?",new String[] {id});
    }
    public  boolean isInserted(String date){
        Cursor cursor = db.query(ZhihuDailyDBhelper.TABLE_NAME,null,"date = ?",new String[]{date},null,null,null,null);
        cursor.moveToFirst();
        if (cursor.moveToNext()){
            Log.d("isInserted","moveToNext_success");
            cursor.close();
            return true;
        }else {
            Log.d("isInserted","moveToNext_failed");
            cursor.close();
            return false;
        }
    }
    /**
     * read the news of a certain day
     * @param date which day do you want to read ?
    */
    public List<NewsInfo> loadNewsInfo(String  date){
        List<NewsInfo> list = new ArrayList<>();
        Cursor cursor = db.query(ZhihuDailyDBhelper.TABLE_NAME,null,"date = ?",new String[] {date,null,null,null,null},null,null,null,null);
        if (cursor.moveToFirst()){
            do{
                NewsInfo newsInfo = new NewsInfo();
                newsInfo.setId(cursor.getInt(cursor.getColumnIndex("id")));
                newsInfo.setDate(date);
                newsInfo.setContent(cursor.getString(cursor.getColumnIndex("content")));
                newsInfo.setUrls(cursor.getString(cursor.getColumnIndex("img")));
                newsInfo.setTitle(cursor.getString(cursor.getColumnIndex("title")));
                list.add(newsInfo);
            }while (cursor.moveToNext());
        }
        if (null != cursor){
            cursor.close();
        }
        return list;
    }
}
