package com.weather_4sure;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

public class DayWeatherAdapter extends RecyclerView.Adapter<DayWeatherViewHolder>{
    private final Context context;
//    private final ArrayList<String> days;
    private ArrayList<ArrayList<JSONObject>>  daysForecastedMap;

    public DayWeatherAdapter(Context context, ArrayList<ArrayList<JSONObject>>  daysForecastedMap){
        this.context = context;
        this.daysForecastedMap = daysForecastedMap;

    }

    @NonNull
    @Override
    public DayWeatherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_day_weather, parent, false);
        return new DayWeatherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayWeatherViewHolder holder, int position) {
        JSONObject day = daysForecastedMap.get(position).get(0);
        holder.bindView(day);
    }

    @Override
    public int getItemCount() {
        return daysForecastedMap.size();
    }
}
