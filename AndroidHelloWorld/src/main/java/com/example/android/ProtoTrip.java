package com.example.android;



public class ProtoTrip {
	private String originName, destinationName;
	private String id;
	private double distance, cost, duration;
	private String tripJson;

	public ProtoTrip(String origin, String destination, String tripId){
		this.originName = origin;
		this.destinationName = destination;
		id = tripId;
	}

	public String getOriginName(){
		return this.originName;
	}

	public String getDestinationName(){
		return this.destinationName;
	}

	public String getTripId(){
		return id;
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

}
