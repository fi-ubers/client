package com.example.Principal;

import android.util.Log;
import org.json.*;


/**
 * A class for reading and writing Json strings. Used by the
 * {@link ConexionRest} for REST messages.
 */

public class Jsonator {
    /**
     * Generates a Json string request for creating a new user.
     * @param userId The new user's Id number you wish to assign
     * @param userName The new user's name
     */
    public String writeUser(Integer userId, String userName){
        JSONObject objJson = new JSONObject();

        try {
            JSONObject inner = new JSONObject();
            inner.put("id", userId);
            inner.put("name", userName);

            objJson.put("user", inner);
        }
        catch (Exception e) {
            System.out.println(e);
            Log.e("Fiuber Jsonator", "exception", e);
        }

        return objJson.toString();
    }

    /**
     * Generates a Json string request for user log in
     * @param userId The user's name
     * @param userPass The user's password (if manually logged in)
     * @param userFbToken The user's Facebook token (if logged in with Fb)
     */
    public String writeUserLoginCredentials(String userId, String userPass, String userFbToken){
        JSONObject objJson = new JSONObject();

        try {
            objJson.put("username", userId);
            objJson.put("password", userPass);

            JSONObject innerFb = new JSONObject();
            innerFb.put("userId", userId);
            innerFb.put("fbtoken", userFbToken);

            objJson.put("fbAuth", innerFb);
        }
        catch (Exception e) {
            System.out.println(e);
            Log.e("Fiuber Jsonator", "exception", e);
        }

        return objJson.toString();
    }

    public Boolean userLoggedInIsOk(String jsonResponse){
        try {
            JSONObject objJson = new JSONObject(jsonResponse);
            Log.i("Fiuber Jsonator", "Received response:"+jsonResponse);
            if(objJson.getInt("code") == 200)
                return true;
            return false;
        }
        catch (Exception e) {
            System.out.println(e);
            Log.e("Fiuber Jsonator", "exception", e);
            return false;
        }
    }

    public void readUserLoggedInInfo(String jsonResponse){
        if(!this.userLoggedInIsOk(jsonResponse)) return;

        try {
            JSONObject objJson = new JSONObject(jsonResponse);
            Log.i("Fiuber Jsonator", "Received response:"+jsonResponse);
            String appTkn = objJson.getString("token");
			JSONObject uiJson = new JSONObject(objJson.getString("user"));

			String mail = uiJson.getString("email");
			String fName = uiJson.getString("name");
			String lName = uiJson.getString("surname");
			String country = uiJson.getString("country");
			String bth = uiJson.getString("birthdate");
			String userId = uiJson.getString("username");

			UserInfo ui = UserInfo.getInstance();
			ui.initializeUserInfo(userId, mail, fName, lName, country, bth, "", "", appTkn);
        }
        catch (Exception e) {
            System.out.println(e);
            Log.e("Fiuber Jsonator", "exception", e);
        }
    }

    public String writeUserSignUpInfo(){
        UserInfo ui = UserInfo.getInstance();
        if(!ui.wasInitialized())
            return "";

        JSONObject objJson = new JSONObject();

        try {
            objJson.put("type", "passenger");
            objJson.put("username", ui.getUserId());
            objJson.put("password", ui.getPassword());

            JSONObject innerFb = new JSONObject();
            innerFb.put("userId", ui.getUserId());
            innerFb.put("fbtoken", ui.getFbToken());

            objJson.put("fbAuth", innerFb);
            objJson.put("firstname", ui.getFirstName());
            objJson.put("lastname", ui.getLastName());
            objJson.put("country", ui.getCountry());
            objJson.put("email", ui.getEmail());
            objJson.put("birthdate", ui.getBirthdate());

            JSONArray imagesArr = new JSONArray();
            imagesArr.put("");

            objJson.put("images", imagesArr);

        }
        catch (Exception e) {
            System.out.println(e);
            Log.e("Fiuber Jsonator", "exception", e);
        }

        return objJson.toString();
    }

    public Boolean userSignedUpIsOk(String jsonResponse){
        try {
            JSONObject objJson = new JSONObject(jsonResponse);
            Log.i("Fiuber Jsonator", "Received response:"+jsonResponse);
            if(objJson.getInt("code") == 201)
                return true;
            return false;
        }
        catch (Exception e) {
            System.out.println(e);
            Log.e("Fiuber Jsonator", "exception", e);
            return false;
        }
    }

}
