package com.james.simplezhihudaily.Model;

import android.graphics.Bitmap;

public class ThemeStory extends BaseStory{
    final static private int isThemeStory = 2;
    private String categoryID;
    private Bitmap bitmap;


    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getCategoryID() {
        return this.categoryID;
    }

    public int getType(){
        return type;
    }
    public void setType(int mType){
        type = mType;
    }

    public boolean isMultipic() {
        return multipic;
    }

    public void setMultipic(boolean mMultipic) {
        multipic = mMultipic;
    }
    public void setCategoryID(String mCategoryID) {
        categoryID = mCategoryID;
    }

    public String getUrls() {
        if (urls != null){
            return urls[0];
        }else {
            return null;
        }
    }

    public String getUrls(String  all){
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

    public void setUrls(String mUrls) {
        urls = new String[1];
        urls[0] = mUrls;
    }

    public void setUrls(String[] mUrls){
        urls = new String[mUrls.length];
        urls = mUrls;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String mDate) {
        date = mDate;
    }

    public ThemeStory(String mImg, int mId, int mGa_prefix, String mTitle) {
        urls[0] = mImg;
        id = mId;
        ga_prefix = mGa_prefix;
        title = mTitle;
    }



    public int getId() {
        return id;
    }

    public void setId(int mId) {
        id = mId;
    }

    public int getGa_prefix() {
        return ga_prefix;
    }

    public void setGa_prefix(int mGa_prefix) {
        ga_prefix = mGa_prefix;
    }

    public String getTitle() {
        return title;
    }


    public void setTitle(String mTitle) {
        title = mTitle;
    }
    public int getAttr(){
        return isThemeStory;
    }

    @Override
    public String toString() {
        return "ThemeStory{" +
                "categoryID='" + categoryID + '\'' + "url = " + urls[0] +
                '}';
    }
}
