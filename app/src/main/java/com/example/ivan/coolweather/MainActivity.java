package com.example.ivan.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.ivan.coolweather.gson.Weather;

public class MainActivity extends AppCompatActivity
{
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT >= 21)
		{
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
		}
		setContentView(R.layout.activity_main);
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		if (preferences.getString("weather_id", null) != null)
		{
			startActivity(new Intent(this, WeatherActivity.class));
			finish();
		}
	}
}
