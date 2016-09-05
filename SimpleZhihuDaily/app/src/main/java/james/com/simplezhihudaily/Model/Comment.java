package james.com.simplezhihudaily.Model;

import android.graphics.Bitmap;

import com.google.gson.annotations.SerializedName;

/**
 * comments : 长评论列表，形式为数组（请注意，其长度可能为 0）
 author : 评论作者
 id : 评论者的唯一标识符
 content : 评论的内容
 likes : 评论所获『赞』的数量
 time : 评论时间
 avatar : 用户头像图片的地址
 */
public class Comment {
    @SerializedName("author")
    private String author;
    @SerializedName("content")
    private String content;
    @SerializedName("likes")
    private String likes;
    @SerializedName("time")
    private String time;
    @SerializedName("avatar")
    private String avatar;
    private Bitmap bitmap;

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Comment(String author, String content, String likes, String time, String avatar) {
        this.author = author;
        this.content = content;
        this.likes = likes;
        this.time = time;
        this.avatar = avatar;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLikes() {
        return likes;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
