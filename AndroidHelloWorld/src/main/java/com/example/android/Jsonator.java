package com.example.android;

import android.app.Application;
import android.app.Dialog;
import android.graphics.Path;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.*;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


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

    private String normalizeString(String s){
        String sn = s.replace("á", "\\u00e1");
        sn = sn.replace("é", "\\u00e9");
        sn = sn.replace("í", "\\u00ed");
        sn = sn.replace("ó", "\\u00f3");
        sn = sn.replace("ú", "\\u00fa");
        sn = sn.replace("Á", "\\u00c1");
        sn = sn.replace("É", "\\u00c9");
        sn = sn.replace("Í", "\\u00cd");
        sn = sn.replace("Ó", "\\u00d3");
        sn = sn.replace("Ú", "\\u00da");
        sn = sn.replace("ñ", "\\u00f1");
        sn = sn.replace("Ñ", "\\u00d1");
        return sn;
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
			String appTkn = UserInfo.getInstance().getAppServerToken();
			if (objJson.has("token"))
            	appTkn = objJson.getString("token");
			JSONObject uiJson = new JSONObject(objJson.getString("user"));

			String mail = uiJson.getString("email");
			String fName = uiJson.getString("name");
			String lName = uiJson.getString("surname");
			String country = uiJson.getString("country");
			String bth = uiJson.getString("birthdate");
			String userId = uiJson.getString("username");
            String typeUser = uiJson.getString("type");
            int intId = uiJson.getInt("_id");
			int stateCode = uiJson.getInt("state");
			String tripId = uiJson.getString("tripId");
			UserInfo ui = UserInfo.getInstance();
			ui.initializeUserInfo(userId, mail, fName, lName, country, bth, "", "", appTkn);
            ui.setIntegerId(intId);
			ui.setUserStatus(UserStatus.createFromCode(stateCode));
			// Get trip data if any trip here
			TripLoaderLoggin tll = new TripLoaderLoggin();
			tll.getTripInfo(tripId);
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

        return normalizeString(objJson.toString());
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

        return normalizeString(objJson.toString());
    }

    public String writeDirectionsInfo(LatLng orig, LatLng dest){
        UserInfo ui = UserInfo.getInstance();
        if((!ui.wasInitialized()) || ui.isDriver())
            return "";

        JSONObject objJson = new JSONObject();
        JSONObject origJson = new JSONObject();
        JSONObject destJson = new JSONObject();

        try {

            origJson.put("lat", orig.latitude);
            origJson.put("lng", orig.longitude);

            destJson.put("lat", dest.latitude);
            destJson.put("lng", dest.longitude);

            objJson.put("origin", origJson);
            objJson.put("destination", destJson);
        }
        catch (Exception e) {
            Log.e("Fiuber Jsonator", "exception", e);
        }

        return objJson.toString();
    }

    public ArrayList<LatLng> readDirectionsPath(String jsonResponse, boolean tripWasProposed){
        if(jsonResponse == null) return null;
        ArrayList<LatLng> pathPoints = new ArrayList<>();
        try {
            Log.d("Fiuber Jsonator", "Received response:"+ jsonResponse);
            JSONObject objJson = new JSONObject(jsonResponse);
            if(objJson.getInt("code") != 200)
                return null;

            if(tripWasProposed) {
                objJson = objJson.getJSONObject("trip");
                String oth_id;

                if(UserInfo.getInstance().isDriver())
                    oth_id = objJson.getString("passengerId");
                else {
                    oth_id = objJson.getString("driverId");
                }

				Log.d("Jsonator", "Read other user id: " + oth_id);

                OtherUsersInfo oui = new OtherUsersInfo(oth_id, "", "");
                UserInfo.getInstance().setOtherUser(oui);
                }

            objJson = objJson.getJSONObject("directions");
            PathInfo.getInstance().setTripJson(objJson.toString());
            double distance = objJson.getDouble("distance");
            PathInfo.getInstance().setDistance(distance / 1000.0); // in km
			PathInfo.getInstance().setDuration(objJson.getDouble("duration"));
			// TODO: get real cost
			PathInfo.getInstance().setCost(11.9);
            if(tripWasProposed){
                String origin = objJson.getString("origin_name").split(",")[0];
                String destination = objJson.getString("destination_name").split(",")[0];
                PathInfo.getInstance().setAddresses(origin, destination);
            }

            double origLat = objJson.getJSONObject("origin").getDouble("lat");
            double origLong = objJson.getJSONObject("origin").getDouble("lng");
            LatLng firstPoint = new LatLng(origLat, origLong);
            pathPoints.add(firstPoint);

            JSONArray path = objJson.getJSONArray("path");
            int i;
            for(i = 0; i < path.length(); i++){
                JSONObject nextPointJson = path.getJSONObject(i);
                double lng = nextPointJson.getJSONObject("coords").getDouble("lng");
                double lat = nextPointJson.getJSONObject("coords").getDouble("lat");
                LatLng nextPoint = new LatLng(lat, lng);
                pathPoints.add(nextPoint);
            }

        }
        catch (Exception e) {
            Log.e("Fiuber Jsonator", "exception", e);
            return null;
        }
        return pathPoints;
    }

	public String writeProposedTrip(){
		UserInfo ui = UserInfo.getInstance();
		if((!ui.wasInitialized()) || ui.isDriver())
			return "";

		return normalizeString(PathInfo.getInstance().getTripJson());

	/*
		JSONObject objJson = new JSONObject();
		JSONObject origJson = new JSONObject();
		JSONObject destJson = new JSONObject();
		JSONArray pathJson = new JSONArray();

		PathInfo pi = PathInfo.getInstance();

		try {
			List<LatLng> path =  pi.getPath();
			LatLng origin = path.get(0);
			origJson.put("lat", origin.latitude);
			origJson.put("lng", origin.longitude);

			LatLng destination = path.get(path.size() - 1);
			destJson.put("lat", destination.latitude);
			destJson.put("lng", destination.longitude);

			objJson.put("origin", origJson);
			objJson.put("destination", destJson);
			objJson.put("destination_name", pi.getDestAddress());
			objJson.put("origin_name", pi.getOrigAddress());
			objJson.put("distance", pi.getDistance());
			objJson.put("duration", pi.getDuration());
			objJson.put("status", "OK");

			Iterator<LatLng> it = path.iterator();
			while(it.hasNext()){
				LatLng nexPoint = it.next();
				JSONObject pointJson = new JSONObject();
				pointJson.put("duration", 0);
				pointJson.put("distance", 0);
				JSONObject coordsJson = new JSONObject();
                coordsJson.put("lat", nexPoint.latitude);
                coordsJson.put("lng", nexPoint.longitude);
				pointJson.put("coords", coordsJson);
				pathJson.put(pointJson);
			}

			objJson.put("path", pathJson);


		}
		catch (Exception e) {
			Log.e("Fiuber Jsonator", "exception", e);
		}

        return normalizeString(objJson.toString());
*/
	}

    public void readTripResponseId(String jsonResponse){
        if(jsonResponse == null) return;
        try {
            Log.d("Fiuber Jsonator", "Received response:"+ jsonResponse);
            JSONObject objJson = new JSONObject(jsonResponse);
            if(objJson.getInt("code") != 200)
                return;

            objJson = objJson.getJSONObject("trip");
            String tripId = objJson.getString("_id");
            PathInfo.getInstance().setTripId(tripId);
        }
        catch (Exception e) {
            Log.e("Fiuber Jsonator", "exception", e);;
        }
    }


	public ArrayList<ProtoTrip> readTripsProposed(String jsonResponse) {
        ArrayList<ProtoTrip> trips = new ArrayList<>();
        try {
            Log.d("Fiuber Jsonator", "Received response:" + jsonResponse);
            JSONObject objJson = new JSONObject(jsonResponse);
            if (objJson.getInt("code") != 200)
                return null;

            JSONArray tripsArray = objJson.getJSONArray("trips");
            int i;
            for (i = 0; i < tripsArray.length(); i++) {
                JSONObject nextTrip = tripsArray.getJSONObject(i);
                String tripId = nextTrip.getString("_id");
                nextTrip = nextTrip.getJSONObject("directions");
                Log.d("Jsonator", "Trip JSON:" + nextTrip.toString());
                String orig = nextTrip.getString("origin_name");
                String dest = nextTrip.getString("destination_name");
                ProtoTrip nextProto = new ProtoTrip(orig, dest, tripId);
                nextProto.setTripJson(nextTrip.toString());
                nextProto.setDistance(nextTrip.getDouble("distance"));
                nextProto.setDuration(nextTrip.getDouble("duration"));
                // TODO: Get real cost
                nextProto.setCost(12.3);
                trips.add(nextProto);
            }

        } catch (Exception e) {
            Log.e("Fiuber Jsonator", "exception", e);
            return null;
        }
        return trips;
    }

    public OtherUsersInfo readOtherUserInfo(String jsonResponse) {
        try {
            Log.d("Fiuber Jsonator", "Received response:" + jsonResponse);
            JSONObject objJson = new JSONObject(jsonResponse);
            if (objJson.getInt("code") != 200)
                return null;

            objJson = objJson.getJSONObject("user");
            String userId = objJson.getString("_id");
            String userName = objJson.getString("name") + " " + objJson.getString("surname");
            // TODO: Get real picture!
            String userPic = "107457569994960";
            OtherUsersInfo oui = new OtherUsersInfo(userId, userName, userPic);

            if(!UserInfo.getInstance().isDriver()) {
                // TODO: Get driver rate
                double rate = 3.3;
                int rateCount = 10;
                oui.setDriverRates(rate, rateCount);
            }

            return oui;
        } catch (Exception e) {
            Log.e("Fiuber Jsonator", "exception", e);
            return null;
        }
    }


    public String writeLocationCoords(LatLng location){
        JSONObject objJson = new JSONObject();
        JSONObject innerCoords = new JSONObject();

        try {
            innerCoords.put("lat", location.latitude);
            innerCoords.put("lng", location.longitude);
            objJson.put("coord", innerCoords);
        }
        catch (Exception e) {
            Log.e("Fiuber Jsonator", "exception", e);
        }

        return objJson.toString();
    }


