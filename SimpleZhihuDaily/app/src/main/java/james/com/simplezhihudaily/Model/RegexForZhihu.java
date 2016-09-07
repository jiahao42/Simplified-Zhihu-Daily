package james.com.simplezhihudaily.Model;

public class RegexForZhihu {
    /**
     * 获取头像地址
     * 形式如下：
     * <img class="Avatar Avatar--xs" src="https://pic2.zhimg.com/26be3aefd48c1ed88fa1b597915d7c19_xs.jpg" srcset="
     */
    public static final String getAvatar = "(Avatar--xs\" src=\")(.*)(\" srcset=\")";
    /**
     * 获取用户的赞数
     * 形式如下：
     * <strong>115</strong>赞同</span>
     */
    public static final String getLikes = "(<strong>)(.*)(</strong>赞同</span>)";
    /**
     * 获取用户的真实ID
     * 形式如下：
     * class="zu-top-nav-link" href="/people/cai-jia-hao-77"> 我<span class="mobi-arrow"></span>
     */
    public static final String getSpecialID = "(href=\")(.*)(\"> 我<)";
    /**
     * 登录时需获取官网的xsrf值
     * 形式如下：
     * <input type="hidden" name="_xsrf" value="eb1735c893ed06df9e32986ef0c728f5"/>
     */
    public static final String getXSRF = "(name=\"_xsrf\" value=\")(.*)(\">)";
}
