package com.weather_4sure;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class WeatherYdnRequestManager {
    private static WeatherYdnRequestManager sInstance;

    Context mContext;
    RequestQueue mRequestQueue;

    public static synchronized WeatherYdnRequestManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new WeatherYdnRequestManager(context);
        }
        return sInstance;
    }

    private WeatherYdnRequestManager(Context context) {
        mContext = context;
        mRequestQueue = Volley.newRequestQueue(mContext);
    }

    public <T> void addToRequestQueue(Request<T> request) {
        mRequestQueue.add(request);
    }
}
