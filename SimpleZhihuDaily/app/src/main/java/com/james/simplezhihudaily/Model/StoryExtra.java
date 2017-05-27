package com.james.simplezhihudaily.Model;

import com.google.gson.annotations.SerializedName;

/**
 * long_comments : 长评论总数
 * popularity : 点赞总数
 * short_comments : 短评论总数
 * comments : 评论总数
 */
public class StoryExtra {
    @SerializedName("long_comments")
    private int numberOfLongComment ;
    @SerializedName("popularity")
    private int popularity ;
    @SerializedName("short_comments")
    private int numberOfShortComment ;
    @SerializedName("comments")
    private int snmOfComment ;

    @Override
    public String toString() {
        return "StoryExtra{" +
                "numberOfLongComment=" + numberOfLongComment +
                ", popularity=" + popularity +
                ", numberOfShortComment=" + numberOfShortComment +
                ", snmOfComment=" + snmOfComment +
                '}';
    }

    public int getNumberOfLongComment() {
        return numberOfLongComment;
    }

    public void setNumberOfLongComment(int numberOfLongComment) {
        this.numberOfLongComment = numberOfLongComment;
    }

    public int getPopularity() {
        return popularity;
    }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
    }

    public int getNumberOfShortComment() {
        return numberOfShortComment;
    }

    public void setNumberOfShortComment(int numberOfShortComment) {
        this.numberOfShortComment = numberOfShortComment;
    }

    public int getSumOfComment() {
        return snmOfComment;
    }

    public void setSnmOfComment(int snmOfComment) {
        this.snmOfComment = snmOfComment;
    }

    public StoryExtra(int numberOfLongComment, int popularity, int numberOfShortComment, int snmOfComment) {

        this.numberOfLongComment = numberOfLongComment;
        this.popularity = popularity;
        this.numberOfShortComment = numberOfShortComment;
        this.snmOfComment = snmOfComment;
    }
}
