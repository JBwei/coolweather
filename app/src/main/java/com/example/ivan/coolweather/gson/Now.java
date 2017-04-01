package com.example.ivan.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Ivan on 2017/4/1.
 */

public class Now
{
	@SerializedName("tem")
	public String temperature;
	@SerializedName("cond")
	public More more;
	public class More
	{
		@SerializedName("txt")
		public String inf;
	}
}
