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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.Status;
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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.textfield.TextInputEditText;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.weather_4sure.RecyclerViewItems.DayWeatherAdapter;
import com.weather_4sure.RecyclerViewItems.DayWeatherViewHolder;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMyLocationButtonClickListener {

    private GoogleMap mMap;
    private String TAG = "MainActivityTAG";
    private RelativeLayout headerLayout;
    private CardView movableSheet;
    private BottomSheetBehavior<CardView> behavior;
    private TextView userAddress;
    private TextView userAddressLabel;
    private TextView degreeLabelView;
    private Marker marker;
    private RecyclerView recycler;
    private TextInputEditText searchField;

    private String locality;


    // Constants
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 2;

    private static final short DEFAULT_ZOOM = 15;
    private static final short ZOOM_2 = 11;
    private static final short ZOOM_3 = 6;

    public static final double DEFAULT_LATITUDE = -25.686357;
    public static final double DEFAULT_LONGITUDE = 28.2410923;

    private boolean firstTime = true;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AndroidThreeTen.init(this);

        // Initialize the SDK
        Places.initialize(getApplicationContext(), getResources().getString(R.string.places_key));

        // Create a new PlacesClient instance
//        PlacesClient placesClient = Places.createClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);

        headerLayout = findViewById(R.id.headerLayout);
        movableSheet = findViewById(R.id.bottom_sheet);
        behavior = BottomSheetBehavior.from(movableSheet);
        userAddress = findViewById(R.id.userAddress);
        userAddressLabel = findViewById(R.id.userAddressLabel);
        recycler = findViewById(R.id.recycler);
        degreeLabelView = findViewById(R.id.degreeLabelView);
        searchField = findViewById(R.id.searchField);
        setLabelCelsius(degreeLabelView);


        // On Bottom Sheet View Expand
        behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    mMap.setPadding(0, 0, 0, headerLayout.getHeight() + recycler.getHeight());
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED){
                    mMap.setPadding(0, 0, 0, headerLayout.getHeight());
                }

                if(newState == BottomSheetBehavior.STATE_EXPANDED | newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    if (marker != null) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), DEFAULT_ZOOM));
                    } else if (mMap.isMyLocationEnabled()) {
                        getMyLocation(true, false);
                    }
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });


        searchField.setOnClickListener(v -> {
            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);

            // Start the autocomplete intent.
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                    .setCountry("ZA")
                    .build(this);
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());

                hideKeyboard();
                onMapClick(place.getLatLng());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), DEFAULT_ZOOM));
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void hideKeyboard(){
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
        mMap.setOnMapClickListener(this);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(DEFAULT_LATITUDE, DEFAULT_LONGITUDE), ZOOM_2));
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
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM));
                            else
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM));
                        }
                    });
        }
    }

    private void getAddressInfo(double userLat, double userLon){

        getGeoInfo(userLat, userLon);
        getWeatherInfo(userLat, userLon);

    }

    private void getGeoInfo(double userLat, double userLon){
        locality = null;

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(userLat, userLon, 1);
            if(addresses.size() > 0){
                StringBuilder display = new StringBuilder();
                if(addresses.get(0).getSubLocality() != null) {
                    display.append(addresses.get(0).getSubLocality()).append(", ");
                    locality = addresses.get(0).getSubLocality();
                }

                if(addresses.get(0).getLocality() != null) {
                    display.append(addresses.get(0).getLocality()).append(", ");

                    if(locality == null) {
                        locality = addresses.get(0).getLocality();
                    }else{
                        locality += ", " + addresses.get(0).getLocality();

                    }
                }

                if(addresses.get(0).getAdminArea() != null) {
                    display.append(addresses.get(0).getAdminArea()).append(", ");
                    if(locality == null) {
                        locality = addresses.get(0).getAdminArea();
                    }
                }

                if(addresses.get(0).getCountryName() != null) {
                    display.append(addresses.get(0).getCountryName());
                    if(locality == null) {
                        locality = addresses.get(0).getCountryName();
                    }
                }

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

    public void reloadRecycler(ArrayList<ArrayList<JSONObject>> daysForecastedMap, JSONObject cityData){
        //Recycler
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(new DayWeatherAdapter(this, daysForecastedMap, cityData, locality));

        if(firstTime) {
            if (behavior.getState() != BottomSheetBehavior.STATE_EXPANDED)
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

            firstTime = false;
        }
    }

    public void degreeToggle(View view) {
        DayWeatherAdapter adapter = (DayWeatherAdapter)recycler.getAdapter();
        ArrayList<DayWeatherViewHolder> holders = adapter.getHolders();


        if(DayWeatherAdapter.isCelsius) {
            for (DayWeatherViewHolder holder : holders) {
                holder.hiTempView.setText((int)DayWeatherViewHolder.getFahrenheit(holder.maxTempInKelvin)+ "°");
                holder.loTempView.setText((int)DayWeatherViewHolder.getFahrenheit(holder.minTempInKelvin)+ "°");
            }
            DayWeatherAdapter.isCelsius = false;
            setLabelFahrenheit(degreeLabelView);
        }else{
            for (DayWeatherViewHolder holder : holders) {
                holder.hiTempView.setText((int)DayWeatherViewHolder.getCelsius(holder.maxTempInKelvin)+ "°");
                holder.loTempView.setText((int)DayWeatherViewHolder.getCelsius(holder.minTempInKelvin)+ "°");
            }
            DayWeatherAdapter.isCelsius = true;
            setLabelCelsius(degreeLabelView);
        }

        if(behavior.getState() != BottomSheetBehavior.STATE_EXPANDED)
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void setLabelCelsius(TextView view){
        Spannable spannable = new SpannableString(view.getText().toString());
//        spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        spannable.setSpan(new StyleSpan(Typeface.NORMAL), 1, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.black)), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.light_grey)), 1, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        view.setText(spannable);
    }

    private void setLabelFahrenheit(TextView view){
        Spannable spannable = new SpannableString(view.getText().toString());
//        spannable.setSpan(new StyleSpan(Typeface.NORMAL), 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        spannable.setSpan(new StyleSpan(Typeface.BOLD), 4, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.light_grey)), 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.black)), 4, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        view.setText(spannable);
    }

}