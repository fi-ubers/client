package com.example.Principal;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * A simple singleton class for changing from one activity
 * to another. Basically a transition-maker.
 */
public class ActivityChanger {
    private static ActivityChanger instancia = null;

    /**
     * NOT FOR USE! Exists only to prevent instantiation.
     */
    protected ActivityChanger() {
        // Exists only to prevent instantiation.
    }

    /**
     * Get the only singleton instance of the class.
     * @return  Current singleton instance
     */
    public static ActivityChanger getInstance() {
        if(instancia == null)
            instancia = new ActivityChanger();
        return instancia;
    }

    /**
     * Sets the current Activity to MainActivity. Basic method for
     * returning to the app's main menu.
     * @param prevContext Previous context for the switch
     */
    public void gotoMenuScreen(Context prevContext){
        Log.i("Fiuber ActivityChanger", "Returning to Menu screen");
        Intent intent = new Intent(prevContext, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        prevContext.startActivity(intent);
    }

    /**
     * Sets the current Activity to LoginActivity. Uses Intent flags
     * to clear current task and make LoginActivity the one on the top.
     * @param prevContext Previous context for the switch

     */
    public void gotoLogInScreen(Context prevContext){
        Log.i("Fiuber ActivityChanger", "Returning to Log In screen");
        Intent intent = new Intent(prevContext, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        prevContext.startActivity(intent);
    }
}
