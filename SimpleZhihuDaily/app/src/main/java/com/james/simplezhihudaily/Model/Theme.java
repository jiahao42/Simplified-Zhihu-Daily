package com.james.simplezhihudaily.Model;

import android.graphics.Bitmap;

import com.google.gson.annotations.SerializedName;

/**
 * {"limit":1000,"subscribed":[],"others":[
 * {"color":15007,"thumbnail":"http:\/\/pic3.zhimg.com\/0e71e90fd6be47630399d63c58beebfc.jpg",
 * "description":"了解自己和别人，了解彼此的欲望和局限。","id":13,"name":"日常心理学"}
 */
//该内容无需进数据库
public class Theme {
    @SerializedName("thumbnail")
    private String url;
    @SerializedName("description")
    private String description;
    @SerializedName("id")
    private int id;
    @SerializedName("name")
    private String name;

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    private Bitmap bitmap;

    public Theme(){}
    public Theme(String url, String description, int id, String name) {
        this.url = url;
        this.description = description;
        this.id = id;
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
