package com.example.ivan.coolweather.util;

import com.example.ivan.coolweather.db.City;
import com.example.ivan.coolweather.db.County;
import com.example.ivan.coolweather.db.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Ivan on 2017/3/31.
 */

public class Utility
{
	/**
	 * 解析并处理服务器返回的省级数据。
	 *
	 * @param response 服务器返回的JSON格式的String类型数据。
	 * @return 处理成功返回true， 否则返回false。
	 */
	public static boolean handleProvinceResponse(String response)
	{
		if (response != null && response.length() != 0)
		{
			try
			{
				JSONArray jProvinces = new JSONArray(response);
				for (int i = 0; i < jProvinces.length(); ++i)
				{
					JSONObject jProvince = jProvinces.getJSONObject(i);
					Province province = new Province();
					province.setProvinceCode(jProvince.getInt("id"));
					province.setProvinceName(jProvince.getString("name"));
					province.save();
				}
				return true;
			} catch (JSONException e)
			{
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * 解析并处理服务器返回的市级数据。
	 * @param response 服务器返回的JSON格式的String类型数据。
	 * @param provinceId 此市所属省的id。
	 * @return 处理成功返回true， 否则返回false。
	 */
	public static boolean handleCityResponse(String response, int provinceId)
	{
		if (response != null && response.length() != 0)
		{
			try
			{
				JSONArray jCities = new JSONArray(response);
				for (int i = 0; i < jCities.length(); ++i)
				{
					JSONObject jCity = jCities.getJSONObject(i);
					City city = new City();
					city.setCityCode(jCity.getInt("id"));
					city.setCityName(jCity.getString("name"));
					city.setProvinceId(provinceId);
					city.save();
				}
				return true;
			} catch (JSONException e)
			{
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * 解析并处理服务器返回的县级数据。
	 * @param response 服务器返回的JSON格式的String类型数据。
	 * @param provinceId 此县所属市的id。
	 * @return 处理成功返回true， 否则返回false。
	 */
	public static boolean handleCountyResponse(String response, int provinceId)
	{
		if (response != null && response.length() != 0)
		{
			try
			{
				JSONArray jCounties = new JSONArray(response);
				for (int i = 0; i < jCounties.length(); ++i)
				{
					JSONObject jCounty = jCounties.getJSONObject(i);
					County county = new County();
					county.setCountyCode(jCounty.getInt("id"));
					county.setCountyName(jCounty.getString("name"));
					county.setWeatherId(jCounty.getString("weather_id"));
					county.setCityId(provinceId);
					county.save();
				}
				return true;
			} catch (JSONException e)
			{
				e.printStackTrace();
			}
		}
		return false;
	}
}
