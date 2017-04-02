package com.example.ivan.coolweather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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
	private LinearLayout weatherLinearLayout;
	
	public SwipeRefreshLayout refreshLayout;
	public DrawerLayout drawerLayout;
	public Button selectCity;
	
	private float x = 1;
	private float y = 1;
	private boolean up = true;
	private boolean resume;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT >= 21)
		{
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
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
		refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
		weatherLinearLayout = (LinearLayout) findViewById(R.id.weather_linear_layout);
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		selectCity = (Button) findViewById(R.id.select_city_button);
		
		selectCity.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				drawerLayout.openDrawer(GravityCompat.START);
			}
		});
		
		final String weatherId;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String weatherString = prefs.getString("weather", null);
		String bingPic = prefs.getString("bing_pic", null);
		if (bingPic != null)
		{
			Glide.with(this).load(bingPic).into(backgroundPic);
		} else
		{
			loadBackgroundPic();
		}
		
		if (weatherString != null)
		{
			Weather weather = Utility.handleWeatherResponse(weatherString);
			weatherId = weather.basic.weatherId;
			showWeatherInfo(weather);
		} else
		{
			weatherId = getIntent().getStringExtra("weather_id");
			weatherLayout.setVisibility(View.INVISIBLE);
			requestWeather(weatherId);
		}
		refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
		{
			@Override
			public void onRefresh()
			{
				requestWeather(weatherId);
			}
		});
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
	
	public void requestWeather(String weatherId)
	{
		loadBackgroundPic();
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
						refreshLayout.setRefreshing(false);
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
						weatherLinearLayout.setVisibility(View.INVISIBLE);
					}
				});
				try
				{
					Thread.sleep(300);
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
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
							refreshLayout.setRefreshing(false);
							showWeatherInfo(weather);
							Toast.makeText(WeatherActivity.this, "天气已更新！", Toast.LENGTH_SHORT).show();
							weatherLinearLayout.setVisibility(View.VISIBLE);
						} else
						{
							weatherLinearLayout.setVisibility(View.VISIBLE);
							Toast.makeText(WeatherActivity.this, "获取天气信息失败！", Toast.LENGTH_SHORT).show();
							refreshLayout.setRefreshing(false);
						}
					}
				});
			}
		});
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		resume = true;
		//实现背景图片移动
		new Thread()
		{
			@Override
			public void run()
			{
				while (resume)
				{
					try
					{
						sleep(60);
					} catch (InterruptedException e)
					{
						e.printStackTrace();
					}
					runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							if (x > 1.25)
								up = false;
							else if (x < 1)
								up = true;
							if (up)
							{
								backgroundPic.setScaleX(x *= 1.001);
								backgroundPic.setScaleY(y *= 1.001);
							} else
							{
								backgroundPic.setScaleX(x *= 0.999);
								backgroundPic.setScaleY(y *= 0.999);
							}
							Log.d("hello", x + "");
						}
					});
				}
			}
		}.start();
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
	
	@Override
	protected void onStop()
	{
		super.onStop();
		resume = false;
	}
}
