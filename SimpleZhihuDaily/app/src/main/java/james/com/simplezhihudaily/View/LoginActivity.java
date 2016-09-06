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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import james.com.simplezhihudaily.Model.Symbol;
import james.com.simplezhihudaily.Model.Url;
import james.com.simplezhihudaily.R;

import static android.R.id.message;
import static james.com.simplezhihudaily.Model.Url.getXSRF;

public class LoginActivity extends Activity implements View.OnClickListener {
    private EditText username;
    private EditText password;
    private Button login;
    private ImageView checksum;
    private RequestQueue mQueue;
    public LoginActivity loginActivity;
    private String XSRF;

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
        login = (Button) findViewById(R.id.login);
        login.setOnClickListener(this);
        checksum = (ImageView) findViewById(R.id.checksum);
        loginActivity = this;
        mQueue = Volley.newRequestQueue(loginActivity);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.login:
                fetchXSRF();
        }
    }
    private void getCheckSum(){
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message message){

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
                    if (matcher.find()){
                        XSRF = matcher.group(2);
                    }
                    Log.d("Login",XSRF);
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

}