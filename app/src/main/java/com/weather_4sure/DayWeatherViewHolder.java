package com.weather_4sure;

import android.os.Build;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

public class DayWeatherViewHolder extends RecyclerView.ViewHolder{
    private TextView dayView;
    private ShapeableImageView  weatherIcon;
    private TextView hiTempView;
    private TextView loTempView;

    public DayWeatherViewHolder(@NonNull View itemView) {
        super(itemView);
        dayView = itemView.findViewById(R.id.dayView);
        weatherIcon = itemView.findViewById(R.id.weatherIcon);
        hiTempView = itemView.findViewById(R.id.hiTemp);
        loTempView = itemView.findViewById(R.id.loTemp);
    }

    public void bindView(String day){
        dayView.setText(day);
        hiTempView.setText("29");
        loTempView.setText("15");
    }
}
