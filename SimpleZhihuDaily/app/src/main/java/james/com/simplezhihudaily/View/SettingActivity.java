package james.com.simplezhihudaily.View;

import android.app.Activity;
import android.os.Bundle;

import james.com.simplezhihudaily.R;

import static android.os.Build.VERSION_CODES.M;

public class SettingActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_setting);
    }
    @Override
    public void onBackPressed(){
        MainActivity.mainActivity.spinner.setSelection(0);//将spinner变回今日热闻
        this.finish();
    }
}
