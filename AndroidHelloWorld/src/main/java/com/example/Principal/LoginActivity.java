package com.example.Principal;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * {@link Activity} for allowing the user of the app to log in.
 * @see LoginFragment
 */
public class LoginActivity extends Activity{

    Button signBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signBtn = (Button) findViewById(R.id.signBtn);
        signBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                ActivityChanger.getInstance().gotoActivity(LoginActivity.this, ManualSignInActivity.class);
            }
        });


    }
}
