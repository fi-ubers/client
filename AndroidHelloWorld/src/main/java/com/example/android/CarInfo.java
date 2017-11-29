package com.example.android;

/**
 * A class modeling a driver car.
 */
public class CarInfo{
	private String model;
	private String number;
	private int id;

	/**
	 * Class default constructor.
	 */
	public CarInfo(){

	}

	/**
	 * Class constructor with predefined parameters for the car.
	 * @param model Model of the car
	 * @param number Number of the car's plate
	 * @param id This car's id given by the app-server
	 */
	public CarInfo(String model, String number, int id){
		this.model = model;
		this.number = number;
		this.id = id;
	}

	/**
	 * Retrieves this car model.
	 */
	public String getModel(){
		return this.model;
	}

	/**
	 * Retrieves this car number.
	 */
	public String getNumber(){
		return this.number;
	}

	/**
	 * Retrieves this car id.
	 */
	public int getId(){
		return this.id;
	}
}