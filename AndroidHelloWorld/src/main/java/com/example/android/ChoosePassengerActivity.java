package com.example.android;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Am {@link Activity} with pending trips for drivers to accept them.
 */
public class ChoosePassengerActivity extends Activity {
	private ArrayList<ProtoTrip> trips;
	private ListView listView;
	private int tripAmount;
	private ArrayList<LatLng> selectedTrip;

	/**
	 * Activity onCreate method.
	 */
	public void onCreate(Bundle savedInstanceState) {
		tripAmount = 4;
		selectedTrip = null;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose_passenger);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		listView = (ListView) findViewById(R.id.listTrips);
		int[] colors = {0, 0xFFFFFFFF, 0};
		listView.setDivider(new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, colors));
		listView.setDividerHeight(2);

		List<String> initialList = new ArrayList<>();
		final ArrayAdapter mAdapter = new ArrayAdapter(ChoosePassengerActivity.this, R.layout.destinations_item, initialList);
		listView.setAdapter(mAdapter);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ProtoTrip proto = trips.get(position);
				String tripId = proto.getTripId();
				try {
					TripsHandler th = new TripsHandler(TripsHandler.GET_TRIP_DATA);
					ConexionRest conn = new ConexionRest(th);
					String tripUrl = conn.getBaseUrl() + "/trips/" + tripId;
					Log.d("SelectTripActivity", "URL to GET trip: " + tripUrl);
					conn.generateGet(tripUrl, null);
				}
				catch(Exception e){
					Log.e("ChoosePassengerActivity", "GET trips error: ", e);
				}
			}
		});

		Button moreTripsBtn = (Button) findViewById(R.id.moreTripsBtn);
		moreTripsBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				tripAmount += 8;
				try {
					TripsHandler th = new TripsHandler(TripsHandler.GET_TRIPS_LIST);
					ConexionRest conn = new ConexionRest(th);
					String tripUrl = conn.getBaseUrl() + "/trips?limit=" + tripAmount + "&filter=proposed&sort=near";
					Log.d("ChoosePassengerActivity", "URL to POST trip: " + tripUrl);
					conn.generateGet(tripUrl, null);
				}
				catch(Exception e){
					Log.e("ChoosePassengerActivity", "GET trips error: ", e);
				}
			}
		});

		try {
			TripsHandler th = new TripsHandler(TripsHandler.GET_TRIPS_LIST);
			ConexionRest conn = new ConexionRest(th);
			String tripUrl = conn.getBaseUrl() + "/trips?limit=" + tripAmount + "&filter=proposed&sort=near";
			Log.d("ChoosePassengerActivity", "URL to POST trip: " + tripUrl);
			conn.generateGet(tripUrl, null);
		}
		catch(Exception e){
			Log.e("ChoosePassengerActivity", "GET trips error: ", e);
		}

	}

	/**
	 * Overrided method for returning to parent {@link Activity}.
	 * @param item {@link MenuItem} clicked on {@link android.app.ActionBar}
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		Log.d("ChoosePassengerActivity", "Back button pressed on actionBar");
		ActivityChanger.getInstance().gotoActivity(ChoosePassengerActivity.this, SelectTripActivity.class);
		finish();
		return true;

	}


// ------------------------------------------------------------------------------------------------
	/**
	 * A class for handling app-server trips.
	 */
	public class TripsHandler implements RestUpdate{

		public static final int GET_TRIPS_LIST = 1;
		public static final int GET_TRIP_DATA = 2;
		public static final int GET_PASSENGER_DATA = 3;

		private int dataMode;

		/**
		 * Class constructor. The mode represents the action to do.
		 * @param mode The action for this {@link TripsHandler} to perform. Either
		 *             GET_TRIPS_LIST, GET_TRIP_DATA or GET_PASSENGER_DATA.
		 */
		public TripsHandler(int mode){
			this.dataMode = mode;
		}

		/**
		 * Gets a list of trips for the driver to accept from the app-server response, a
		 * nd updates the {{@link ListView}} showing them. This function is only called if
		 * this {@link TripsHandler} mode is GET_TRIPS_LIST.
		 * @param servResponse Response from app-server
		 */
		private void getTripsList(String servResponse){
			int itemChecked = listView.getCheckedItemPosition();
			listView.clearChoices();
			Log.d("ChoosePassengerActivity", "GET trips response:" + servResponse);
			Jsonator jnator = new Jsonator();
			trips = jnator.readTripsProposed(servResponse);
			ArrayAdapter<String> mAdapter = (ArrayAdapter<String>) listView.getAdapter();
			mAdapter.clear();
			Iterator<ProtoTrip> it = trips.iterator();
			while(it.hasNext()){
				ProtoTrip nexTrip = it.next();
				String addrO = nexTrip.getOriginName().split(",")[0];
				String addrD = nexTrip.getDestinationName().split(",")[0];
				String fromTo =  "From: " + addrO + "\nTo: " + addrD;
				mAdapter.add(fromTo);
			}
			listView.setItemChecked(itemChecked, true);
			return;
		}

		/**
		 * Gets the passenger data from the app-server response, and updates the {@link OtherInfoFragment}
		 * fields with it. This function is only called if this {@link TripsHandler} mode is GET_PASSENGER_DATA.
		 * @param servResponse Response from app-server
		 */
		private void getPassengerData(String servResponse){
			Log.d("ChoosePassengerActivity", "GET passenger response:" + servResponse);
			Jsonator jnator = new Jsonator();
			OtherUsersInfo oui = jnator.readOtherUserInfo(servResponse);
			ProtoTrip trip = trips.get(listView.getCheckedItemPosition());
			oui.setOriginDestination(trip.getOriginName(), trip.getDestinationName());
			UserInfo.getInstance().setOtherUser(oui);
			OtherInfoFragment fr = (OtherInfoFragment) getFragmentManager().findFragmentById(R.id.fragmentTripInfo);
			// Start the fragment with passenger info and delete the textView that
			// asks to select a trip
			fr.updateFragmentElements(oui, true);
			ViewGroup.LayoutParams params = fr.getView().getLayoutParams();
			params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
			fr.getView().setLayoutParams(params);
			TextView entry= (TextView) findViewById(R.id.textViewHeader);
			if(entry != null)
				((ViewManager)entry.getParent()).removeView(entry);

			Button otherBtnConfirm = (Button) fr.getView().findViewById(R.id.otherBtnConfirm);
			otherBtnConfirm.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					PathInfo pi = PathInfo.getInstance();
					pi.setPath(selectedTrip);
					ProtoTrip trip = trips.get(listView.getCheckedItemPosition());
					pi.setAddresses(trip.getOriginName(), trip.getDestinationName());
					pi.setDistance(trip.getDistance());
					pi.setDuration(trip.getDuration());
					pi.setCost(trip.getCost());
					pi.setTripJson(trip.getTripJson());
					pi.setTripId(trip.getTripId());
					ActivityChanger.getInstance().gotoActivity(ChoosePassengerActivity.this, TripOtherInfoActivity.class);
				}
			});
		}

		/**
		 * Gets trips data from a certain trip. This function is only called if this
		 * {@link TripsHandler} mode is GET_TRIP_DATA.
		 * @param servResponse Response from app-server
		 */
		private void getTripsData(String servResponse) {
			Log.d("ChoosePassengerActivity", "GET trip Id response:" + servResponse);
			Jsonator jnator = new Jsonator();
			selectedTrip = jnator.readDirectionsPath(servResponse, true);
			// Get pasenger data
			String passengerId = UserInfo.getInstance().getOtherUser().getUserId();
			this.dataMode = GET_PASSENGER_DATA;
			try {
				ConexionRest conn = new ConexionRest(this);
				String passUrl = conn.getBaseUrl() + "/users/" + passengerId;
				Log.d("SelectTripActivity", "URL to GET passenger data: " + passUrl);
				conn.generateGet(passUrl, null);
			}
			catch(Exception e){
				Log.e("ChoosePassengerActivity", "GET passenger error: ", e);
			}

		}

		/**
		 * Overrided {@link RestUpdate} method to be performed after the request
		 * to the app-server. Simply dispatches the response to the corresponding
		 * method, based on this {@link TripsHandler} mode value.
		 * @param servResponse Response from app-server.
		 */
		@Override
		public void executeUpdate(String servResponse) {
			switch(dataMode){
				case GET_TRIPS_LIST:
					getTripsList(servResponse);
					break;
				case GET_TRIP_DATA:
					getTripsData(servResponse);
					break;
				case GET_PASSENGER_DATA:
					getPassengerData(servResponse);
					break;
				default:
					break;
			}
		}
	}
}