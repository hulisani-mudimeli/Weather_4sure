package com.weather_4sure;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class OpenWeatherAPI {
    private Context context;
    private final AsyncHttpClient client;
    private final String TAG = "OpenWeatherAPI_TAG";

    public static OpenWeatherAPI getInstance(Context context){
        return new OpenWeatherAPI(context);
    }

    private OpenWeatherAPI(Context context){
        this.context = context;
        client = new AsyncHttpClient();
        client.setTimeout(20000);
    }


    private static final String forecastURL = "https://api.openweathermap.org/data/2.5/forecast";

    public void getForecast(LatLng latLng){
        String paramBuilder = "?lat=" + latLng.latitude + "&lon=" + latLng.longitude +
                "&appid=" + context.getResources().getString(R.string.open_weather_key);

        client.get(forecastURL + paramBuilder, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(context, "Error retrieving weather information!", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onFailure: "+throwable);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                forecastConversion(responseString);
            }
        });

    }

    private void forecastConversion(String responseString){
        try {
            JSONObject jsonResult = new JSONObject(responseString);
            JSONArray listArr = jsonResult.getJSONArray("list");

            ArrayList<ArrayList<JSONObject>> daysForecastedMap = new ArrayList<>();
            daysForecastedMap.add(new ArrayList<>());
            int forecastIndex = 0;

            for (int i = 0; i < listArr.length(); i++) {

                if(i > 0 && !getDateForecasted(listArr, i).equals(getDateForecasted(listArr, i-1))){
                    forecastIndex++;
                    daysForecastedMap.add(new ArrayList<>());
                }

                daysForecastedMap.get(forecastIndex).add(listArr.getJSONObject(i));
            }

            ((MainActivity)context).reloadRecycler(daysForecastedMap);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String getDateForecasted(JSONArray listArr, int i) throws JSONException {
        String dateTimeForecasted = listArr.getJSONObject(i).get("dt_txt").toString();
        return dateTimeForecasted.substring(0, dateTimeForecasted.indexOf(' '));
    }
}
