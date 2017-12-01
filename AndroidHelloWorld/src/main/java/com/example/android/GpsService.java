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

/**
 * A service created to run in background, which only purpose is to constantly
 * update location to app-server.
 */
public class GpsService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
	private LocationRequest mLocationRequest;
	private GoogleApiClient mGoogleApiClient;
	private static final String LOGSERVICE = "GpsService";

	/**
	 * Overrided onCreate method for this class. Builds the {@link GoogleApiClient} inside.
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		buildGoogleApiClient();
		Log.i(LOGSERVICE, "onCreate");

	}

	/**
	 * Overrided onStartCommand. Simply starts the service and connects
	 * as a google API client.
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(LOGSERVICE, "onStartCommand");

		if (!mGoogleApiClient.isConnected())
			mGoogleApiClient.connect();
		return START_STICKY;
	}

	/**
	 * Overrided onConnected. Makes the first return of coordinates.
	 */
	@Override
	public void onConnected(Bundle bundle) {
		Log.i(LOGSERVICE, "onConnected" + bundle);

		Location l = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
		if (l != null) {
			Log.i(LOGSERVICE, "Coordinates are: (" + l.getLatitude() + ", " + l.getLongitude() + ")");

		}

		startLocationUpdate();
	}

	/**
	 * Overrided onConnectionSuspended. Does nothing.
	 */
	@Override
	public void onConnectionSuspended(int i) {
		Log.i(LOGSERVICE, "onConnectionSuspended " + i);

	}

	/**
	 * Overrided onLocationChanged. POSTs the retrieved user location
	 * to the app-server.
	 * @param location Last user retrieved location
	 */
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

	/**
	 * Overrided onDestroy. Does nothing.
	 * */
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(LOGSERVICE, "onDestroy - Estou sendo destruido ");

	}

	/**
	 * Overrided onBind. Does nothing.
	 */
	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * Overrided onConnetionFailed. Simply logs an error.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Log.e(LOGSERVICE, "onConnectionFailed ");

	}

	/**
	 * Starts the location request. This function sets the time interval between
	 * every get location request.
	 */
	private void initLocationRequest() {
		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(12000);
		mLocationRequest.setFastestInterval(7000);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

	}

	/**
	 * As its name suggests, starts the location update.
	 */
	private void startLocationUpdate() {
		initLocationRequest();

		LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
	}

	/**
	 * Stop the location updates.
	 */
	private void stopLocationUpdate() {
		LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

	}

	/**
	 * Creates the {@link GoogleApiClient} needed to retrieve locations.
	 */
	protected synchronized void buildGoogleApiClient() {
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addOnConnectionFailedListener(this)
				.addConnectionCallbacks(this)
				.addApi(LocationServices.API)
				.build();
	}

}

