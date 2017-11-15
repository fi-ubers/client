package com.example.android;

import android.util.Log;
import org.json.*;

import java.util.ArrayList;
import java.util.Arrays;


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
            Log.e("Fiuber Jsonator", "exception", e);
        }

        return objJson.toString();
    }

    /**
     * Checks if the user's login was successful. It should be called
     * after performing the login request.
     * @param jsonResponse The app-server's response to the login request
     */
    public Boolean userLoggedInIsOk(String jsonResponse){
        try {
            JSONObject objJson = new JSONObject(jsonResponse);
            Log.d("Fiuber Jsonator", "Received response:"+jsonResponse);
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

    /**
     * Reads user's data from the app-server's response and
     * initializes {@link UserInfo} with it. This function
     * should be called after validating that the user has
     * correctly logged in (i.e. after calling userLoggedInIsOk).
     * @param jsonResponse The app-server's response to the login request
     */
    public void readUserLoggedInInfo(String jsonResponse){
        if(!this.userLoggedInIsOk(jsonResponse)) return;

        try {
            JSONObject objJson = new JSONObject(jsonResponse);
            Log.d("Fiuber Jsonator", "Received response:"+jsonResponse);
            String appTkn = objJson.getString("token");
			JSONObject uiJson = new JSONObject(objJson.getString("user"));

			String mail = uiJson.getString("email");
			String fName = uiJson.getString("name");
			String lName = uiJson.getString("surname");
			String country = uiJson.getString("country");
			String bth = uiJson.getString("birthdate");
			String userId = uiJson.getString("username");
            String typeUser = uiJson.getString("type");
            int intId = uiJson.getInt("_id");

			UserInfo ui = UserInfo.getInstance();
			ui.initializeUserInfo(userId, mail, fName, lName, country, bth, "", "", appTkn);
            ui.setIntegerId(intId);
            // Retrieve cars o.o
            if(typeUser.toLowerCase().equals("driver")) {
                JSONArray carsArray = uiJson.getJSONArray("cars");
                Log.d("Jsonator", "Parsed cars array:" + carsArray.toString());
                int carsAmount = carsArray.length();
                int i;
                ArrayList<CarInfo> userCars = new ArrayList<>(Arrays.asList(new CarInfo[] {}));

                for(i = 0; i < carsAmount; i++){
                    JSONObject carJson = carsArray.getJSONObject(i);
                    // Create CarInfo
                    CarInfo thisCar = this.readCarInfo(carJson.toString(), false);
                    if(thisCar != null)
                        userCars.add(thisCar);
                }
                ui.setAsDriver(userCars);
                Log.d("Jsonator", "UserInfo read cars are: " + ui.getCars().size());
                }
        }
        catch (Exception e) {
            Log.e("Fiuber Jsonator", "exception", e);
        }
    }

    /**
     * Writes user's data from {@link UserInfo} into a String
     * JSON-formatted. Beware there are two different JSON formats
     * due to the ugly-specified REST API on the shared-server side,
     * so the boolean parameter is used to alternate between them.
     * @param isEditProfile Should be true if the "edit profile"
     *                      format is required
     */
    public String writeUserSignUpInfo(boolean isEditProfile){
        UserInfo ui = UserInfo.getInstance();
        if(!ui.wasInitialized())
            return "";

        JSONObject objJson = new JSONObject();

        try {
            if(ui.isDriver())
                objJson.put("type", "driver");
            else
                objJson.put("type", "passenger");
            objJson.put("username", ui.getUserId());
            objJson.put("password", ui.getPassword());

            JSONObject innerFb = new JSONObject();
            innerFb.put("userId", ui.getUserId());
            objJson.put("firstname", ui.getFirstName());
            objJson.put("lastname", ui.getLastName());

            if(isEditProfile) {
                objJson.put("_ref", "Sarasa");
                objJson.put("_id", ui.getIntegerId());
                innerFb.put("authToken", ui.getFbToken());
                objJson.put("fb", innerFb);
            }
            else{
                innerFb.put("fbtoken", ui.getFbToken());
                objJson.put("fbAuth", innerFb);
            }
            objJson.put("country", ui.getCountry());
            objJson.put("email", ui.getEmail());
            objJson.put("birthdate", ui.getBirthdate());

            JSONArray imagesArr = new JSONArray();
            imagesArr.put("");

            objJson.put("images", imagesArr);

        }
        catch (Exception e) {
            Log.e("Fiuber Jsonator", "exception", e);
        }

        return objJson.toString();
    }

    /**
     * Checks if the user's sign up was successful. It should be called
     * after performing the signing request.
     * @param jsonResponse The app-server's response to the sign up request
     */
    public Boolean userSignedUpIsOk(String jsonResponse){
        try {
            JSONObject objJson = new JSONObject(jsonResponse);
            Log.d("Fiuber Jsonator", "Received response:"+jsonResponse);
            if(objJson.getInt("code") == 201)
                return true;
            return false;
        }
        catch (Exception e) {
            Log.e("Fiuber Jsonator", "exception", e);
            return false;
        }
    }


    public CarInfo readCarInfo(String jsonResponse, boolean isPost){
        try {
            Log.d("Fiuber Jsonator", "Received response:"+jsonResponse);
            JSONObject objJson = new JSONObject(jsonResponse);
            if(isPost)
                objJson = new JSONObject(objJson.getString("car"));
            int carId;
            if(isPost)
                carId = objJson.getInt("_id");
            else
                carId = objJson.getInt("id");
            JSONObject carProps = objJson.getJSONArray("properties").getJSONObject(0);
            String carModel = carProps.getString("name");
            String carNumber = carProps.getString("value");
            // Create CarInfo
            CarInfo thisCar = new CarInfo(carModel, carNumber, carId);
            return thisCar;
        }
        catch (Exception e) {
            Log.e("Fiuber Jsonator", "exception", e);
            return null;
        }
    }


    public String writeCarInfo(CarInfo car){
        JSONObject objJson = new JSONObject();
        UserInfo ui = UserInfo.getInstance();
        try {
            objJson.put("owner", ui.getIntegerId());
            objJson.put("id", car.getId());
            objJson.put("_ref", "Sarasa");

            JSONObject innerProp = new JSONObject();
            innerProp.put("name", car.getModel());
            innerProp.put("value", car.getNumber());
            JSONArray propArray = new JSONArray();
            propArray.put(innerProp);
            objJson.put("properties", propArray);
        }
        catch (Exception e) {
            Log.e("Fiuber Jsonator", "exception", e);
        }

        return objJson.toString();
    }
}
