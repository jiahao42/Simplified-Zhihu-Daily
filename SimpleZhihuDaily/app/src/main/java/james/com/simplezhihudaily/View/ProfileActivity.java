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

import static james.com.simplezhihudaily.Model.Symbol.cookie;

public class ProfileActivity extends Activity {
    private Button buttonLogin;
    private String specialID;
    private String avatar;
    private String likes;
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
                    editor.putString("html",document.toString());
                    editor.apply();
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
                    Log.d("getProfile",document.toString());
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
                        Log.d("ProfileAvatar",avatar);
                    }
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
