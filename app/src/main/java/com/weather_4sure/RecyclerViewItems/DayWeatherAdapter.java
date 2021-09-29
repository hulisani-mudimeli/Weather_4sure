package com.weather_4sure.RecyclerViewItems;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.weather_4sure.R;

import org.json.JSONObject;

import java.util.ArrayList;

public class DayWeatherAdapter extends RecyclerView.Adapter<DayWeatherViewHolder>{
    private final Context context;
//    private final ArrayList<String> days;
    private ArrayList<ArrayList<JSONObject>>  daysForecastedMap;
    private ArrayList<DayWeatherViewHolder> holders;
    public static boolean isCelsius = true;
    private JSONObject cityData;
    private String locality;

    public DayWeatherAdapter(Context context, ArrayList<ArrayList<JSONObject>>  daysForecastedMap, JSONObject cityData, String locality){
        this.locality = locality;
        this.context = context;
        this.daysForecastedMap = daysForecastedMap;
        this.cityData = cityData;
        holders = new ArrayList<>();
    }

    @NonNull
    @Override
    public DayWeatherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_day_weather, parent, false);

        DayWeatherViewHolder holder = new DayWeatherViewHolder(view);
        holders.add(holder);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull DayWeatherViewHolder holder, int position) {
        //TODO: get appropriate data
        ArrayList<JSONObject> dayForecast = daysForecastedMap.get(position);
        holder.bindView(dayForecast, cityData, position, daysForecastedMap.size(), locality);
    }

    @Override
    public int getItemCount() {
        return daysForecastedMap.size();
    }

    public ArrayList<DayWeatherViewHolder> getHolders() {
        return holders;
    }
}
