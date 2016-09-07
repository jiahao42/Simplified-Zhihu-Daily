package james.com.simplezhihudaily.Model;

import java.util.Map;

public class Symbol{
    /**
     * 网络请求成功
     */
    public static final int RECEIVE_SUCCESS = 1;
    /**
     * 网络请求失败
     */
    public static final int RECEIVER_FAILED = 0;
    /**
     * 网络请求超时
     */
    public static final int RECEIVER_TIMEOUT = -1;
    /**
     * 文章是从数据库中取出来的
     */
    public static final int GET_ARTICLE_FROM_DB = 999;
    /**
     * 列表文章的标识
     */
    public static final int Story = 0;
    /**
     * 顶部文章的标识
     */
    public static final int TopStory = 1;
    /**
     * 主题文章的标识
     */
    public static final int ThemeStory = 2;
    /**
     * 评论的标识
     */
    public static final int LongComment = 0;
    public static final int ShortComment = 1;
    public static Map<String,String> cookie;
}
/*
Enum类在这里似乎是画蛇添足
public enum Symbol {
    RECEIVE_SUCCESS(1),RECEIVE_FAILED(0),RECEIVER_TIMEOUT(-1);
    private int code;
    private Symbol(int code){
        this.code = code;
    }
    public int toInt(){
        return code;
    }
}
*/