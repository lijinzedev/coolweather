package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

public class AQI {
    public AQICity city;

    public class AQICity {
        @SerializedName("aqi")
        public String api;
        @SerializedName("pm25")
        public String pm25;
    }
}
