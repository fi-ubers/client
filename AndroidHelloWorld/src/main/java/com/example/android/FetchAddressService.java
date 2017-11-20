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

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

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
	private boolean coordFromAddress;
	private Marker mMarker;
	private LatLng coords;

	public FetchAddressService(Context context, ListView destList, SelectTripActivity.MapHandler mHandler){
		this.destList = destList;
		this.mHandler = mHandler;
		this.context = context;
		coordFromAddress = true;
	}

	public FetchAddressService(Context context, Marker coords){
		this.context = context;
		this.mMarker = coords;
		coordFromAddress = false;
		this.coords = mMarker.getPosition();
	}

	@Override
	protected void onPostExecute(List<Address> aVoid){
		Iterator<Address> it = aVoid.iterator();
		if(coordFromAddress) {
			ArrayAdapter<String> mAdapter = (ArrayAdapter<String>) destList.getAdapter();
			while (it.hasNext()) {
				Address addr = it.next();
				Log.d("SelectTripActivity", "Place is: (" + addr.getLatitude() + ", " + addr.getLongitude() + ")");
				mAdapter.add(addr.getAddressLine(0));
			}
			mHandler.setFoundDestinations(aVoid);
		} else {
			while (it.hasNext()) {
				Address addr = it.next();
				String addrStr = addr.getAddressLine(0).split(",")[0];
				Log.d("SelectTripActivity", "Address is: " + addrStr);
				mMarker.setSnippet(addrStr);
			}
		}
	}

	@Override
	protected List<Address> doInBackground(String... voids) {
		List<Address> foundDestinations = null;
		try {
			//FetchAddressService addServ = new FetchAddressService();
			Geocoder gc = new Geocoder(this.context, Locale.getDefault());
			if(gc.isPresent())
				if(coordFromAddress)
					foundDestinations = gc.getFromLocationName(voids[0], 10);
				else
					foundDestinations = gc.getFromLocation(coords.latitude, coords.longitude, 1);
			Log.d("FetchAddressService", "Found: " + foundDestinations.size() + " places");

			// destList.refreshDrawableState();
		}
		catch (Exception e){
			Log.e("SelectTripActivity", "Exception: ", e);
		}
		return foundDestinations;
	}
}