// --------------------------------------------------------------------------------------

	public class TripLoaderLoggin implements RestUpdate{
        private boolean retrieveTrip;

		public TripLoaderLoggin(){
            retrieveTrip = true;
		}

		public void getTripInfo(String tripId){
			if((tripId == null) || (tripId.length() == 0))
				return;
			// Get trip info from app-server
            PathInfo.getInstance().setTripId(tripId);
			try {
				ConexionRest conn = new ConexionRest(this);
				String tripUrl = conn.getBaseUrl() + "/trips/" + tripId;
				Log.d("TripLoaderLoggin", "URL to GET trip: " + tripUrl);
				conn.generateGet(tripUrl, null);
			}
			catch(Exception e){
				Log.e("TripLoaderLoggin", "GET trips error: ", e);
			}
		}

		@Override
		public void executeUpdate(String servResponse) {
            if(servResponse == null)    return;
            if(retrieveTrip) {
                Log.d("TripLoaderLoggin", "Received response: " + servResponse);
                Jsonator jnator = new Jsonator();
                ArrayList<LatLng> selectedTrip = jnator.readDirectionsPath(servResponse, true);
                PathInfo.getInstance().setPath(selectedTrip);
                String otherId = UserInfo.getInstance().getOtherUser().getUserId();
                if(otherId.equals("-1")) {
                    // If here, there's no other user yet
                    // TODO: Set profile pic to "-1"
                    OtherUsersInfo oui = new OtherUsersInfo("-1", "No driver took this trip yet", "");
                    PathInfo pi = PathInfo.getInstance();
                    oui.setOriginDestination(pi.getOrigAddress(), pi.getDestAddress());
                    UserInfo.getInstance().setOtherUser(oui);
                    return;
                }
                    try {
                        ConexionRest conn = new ConexionRest(this);
                        String passUrl = conn.getBaseUrl() + "/users/" + otherId;
                        Log.d("SelectTripActivity", "URL to GET passenger data: " + passUrl);
                        conn.generateGet(passUrl, null);
                        retrieveTrip = false;
                    } catch (Exception e) {
                        Log.e("ChoosePassengerActivity", "GET passenger error: ", e);
                    }
            } else {
                Log.d("TripLoaderLoggin", "Received response: " + servResponse);
                Jsonator jnator = new Jsonator();
                OtherUsersInfo oui = jnator.readOtherUserInfo(servResponse);
                PathInfo pi = PathInfo.getInstance();
                oui.setOriginDestination(pi.getOrigAddress(), pi.getDestAddress());
                UserInfo.getInstance().setOtherUser(oui);
            }
		}

	}

}
