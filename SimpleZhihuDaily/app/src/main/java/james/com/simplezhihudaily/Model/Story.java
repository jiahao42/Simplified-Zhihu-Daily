package james.com.simplezhihudaily.Model;

import android.graphics.Bitmap;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

import static android.support.v7.widget.AppCompatDrawableManager.get;

/**
 * date : 日期
 * stories : 当日新闻
 * content : 新闻标题
 * urls : 图像地址（官方 API 使用数组形式。目前暂未有使用多张图片的情形出现，曾见无 urls 属性的情况，请在使用中注意 ）
 * ga_prefix : 供 Google Analytics 使用
 * type : 作用未知
 * id : url 与 share_url 中最后的数字（应为内容的 id）
 * multipic : 消息是否包含多张图片（仅出现在包含多图的新闻中）
 * top_stories : 界面顶部 ViewPager 滚动显示的显示内容（子项格式同上）（请注意区分此处的 image 属性与 stories 中的 urls 属性
 */
//{"urls":["http:\/\/pic4.zhimg.com\/5baad8cabc7468b74b01229604bd0e73.jpg"],
// "type":0,"id":8739884,"ga_prefix":"083010","content":"摆满货架的快消巨头，是如何失去了自己的黄金时代"},
public class Story extends BaseStory implements Serializable {
    private static final int isBaseStory = 0;
    private Bitmap bitmap;

    public Story() {
    }

    public Story(String mTitle, String[] mUrls, int mId, int mType, int mGa_prefix) {
        title = mTitle;
        urls = mUrls;
        id = mId;
        type = mType;
        ga_prefix = mGa_prefix;
    }

    public Story(String mTitle, String[] mUrls, int mId, int mType, int mGa_prefix, boolean mMultipic) {
        title = mTitle;
        urls = mUrls;
        id = mId;
        type = mType;
        ga_prefix = mGa_prefix;
        multipic = mMultipic;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String mContent) {
        content = mContent;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String mDate) {
        date = mDate;
    }

    public Bitmap getBitmap() {
        return this.bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public boolean isMultipic() {
        return multipic;
    }

    public void setMultipic(boolean mMultipic) {
        multipic = mMultipic;
    }

    public int getType() {
        return type;
    }

    public int getGa_prefix() {
        return ga_prefix;
    }


    public void setType(int mType) {
        type = mType;
    }

    public void setId(int mId) {
        id = mId;
    }

    public void setGa_prefix(int mGa_prefix) {
        ga_prefix = mGa_prefix;
    }

    public void setTitle(String mTitle) {
        title = mTitle;
    }

    public String getTitle() {
        return title;
    }

    public int getId() {
        return id;
    }

    public String getUrls() {
        return urls[0];
    }

    public String getUrls(String all) {
        String combined = null;
        if (urls == null)
        {
            return null;
        }
        for (int i = 0; i < urls.length; i++)
        {
            combined += urls[i];
            combined += ",";
        }
        return combined;
    }

    public void setUrls(String string) {
        urls = new String[1];
        urls[0] = string;
    }

    public void setUrls(String[] mUrls) {
        urls = new String[mUrls.length];
        urls = mUrls;
    }

    @Override
    public String toString() {
        return "id: " + getId() + ",date: " + getDate() + ",content: " + getTitle() + ",urls: " + getUrls("all");
    }
    public int getAttr(){
        return isBaseStory;
    }
}
