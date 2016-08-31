package james.com.simplezhihudaily.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ZhihuDailyDBhelper extends SQLiteOpenHelper {
    //create province
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_CONTENT = "content";
    private static final String COLUMN_IMG = "img";
    private static final String COLUMN_ID = "id";
    public static final String TABLE_NAME = "ZhihuNews";
    private static final String CREATE_TABLE = "create table " + TABLE_NAME + " ("
            + COLUMN_ID + " int unique, "//id代表文章 id必须是唯一的
            + COLUMN_TITLE + " text not null, "
            + COLUMN_CONTENT + " text, "//content 有可能为null
            + COLUMN_DATE + " text not null, "
            + COLUMN_IMG + " text)";//img的url

    public ZhihuDailyDBhelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    /**
     * -- 调用时机: 当使用getReadableDatabase()方法 获取数据库 实例 的时候, 如果数据库不存在, 就会调用这个方法;
     * -- 方法内容 : 重写该方法一般 将 创建数据库表的 execSQL()方法 和 初始化表数据的一些 insert()方法写在里面;
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

