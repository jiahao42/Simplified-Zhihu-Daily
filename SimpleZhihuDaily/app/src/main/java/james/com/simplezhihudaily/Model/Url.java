package james.com.simplezhihudaily.Model;

public class Url {
    /**
     * 得到往日的新闻 在最后加上日期
     */
    public static final String getNewsBefore = "http://news-at.zhihu.com/api/4/news/before/";
    /**
     * 得到今日的新闻
     */
    public static final String getLatestNews = "http://news-at.zhihu.com/api/4/news/latest";
    /**
     * 得到栏目的主题们
     */
    public static final String getThemes = "http://news-at.zhihu.com/api/4/themes";
    /**
     * 得到评论总数 赞数等数据 在最后加上文章id
     */
    public static final String getStoryExtra = "http://news-at.zhihu.com/api/4/story-extra/";
    /**
     * 注意
     * 如何得到文章的长评论与短评论
     * 在getComment之后加上id 然后再加上long-comments 或者 short-comments
     */
    public static final String getComment = "http://news-at.zhihu.com/api/4/story/";
    /**
     * 加上栏目id即可得到栏目信息
     */
    public static final String getThemeStory = "http://news-at.zhihu.com/api/4/theme/";
}
