package com.example.ivan.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.effect.Effect;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.util.Util;
import com.example.ivan.coolweather.WeatherActivity;
import com.example.ivan.coolweather.gson.Weather;
import com.example.ivan.coolweather.util.HttpUtil;
import com.example.ivan.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

/**
 * Created by Ivan on 2017/4/3.
 */

public class AutoUpdateWeatherService extends Service
{
	private static final long UPDATE_TIME = 1000 * 60 * 60 * 8;//八小时更新一次
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Log.d("hello", "service update onStartCommand");
		updateWeather();
		updateBackgroundPic();
		long triggerTime = SystemClock.elapsedRealtime() + UPDATE_TIME;
		AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
		Intent intnet = new Intent(this, AutoUpdateWeatherService.class);
		PendingIntent pi = PendingIntent.getService(this, 0, intent, 0);
		manager.cancel(pi);
		manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pi);
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	private void updateBackgroundPic()
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		String weatherString = preferences.getString("weather", null);
		if (weatherString != null)
		{
			final Weather weather = Utility.handleWeatherResponse(weatherString);
			final String weatherId = weather.basic.weatherId;
			String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=f963e66355234362b4462e74e7bb408a";
			HttpUtil.sendOkhttpRequest(weatherUrl, new Callback()
			{
				@Override
				public void onFailure(Call call, IOException e)
				{
					e.printStackTrace();
				}
				
				@Override
				public void onResponse(Call call, Response response) throws IOException
				{
					String responseText = response.body().string();
					Weather weather = Utility.handleWeatherResponse(responseText);
					if (weather != null && weather.status.equals("ok"))
					{
						SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AutoUpdateWeatherService.this);
						SharedPreferences.Editor editor = preferences.edit();
						editor.putString("weather", responseText);
						editor.apply();
					}
				}
			});
		}
	}
	
	private void updateWeather()
	{
		final String requestUrl = "http://guolin.tech/api/bing_pic";
		HttpUtil.sendOkhttpRequest(requestUrl, new Callback()
		{
			@Override
			public void onFailure(Call call, IOException e)
			{
				e.printStackTrace();
			}
			
			@Override
			public void onResponse(Call call, Response response) throws IOException
			{
				String backgroundPic = response.body().string();
				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AutoUpdateWeatherService.this);
				SharedPreferences.Editor editor = preferences.edit();
				editor.putString("bing_pic", backgroundPic);
				editor.apply();
			}
		});
	}
	
	@Nullable
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}
}
