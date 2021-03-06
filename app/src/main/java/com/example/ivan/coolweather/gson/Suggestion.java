package com.example.ivan.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Ivan on 2017/4/1.
 */

public class Suggestion
{
	@SerializedName("comf")
	public Comfort comfort;
	
	@SerializedName("cw")
	public CarWash carWash;
	
	public Sport sport;
	
	public class Comfort
	{
		@SerializedName("txt")
		public String inf;
	}
	public class CarWash
	{
		@SerializedName("txt")
		public String inf;
	}
	public class Sport
	{
		@SerializedName("txt")
		public String inf;
	}
}
