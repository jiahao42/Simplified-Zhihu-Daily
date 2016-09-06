package james.com.simplezhihudaily.db;

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
    private static final String COLUMN_Attr = "attr";
    private static final String COLUMN_themeID = "categoryID";
    public static final String TABLE_NAME = "ZhihuNews";
    private static final String CREATE_TABLE = "create table " + TABLE_NAME + " ("
            + COLUMN_ID + " int not null, "//id代表文章 id必须是唯一的
            + COLUMN_TITLE + " text not null, "
            + COLUMN_CONTENT + " text, "//content 有可能为null
            + COLUMN_DATE + " text not null, "
            + COLUMN_IMG + " text,"//img的url
            + COLUMN_Attr + " int,"//文章属性 可以是下拉列表的文章(0) 或者顶部的文章(1) 或者主题日报的文章(2)
            + COLUMN_themeID + " int)";

    /**
     * 这个用来存栏目信心 事实证明 用SharedPreference来存效果不好
     */
    private static final String COLUMN_ID_THEME = "id";
    private static final String COLUMN_DESC_THEME = "description";
    private static final String COLUMN_NAME_THEME = "name";
    private static final String COLUMN_IMG_THEME = "img";
    public static final String TABLE_NAME_THEME = "ThemeTable";
    private static final String CREATE_TABLE_THEME = "create table " + TABLE_NAME_THEME + " ( "
            + COLUMN_ID_THEME + " int unique not null, "
            + COLUMN_DESC_THEME + " text , "
            + COLUMN_IMG_THEME + " text, "
            + COLUMN_NAME_THEME + " text not null)";

    /**
     * 这个表用来存储文章所有的评论信息
     */
    private static final String COLUMN_COMMENT_AUTHOR = "author";
    private static final String COLUMN_COMMENT_CONTENT = "content";
    private static final String COLUMN_COMMENT_AVATAR = "avatar";
    private static final String COLUMN_COMMENT_TIME = "time";
    private static final String COLUMN_COMMENT_LIKES = "likes";
    public static final String TABLE_NAME_COMMENT = "CommentTable";
    private static final String COLUMN_COMMENT_ID = "id";
    private static final String COLUMN_COMMENT_TYPE = "type";
    private static final String CREATE_TABLE_COMMENT = "create table " + TABLE_NAME_COMMENT + " ( "
            + COLUMN_COMMENT_ID + " int not null unique, "
            + COLUMN_COMMENT_AUTHOR + " text not null, "
            + COLUMN_COMMENT_AVATAR + " text, "
            + COLUMN_COMMENT_CONTENT + " text not null, "
            + COLUMN_COMMENT_TIME + " text not null, "
            + COLUMN_COMMENT_LIKES + " int not null, "
            + COLUMN_COMMENT_TYPE + " int not null ) ";

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
        db.execSQL(CREATE_TABLE_THEME);
        db.execSQL(CREATE_TABLE_COMMENT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

