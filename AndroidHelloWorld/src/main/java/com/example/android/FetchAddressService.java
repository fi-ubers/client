package com.example.android;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Created by ale on 11/14/17.
 */

public class FetchAddressService extends AsyncTask<String, Void, List<Address>> {

	private Context context;
	private SelectTripActivity.MapHandler mHandler;
	private ListView destList;

	public FetchAddressService(Context context, ListView destList, SelectTripActivity.MapHandler mHandler){
		this.destList = destList;
		this.mHandler = mHandler;
		this.context = context;
	}

	@Override
	protected void onPostExecute(List<Address> aVoid){
		Iterator<Address> it = aVoid.iterator();
		ArrayAdapter<String> mAdapter = (ArrayAdapter<String>) destList.getAdapter();
		while(it.hasNext()) {
			Address addr = it.next();
			Log.d("SelectTripActivity", "Place is: (" + addr.getLatitude() + ", " + addr.getLongitude() + ")");
			mAdapter.add(addr.getAddressLine(0));
		}
		mHandler.setFoundDestinations(aVoid);
	}

	@Override
	protected List<Address> doInBackground(String... voids) {
		List<Address> foundDestinations = null;
		try {
			//FetchAddressService addServ = new FetchAddressService();
			Geocoder gc = new Geocoder(this.context, Locale.getDefault());
			if(gc.isPresent())
				foundDestinations = gc.getFromLocationName(voids[0], 10);
			Log.d("FetchAddressService", "Found: " + foundDestinations.size() + " places");

			// destList.refreshDrawableState();
		}
		catch (Exception e){
			Log.e("SelectTripActivity", "Exception: ", e);
		}
		return foundDestinations;
	}
}
