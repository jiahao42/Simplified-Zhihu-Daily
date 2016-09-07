package james.com.simplezhihudaily.Model;

public class RegexForZhihu {
    /**
     * 获取头像地址以及昵称
     * 形式如下：
     * <img class="Avatar Avatar--l" src="https://pic2.zhimg.com/26be3aefd48c1ed88fa1b597915d7c19_l.jpg" srcset="https://pic2.zhimg.com/26be3aefd48c1ed88fa1b597915d7c19_xl.jpg 2x" alt="Pyjamas" />
     * 头像地址为group2  昵称为group6
     */
    public static final String getAvatar = "(Avatar--l\" src=\")(.*)(\" srcset=\")(.*)(alt=\")(.*)(\")";
    /**
     * 获取用户的赞数
     * 形式如下：
     * <strong>115</strong>赞同</span>
     * group2
     */
    public static final String getLikes = "(<strong>)(.*)(</strong>赞同</span>)";
    /**
     * 用户获得的感谢数
     * 形式如下：
     * <strong>3</strong>感谢</span>
     * group2
     */
    public static final String getThanks = "(strong>)(.*)(</strong>感谢</span>)";
    /**
     * 获取用户的真实ID
     * 形式如下：
     * class="zu-top-nav-link" href="/people/cai-jia-hao-77"> 我<span class="mobi-arrow"></span>
     * group2
     */
    public static final String getSpecialID = "(href=\")(.*)(\"> 我<)";
    /**
     * 登录时需获取官网的xsrf值
     * 形式如下：
     * <input type="hidden" name="_xsrf" value="eb1735c893ed06df9e32986ef0c728f5"/>
     * group2
     */
    public static final String getXSRF = "(name=\"_xsrf\" value=\")(.*)(\">)";
    /**
     * 获取个性签名
     * 形式如下：
             <div class="bio ellipsis" title="特立独行的沉默的大多数">
             特立独行的沉默的大多数
     * group4
     */
    public static final String getBio = "(<div class=\"bio ellipsis\" title=\")(.*)(\">\\n)(.*)";
    /**
     * 获取用户提问数
     * 形式如下：
     * <a class="item " href="/people/cai-jia-hao-77/asks"> 提问 <span class="num">1</span> </a>
     * group2
     */
    public static final String getQuestion = "(提问 <span class=\"num\">)(.*)(</span>)";

    /**
     * 获取用户的回答数
     * 形式如下：
     * 回答 <span class="num">28</span> </a>
     * group2
     */
    public static final String getAnswer = "(回答 <span class=\"num\">)([0-9]*)(</span>)";

    /**
     * 获取用户的文章数
     * 形式如下：
     * 文章 <span class="num">0</span> </a>
     * group2
     */
    public static final String getArticle = "(文章 <span class=\\\"num\\\">)([0-9]*)(</span>)";

    /**
     * 获得用户的收藏数据
     * 形式如下：
     * 收藏 <span class="num">5</span> </a>
     * group2
     */
    public static final String getFavourite = "(收藏 <span class=\\\"num\\\">)([0-9]*)(</span>)";

    /**
     * 获取用户关注的人
     * 形式如下：
     * 关注了</span><br> <strong>348</strong><label> 人</label> </a>
     * group2
     */
    public static final String getFollowed = "(关注了</span><br> <strong>)(([0-9])*)(</strong>)";

    /**
     * 获取关注用户的人
     * 形式如下：
     * 关注者</span><br> <strong>7</strong><label> 人</label> </a>
     * group2
     */
    public static final String getFollower = "(关注者</span><br> <strong>)(([0-9])*)(</strong>)";
}
