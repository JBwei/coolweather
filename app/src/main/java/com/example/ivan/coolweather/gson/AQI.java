package com.example.ivan.coolweather.gson;

/**
 * Created by Ivan on 2017/4/1.
 */

public class AQI
{
	public AQICity city;
	
	public class AQICity
	{
		public String pm25;
		public String aqi;
	}
}