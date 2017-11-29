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
 * A place-to-coordinates translation class. It can retrieve an {@link Address} from a {@link LatLng},
 * or a group of {@link Address} from a location name. This class is intended to aid {@link SelectTripActivity}
 * in addresses translation.
 */
public class FetchAddressService extends AsyncTask<String, Void, List<Address>> {
	private Context context;
	private SelectTripActivity.MapHandler mHandler;
	private ListView destList;
	private boolean coordFromAddress;
	private Marker mMarker;
	private LatLng coords;

	/**
	 * Use this constructor for getting a list of addresses from a string, and updating
	 * those results on the received {@link ListView}.
	 * @param context Current {@link Context}
	 * @param destList {@link ListView} to put locations into
	 * @param mHandler {@link com.example.android.SelectTripActivity.MapHandler} of the map.
	 */
	public FetchAddressService(Context context, ListView destList, SelectTripActivity.MapHandler mHandler){
		this.destList = destList;
		this.mHandler = mHandler;
		this.context = context;
		coordFromAddress = true;
	}

	/**
	 * Use this constructor for getting the coordinates of a place from sting, and
	 * setting a {@link Marker} position there,
	 * @param context Current {@link Context}
	 * @param coords {@link Marker} to put location into
	 */
	public FetchAddressService(Context context, Marker coords){
		this.context = context;
		this.mMarker = coords;
		coordFromAddress = false;
		this.coords = mMarker.getPosition();
	}

	/**
	 * Updates the graphic elemetns after translation, accordingly to the
	 * constructor method called for this class.
	 * @param aVoid Addresses obtained as a result from translation
	 */
	@Override
	protected void onPostExecute(List<Address> aVoid){
		if((aVoid == null) || (aVoid.size() == 0))
			return;
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

	/**
	 * Overrided doInBackground method. Performs proper translation via {@link Geocoder}, and
	 * accordingly to the constructor method called for this class.
	 * @param voids Address String to translate
	 */
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
