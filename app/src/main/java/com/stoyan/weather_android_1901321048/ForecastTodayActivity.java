package com.stoyan.weather_android_1901321048;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ForecastTodayActivity extends DBActivity implements LocationListener{

    protected EditText editPlace;
    protected Button btnSearch, btn5, btn8, btnHistory;
    protected ListView simpleList;
    protected LocationManager locationManager;
    protected LinearLayout linearLayout;

    protected String GetDate(){
        DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 0);
        Date date = calendar.getTime();
        String dateString = dateFormat.format(date);
        return dateString;
    }

    private void GPSOff() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable Location").setCancelable(false).setPositiveButton("Yes", (dialog, which) -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))).
                setNegativeButton("No", (dialog, which) -> dialog.cancel());
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void getLocation(){
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(ForecastTodayActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                locationManager.removeUpdates(this);
                double lat = location.getLatitude();
                double lon = location.getLongitude();

                String openWeather = "https://api.openweathermap.org/data/2.5/weather?lat="+ lat +"&lon="+ lon +"&appid=3e28280e44058a208eb434aa54b13f5c";
                getResult(openWeather, false);

            } else {
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, this);
                Toast.makeText(getApplicationContext(), "Finding location. Please wait!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getResult(String openWeather, boolean saveToDb){
        AsyncForecastToday(openWeather,
                new CallBackListenerToday() {
                    @Override
                    public void onError() {
                        Toast.makeText(getApplicationContext(),"unsuccessful rquest", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(int weatherId, boolean isDay, ArrayList<String> elems, String place) {
                        editPlace.setText(place);
                        ArrayAdapter<String> arrayAdapter = null;
                        if(!isDay){
                            arrayAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.activity_listview_night, R.id.textView, elems );
                            editPlace.setTextColor(Color.WHITE);
                        }
                        else{
                            arrayAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.activity_listview, R.id.textView, elems);
                            linearLayout.getBackground().setAlpha(80);
                            editPlace.setTextColor(Color.BLACK);
                        }
                        simpleList.clearChoices();
                        simpleList.setAdapter(arrayAdapter);

                        if(weatherId > 800) {
                            if(isDay)
                                linearLayout.setBackground(getDrawable(R.drawable.clouds));
                            else
                                linearLayout.setBackground(getDrawable(R.drawable.cloudsnight));
                        }
                        else if(weatherId == 800) {
                            if(isDay)
                                linearLayout.setBackground(getDrawable(R.drawable.clear));
                            else
                                linearLayout.setBackground(getDrawable(R.drawable.nightsky));
                        }
                        else if(weatherId > 700 && weatherId < 800) {
                            if(isDay)
                                linearLayout.setBackground(getDrawable(R.drawable.fog));
                            else
                                linearLayout.setBackground(getDrawable(R.drawable.fognight));
                        }
                        else if(weatherId >= 600 && weatherId < 700) {
                            if(isDay)
                                linearLayout.setBackground(getDrawable(R.drawable.snow));
                            else
                                linearLayout.setBackground(getDrawable(R.drawable.snownight));
                        }
                        else if(weatherId >= 300 && weatherId < 600) {
                            if(isDay)
                                linearLayout.setBackground(getDrawable(R.drawable.rain));
                            else
                                linearLayout.setBackground(getDrawable(R.drawable.rainnight));
                        }
                        else if(weatherId >= 200 && weatherId < 300) {
                            if(isDay)
                                linearLayout.setBackground(getDrawable(R.drawable.thunderstorm));
                            else
                                linearLayout.setBackground(getDrawable(R.drawable.thunderstormnight));
                        }
                        if(saveToDb == true) {
                            try {
                                initDB();
                                ExecSQL(
                                        "INSERT INTO QueryHistory(Place, Type, Date) " +
                                                "Values(?,?, ?) ",
                                        new Object[]{
                                                editPlace.getText().toString(),
                                                "That day",
                                                GetDate()
                                        },
                                        () -> Toast.makeText(getApplicationContext(), "Record Inserted", Toast.LENGTH_SHORT).show()

                                );
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), "Insert Failed" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_forecast_today);

        editPlace = findViewById(R.id.editPlaceToday);
        btnSearch = findViewById(R.id.btnSearch);
        btn5 = findViewById(R.id.button5);
        btn8 = findViewById(R.id.button8);
        btnHistory = findViewById(R.id.buttonHistory);
        simpleList = findViewById(R.id.simpleList);
        linearLayout = findViewById(R.id.layoutToday);
        linearLayout.getBackground().setAlpha(140);

        ActivityCompat.requestPermissions( this,
                new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            GPSOff();
        } else {
            getLocation();
        }
        editPlace.setOnLongClickListener(view -> {
            editPlace.setText("");
            return true;
        });

        btnSearch.setOnClickListener(view ->{
            locationManager.removeUpdates(this);
            String openWeather = "https://api.openweathermap.org/data/2.5/weather?q="+ editPlace.getText().toString() +"&appid=3e28280e44058a208eb434aa54b13f5c";
            getResult(openWeather, true);
        });
        btn5.setOnClickListener(onClick);
        btn8.setOnClickListener(onClick);
        btnHistory.setOnClickListener(onClick);

    }

    View.OnClickListener onClick = v -> {

        Intent intent;

        switch (v.getId()){
            case R.id.button5:
                intent = new Intent(ForecastTodayActivity.this, ForecastActivity.class);
                break;
            case R.id.button8:
                intent = new Intent(ForecastTodayActivity.this, Forecast8Activity.class);
                break;
            case R.id.buttonHistory:
                intent = new Intent(ForecastTodayActivity.this, HistoryActivity.class);
                break;

            default:
                intent = new Intent();
        }
        startActivity(intent);
    };

    @Override
    public void onLocationChanged(@NonNull Location location) {
        getLocation();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}