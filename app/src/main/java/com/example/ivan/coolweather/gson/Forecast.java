package com.example.ivan.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Ivan on 2017/4/1.
 */

public class Forecast
{
	public String date;
	
	@SerializedName("tem")
	public Temperature temperature;
	
	@SerializedName("cond")
	public More more;
	
	private class Temperature
	{
		public String max;
		public String min;
	}
	
	private class More
	{
		@SerializedName("txt_d")
		public String inf;
	}
}
