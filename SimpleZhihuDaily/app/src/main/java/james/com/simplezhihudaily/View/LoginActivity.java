package james.com.simplezhihudaily.View;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import james.com.simplezhihudaily.Model.Cookies;
import james.com.simplezhihudaily.Model.Symbol;
import james.com.simplezhihudaily.Model.Url;
import james.com.simplezhihudaily.R;

import static android.R.id.message;
import static james.com.simplezhihudaily.Model.Url.getXSRF;

public class LoginActivity extends Activity implements View.OnClickListener {
    private EditText username;
    private EditText password;
    private EditText checksumBox;
    private Button login;
    private ImageView checksum;
    private RequestQueue mQueue;
    public LoginActivity loginActivity;
    private String XSRF;
    private String captcha;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_login);
        initWidget();
        getCheckSum();
    }

    private void initWidget() {
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        checksumBox = (EditText) findViewById(R.id.input_checksum);
        login = (Button) findViewById(R.id.login);
        login.setOnClickListener(this);
        checksum = (ImageView) findViewById(R.id.checksum);
        loginActivity = this;
        mQueue = Volley.newRequestQueue(loginActivity);
        gson = new Gson();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.login:
                captcha = checksumBox.getText().toString();
                Log.d("test",checksumBox.getText().toString());
                Log.d("test",captcha);
                fetchXSRF();
                break;

        }
    }

    private void getCheckSum() {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message message) {

            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                ImageRequest imageRequest = new ImageRequest(Url.getCheckSum, new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        checksum.setImageBitmap(response);
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

    private void fetchXSRF() {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                if (message.what == Symbol.RECEIVE_SUCCESS)
                {
                    Log.d("Login","Loading");
                    login();
                }
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                try
                {
                    Document document = Jsoup.connect(getXSRF).get();
                    //Log.d("Login", document.toString());
                    String pattern = "(name=\"_xsrf\" value=\")(.*)(\">)";
                    Pattern getXSRF = Pattern.compile(pattern);
                    Matcher matcher = getXSRF.matcher(document.toString());
                    //Log.d("Login",data);
                    XSRF = null;
                    if (matcher.find())
                    {
                        XSRF = matcher.group(2);
                    }
                    Log.d("Login", XSRF);
                    Message message = new Message();
                    message.what = Symbol.RECEIVE_SUCCESS;
                    handler.sendMessage(message);
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void login() {
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message message){

            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                try
                {
                    Connection.Response response = Jsoup.connect(Url.loginByMail).
                            data("_xsrf", XSRF,
                                    "captcha", captcha,
                                    "email", "caterpillarous@gmail.com",
                                    "password","",
                                    "remember_me","true")
                            .header("User-Agent",
                                    "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36")
                            .ignoreContentType(true)
                            .method(Connection.Method.POST).execute();
                    Document document = response.parse();
                    //JsonReader reader = new JsonReader(new StringReader(response.cookies().toString()));
                    //reader.setLenient(true);
                    String temp = response.cookies().toString().trim();
                    Cookies cookies = gson.fromJson(temp,Cookies.class);
                    //Cookies.getInstance();
                    Map<String,String> map = response.cookies();
                    //Log.d("Login_Document", document.toString());
                    Log.d("Login_cookies_as_Map",map.toString());
                    Log.d("Login_cookies",response.cookies().toString());
                    //Log.d("Login_sessionId",sessionId);

                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}