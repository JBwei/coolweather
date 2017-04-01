package com.example.ivan.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Ivan on 2017/4/1.
 */

public class Basic
{
	@SerializedName("city")
	public String cityName;
	@SerializedName("id")
	public String weatherId;
	public Update update;
	public class Update
	{
		@SerializedName("loc")
		public String updateTime;
	}
}
