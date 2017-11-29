package com.example.android;

/**
 * Created by ale on 11/29/17.
 */

public class PaymethodInfo {
	public String method;
	public String cardNumber, cardType, cardCcvv;
	public String expMonth, expYear;

	public PaymethodInfo(){
		method = "";
		cardNumber = "";
		cardCcvv = "";
		cardType = "";
		expMonth = "";
		expYear = "";
	}

	public PaymethodInfo(String paymethod){
		method = paymethod;
		cardNumber = "";
		cardCcvv = "";
		cardType = "";
		expMonth = "";
		expYear = "";
	}
}
