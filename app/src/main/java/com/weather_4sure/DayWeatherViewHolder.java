package com.weather_4sure;

import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import org.json.JSONException;
import org.json.JSONObject;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;

public class DayWeatherViewHolder extends RecyclerView.ViewHolder{
    private TextView dayView;
    private ShapeableImageView  weatherIcon;
    public TextView hiTempView;
    public TextView loTempView;

    public double minTempinKelvin;
    public double maxTempinKelvin;

    private String TAG = "DayWeatherViewHolderTAG";

    public static final double KELVIN_CELSIUS = 273.15;

    public DayWeatherViewHolder(@NonNull View itemView) {
        super(itemView);
        dayView = itemView.findViewById(R.id.dayView);
        weatherIcon = itemView.findViewById(R.id.weatherIcon);
        hiTempView = itemView.findViewById(R.id.hiTemp);
        loTempView = itemView.findViewById(R.id.loTemp);
    }

    public void bindView(ArrayList<JSONObject> dayForecast, int dayPosition, int dayCount){
        try {
//            Log.d(TAG, "bindView: "+day.getLong("dt"));
            LocalDate ld = Instant.ofEpochMilli(dayForecast.get(0).getLong("dt")*1000).atZone(ZoneId.systemDefault()).toLocalDate();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE");
            dayView.setText(ld.format(formatter));

            minTempinKelvin = getMinTemp(dayForecast);
            maxTempinKelvin = getMaxTemp(dayForecast);


            if(DayWeatherAdapter.isCelsius) {
                loTempView.setText((int) (minTempinKelvin - KELVIN_CELSIUS) + "°");
                hiTempView.setText((int) (maxTempinKelvin - KELVIN_CELSIUS) + "°");
            }else{
                hiTempView.setText((int)DayWeatherViewHolder.getFahrenheit(maxTempinKelvin) + "°");
                loTempView.setText((int)DayWeatherViewHolder.getFahrenheit(minTempinKelvin) + "°");
            }


            // Weather Icon
//            int[] list_10d = new int[]{};
            int weatherID = getWeatherId(dayForecast, dayPosition, dayCount);
            if(weatherID >= 200 && weatherID <= 232){
                weatherIcon.setImageDrawable(itemView.getContext().getResources().getDrawable(R.drawable.ic_11d));
            }else if((weatherID >= 300 && weatherID <= 321) ||(weatherID >= 520 && weatherID <= 521)){
                weatherIcon.setImageDrawable(itemView.getContext().getResources().getDrawable(R.drawable.ic_09d));
            }else if(weatherID >= 500 && weatherID <= 504){
                weatherIcon.setImageDrawable(itemView.getContext().getResources().getDrawable(R.drawable.ic_10d));
            }else if(weatherID == 511 || (weatherID >= 600 && weatherID <= 622)){
                weatherIcon.setImageDrawable(itemView.getContext().getResources().getDrawable(R.drawable.ic_13d));
            }else if(weatherID >= 701 && weatherID <= 781){
                weatherIcon.setImageDrawable(itemView.getContext().getResources().getDrawable(R.drawable.ic_50d));
            }else if(weatherID == 800){
                weatherIcon.setImageDrawable(itemView.getContext().getResources().getDrawable(R.drawable.ic_01d));
            }else if(weatherID == 801){
                weatherIcon.setImageDrawable(itemView.getContext().getResources().getDrawable(R.drawable.ic_02d));
            }else if(weatherID == 802){
                weatherIcon.setImageDrawable(itemView.getContext().getResources().getDrawable(R.drawable.ic_03d));
            }else if(weatherID == 803){
                weatherIcon.setImageDrawable(itemView.getContext().getResources().getDrawable(R.drawable.ic_04d));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "bindView: " + e.getMessage());
        }


        itemView.setOnClickListener(v -> {
//            Intent intent = new Intent(itemView.getContext(), )
        });
    }

    public static double getFahrenheit(double kelvin){
        return (kelvin - KELVIN_CELSIUS) * 9/5 +32;
    }

    private double getMinTemp(ArrayList<JSONObject> dayForecast) throws JSONException {
        double minTemp = Double.MAX_VALUE;

        for (JSONObject timeForecast : dayForecast){
            double timeTemp = timeForecast.getJSONObject("main").getDouble("temp_min");;
            if(minTemp > timeTemp){
                minTemp = timeTemp;
            }
        }

        return  minTemp;
    }

    private double getMaxTemp(ArrayList<JSONObject> dayForecast) throws JSONException {
        double maxTemp = 0.0;

        for (JSONObject timeForecast : dayForecast){
            double timeTemp = timeForecast.getJSONObject("main").getDouble("temp_max");;
            if(maxTemp < timeTemp){
                maxTemp = timeTemp;
            }
        }

        return  maxTemp;
    }

    private int getWeatherId(ArrayList<JSONObject> dayForecast, int dayPosition, int dayCount) throws JSONException {
        int preferredTimeIndex = 5;// Index for 15h00

        if (dayPosition == 0) {// If its the first forecasted day
            return dayForecast.get(0).getJSONArray("weather").getJSONObject(0).getInt("id");
        }else if(dayForecast.size() < preferredTimeIndex/*dayPosition == dayCount-1*/){// If its last forecasted day
            return dayForecast.get(dayForecast.size()-1).getJSONArray("weather").getJSONObject(0).getInt("id");
        }else{// For coming days, return weather icon for 15h00
            return dayForecast.get(preferredTimeIndex).getJSONArray("weather").getJSONObject(0).getInt("id");
        }
    }
}
