package com.example.android;

import com.google.android.gms.maps.model.LatLng;



public class OtherUsersInfo {
	private String userId;
	private String name;
	private LatLng location;
	private String picture;

	public OtherUsersInfo(String userId, String name, String picture){
		this.name = name;
		this.picture = picture;
		this.userId = userId;
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
}
