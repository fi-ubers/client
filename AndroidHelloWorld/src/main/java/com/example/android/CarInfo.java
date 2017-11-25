package com.example.android;

/**
 * Created by ale on 11/10/17.
 */
public class CarInfo{
	private String model;
	private String number;
	private int id;

	public CarInfo(){

	}

	public CarInfo(String model, String number, int id){
		this.model = model;
		this.number = number;
		this.id = id;
	}

	public String getModel(){
		return this.model;
	}

	public String getNumber(){
		return this.number;
	}

	public int getId(){
		return this.id;
	}
}