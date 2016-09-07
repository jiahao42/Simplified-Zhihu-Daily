package james.com.simplezhihudaily.View;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import james.com.simplezhihudaily.Model.RegexForZhihu;
import james.com.simplezhihudaily.Model.Symbol;
import james.com.simplezhihudaily.Model.Url;
import james.com.simplezhihudaily.R;

import static james.com.simplezhihudaily.Model.RegexForZhihu.getAnswer;
import static james.com.simplezhihudaily.Model.RegexForZhihu.getFavourite;
import static james.com.simplezhihudaily.Model.Symbol.cookie;

public class ProfileActivity extends Activity {
    private Button buttonLogin;
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
    private SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_profile);
        initWidget();
    }
    private void initWidget(){
        buttonLogin = (Button)findViewById(R.id.get_profile);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSpecialID();
            }
        });
        editor = getSharedPreferences("temp",MODE_PRIVATE).edit();
        getSpecialID();
    }
    private void getSpecialID(){
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message message){
                if (message.what == Symbol.RECEIVE_SUCCESS){
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
                    if (matcher.find()){
                        Log.d("ProfileUrl",matcher.group(2));
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
    private void getProfile(){
        final Handler handler = new Handler(){

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
                    editor.putString("html",document.toString());
                    editor.apply();
                    Pattern getLikes = Pattern.compile(RegexForZhihu.getLikes);
                    Matcher matcher = getLikes.matcher(document.toString());
                    if (matcher.find()){
                        likes = matcher.group(2);
                        Log.d("ProfileLikes",likes);
                    }
                    Pattern getAvatar = Pattern.compile(RegexForZhihu.getAvatar);
                    matcher = getAvatar.matcher(document.toString());
                    if (matcher.find()){
                        avatar = matcher.group(2);
                        nickname = matcher.group(6);
                        Log.d("ProfileAvatar",avatar);
                        Log.d("ProfileNickname",nickname);
                    }
                    Pattern getThanks = Pattern.compile(RegexForZhihu.getThanks);
                    matcher = getThanks.matcher(document.toString());
                    if (matcher.find()){
                        thanks = matcher.group(2);
                        Log.d("ProfileThanks",thanks);
                    }
                    Pattern getBio = Pattern.compile(RegexForZhihu.getBio);
                    matcher = getBio.matcher(document.toString());
                    if (matcher.find()){
                        bio = matcher.group(4);
                        Log.d("ProfileBio",bio);
                    }
                    Pattern getQuestion = Pattern.compile(RegexForZhihu.getQuestion);
                    matcher = getQuestion.matcher(document.toString());
                    if (matcher.find()){
                        numOfQuestion = matcher.group(2);
                        Log.d("ProfileQuestion",numOfQuestion);
                    }
                    Pattern getArticle = Pattern.compile(RegexForZhihu.getArticle);
                    matcher = getArticle.matcher(document.toString());
                    if (matcher.find()){
                        numOfArticle = matcher.group(2);
                        Log.d("ProfileArticle",numOfArticle);
                    }
                    Pattern getAnswer = Pattern.compile(RegexForZhihu.getAnswer);
                    matcher = getAnswer.matcher(document.toString());
                    if (matcher.find()){
                        numOfAnswer = matcher.group(2);
                        Log.d("ProfileAnswer",numOfAnswer);
                    }
                    Pattern getFavourite = Pattern.compile(RegexForZhihu.getFavourite);
                    matcher = getFavourite.matcher(document.toString());
                    if (matcher.find()){
                        numOfFavourite = matcher.group(2);
                        Log.d("ProfileFavourite",numOfFavourite);
                    }
                    Pattern getFollowed = Pattern.compile(RegexForZhihu.getFollowed);
                    matcher = getFollowed.matcher(document.toString());
                    if (matcher.find()){
                        numOfFollowed = matcher.group(2);
                        Log.d("ProfileFollowed",numOfFollowed);
                    }
                    Pattern getFollower = Pattern.compile(RegexForZhihu.getFollower);
                    matcher = getFollower.matcher(document.toString());
                    if (matcher.find()){
                        numOfFollower = matcher.group(2);
                        Log.d("ProfileFollower",numOfFollower);
                    }
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
