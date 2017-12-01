package com.example.android;

import android.location.Address;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by ale on 11/19/17.
 */

public class PathInfo {
	private static PathInfo instance = null;
	private List<LatLng> destinations;
	private String origAddress, destAddress;
	private double distance, cost, duration;
	private String tripJson;
	private String tripId;

	/**
	 * NOT FOR USE! Exists only to prevent instantiation.
	 */
	protected PathInfo() {
		// Exists only to prevent instantiation.
		origAddress = "";
		destAddress = "";
		tripJson = "";
		tripId = "";
		distance = -1.0;
		destinations = null;
		cost = -1.0;
		destinations = null;
	}

	/**
	 * Get the only singleton instance of the class.
	 * @return  Current singleton instance
	 */
	public static PathInfo getInstance() {
		if(instance == null)
			instance = new PathInfo();
		return instance;
	}

	public void setPath(List<LatLng> path){
		destinations = path;
	}

	public List<LatLng> getPath(){
		return this.destinations;
	}

	public void setAddresses(String origAddr, String destAddr){
		origAddress = origAddr;
		destAddress = destAddr;
	}

	public String getOrigAddress(){
		return origAddress;
	}

	public String getDestAddress(){
		return destAddress;
	}

	public void setDistance(double dist){
		this.distance = dist;
	}

	public double getDistance(){
		return this.distance;
	}

	public double getCost(){
		return cost;
	}

	public void setCost(double cost){
		this.cost = cost;
	}

	public double getDuration(){
		return duration;
	}

	public void setDuration(double duration){
		this.duration = duration;
	}

	public String getTripJson(){
		return tripJson;
	}

	public void setTripJson(String tripJson){
		this.tripJson = tripJson;
	}

	public String getTripId(){
		return tripId;
	}

	public void setTripId(String tripId){
		this.tripId = tripId;
	}

	public void selfDestruct(){
		origAddress = "";
		destAddress = "";
		tripJson = "";
		tripId = "";
		distance = -1.0;
		destinations = null;
		cost = -1.0;
		destinations = null;
	}
}
