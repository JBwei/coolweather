package com.example.ivan.coolweather;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ivan.coolweather.db.City;
import com.example.ivan.coolweather.db.County;
import com.example.ivan.coolweather.db.Province;
import com.example.ivan.coolweather.util.HttpUtil;
import com.example.ivan.coolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class chooseAreaFragment extends Fragment
{
	private static final int PROVINCE = 0;
	private static final int CITY = 1;
	private static final int COUNTY = 2;
	
	private int currentLevel;
	
	private Province selectedProvince;
	private City selectedCity;
	private List<Province> provinceList;
	private List<City> cityList;
	private List<County> countyList;
	
	private TextView title;
	private Button butBack;
	private ListView areasListView;
	private ArrayAdapter<String> areasListAdapter;
	private List<String> areasList = new ArrayList<>();
	
	private ProgressDialog progessDialog;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.choose_area, container, false);
		butBack = (Button) v.findViewById(R.id.button_back);
		title = (TextView) v.findViewById(R.id.title_text);
		areasListView = (ListView) v.findViewById(R.id.area_list);
		areasListAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, areasList);
		areasListView.setAdapter(areasListAdapter);
		return v;
	}
	
	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		queryProvince();
		areasListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				if (currentLevel == PROVINCE)
				{
					selectedProvince = provinceList.get(position);
					queryCity();
				} else if (currentLevel == CITY)
				{
					selectedCity = cityList.get(position);
					queryCounties();
				}
			}
		});
		butBack.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (currentLevel == COUNTY)
					queryCity();
				else if (currentLevel == CITY)
					queryProvince();
			}
		});
	}
	
	private void queryCounties()
	{
		title.setText(selectedCity.getCityName());
		butBack.setVisibility(View.VISIBLE);
		areasList.clear();
		areasListAdapter.notifyDataSetChanged();
		countyList = DataSupport.where("cityid = ?",
				String.valueOf(selectedCity.getId())).find(County.class);
		if (countyList.size() > 0)
		{
			for (County county : countyList)
				areasList.add(county.getCountyName());
			areasListAdapter.notifyDataSetChanged();
			areasListView.setSelection(0);
			currentLevel = COUNTY;
		} else
		{
			int provinceCode = selectedProvince.getProvinceCode();
			int cityCode = selectedCity.getCityCode();
			String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
			Log.d("hello", address);
			queryFromServer(address, COUNTY);
		}
	}
	
	private void queryCity()
	{
		title.setText(selectedProvince.getProvinceName());
		butBack.setVisibility(View.VISIBLE);
		areasList.clear();
		areasListAdapter.notifyDataSetChanged();
		cityList = DataSupport.where("provinceid = ?",
				String.valueOf(selectedProvince.getId())).find(City.class);
		if (cityList.size() > 0)
		{
			for (City city : cityList)
				areasList.add(city.getCityName());
			areasListAdapter.notifyDataSetChanged();
			areasListView.setSelection(0);
			currentLevel = CITY;
		} else
		{
			int provinceCode = selectedProvince.getProvinceCode();
			String address = "https://guolin.tech/api/china/" + provinceCode;
			Log.d("hello", address);
			queryFromServer(address, CITY);
		}
	}
	
	private void queryProvince()
	{
		butBack.setVisibility(View.GONE);
		title.setText("中国");
		provinceList = DataSupport.findAll(Province.class);
		if (provinceList.size() > 0)
		{
			areasList.clear();
			for (Province province : provinceList)
				areasList.add(province.getProvinceName());
			areasListAdapter.notifyDataSetChanged();
			areasListView.setSelection(0);
			currentLevel = PROVINCE;
		} else
		{
			String address = "http://guolin.tech/api/china";
			Log.d("hello", address);
			queryFromServer(address, PROVINCE);
		}
	}
	
	private void queryFromServer(String address, final int type)
	{
		showProgressDialog();
		HttpUtil.sendOkhttpRequest(address, new Callback()
		{
			@Override
			public void onFailure(Call call, IOException e)
			{
				getActivity().runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						closeProgressDialog();
						currentLevel = type;
						Toast.makeText(getContext(), "#@*%$^ 加 $# 载 <>? 失 #@! 败 #$", Toast.LENGTH_SHORT).show();
					}
				});
			}
			
			@Override
			public void onResponse(Call call, Response response) throws IOException
			{
				String text = response.body().string();
				boolean result = false;
				if (type == PROVINCE)
					result = Utility.handleProvinceResponse(text);
				else if (type == CITY)
					result = Utility.handleCityResponse(text, selectedProvince.getId());
				else if (type == COUNTY)
					result = Utility.handleCountyResponse(text, selectedCity.getId());
				if (result)
				{
					getActivity().runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							closeProgressDialog();
							if (type == PROVINCE)
								queryProvince();
							else if (type == CITY)
								queryCity();
							else if (type == COUNTY)
								queryCounties();
						}
					});
				}
			}
		});
	}
	
	private void showProgressDialog()
	{
		if (progessDialog == null)
		{
			progessDialog = new ProgressDialog(getActivity());
			progessDialog.setMessage("正在加载...");
			progessDialog.setCanceledOnTouchOutside(false);
		}
		progessDialog.show();
	}
	
	private void closeProgressDialog()
	{
		if (progessDialog != null)
			progessDialog.dismiss();
	}
}
