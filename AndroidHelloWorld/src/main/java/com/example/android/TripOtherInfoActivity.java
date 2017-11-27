package com.example.android;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Iterator;

public class TripOtherInfoActivity extends FragmentActivity implements OnMapReadyCallback {
    // Curious and useful fact: 0.1 degrees in latitude/longitude are equivalent
    // to 11 km of the Earth surface
    private GoogleMap mMap;
    private Button superTripBtn;
    Marker origMarker, destMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


		if(!UserInfo.getInstance().getUserStatus().tripOtherInfoEnabled())
			Log.e("TripInfoActivity", "Critical bug: user shouldnt be here!");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_other_info);
		getActionBar().setDisplayHomeAsUpEnabled(true);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapInfoOther);
        mapFragment.getMapAsync(this);


		superTripBtn = (Button) findViewById(R.id.superTripBtn);
		UserStatus st = UserInfo.getInstance().getUserStatus();
		if(st == UserStatus.D_ON_DUTY) {
			superTripBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					try {
						TripActionsHandler tah = new TripActionsHandler(TripActionsHandler.D_TAKE_TRIP);
						ConexionRest conn = new ConexionRest(tah);
						String tripId = PathInfo.getInstance().getTripId();
						String tripUrl = conn.getBaseUrl() + "/trips/" + tripId + "/action";
						Log.d("TripOtherInfoActivity", "URL to POST trip: " + tripUrl);
						conn.generatePost("{ \"action\": \"accept\" }", tripUrl, null);
					} catch (Exception e) {
						Log.e("TripOtherInfoActivity", "GET trips error: ", e);
					}
				}
			});
		} else if((st == UserStatus.P_WAITING_CONFIRMATION) || (st == UserStatus.P_WAITING_DRIVER)){
			superTripBtn.setText("CANCEL TRIP");
			superTripBtn.setBackgroundResource(R.drawable.urgentborder);
			superTripBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					// TODO: Cancel trip and restore user status
					try {
						TripActionsHandler tah = new TripActionsHandler(TripActionsHandler.P_CANCEL_TRIP);
						ConexionRest conn = new ConexionRest(tah);
						String tripId = PathInfo.getInstance().getTripId();
						String tripUrl = conn.getBaseUrl() + "/trips/" + tripId + "/action";
						Log.d("TripOtherInfoActivity", "URL to cancel trip: " + tripUrl);
						conn.generatePost("{ \"action\": \"cancel\" }", tripUrl, null);
					} catch (Exception e) {
						Log.e("TripOtherInfoActivity", "Cancel trip error: ", e);
					}

				}
			});
		} else if(st == UserStatus.D_GOING_TO_PIKCUP) {
			superTripBtn.setEnabled(false);
			superTripBtn.setVisibility(View.INVISIBLE);
		} else if(st == UserStatus.D_WAITING_COFIRMATION){
			superTripBtn.setEnabled(false);
			superTripBtn.setBackgroundColor(Color.TRANSPARENT);
			superTripBtn.setTextColor(Color.parseColor("#050f9f"));
			superTripBtn.setText("Waiting passenger confirmation");
		}
    }

	/**
	 * Overrided method for returning to parent {@link Activity}.
	 * @param item {@link MenuItem} clicked on {@link android.app.ActionBar}
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		Log.d("TripOtherInfoActivity", "Back button pressed on actionBar");
		UserStatus st = UserInfo.getInstance().getUserStatus();
		if(st == UserStatus.D_ON_DUTY)
			ActivityChanger.getInstance().gotoActivity(TripOtherInfoActivity.this, ChoosePassengerActivity.class);
		else
			ActivityChanger.getInstance().gotoActivity(TripOtherInfoActivity.this, MainActivity.class);
		finish();
		return true;

	}

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        UserInfo ui = UserInfo.getInstance();

		PathInfo pi = PathInfo.getInstance();
		ArrayList<LatLng> pathPoints = new ArrayList<>(pi.getPath());
		drawPath(pathPoints);

		// Creates a border to see the whole path
		Iterator<LatLng> it = pathPoints.iterator();
		double minLat = 400, minLong = 400, maxLat = -400, maxLong = -400;
		while(it.hasNext()) {
			LatLng thisPoint = it.next();
			if(thisPoint.latitude > maxLat)
				maxLat = thisPoint.latitude;
			if(thisPoint.longitude > maxLong)
				maxLong = thisPoint.longitude;
			if(thisPoint.latitude < minLat)
				minLat = thisPoint.latitude;
			if(thisPoint.longitude < minLong)
				minLong = thisPoint.longitude;
		}

		Log.d("TripOtherInfoActivity", "Borders are: ("+minLat+","+minLong+"), ("+maxLat+","+maxLong+")");

		final LatLngBounds border = new LatLngBounds(new LatLng(minLat - 0.006, minLong - 0.006),
				new LatLng(maxLat + 0.0048, maxLong + 0.0048));

		OtherUsersInfo oui = UserInfo.getInstance().getOtherUser();

		OtherInfoFragment fr = (OtherInfoFragment) getFragmentManager().findFragmentById(R.id.fragmentOther);
		fr.updateFragmentElements(oui, false);

		FusedLocationProviderClient flpc = LocationServices.getFusedLocationProviderClient(this);
		flpc.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
			@Override
			public void onSuccess(Location location) {
				mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(border, 0));
			}
		});

		flpc.getLastLocation();
    }


    public void addTextToMarker(Marker mMarker){
        if(mMarker == null) return;
        FetchAddressService addServ= new FetchAddressService(TripOtherInfoActivity.this, mMarker);
        addServ.execute((String) null);
    }

    public void drawPath(ArrayList<LatLng> pathPoints){
        PolylineOptions pathOptions = new PolylineOptions();
		Polyline path = mMap.addPolyline(pathOptions);

        path.setPoints(pathPoints);
        path.setColor(Color.BLUE);
        path.setWidth(12);

		LatLng dest = pathPoints.get(pathPoints.size() - 1);
		LatLng orig = pathPoints.get(0);
		mMap.addMarker(new MarkerOptions().position(dest).draggable(false).title("Destination")
				.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))).
				setSnippet(PathInfo.getInstance().getDestAddress());
		mMap.addMarker(new MarkerOptions().position(orig).draggable(false).title("Pickup location")).
				setSnippet(PathInfo.getInstance().getOrigAddress());


    }

// ----------------------------------------------------------------------------------------------------

	public class TripActionsHandler implements RestUpdate{
		public static final int D_TAKE_TRIP = 1;
		public static final int P_CANCEL_TRIP = 2;

		private int tripMode;

		public TripActionsHandler(int tMode){
			tripMode = tMode;
		}

		@Override
		public void executeUpdate(String servResponse) {
			Log.d("TripOtherInfoActivity", "Received response:" + servResponse);
			if(tripMode == D_TAKE_TRIP) {
				UserInfo.getInstance().setUserStatus(UserStatus.D_WAITING_COFIRMATION);
				ActivityChanger.getInstance().gotoActivity(TripOtherInfoActivity.this, MainActivity.class);
				Toast.makeText(getApplicationContext(), "Trip taken! Wait for passenger confirmation.", Toast.LENGTH_SHORT).show();
				finish();
			} else if(tripMode == P_CANCEL_TRIP){
				UserInfo.getInstance().setUserStatus(UserStatus.P_IDLE);
				ActivityChanger.getInstance().gotoActivity(TripOtherInfoActivity.this, MainActivity.class);
				Toast.makeText(getApplicationContext(), "You have canceled your trip.", Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

}
