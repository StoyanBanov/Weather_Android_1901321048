package com.stoyan.weather_android_1901321048;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ForecastActivity extends DBActivity implements LocationListener {

    protected EditText editPlace;
    protected Button btnSearch;
    protected ListView simpleList;
    protected LinearLayout linearLayout;
    protected LocationManager locationManager;

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
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes", new  DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void getLocation(){
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(ForecastActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                locationManager.removeUpdates(this);
                double lat = location.getLatitude();
                double lon = location.getLongitude();

                String openWeather = "https://api.openweathermap.org/data/2.5/weather?lat="+ lat +"&lon="+ lon +"&appid=3e28280e44058a208eb434aa54b13f5c";

                AsyncPlace(openWeather, new CallBackListenerString() {
                    @Override
                    public void onError(String message) {
                        Toast.makeText(getApplicationContext(), "No data about your location", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String place) {
                        editPlace.setText(place);
                        getResult("https://api.openweathermap.org/data/2.5/forecast?lat="+ lat +"&lon="+ lon +"&appid=3e28280e44058a208eb434aa54b13f5c", false);
                    }
                });
            } else {
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, this);
                Toast.makeText(getApplicationContext(), "Finding location. Please wait!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getResult(String openWeather, boolean saveToDb){
        AsyncFrecast5(openWeather,
                new CallBackListener() {
                    @Override
                    public void onError() {
                        Toast.makeText(getApplicationContext(),"unsuccessful rquest", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(ArrayList<String> elems) {
                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.activity_listview, R.id.textView, elems);
                        simpleList.clearChoices();
                        simpleList.setAdapter(arrayAdapter);
                        if(saveToDb == true) {
                            try {
                                initDB();
                                ExecSQL(
                                        "INSERT INTO QueryHistory(Place, Type, Date) " +
                                                "Values(?,?,?) ",
                                        new Object[]{
                                                editPlace.getText().toString(),
                                                "Five-day",
                                                GetDate()
                                        },
                                        () -> Toast.makeText(getApplicationContext(), "Record Inserted", Toast.LENGTH_LONG).show()

                                );
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), "Insert Failed" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_forecast);

        linearLayout = findViewById(R.id.Forecast5Layout);
        linearLayout.getBackground().setAlpha(85);

        editPlace = findViewById(R.id.editPlace5);
        btnSearch = findViewById(R.id.btnSearch);
        simpleList = findViewById(R.id.simpleList);

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
            String openWeather = "https://api.openweathermap.org/data/2.5/forecast?q=" + editPlace.getText().toString() + "&appid=3e28280e44058a208eb434aa54b13f5c";
            getResult(openWeather, true);
        });
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        getLocation();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(ForecastActivity.this, ForecastTodayActivity.class));
        finish();
    }
}