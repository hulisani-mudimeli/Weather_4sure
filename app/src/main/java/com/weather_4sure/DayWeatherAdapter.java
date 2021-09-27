package com.weather_4sure;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DayWeatherAdapter extends RecyclerView.Adapter<DayWeatherViewHolder>{
    private final Context context;
    private final ArrayList<String> days;

    public DayWeatherAdapter(Context context){
        this.context = context;

        days = new ArrayList<>();
        days.add("Tuesday");
        days.add("Wednesday");
        days.add("Thursday");
        days.add("Friday");
        days.add("Saturday");
        days.add("Sunday");
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
        String day = days.get(position);
        holder.bindView(day);
    }

    @Override
    public int getItemCount() {
        return days.size();
    }
}
