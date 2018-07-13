package com.coolweather.android.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.coolweather.android.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        Log.d("Lijinze", "onCreate: "+"MainActivity");
        if (prefs.getString("weahter",null)!=null){
            Log.d("Lijinze", "onCreate: "+"MainAtivity有缓存直接跳转到WeatherActivity");
            Intent intent=new Intent(MainActivity.this,WeatherActivity.class);
            startActivity(intent);
            Log.d("Lijinze", "onCreate: "+"跳转到WeatherActivity");
            finish();
        }
    }
}
