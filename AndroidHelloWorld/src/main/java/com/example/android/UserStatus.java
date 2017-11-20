package com.example.android;

/**
 * Created by ale on 11/18/17.
 */

public enum UserStatus {
	NO_STATE(0),
	P_IDLE(1),
	P_WAITING_CONFIRMATION(2),
	P_EXAMINING_DRIVER(3),
	P_WAITING_DRIVER(4),
	P_TRAVELLING(5),
	P_ARRIVED(6),
	D_ON_DUTY(11),
	D_WAITING_COFIRMATION(12),
	D_GOING_TO_PIKCUP(13),
	D_TRAVELLING(14);

	private int code;

	UserStatus(int code){
		this.code = code;
	}

	public int getCode(){
		return this.code;
	}

	public boolean tripCreationEnabled(){
		return (this.code == 1);
	}

	public boolean chatEnabled(){
		boolean isEnabled = (this.code == 13);
		isEnabled |= (this.code == 14);
		isEnabled |= (this.code == 4);
		isEnabled |= (this.code == 5);
		return isEnabled;
	}

	public boolean chooseTripEnabled(){
		return (this.code == 11);
	}
}
