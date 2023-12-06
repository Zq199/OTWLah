package com.project.sc2006;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.libraries.places.api.model.Place;
import com.project.sc2006.controllers.PartyModeController;
import com.project.sc2006.controllers.WeatherController;
import com.project.sc2006.entities.Party;

public class WeatherActivity extends AppCompatActivity {
    private WeatherController weatherController;
    TextView weather_txt;
    Button refreshButton;
    ImageView backBtn;
    private Place destination;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_weather);

        Intent i = getIntent();
        destination = (Place)i.getExtras().get("location");

        weather_txt = findViewById(R.id.weather_txt);
        refreshButton = findViewById(R.id.refreshButton);
        weatherController = new WeatherController();
        backBtn = findViewById(R.id.check_weather_back);

        refreshWeather();

        refreshButton.setOnClickListener(v -> refreshWeather());

        backBtn.setOnClickListener(v -> finish());
    }

    private void refreshWeather() {
        weatherController.getWeather(destination, this, new WeatherController.WeatherCallback() {
            @Override
            public void onWeatherReceived(String weatherInfo) {
                Log.d("response", weatherInfo);
                weather_txt.setText(weatherInfo);
//                Toast.makeText(WeatherActivity.this, "Weather Fetched", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String errorMessage) {
//                Toast.makeText(WeatherActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
