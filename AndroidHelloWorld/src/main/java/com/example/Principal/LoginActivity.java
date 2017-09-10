package com.example.Principal;

import android.app.Activity;
import android.os.Bundle;

/**
 * {@link Activity} for allowing the user of the app to log in.
 * @see LoginFragment
 */
public class LoginActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }
}
