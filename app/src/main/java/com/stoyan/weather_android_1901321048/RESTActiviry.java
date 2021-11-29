package com.stoyan.weather_android_1901321048;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;

public class RESTActiviry extends AppCompatActivity {

    public interface CallBackListener{
        void onError();

        void onResponse(ArrayList<String> elems);
    }

    public interface CallBackListenerToday {
        void onError();

        void onResponse(int conditions, boolean isDay, ArrayList<String> elems, String place);
    }

    public interface CallBackListenerString{
        void onError(String message);

        void onResponse(String place);
    }

    public void AsyncFrecast5(String url, CallBackListener callBack){

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                final ArrayList<String> arrayList=new ArrayList<>();

                try {
                    JSONArray ja = response.getJSONArray("list");

                    for (int i=0; i<ja.length();i+=8) {
                        JSONObject jo = (JSONObject) ja.get(i);
                        String date = jo.get("dt_txt").toString();
                        date = date.split(" ")[0];

                        String nextDay = "Date: " + date + "\n" + "\t";
                        int hour = 0;

                        for (int j=i; j < i + 8; j++) {
                            jo = (JSONObject) ja.get(j);
                            JSONObject main = (JSONObject) jo.get("main");
                            String current = main.get("temp").toString();
                            hour+=3;
                            int currentF = (int) (Float.parseFloat(current) - 273.15F);
                            if(j == i + 2 || j == i + 4 || j == i + 6)
                                nextDay = nextDay + "\n" + "\t";
                            if(j < i + 7)
                                nextDay = nextDay  + hour + "h: " + currentF + "\u00B0" + "C, ";
                            else if(j == i + 7)
                                nextDay = nextDay  + hour + "h: " + currentF + "\u00B0" + "C" + "\n";

                        }
                        arrayList.add(nextDay);
                    }
                    Toast.makeText(getApplicationContext(),"successful rquest", Toast.LENGTH_SHORT).show();
                    callBack.onResponse(arrayList);

                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(()-> Toast.makeText(getApplicationContext(),
                            e.getLocalizedMessage(),
                            Toast.LENGTH_LONG).show());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callBack.onError();
                Toast.makeText(getApplicationContext(),"spelling or internet issue", Toast.LENGTH_SHORT).show();
            }
        });
        SingletonClass.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }



    public void AsyncFrecast8(String url,
                              CallBackListener callBack){

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                final ArrayList<String> arrayList=new ArrayList<>();

                try {
                    JSONArray ja = response.getJSONArray("daily");

                    DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

                    for (int i=0; i<ja.length();i++) {

                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.DAY_OF_YEAR, i);
                        Date date = calendar.getTime();
                        String dateString = dateFormat.format(date);

                        JSONObject jo = (JSONObject) ja.get(i);
                        JSONObject temp = (JSONObject) jo.get("temp");
                        int averageTemp = (int)(Float.parseFloat(temp.get("day").toString()) - 273.15F);
                        int minTemp = (int)(Float.parseFloat(temp.get("min").toString()) - 273.15F);
                        int maxTemp = (int)(Float.parseFloat(temp.get("max").toString()) - 273.15F);

                        JSONArray weather = (JSONArray) new JSONTokener(jo.get("weather").toString()).nextValue();
                        JSONObject desc = (JSONObject)weather.get(0);
                        String description = desc.get("description").toString();

                        String nextDay = "Date: " + dateString + "\n" + "\t" + "Temp: " + averageTemp + "\u00B0" + "C; Min: " + minTemp + "\u00B0" + "C; Max: " + maxTemp + "\u00B0" + "C" + "\n" + "\t" + "Conditions: " + description + "\n";
                        arrayList.add(nextDay);
                    }
                    Toast.makeText(getApplicationContext(),"successful rquest", Toast.LENGTH_SHORT).show();
                    callBack.onResponse(arrayList);

                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(()-> Toast.makeText(getApplicationContext(),
                            e.getLocalizedMessage(),
                            Toast.LENGTH_LONG).show());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callBack.onError();
            }
        });
        SingletonClass.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }


    public void AsyncPlace(String url, CallBackListenerString callBack){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    String name = response.getString("name");
                    callBack.onResponse(name);

                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(()-> Toast.makeText(getApplicationContext(),
                            e.getLocalizedMessage(),
                            Toast.LENGTH_LONG).show());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callBack.onError("no place found");
                Toast.makeText(getApplicationContext(),"spelling or internet issue", Toast.LENGTH_SHORT).show();
            }
        });
        SingletonClass.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    public void AsyncPlaceLatLon(String url, CallBackListenerString callBack){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    JSONObject coord = response.getJSONObject("coord");
                    String lat = coord.getString("lat");
                    String lon = coord.getString("lon");
                    String coordinates = lat + "&lon=" + lon;

                    callBack.onResponse(coordinates);


                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(()-> Toast.makeText(getApplicationContext(),
                            e.getLocalizedMessage(),
                            Toast.LENGTH_LONG).show());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callBack.onError("no place found");
                Toast.makeText(getApplicationContext(),"spelling or internet issue", Toast.LENGTH_SHORT).show();
            }
        });
        SingletonClass.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }


    public void AsyncForecastToday(String url, CallBackListenerToday callBack){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                final ArrayList<String> arrayList=new ArrayList<>();
                try {
                    JSONArray ja = response.getJSONArray("weather");
                    JSONObject weather = ja.getJSONObject(0);
                    String conditions = weather.getString("description");
                    int weatherId = weather.getInt("id");

                    DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.DAY_OF_YEAR, 0);
                    Date date = calendar.getTime();
                    String dateString = dateFormat.format(date);

                    String dateElement = "Date: " + dateString + "\n";
                    arrayList.add(dateElement);

                    arrayList.add("Conditions: " + conditions);

                    JSONObject main = response.getJSONObject("main");
                    int temp = (int)(Float.parseFloat(main.getString("temp")) - 273.15F);
                    arrayList.add("Temperature: " + temp + "\u00B0" + "C");

                    int feels_like = (int)(Float.parseFloat(main.getString("feels_like")) - 273.15F);
                    arrayList.add("Feels Like: " + feels_like + "\u00B0" + "C");

                    int temp_min = (int)(Float.parseFloat(main.getString("temp_min")) - 273.15F);
                    arrayList.add("Min Temperature: " + temp_min + "\u00B0" + "C");

                    int temp_max = (int)(Float.parseFloat(main.getString("temp_max")) - 273.15F);
                    arrayList.add("Max Temperature: " + temp_max + "\u00B0" + "C");

                    int pressure = Integer.parseInt(main.getString("pressure"));
                    arrayList.add("Pressure: " + pressure + "hPa");

                    int humidity = Integer.parseInt(main.getString("humidity"));
                    arrayList.add("Humidity: " + humidity);

                    String timezone = response.getString("timezone");

                    JSONObject sun = response.getJSONObject("sys");

                    String sunrise = sun.getString("sunrise");
                    Double sunriseInSeconds = Double.parseDouble(sunrise);
                    sunriseInSeconds += Float.parseFloat(timezone);
                    sunriseInSeconds /= 3600;
                    sunriseInSeconds /= 24;
                    sunriseInSeconds = Double.parseDouble("0." + sunriseInSeconds.toString().split("\\.")[1]);
                    sunriseInSeconds *= 24;
                    String minutes = sunriseInSeconds.toString().split("\\.")[1];

                    Float minutes10 = 0F;
                    if(minutes.toCharArray().length >= 1)
                        minutes10 = Float.parseFloat(minutes.toCharArray()[0] + "");
                    minutes10 = 60 * minutes10 / 10;

                    Float minutes01 = 0F;
                    if(minutes.toCharArray().length > 1) {
                        minutes01 = Float.parseFloat(minutes.toCharArray()[1] + "");
                        if(minutes.toCharArray().length > 2)
                            minutes01 = Float.parseFloat(minutes.toCharArray()[1] + "." + minutes.toCharArray()[2]);
                        if(minutes.toCharArray().length > 3)
                            minutes01 = Float.parseFloat(minutes.toCharArray()[1] + "." + minutes.toCharArray()[2] + minutes.toCharArray()[3]);
                    }
                    minutes01 = 60 * minutes01 / 100;

                    Float minutes11 = minutes10 + minutes01;

                    if(Integer.parseInt(minutes11.toString().split("\\.")[1].toCharArray()[0] + "") > 5)
                        minutes11 +=1;
                    if(minutes11 >= 60) {
                        minutes11 -= 1;
                    }

                    String timeSunrise = "";
                    if(minutes11 >= 10) {
                        sunrise = sunriseInSeconds.toString().split("\\.")[0] + ":" + minutes11.toString().split("\\.")[0];
                        timeSunrise = sunriseInSeconds.toString().split("\\.")[0] + "." + minutes11.toString().split("\\.")[0];
                    }
                    else {
                        sunrise = sunriseInSeconds.toString().split("\\.")[0] + ":0" + minutes11.toString().split("\\.")[0];
                        timeSunrise = sunriseInSeconds.toString().split("\\.")[0] + ".0" + minutes11.toString().split("\\.")[0];
                    }

                    arrayList.add("Sunrise:" + sunrise);


                    String sunset = sun.getString("sunset");
                    Double sunsetInSeconds = Double.parseDouble(sunset);
                    sunsetInSeconds += Float.parseFloat(timezone);
                    sunsetInSeconds /= 3600;
                    sunsetInSeconds /= 24;
                    sunsetInSeconds = Double.parseDouble("0." + sunsetInSeconds.toString().split("\\.")[1]);
                    sunsetInSeconds *= 24;
                    minutes = sunsetInSeconds.toString().split("\\.")[1];

                    minutes10 = 0F;
                    if(minutes.toCharArray().length >= 1)
                        minutes10 = Float.parseFloat(minutes.toCharArray()[0] + "");
                    minutes10 = 60 * minutes10 / 10;

                    minutes01 = 0F;
                    if(minutes.toCharArray().length > 1)
                        minutes01 = Float.parseFloat(minutes.toCharArray()[1] + "");
                    minutes01 = 60 * minutes01 / 100;

                    minutes11 = minutes10 + minutes01;

                    if(Integer.parseInt(minutes11.toString().split("\\.")[1].toCharArray()[0] + "") > 5)
                        minutes11 +=1;
                    if(minutes11 == 60)
                        minutes11 -=1;

                    String timeSunset = "";
                    if(minutes11 >= 10) {
                        sunset = sunsetInSeconds.toString().split("\\.")[0] + ":" + minutes11.toString().split("\\.")[0];
                        timeSunset = sunsetInSeconds.toString().split("\\.")[0] + "." + minutes11.toString().split("\\.")[0];
                    }
                    else {
                        sunset = sunsetInSeconds.toString().split("\\.")[0] + ":0" + minutes11.toString().split("\\.")[0];
                        timeSunset = sunsetInSeconds.toString().split("\\.")[0] + ".0" + minutes11.toString().split("\\.")[0];
                    }

                    arrayList.add("Sunset:" + sunset);

                    Float currentTime = Float.parseFloat("" + calendar.get(Calendar.HOUR_OF_DAY) + "." + calendar.get(Calendar.MINUTE));

                    if(Float.parseFloat(timezone)/3600 < 0)
                        if (currentTime < Float.parseFloat(timezone)/3600 * -1)
                            currentTime += 24;
                    currentTime += Float.parseFloat(timezone)/3600 - 2;

                    boolean isDay = false;
                    if(currentTime > Float.parseFloat(timeSunrise) &&
                        currentTime < Float.parseFloat(timeSunset))
                        isDay = true;

                    boolean finalIsDay = isDay;
                    Toast.makeText(getApplicationContext(),"successful rquest", Toast.LENGTH_SHORT).show();
                    callBack.onResponse(weatherId, finalIsDay, arrayList, response.getString("name"));

                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(()-> Toast.makeText(getApplicationContext(),
                            e.getLocalizedMessage(),
                            Toast.LENGTH_LONG).show());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callBack.onError();
                Toast.makeText(getApplicationContext(),"spelling or internet issue", Toast.LENGTH_SHORT).show();
            }
        });
        SingletonClass.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_r_e_s_t_activiry);
    }
}