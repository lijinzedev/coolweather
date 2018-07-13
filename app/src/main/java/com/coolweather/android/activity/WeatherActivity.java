package com.coolweather.android.activity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.android.R;
import com.coolweather.android.gson.Forecast;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
private ScrollView weatherLayout;
private TextView titleCity;
private TextView titleUpdateTime;
private TextView degreeText;
private TextView weatherInfoText;
private LinearLayout forecastLayout;
private TextView apiText;
private TextView pm25Text;
private TextView comfortText;
private TextView carWashText;
private TextView sportText;
private ImageView bingPicImg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        Log.d("Lijinze", "onCreate: "+"加载WeatherActivity视图");
        setContentView(R.layout.activity_weather);
        Log.d("Lijinze", "onCreate: "+"WeatherActivity");
        bingPicImg=(ImageView)findViewById(R.id.bing_pic_img);
        weatherLayout=(ScrollView)findViewById(R.id.weather_layout);
        titleCity=(TextView)findViewById(R.id.title_city);
        titleUpdateTime=(TextView)findViewById(R.id.title_update_time);
        degreeText=(TextView)findViewById(R.id.degree_text);
        weatherInfoText=(TextView)findViewById(R.id.weather_info_text);
        forecastLayout=(LinearLayout)findViewById(R.id.forecast_layout);
        apiText=(TextView)findViewById(R.id.api_text);
        pm25Text=(TextView)findViewById(R.id.pm25_text);
        comfortText=(TextView)findViewById(R.id.comfort_text);
        carWashText=(TextView)findViewById(R.id.car_wash_text);
        sportText=(TextView)findViewById(R.id.sport_text);
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        Log.d("Lijinze", "onCreate: "+prefs.toString());
        String weatherString=prefs.getString("weahter",null);
        if (weatherString!=null){
            // 有缓存时直接加载天气预报
            Log.d("Lijinze", "onCreate: "+"Weather从本地加载数据");
            Weather weather= Utility.handelWeatherResponse(weatherString);
            showWeatherInfo(weather);

        }else {
            //无缓存是去服务器查询天气
            String weatherid=getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            Log.d("Lijinze", "onCreate: "+"从服务加载数据");
            requestWeather(weatherid);
        }
        String bingpic=prefs.getString("bing_pic",null);
        if (bingpic!=null){
            Glide.with(this).load(bingpic).into(bingPicImg);
        }else {
            loadBingPic();
        }
    }

    private void loadBingPic() {
        String requsetBingPic="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requsetBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
final  String bingpic=response.body().string();
SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
editor.putString("bing_pic",bingpic);
editor.apply();
runOnUiThread(new Runnable() {
    @Override
    public void run() {
        Glide.with(WeatherActivity.this).load(bingpic).into(bingPicImg);
    }
});
            }
        });
    }

    private void requestWeather(String weatherid) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid="+weatherid+"&key=b454d55fa49f4675be40d25ae94cec46";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
final String responsetext=response.body().string();
final Weather weather=Utility.handelWeatherResponse(responsetext);
runOnUiThread(new Runnable() {
    @Override
    public void run() {
        if (weather!=null&&"ok".equals(weather.status)){
            @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
            editor.putString("weahter",responsetext);
            editor.apply();
            showWeatherInfo(weather);
        }else {
            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
        }
    }
});
            }
        });
    }

    private void showWeatherInfo(Weather weather) {
        String cityname=weather.basic.cityName;
        String updatatime=weather.basic.update.updateTime.split(" ")[1];
        String degree=weather.now.temperature +"℃";
        String weatherinfo=weather.now.more.info;
        titleCity.setText(cityname);
        titleUpdateTime.setText(updatatime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherinfo);
        forecastLayout.removeAllViews();
        for(Forecast forecast:weather.forecastList){
            View view= LayoutInflater.from(WeatherActivity.this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText=(TextView)view.findViewById(R.id.date_text);
            TextView infoText=(TextView)view.findViewById(R.id.info_text);

            TextView maxText=(TextView)view.findViewById(R.id.max_text);

            TextView minText=(TextView)view.findViewById(R.id.min_text);

            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);

        }
        if (weather.aqi!=null){
            apiText.setText(weather.aqi.city.api);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort="舒适度: " +weather.suggestion.comfort.info;
        String carWash="洗车指数: "+weather.suggestion.carWash.info;
        String sport="运动建议: "+weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
    }
}
