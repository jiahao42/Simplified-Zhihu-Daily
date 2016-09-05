package james.com.simplezhihudaily.Model;

import com.google.gson.annotations.SerializedName;

public class TopStory extends BaseStory{
    final static private int isTopStory = 1;
    /*
    这里重写一下 因为发现Top-story是image 不是images
     */
    @SerializedName("image")
    private String myUrls;
    public TopStory(){}
    public int getAttr(){
        return isTopStory;
    }
    public String getUrls(){
        return this.urls[0];
    }

    public void setUrls(String s){
        urls[0] = s;
    }
    public void setUrls(String[] s){
        urls = new String[s.length];
        urls = s;
    }
    public String getUrls(String all){
        return urls[0];
    }
    public String getmyUrls() {
        return this.myUrls;
    }
    public boolean isMultipic() {
        return multipic;
    }

    public void setMultipic(boolean mMultipic) {
        multipic = mMultipic;
    }


    public void setmyUrls(String mmyUrls) {
        this.myUrls = mmyUrls;
    }

    public int getType(){
        return type;
    }
    public void setType(int mType){
        type = mType;
    }
    public String getDate() {
        return date;
    }

    public void setDate(String mDate) {
        date = mDate;
    }

    public TopStory(String img, int mId, int mGa_prefix, String mTitle) {
        this.myUrls = img;
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
    @Override
    public String toString() {
        return "id: " + getId() + ",date: " + getDate() + ",content: " + getTitle() + ",this.myUrls: " + getmyUrls();
    }
}
