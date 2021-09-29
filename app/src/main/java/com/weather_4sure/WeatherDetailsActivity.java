package com.weather_4sure;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.imageview.ShapeableImageView;
import com.weather_4sure.RecyclerViewItems.DayWeatherAdapter;
import com.weather_4sure.RecyclerViewItems.DayWeatherViewHolder;

import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;

public class WeatherDetailsActivity extends AppCompatActivity {

    private ArrayList<JSONObject> dayForecast;
    private JSONObject cityData;

    private TextView addressView;
    private TextView dateTimeView;
    private TextView tempView;
    private TextView weatherDescView;
    private TextView topLoTempView;
    private TextView feelsView;
    private TextView sunriseView;
    private TextView sunsetView;
    private TextView windView;
    private TextView humidityView;
    private TextView pressureView;
    private TextView visibilityView;
    private ShapeableImageView weatherIcon;

    private String TAG = "WeatherDetailsActivityTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_details);

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        addressView = findViewById(R.id.address);
        dateTimeView = findViewById(R.id.dateTime);
        tempView = findViewById(R.id.temp);
        weatherDescView = findViewById(R.id.weatherDesc);
        topLoTempView = findViewById(R.id.topLoTemp);
        feelsView = findViewById(R.id.feels);
        sunriseView = findViewById(R.id.sunrise);
        sunsetView = findViewById(R.id.sunset);
        windView = findViewById(R.id.wind);
        humidityView = findViewById(R.id.humidity);
        pressureView = findViewById(R.id.pressure);
        visibilityView = findViewById(R.id.visibility);
        weatherIcon = findViewById(R.id.weatherIcon);


        try {
            dayForecast = new ArrayList<>();
            ArrayList<String> dayForecastDecode = (ArrayList<String>)getIntent().getSerializableExtra("dayForecast");
            for (String jsonStr : dayForecastDecode) {
                dayForecast.add(new JSONObject(jsonStr));
            }
            // Title
            LocalDate ld = Instant.ofEpochMilli(dayForecast.get(0).getLong("dt")*1000).atZone(ZoneId.systemDefault()).toLocalDate();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE");
            setTitle(ld.format(formatter));



            //Idle Time Forecast
            JSONObject idleTimeForecast = getIdleTimeForecast(dayForecast, getIntent().getIntExtra("dayPosition", 0));
            humidityView.setText(idleTimeForecast.getJSONObject("main").getInt("humidity") + "%");
            pressureView.setText(idleTimeForecast.getJSONObject("main").getInt("pressure") + " hPa");
            visibilityView.setText(idleTimeForecast.getInt("visibility")/1000 + " km");
            weatherDescView.setText(idleTimeForecast.getJSONArray("weather").getJSONObject(0).getString("description"));
            windView.setText(idleTimeForecast.getJSONObject("wind").getDouble("speed") + " m/s");

            LocalDateTime dateTimeLTD = Instant.ofEpochMilli(idleTimeForecast.getLong("dt")*1000).atZone(ZoneId.systemDefault()).toLocalDateTime();
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("EEE, d MMMM HH:ss");
            dateTimeView.setText(dateTimeLTD.format(dateTimeFormatter));

            addressView.setText(getIntent().getStringExtra("locality"));


            int weatherId = idleTimeForecast.getJSONArray("weather").getJSONObject(0).getInt("id");
            DayWeatherViewHolder.weatherIconSet(weatherId, weatherIcon, this);

            if(DayWeatherAdapter.isCelsius) {
                tempView.setText((int) DayWeatherViewHolder.getCelsius(idleTimeForecast.getJSONObject("main").getDouble("temp")) + "°");
                feelsView.setText("Feels like " + (int) DayWeatherViewHolder.getCelsius(idleTimeForecast.getJSONObject("main").getDouble("feels_like")) + "°");

                String minTemp = (int) DayWeatherViewHolder.getCelsius(getIntent().getDoubleExtra("minTempInKelvin", 0.0)) + "°";
                String maxTemp = (int) DayWeatherViewHolder.getCelsius(getIntent().getDoubleExtra("maxTempInKelvin", 0.0)) + "°";
                topLoTempView.setText(maxTemp+"/"+minTemp);
            }else{
                tempView.setText((int) DayWeatherViewHolder.getFahrenheit(idleTimeForecast.getJSONObject("main").getDouble("temp")) + "°");
                feelsView.setText("Feels like " + (int) DayWeatherViewHolder.getFahrenheit(idleTimeForecast.getJSONObject("main").getDouble("feels_like")) + "°");

                String minTemp = (int) DayWeatherViewHolder.getFahrenheit(getIntent().getDoubleExtra("minTempInKelvin", 0.0)) + "°";
                String maxTemp = (int) DayWeatherViewHolder.getFahrenheit(getIntent().getDoubleExtra("maxTempInKelvin", 0.0)) + "°";
                topLoTempView.setText(maxTemp+"/"+minTemp);
            }

            // City Data
            cityData = new JSONObject(getIntent().getStringExtra("cityData"));
            LocalDateTime sunriseLdt = Instant.ofEpochMilli(cityData.getLong("sunrise")*1000).atZone(ZoneId.systemDefault()).toLocalDateTime();
            LocalDateTime sunsetLdt = Instant.ofEpochMilli(cityData.getLong("sunset")*1000).atZone(ZoneId.systemDefault()).toLocalDateTime();

            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            sunriseView.setText(sunriseLdt.format(timeFormatter));
            sunsetView.setText(sunsetLdt.format(timeFormatter));

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "onCreate: "+e.getMessage());
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private JSONObject getIdleTimeForecast(ArrayList<JSONObject> dayForecast, int dayPosition)  {
        int preferredTimeIndex = 4;// Index for 15h00

        if (dayPosition == 0) {// If its the first forecasted day
            return dayForecast.get(0);
        }else if(dayForecast.size() <= preferredTimeIndex/*dayPosition == dayCount-1*/){// If its last forecasted day
            return dayForecast.get(dayForecast.size()-1);
        }else{// For coming days, return weather icon for 15h00
            return dayForecast.get(preferredTimeIndex);
        }
    }
}