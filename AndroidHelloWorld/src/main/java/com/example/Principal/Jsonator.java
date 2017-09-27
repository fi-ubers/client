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
}
