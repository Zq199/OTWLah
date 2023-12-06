package com.project.sc2006.controllers;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.libraries.places.api.model.Place;
import com.project.sc2006.entities.Party;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

import okhttp3.HttpUrl;

public class WeatherController {
    public interface WeatherCallback {
        void onWeatherReceived(String weatherInfo);
        void onError(String errorMessage);
    }

    private DecimalFormat df = new DecimalFormat("#.##");
    private final String url = "http://api.openweathermap.org/data/2.5/weather";
    private final String appID = "5c9accb0d6c1dc2044a286d7f85cc6ef";

    public WeatherController() {
    }

    public void getWeather(Place destination, Activity runningActivity, WeatherCallback callback) {
        double latitude = destination.getLatLng().latitude;
        double longitude = destination.getLatLng().longitude;
        String tempUrl = url + "?lat=" + latitude + "&lon=" + longitude + "&appid=" + appID;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, tempUrl, response -> {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                JSONArray jsonArray = jsonResponse.getJSONArray("weather");
                JSONObject jsonObjectWeather = jsonArray.getJSONObject(0);
                String description = jsonObjectWeather.getString("description");
                JSONObject jsonObjectMain = jsonResponse.getJSONObject("main");
                double temp = jsonObjectMain.getDouble("temp") - 273.15;
                double feelsLike = jsonObjectMain.getDouble("feels_like") - 273.15;
                float pressure = jsonObjectMain.getInt("pressure");
                int humidity = jsonObjectMain.getInt("humidity");
                JSONObject jsonObjectWind = jsonResponse.getJSONObject("wind");
                String wind = jsonObjectWind.getString("speed");
                JSONObject jsonObjectClouds = jsonResponse.getJSONObject("clouds");
                String clouds = jsonObjectClouds.getString("all");
                String destinationName = destination.getName();
                String output = "Current weather at " + destinationName
                        + "\n Temp: " + df.format(temp) + " °C"
                        + "\n Feels Like: " + df.format(feelsLike) + " °C"
                        + "\n Humidity: " + humidity + "%"
                        + "\n Description: " + description
                        + "\n Wind Speed: " + wind + "m/s (meters per second)"
                        + "\n Cloudiness: " + clouds + "%"
                        + "\n Pressure: " + pressure + " hPa";

                callback.onWeatherReceived(output);

            } catch (JSONException e) {
                callback.onError("Error parsing JSON response");
            }
        }, error -> {
            String errorMessage = "Error in the request: " + error.toString();
            callback.onError("Error in the request: " + error.toString());
            Toast.makeText(runningActivity, errorMessage, Toast.LENGTH_SHORT).show();
        });

        RequestQueue requestQueue = Volley.newRequestQueue(runningActivity);
        requestQueue.add(stringRequest);
    }
}