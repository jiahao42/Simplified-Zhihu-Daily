package com.james.simplezhihudaily.View;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.james.simplezhihudaily.R;

public class SettingActivity extends Activity {
    public static Switch noPics;
    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_setting);
        initWidget();
    }
    private void initWidget(){
        noPics = (Switch)findViewById(R.id.no_pic_pattern);
        editor = getSharedPreferences("settings",MODE_PRIVATE).edit();
        sharedPreferences = getSharedPreferences("settings",MODE_PRIVATE);
        /* 无图模式 */
        noPics.setChecked(sharedPreferences.getBoolean("pictures?",true));
        noPics.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    editor.putBoolean("pictures?",true);
                    editor.apply();
                }else {
                    editor.putBoolean("pictures?",false);
                    editor.apply();
                }
            }
        });
    }
    @Override
    public void onBackPressed(){
        MainActivity.mainActivity.spinner.setSelection(0);//将spinner变回今日热闻
        this.finish();
    }
}
