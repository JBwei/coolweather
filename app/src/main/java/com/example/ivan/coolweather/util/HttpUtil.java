package com.example.ivan.coolweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by Ivan on 2017/3/31.
 */

public class HttpUtil
{
	public static void sendOkhttpRequest(String address, okhttp3.Callback callback)
	{
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder().url(address).build();
		client.newCall(request).enqueue(callback);
	}
}
