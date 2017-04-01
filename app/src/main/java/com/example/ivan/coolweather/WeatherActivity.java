package com.example.ivan.coolweather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ivan.coolweather.gson.Forecast;
import com.example.ivan.coolweather.gson.Weather;
import com.example.ivan.coolweather.util.HttpUtil;
import com.example.ivan.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity
{
	private static final String HEWEATHER_API_KEY = "f963e66355234362b4462e74e7bb408a";
	private static final String BING_PIC_API = "http://guolin.tech/api/bing_pic";
	
	private ScrollView weatherLayout;
	private TextView titleCity;
	private TextView titleUpdateTime;
	private TextView degreeText;
	private TextView weatherInfoText;
	private LinearLayout forecastLayout;
	private LinearLayout aqiLayout;
	private TextView aqiText;
	private TextView pm25Text;
	private TextView comfortText;
	private TextView carWashText;
	private TextView sportText;
	private ImageView backgroundPic;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT >= 21)
		{
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN );
			getWindow().setStatusBarColor(Color.TRANSPARENT);
		}
		setContentView(R.layout.activity_weather);
		
		weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
		titleCity = (TextView) findViewById(R.id.title_city);
		titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
		degreeText = (TextView) findViewById(R.id.degree_text);
		weatherInfoText = (TextView) findViewById(R.id.weather_inf_text);
		forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
		aqiLayout = (LinearLayout) findViewById(R.id.aqi_layout);
		aqiText = (TextView) findViewById(R.id.aqi_text);
		pm25Text = (TextView) findViewById(R.id.pm25_text);
		comfortText = (TextView) findViewById(R.id.comfort_text);
		carWashText = (TextView) findViewById(R.id.car_wash_text);
		sportText = (TextView) findViewById(R.id.sport_text);
		backgroundPic = (ImageView) findViewById(R.id.background_pic);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String weatherString = prefs.getString("weather", null);
		String bingPic = prefs.getString("bing_pic", null);
		if (bingPic != null)
			Glide.with(this).load(bingPic).into(backgroundPic);
		else
			loadBackgroundPic();
		
		if (weatherString != null)
		{
			Weather weather = Utility.handleWeatherResponse(weatherString);
			showWeatherInfo(weather);
		} else
		{
			String weatherId = getIntent().getStringExtra("weather_id");
			weatherLayout.setVisibility(View.INVISIBLE);
			requestWeather(weatherId);
		}
	}
	
	private void loadBackgroundPic()
	{
		HttpUtil.sendOkhttpRequest(BING_PIC_API, new Callback()
		{
			@Override
			public void onFailure(Call call, IOException e)
			{
				runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						Toast.makeText(WeatherActivity.this, "加载背景图片失败！", Toast.LENGTH_SHORT).show();
					}
				});
			}
			
			@Override
			public void onResponse(Call call, Response response)
			{
				final String picAddress;
				try
				{
					picAddress = response.body().string();
					SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
					editor.putString("bing_pic", picAddress);
					editor.apply();
					runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							Glide.with(WeatherActivity.this).load(picAddress).into(backgroundPic);
						}
					});
				} catch (Exception e)
				{
					e.printStackTrace();
					Toast.makeText(WeatherActivity.this, "加载背景图片失败！", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
	
	private void requestWeather(String weatherId)
	{
		String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=" + HEWEATHER_API_KEY;
		HttpUtil.sendOkhttpRequest(weatherUrl, new Callback()
		{
			@Override
			public void onFailure(Call call, IOException e)
			{
				e.printStackTrace();
				runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						Toast.makeText(WeatherActivity.this, "获取天气信息失败！", Toast.LENGTH_SHORT).show();
					}
				});
			}
			
			@Override
			public void onResponse(Call call, Response response) throws IOException
			{
				final String responseText = response.body().string();
				final Weather weather = Utility.handleWeatherResponse(responseText);
				runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						if (weather != null && weather.status.equals("ok"))
						{
							SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
							editor.putString("weather", responseText);
							editor.apply();
							showWeatherInfo(weather);
						} else
							Toast.makeText(WeatherActivity.this, "获取天气信息失败！", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}
	
	private void showWeatherInfo(Weather weather)
	{
		titleCity.setText(weather.basic.cityName);
		titleUpdateTime.setText("更新时间：" + weather.basic.update.updateTime);
		degreeText.setText(weather.now.temperature + "℃");
		weatherInfoText.setText(weather.now.more.inf);
		
		forecastLayout.removeAllViews();
		for (Forecast forecast : weather.forecastList)
		{
			View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
			((TextView) view.findViewById(R.id.date_text)).setText(forecast.date);
			((TextView) view.findViewById(R.id.info_text)).setText(forecast.more.inf);
			((TextView) view.findViewById(R.id.max_text)).setText("max " + forecast.temperature.max + "℃");
			((TextView) view.findViewById(R.id.min_text)).setText("min " + forecast.temperature.min + "℃");
			forecastLayout.addView(view);
		}
		if (weather.aqi != null)
		{
			aqiText.setText(weather.aqi.city.aqi);
			pm25Text.setText(weather.aqi.city.pm25);
		} else
		{
			aqiLayout.setVisibility(View.GONE);
		}
		comfortText.setText(weather.suggestion.comfort.inf);
		carWashText.setText(weather.suggestion.carWash.inf);
		sportText.setText(weather.suggestion.sport.inf);
		weatherLayout.setVisibility(View.VISIBLE);
	}
}
