package com.coolweather.android.fgm;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.android.R;
import com.coolweather.android.activity.WeatherActivity;
import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.json.JSONException;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTY=2;
    private ProgressDialog progressDialog;
    private TextView titletext;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String>adapter;
    private List<String>datalist=new ArrayList<>();
    private List<Province> provinceList;
    private List<City>cityList;
    private List<County>countyList;
    private Province seletedprovince;
    private City seletedcity;
    private int currentlevel;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.choose_area,container,false);
        Log.d("Lijinze", "onCreate: "+"执行碎片");
        titletext=(TextView)view.findViewById(R.id.title_text);
        backButton=(Button)view.findViewById(R.id.back_button);
        listView=(ListView)view.findViewById(R.id.list_view);
        adapter=new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,datalist);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentlevel==LEVEL_PROVINCE){
                    seletedprovince=provinceList.get(position);
                    queryCities();
                }
                else if (currentlevel==LEVEL_CITY){
                    seletedcity=cityList.get(position);
                    queryCounty();
                }else if (currentlevel==LEVEL_COUNTY){
                    String weatherid=countyList.get(position).getWeatherid();
                    Intent intent=new Intent(getActivity(), WeatherActivity.class);
                    intent.putExtra("weather_id",weatherid);
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentlevel==LEVEL_COUNTY){
                    queryCities();
                }else if (currentlevel==LEVEL_CITY){
                    queryProvices();
                }
            }
        });
        queryProvices();
    }

    private void queryProvices() {
        titletext.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList= DataSupport.findAll(Province.class);
        if (provinceList.size()>0){
            datalist.clear();
         for (Province province:provinceList){
             datalist.add(province.getProvinceName());
         }
         adapter.notifyDataSetChanged();
         listView.setSelection(0);
         currentlevel=LEVEL_PROVINCE;
        }else {
            String address="http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }



    private void queryCounty() {
titletext.setText(seletedcity.getCityName());
        backButton.setVisibility(View.VISIBLE);
countyList=DataSupport.where("cityid=?",String.valueOf(seletedcity.getId())).find(County.class);
if (countyList.size()>0){
    datalist.clear();
    for(County county:countyList){
        datalist.add(county.getCountyName());
    }
    adapter.notifyDataSetChanged();
    listView.setSelection(0);
     currentlevel=LEVEL_COUNTY;
}else {
    int citycode=seletedcity.getCityCode();
    int provinceCode=seletedprovince.getProvinceCode();
    String address="http://guolin.tech/api/china/"+provinceCode+"/"+citycode;
    queryFromServer(address,"county");
}
    }

            private void queryCities() {
                titletext.setText(seletedprovince.getProvinceName());
                backButton.setVisibility(View.VISIBLE);
                cityList= DataSupport.where("provinceid=?",String.valueOf(seletedprovince.getId())).find(City.class);
                if (cityList.size()>0){
                    datalist.clear();
                    for (City city:cityList){
                        datalist.add(city.getCityName());
                    }
                    adapter.notifyDataSetChanged();
                    listView.setSelection(0);
                    currentlevel=LEVEL_CITY;
                }else {
                    int provinceCode=seletedprovince.getProvinceCode();
                    String address="http://guolin.tech/api/china/"+provinceCode;
                    queryFromServer(address,"city");
                }
    }
    private void queryFromServer(String address, final String type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
getActivity().runOnUiThread(new Runnable() {
    @Override
    public void run() {
        closeProgressDialog();
        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
    }
});
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
String responsetext=response.body().string();
boolean result = false;
if ("province".equals(type)){
    result= Utility.handleProvinceResponse(responsetext);
}else if ("city".equals(type)){
    result=Utility.handleCityResponse(responsetext,seletedprovince.getId());
}else if ("county".equals(type)){
    try {
        result=Utility.handleCountyResponse(responsetext,seletedcity.getId());
    } catch (JSONException e) {
        e.printStackTrace();
    }
}
if (result){
    getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {
            closeProgressDialog();
            if ("province".equals(type)){
                queryProvices();
            }else if ("city".equals(type)){
                queryCities();
            }else if ("county".equals(type)){
               queryCounty();
            }
        }
    });
}
            }
        });
    }

    private void closeProgressDialog() {
        if(progressDialog!=null){
            progressDialog.dismiss();
        }
    }

    private void showProgressDialog() {
        if (progressDialog==null){
            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
}
