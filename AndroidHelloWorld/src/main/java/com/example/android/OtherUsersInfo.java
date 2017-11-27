package com.example.android;

import com.google.android.gms.maps.model.LatLng;



public class OtherUsersInfo {
	private String userId;
	private String name;
	private LatLng location;
	private String picture;
	private String orig, dest;
	private double driverRate;

	public OtherUsersInfo(String userId, String name, String picture){
		this.name = name;
		this.picture = picture;
		this.userId = userId;
		orig = "";
		dest = "";
		driverRate = -1.0;
	}

	public void updateLocation(LatLng location){
		this.location = location;
	}

	public String getUserId(){
		return userId;
	}

	public String getName(){
		return name;
	}

	public String getPicture(){
		return picture;
	}

	public LatLng getLocation(){
		return location;
	}

	public void setOriginDestination(String origin, String destination){
		orig = origin;
		dest = destination;
	}

	public String getDest(){
		return dest;
	}

	public String getOrig(){
		return orig;
	}

	public double getDriverRate(){
		return driverRate;
	}

	public void setDriverRate(double rate){
		driverRate = rate;
	}
}
