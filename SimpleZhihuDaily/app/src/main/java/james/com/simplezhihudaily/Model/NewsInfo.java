package james.com.simplezhihudaily.Model;

import android.graphics.Bitmap;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 date : 日期
 stories : 当日新闻
 title : 新闻标题
 urls : 图像地址（官方 API 使用数组形式。目前暂未有使用多张图片的情形出现，曾见无 urls 属性的情况，请在使用中注意 ）
 ga_prefix : 供 Google Analytics 使用
 type : 作用未知
 id : url 与 share_url 中最后的数字（应为内容的 id）
 multipic : 消息是否包含多张图片（仅出现在包含多图的新闻中）
 top_stories : 界面顶部 ViewPager 滚动显示的显示内容（子项格式同上）（请注意区分此处的 image 属性与 stories 中的 urls 属性
 */
//{"urls":["http:\/\/pic4.zhimg.com\/5baad8cabc7468b74b01229604bd0e73.jpg"],
// "type":0,"id":8739884,"ga_prefix":"083010","title":"摆满货架的快消巨头，是如何失去了自己的黄金时代"},
public class NewsInfo implements Serializable{
    @SerializedName("images")
    private String[] urls;
    @SerializedName("type")
    private int type;
    @SerializedName("id")
    private int id;
    @SerializedName("ga_prefix")
    private int ga_prefix;
    @SerializedName("title")
    private String title;
    @SerializedName("multipic")
    private boolean multipic;
    private Bitmap bitmap;

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    NewsInfo(String title, String[] urls, int id, int type, int ga_prefix){
        this.title = title;
        this.urls = urls;
        this.id = id;
        this.type = type;
        this.ga_prefix = ga_prefix;
    }

    NewsInfo(String title, String[] urls, int id, int type, int ga_prefix, boolean multipic){
        this.title = title;
        this.urls = urls;
        this.id = id;
        this.type = type;
        this.ga_prefix = ga_prefix;
        this.multipic = multipic;
    }

    public boolean isMultipic() {
        return multipic;
    }

    public void setMultipic(boolean multipic) {
        this.multipic = multipic;
    }

    public int getType() {
        return type;
    }

    public int getGa_prefix() {
        return ga_prefix;
    }

    public void setUrls(String[] urls) {
        this.urls = urls;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setGa_prefix(int ga_prefix) {
        this.ga_prefix = ga_prefix;
    }

    public void setTitle(String title) {
        this.title = title;
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
}

