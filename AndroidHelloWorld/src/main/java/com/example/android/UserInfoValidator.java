package com.example.android;

import android.text.TextUtils;
import android.widget.TextView;

/**
 * Simple class with many validation functions for {@link UserInfo}
 * fields such as name, email, country, etc.
 */
public class UserInfoValidator {

	public boolean isUserIdValid(String userId) {
		boolean idOk = userId.length() > 1;
		// Next line checks that userId has at least one letter
		idOk &= (userId.matches(".*[a-z].*") || userId.matches(".*[A-Z].*"));
		idOk &= (!userId.contains("#")); // User id must not contain #
		idOk &= (!userId.contains("/")); // User id must not contain /
		idOk &= (!userId.contains("!")); // User id must not contain !
		idOk &= (!userId.contains("?")); // User id must not contain ?
		idOk &= (!userId.contains("@")); // User id must not contain @
		idOk &= (!userId.contains(" ")); // User id must not contain spaces
		return idOk;
	}

	public boolean isUserEmailValid(String email) {
		boolean emailOk = email.contains("@");
		emailOk &= email.contains(".");

		int aPosition = email.indexOf("@");
		emailOk &= (aPosition >= 2);

		int dotPosition = email.indexOf(".");
		emailOk &= (dotPosition >= 3);

		emailOk &= ((dotPosition - aPosition)  >= 2);

		return emailOk;
	}

	public boolean isPasswordValid(String password) {
		return password.length() > 3;
	}

	public boolean isNameValid(String name) {
		boolean idOk = name.length() > 1;
		// Next line checks that userId has at least one letter
		idOk &= (name.matches(".*[a-z].*") || name.matches(".*[A-Z].*"));
		idOk &= (!name.contains("#")); // User id must not contain #
		idOk &= (!name.contains("/")); // User id must not contain /
		idOk &= (!name.contains("!")); // User id must not contain !
		idOk &= (!name.contains("?")); // User id must not contain ?
		idOk &= (!name.contains("@")); // User id must not contain @
		return idOk;
	}

	public boolean isBirthdateValid(String birthdate) {
		if(birthdate.length() != 10) return false;
		// Next line checks that userId has at least one letter
		boolean idOk = (!(birthdate.matches(".*[a-z].*") || birthdate.matches(".*[A-Z].*")));
		idOk &= (birthdate.charAt(2) == '/');
		idOk &= (birthdate.charAt(5) == '/');
		return idOk;
	}

	public boolean checkFieldShowError(TextView tvField, String fieldType, String errorMsg){
		tvField.setError(null);
		String tvValue = tvField.getText().toString();
		if (TextUtils.isEmpty(tvValue)){
			tvField.requestFocus();
			tvField.setError(errorMsg);
			return false;
		}
		boolean ret = true;
		if(fieldType.toLowerCase().equals("name") && (!isNameValid(tvValue)))
			ret = false;
		if(fieldType.toLowerCase().equals("email") && (!isUserEmailValid(tvValue)))
			ret = false;
		if(fieldType.toLowerCase().equals("birthdate") && (!isBirthdateValid(tvValue)))
			ret = false;
		if(fieldType.toLowerCase().equals("password") && (!isPasswordValid(tvValue)))
			ret = false;
		if(fieldType.toLowerCase().equals("userid") && (!isUserIdValid(tvValue)))
			ret = false;

		if(!ret){
			tvField.setError(errorMsg);
			tvField.requestFocus();
		}
		return ret;
	}
}
