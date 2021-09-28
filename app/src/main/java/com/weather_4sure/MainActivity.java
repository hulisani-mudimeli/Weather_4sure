package com.weather_4sure;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMyLocationButtonClickListener {

    private GoogleMap mMap;
    private String TAG = "MainActivityTAG";
    private RelativeLayout headerLayout;
    private CardView movableSheet;
    private BottomSheetBehavior<CardView> behavior;
    private TextView userAddress;
    private TextView userAddressLabel;
    private Marker marker;
    private RecyclerView recycler;


    // Constants
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private static final short DEFAULT_ZOOM = 12;
    private static final short ZOOM_2 = 9;
    private static final short ZOOM_3 = 6;

    public static final double DEFAULT_LATITUDE = -25.686357;
    public static final double DEFAULT_LONGITUDE = 28.2410923;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AndroidThreeTen.init(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);

        headerLayout = findViewById(R.id.headerLayout);
        movableSheet = findViewById(R.id.bottom_sheet);
        behavior = BottomSheetBehavior.from(movableSheet);
        userAddress = findViewById(R.id.userAddress);
        userAddressLabel = findViewById(R.id.userAddressLabel);
        recycler = findViewById(R.id.recycler);


        // On Bottom Sheet View Expand
        behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    mMap.setPadding(0, 0, 0, headerLayout.getHeight() + recycler.getHeight());
                } else {
                    mMap.setPadding(0, 0, 0, headerLayout.getHeight());
                }

                if(marker != null){
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), ZOOM_2));
                }else if(mMap.isMyLocationEnabled()){
                    getMyLocation(true, false);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
        mMap.setOnMapClickListener(this);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(DEFAULT_LATITUDE, DEFAULT_LONGITUDE), ZOOM_3));
        checkLocationPermission();

        headerLayout.post(() -> {
            //Map Label
            mMap.setPadding(0, 0, 0, headerLayout.getHeight());

            // Peek Height
            float dp16 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, getResources().getDisplayMetrics());
            //ABOVE: 16DP height , to assist with hiding bottom radius on card view

            behavior.setPeekHeight((int) (headerLayout.getHeight()+dp16));
        });
    }

    private void checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }else{
            getMyLocation(false, true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if( requestCode == LOCATION_PERMISSION_REQUEST_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getMyLocation(true, true);
            }else{
                Log.w(TAG, "Location denied!");
            }
        }
    }

    private void getMyLocation(boolean animate, boolean getMoreInfo){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            FusedLocationProviderClient fusedLocationClient;
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            if(!mMap.isMyLocationEnabled())
                                mMap.setMyLocationEnabled(true);

                            if(getMoreInfo)
                                getAddressInfo(location.getLatitude(), location.getLongitude());

                            if(animate)
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), ZOOM_2));
                            else
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), ZOOM_2));
                        }
                    });
        }
    }

    private void getAddressInfo(double userLat, double userLon){

        getGeoInfo(userLat, userLon);
        getWeatherInfo(userLat, userLon);

    }

    private void getGeoInfo(double userLat, double userLon){

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(userLat, userLon, 1);
            if(addresses.size() > 0){
                StringBuilder display = new StringBuilder();
                if(addresses.get(0).getSubLocality() != null)
                    display.append(addresses.get(0).getSubLocality()).append(", ");

                if(addresses.get(0).getLocality() != null)
                    display.append(addresses.get(0).getLocality()).append(", ");

                if(addresses.get(0).getAdminArea() != null)
                    display.append(addresses.get(0).getAdminArea()).append(", ");

                if(addresses.get(0).getCountryName() != null)
                    display.append(addresses.get(0).getCountryName());

                userAddress.setText(display.toString());
//                Log.d(TAG, "getUserAddress: "+addresses.get(0));
            }else{
                userAddress.setText("Error retrieve user location!");
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void getWeatherInfo(double userLat, double userLon){
        OpenWeatherAPI.getInstance(this).getForecast(new LatLng(userLat, userLon));
    }

    protected BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public void addMarker(LatLng latLng){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
//        markerOptions.title("Selected Location");
        userAddressLabel.setText("Selected Location");

//        markerOptions.icon(bitmapDescriptorFromVector(this, R.drawable.ic_marker));
        marker = mMap.addMarker(markerOptions);
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        if(marker != null){
            marker.remove();
            marker = null;
        }

        getAddressInfo(latLng.latitude, latLng.longitude);
        addMarker(latLng);
    }

    @Override
    public boolean onMyLocationButtonClick() {
        if(marker != null){
            marker.remove();
            marker = null;
            userAddressLabel.setText("Current Location");
            getMyLocation(true, true);
            return true;
        }

        return false;
    }

    public void reloadRecycler(ArrayList<ArrayList<JSONObject>> daysForecastedMap){
        //Recycler
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(new DayWeatherAdapter(this, daysForecastedMap));

        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

    }
}