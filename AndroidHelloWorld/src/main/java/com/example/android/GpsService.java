package com.example.android;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;


public class GpsService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
	private LocationRequest mLocationRequest;
	private GoogleApiClient mGoogleApiClient;
	private static final String LOGSERVICE = "GpsService";

	@Override
	public void onCreate() {
		super.onCreate();
		buildGoogleApiClient();
		Log.i(LOGSERVICE, "onCreate");

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(LOGSERVICE, "onStartCommand");

		if (!mGoogleApiClient.isConnected())
			mGoogleApiClient.connect();
		return START_STICKY;
	}


	@Override
	public void onConnected(Bundle bundle) {
		Log.i(LOGSERVICE, "onConnected" + bundle);

		Location l = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
		if (l != null) {
			Log.i(LOGSERVICE, "Coordinates are: (" + l.getLatitude() + ", " + l.getLongitude() + ")");

		}

		startLocationUpdate();
	}

	@Override
	public void onConnectionSuspended(int i) {
		Log.i(LOGSERVICE, "onConnectionSuspended " + i);

	}

	@Override
	public void onLocationChanged(Location location) {
		if(UserInfo.getInstance().getIntegerId() < 0)	return;
		LatLng l = new LatLng(location.getLatitude(), location.getLongitude());
		//Log.i(LOGSERVICE, "Coordinates are: (" + l.latitude + ", " + l.longitude + ")");
		try {
			ConexionRest conn = new ConexionRest(null);
			String coordUrl = conn.getBaseUrl() + "/users/" + UserInfo.getInstance().getIntegerId() + "/location";
			//Log.d("GpsService", "URL to update location: " + coordUrl);
			Jsonator jnator = new Jsonator();
			String toSendJson = jnator.writeLocationCoords(l);
			conn.generatePut(toSendJson, coordUrl, null);
		} catch (Exception e) {
			Log.e("GpsService", "updating location error: ", e);
		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(LOGSERVICE, "onDestroy - Estou sendo destruido ");

	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Log.i(LOGSERVICE, "onConnectionFailed ");

	}

	private void initLocationRequest() {
		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(5000);
		mLocationRequest.setFastestInterval(2000);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

	}

	private void startLocationUpdate() {
		initLocationRequest();

	/*	if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATIO) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATIO) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return;
		}*/
		LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
	}

	private void stopLocationUpdate() {
		LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

	}

	protected synchronized void buildGoogleApiClient() {
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addOnConnectionFailedListener(this)
				.addConnectionCallbacks(this)
				.addApi(LocationServices.API)
				.build();
	}

}

