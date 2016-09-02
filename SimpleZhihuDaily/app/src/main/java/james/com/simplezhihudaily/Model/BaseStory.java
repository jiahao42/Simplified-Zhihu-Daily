package james.com.simplezhihudaily.Model;

import com.google.gson.annotations.SerializedName;

public abstract class BaseStory {
    @SerializedName("images")
    protected String[] urls;
    @SerializedName("type")
    protected int type;
    @SerializedName("id")
    protected int id;
    @SerializedName("ga_prefix")
    protected int ga_prefix;
    @SerializedName("title")
    protected String title;
    @SerializedName("multipic")
    protected boolean multipic;
    @SerializedName("date")
    protected String date;
    @SerializedName("content")
    protected String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public abstract String getUrls();

    public abstract String getUrls(String all);

    public abstract void setUrls(String[] urls);

    public abstract void setUrls(String url);

    public abstract int getType();

    public abstract void setType(int type);

    public abstract int getId();

    public abstract void setId(int id);

    public abstract int getGa_prefix();

    public abstract void setGa_prefix(int ga_prefix);

    public abstract String getTitle();

    public abstract void setTitle(String title);

    public abstract boolean isMultipic();

    public abstract void setMultipic(boolean multipic);
}
