package com.example.Principal;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.Profile;
import com.facebook.login.widget.ProfilePictureView;


public class ProfileActivity extends Activity {
    EditText  userNameSurname, userMail, userBirthdate;
    TextView userUsID;
	Button submitBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);

		userNameSurname = (EditText) findViewById(R.id.userNameSurname);
		userMail = (EditText) findViewById(R.id.userMail);
		userBirthdate = (EditText) findViewById(R.id.userBirthdate);
		userUsID = (TextView) findViewById(R.id.userUsID);

		UserInfo uInfo = UserInfo.getInstance();

		// Sanity check
		if(!uInfo.wasInitialized())
			Log.w("ProfileActivity", "UserInfo not initialized!");

		userNameSurname.setText(uInfo.getUserName());
		userMail.setText(uInfo.getEmail());
		userBirthdate.setText(uInfo.getBirthdate());
		userUsID.setText("  " + uInfo.getUserId());

		if(AccessToken.getCurrentAccessToken() == null) {
			// TODO: Do something with profile picture if user has not logged in with FB
		}
		else{
			  // IF here, user logged in with FB
			Profile curProfile = Profile.getCurrentProfile();
			ProfilePictureView profilePictureView;
			profilePictureView = (ProfilePictureView) findViewById(R.id.usProfilePicture);
			profilePictureView.setProfileId(curProfile.getId());
		}


		submitBtn = (Button) findViewById(R.id.submitBtn);
		submitBtn.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view){
				String newName = userNameSurname.getText().toString();
				String newMail = userMail.getText().toString();
				String newBth = userBirthdate.getText().toString();
				String newId = userUsID.getText().toString();
				if(UserInfo.getInstance().infoWillChange(newMail, newName, newId, newBth)){
					// TODO: Post changes to app server
					UserInfo.getInstance().seppuku();
					UserInfo.getInstance().initializeUserInfo(newMail, newName, newId, newBth);
				}

				ActivityChanger.getInstance().gotoActivity(ProfileActivity.this, MainActivity.class);

			}
		});


	}
}