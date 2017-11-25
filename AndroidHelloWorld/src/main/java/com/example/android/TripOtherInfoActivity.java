package com.example.android;

import android.app.Activity;
import android.graphics.Color;
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

		// TODO: Uncomment when status works
		/*
		if(!UserInfo.getInstance().getUserStatus().tripCreationEnabled())
			Log.e("TripInfoActivity", "Critical bug: passenger cannot create trip!");
		*/
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_other_info);
		getActionBar().setDisplayHomeAsUpEnabled(true);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapInfoOther);
        mapFragment.getMapAsync(this);


		superTripBtn = (Button) findViewById(R.id.superTripBtn);
		superTripBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// TODO: Change this when status works
				try {
					TripActionsHandler tah = new TripActionsHandler();
					ConexionRest conn = new ConexionRest(tah);
					String tripId = PathInfo.getInstance().getTripId();
					String tripUrl = conn.getBaseUrl() + "/trips/" + tripId + "/action";
					Log.d("SelectTripActivity", "URL to POST trip: " + tripUrl);
					conn.generatePost("{ \"action\": \"accept\" }", tripUrl, null);
				}
				catch(Exception e){
					Log.e("ChoosePassengerActivity", "GET trips error: ", e);
				}
			}
		});
    }

	/**
	 * Overrided method for returning to parent {@link Activity}.
	 * @param item {@link MenuItem} clicked on {@link android.app.ActionBar}
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		Log.d("TripOtherInfoActivity", "Back button pressed on actionBar");
		// TODO: Change this when status works
		ActivityChanger.getInstance().gotoActivity(TripOtherInfoActivity.this, ChoosePassengerActivity.class);
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

		public TripActionsHandler(){

		}

		@Override
		public void executeUpdate(String servResponse) {
			Log.d("TripOtherInfoActivity", "Received response:" + servResponse);
		}
	}

}
