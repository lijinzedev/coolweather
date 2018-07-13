package com.coolweather.android.util;

import android.text.TextUtils;

import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utility {
    public static Weather handelWeatherResponse(String response)  {
        try {
            JSONObject jsonObject=new JSONObject(response);
            JSONArray jsonArray=jsonObject.getJSONArray("HeWeather");
            String weatherContent=jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent,Weather.class);
        }catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static boolean handleProvinceResponse(String response){
        if(!TextUtils.isEmpty(response)){
try {
    JSONArray allProvinces=new JSONArray(response);
    for(int i=0;i<allProvinces.length();i++){
        JSONObject provinceobject=allProvinces.getJSONObject(i);
        Province province=new Province();
        province.setProvinceName(provinceobject.getString("name"));
        province.setProvinceCode(provinceobject.getInt("id"));
        province.save();
    }
    return true;
} catch (JSONException e) {
    e.printStackTrace();
}
        }
        return false;
    }
    public static boolean handleCityResponse(String response,int proviceid){
        if (!TextUtils.isEmpty(response)){
            try {
             JSONArray allCity=new JSONArray(response);
             for (int i=0;i<allCity.length();i++){
                 JSONObject CityObject=allCity.getJSONObject(i);
                 City city=new City();
                 city.setCityName(CityObject.getString("name"));
                 city.setCityCode(CityObject.getInt("id"));
                 city.setProviceid(proviceid);
                 city.save();
             }
             return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    public static boolean handleCountyResponse(String response,int cityid) throws JSONException {
        if (!TextUtils.isEmpty(response)){
            JSONArray allcounty=new JSONArray(response);

                try {
                    for (int i=0;i<allcounty.length();i++) {
                        JSONObject countyobject = allcounty.getJSONObject(i);
                        County county = new County();
                        county.setCountyName(countyobject.getString("name"));
                        county.setWeatherid(countyobject.getString("weather_id"));
                        county.setCityid(cityid);
                        county.save();
                    }
                    return true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }

        }
        return false;

    }
}
