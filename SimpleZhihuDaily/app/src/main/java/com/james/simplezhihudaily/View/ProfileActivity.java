package com.james.simplezhihudaily.View;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.james.simplezhihudaily.Util.RegexForZhihu;
import com.james.simplezhihudaily.Util.Symbol;
import com.james.simplezhihudaily.Util.Url;
import com.james.simplezhihudaily.R;
import com.james.simplezhihudaily.Util.Util;

import static com.james.simplezhihudaily.View.MainActivity.mainActivity;

public class ProfileActivity extends Activity {
    private String specialID;
    private String avatar;
    private String likes;
    private String thanks;
    private String bio;
    private String nickname;
    private String numOfQuestion;
    private String numOfAnswer;
    private String numOfArticle;
    private String numOfFavourite;
    private String numOfFollowed;
    private String numOfFollower;
    private String location;
    private RequestQueue mQueue;
    public ProfileActivity profileActivity;

    private TextView bioText;
    private TextView nicknameText;
    private TextView myAnswer;
    private TextView myFollowed;
    private TextView myFollower;
    private TextView myLikes;
    private TextView myThanks;
    private TextView myAnswerBelow;
    private TextView myQuestion;
    private TextView myArticle;
    private TextView myFavourites;
    private ImageView myAvatar;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_profile);
        initWidget();
    }

    private void initWidget() {
        myAvatar = (ImageView) findViewById(R.id.avatar);
        bioText = (TextView) findViewById(R.id.bio);
        nicknameText = (TextView) findViewById(R.id.nickname);
        myAnswer = (TextView) findViewById(R.id.answers);
        myFollowed = (TextView) findViewById(R.id.followed);
        myFollower = (TextView) findViewById(R.id.followers);
        myLikes = (TextView) findViewById(R.id.likes);
        myThanks = (TextView) findViewById(R.id.thanks);
        myAnswerBelow = (TextView) findViewById(R.id.my_answer);
        myQuestion = (TextView) findViewById(R.id.my_question);
        myArticle = (TextView) findViewById(R.id.my_article);
        myFavourites = (TextView) findViewById(R.id.my_favourites);
        editor = getSharedPreferences("temp", MODE_PRIVATE).edit();
        profileActivity = this;
        mQueue = Volley.newRequestQueue(profileActivity);
        getSpecialID();
    }

    private void getSpecialID() {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                if (message.what == Symbol.RECEIVE_SUCCESS)
                {
                    getProfile();
                }
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                try
                {
                    Document document = Jsoup.connect(Url.zhihuOfficial)
                            .cookies(Symbol.cookie).get();
                    Pattern pattern = Pattern.compile(RegexForZhihu.getSpecialID);
                    Matcher matcher = pattern.matcher(document.toString());
                    if (matcher.find())
                    {
                        Log.d("ProfileUrl", matcher.group(2));
                        specialID = matcher.group(2);
                        Message message = new Message();
                        message.what = Symbol.RECEIVE_SUCCESS;
                        handler.sendMessage(message);
                    }
                    //Log.d("Profile",document.toString());
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void getProfile() {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                if (message.what == Symbol.RECEIVE_SUCCESS)
                {
                    getAvatarOnline();
                } else if (message.what == 123)
                {
                    myLikes.setText(likes);
                    nicknameText.setText(nickname);
                    myThanks.setText(thanks);
                    bioText.setText(bio);
                    myQuestion.setText(numOfQuestion);
                    myArticle.setText(numOfArticle);
                    myAnswer.setText(numOfAnswer);
                    myAnswerBelow.setText(numOfAnswer);
                    myFavourites.setText(numOfFavourite);
                    myFollowed.setText(numOfFollowed);
                    myFollower.setText(numOfFollower);
                }
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                Document document = null;
                try
                {
                    document = Jsoup.connect(Url.zhihuOfficial + specialID)
                            .cookies(Symbol.cookie)
                            .get();
                    editor.putString("html", document.toString());
                    editor.apply();
                    Pattern getLikes = Pattern.compile(RegexForZhihu.getLikes);
                    Matcher matcher = getLikes.matcher(document.toString());
                    if (matcher.find())
                    {
                        likes = matcher.group(2);
                        Log.d("ProfileLikes", likes);
                    }
                    Pattern getAvatar = Pattern.compile(RegexForZhihu.getAvatar);
                    matcher = getAvatar.matcher(document.toString());
                    if (matcher.find())
                    {
                        avatar = matcher.group(2);
                        nickname = matcher.group(6);
                        Log.d("ProfileAvatar", avatar);
                        Log.d("ProfileNickname", nickname);
                    }
                    Message message = new Message();
                    message.what = Symbol.RECEIVE_SUCCESS;
                    handler.sendMessage(message);
                    Pattern getThanks = Pattern.compile(RegexForZhihu.getThanks);
                    matcher = getThanks.matcher(document.toString());
                    if (matcher.find())
                    {
                        thanks = matcher.group(2);
                        Log.d("ProfileThanks", thanks);
                    }

                    Pattern getBio = Pattern.compile(RegexForZhihu.getBio);
                    matcher = getBio.matcher(document.toString());
                    if (matcher.find())
                    {
                        bio = matcher.group(4).trim();
                        Log.d("ProfileBio", bio);
                    }
                    Pattern getQuestion = Pattern.compile(RegexForZhihu.getQuestion);
                    matcher = getQuestion.matcher(document.toString());
                    if (matcher.find())
                    {
                        numOfQuestion = matcher.group(2);
                        Log.d("ProfileQuestion", numOfQuestion);
                    }

                    Pattern getArticle = Pattern.compile(RegexForZhihu.getArticle);
                    matcher = getArticle.matcher(document.toString());
                    if (matcher.find())
                    {
                        numOfArticle = matcher.group(2);
                        Log.d("ProfileArticle", numOfArticle);
                    }

                    Pattern getAnswer = Pattern.compile(RegexForZhihu.getAnswer);
                    matcher = getAnswer.matcher(document.toString());
                    if (matcher.find())
                    {
                        numOfAnswer = matcher.group(2);
                        Log.d("ProfileAnswer", numOfAnswer);
                    }

                    Pattern getFavourite = Pattern.compile(RegexForZhihu.getFavourite);
                    matcher = getFavourite.matcher(document.toString());
                    if (matcher.find())
                    {
                        numOfFavourite = matcher.group(2);
                        Log.d("ProfileFavourite", numOfFavourite);
                    }

                    Pattern getFollowed = Pattern.compile(RegexForZhihu.getFollowed);
                    matcher = getFollowed.matcher(document.toString());
                    if (matcher.find())
                    {
                        numOfFollowed = matcher.group(2);
                        Log.d("ProfileFollowed", numOfFollowed);
                    }

                    Pattern getFollower = Pattern.compile(RegexForZhihu.getFollower);
                    matcher = getFollower.matcher(document.toString());
                    if (matcher.find())
                    {
                        numOfFollower = matcher.group(2);
                        Log.d("ProfileFollower", numOfFollower);
                    }
                    Message message1 = new Message();
                    message1.what = 123;
                    handler.sendMessage(message1);

                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void getAvatarOnline() {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                if (message.what == Symbol.RECEIVE_SUCCESS)
                {
                }
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                ImageRequest imageRequest = new ImageRequest(avatar, new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        myAvatar.setImageBitmap(response);
                        Message message = new Message();
                        message.what = Symbol.RECEIVE_SUCCESS;
                        handler.sendMessage(message);
                    }
                }, 0, 0, ImageView.ScaleType.FIT_XY, Bitmap.Config.RGB_565, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
                mQueue.add(imageRequest);
                mQueue.start();
            }
        }).start();
    }
    @Override
    public void onBackPressed() {
        mainActivity.spinner.setSelection(Util.safeLongToInt(0));//将spinner变回
        this.finish();
    }
}
